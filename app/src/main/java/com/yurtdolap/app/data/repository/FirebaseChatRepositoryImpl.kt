package com.yurtdolap.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.yurtdolap.app.domain.model.ChatRoom
import com.yurtdolap.app.domain.model.Message
import com.yurtdolap.app.domain.repository.ChatRepository
import com.yurtdolap.app.domain.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class FirebaseChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ChatRepository {

    private val chatsCollection = firestore.collection("chats")

    override suspend fun createOrGetChatRoom(
        otherUserId: String,
        productId: String,
        productTitle: String,
        productImageUrl: String
    ): Resource<String> {
        val currentUserId = auth.currentUser?.uid ?: return Resource.Error("Kullanici girisi yapilmadi")

        return try {
            val sortedParticipants = listOf(currentUserId, otherUserId).sorted()

            val existingRooms = chatsCollection
                .whereEqualTo("productId", productId)
                .whereEqualTo("participants", sortedParticipants)
                .get()
                .await()

            if (!existingRooms.isEmpty) {
                val existingRoom = existingRooms.documents.first()
                existingRoom.reference
                    .update("deletedForUserIds", FieldValue.arrayRemove(currentUserId))
                    .await()
                return Resource.Success(existingRoom.id)
            }

            val newChatId = UUID.randomUUID().toString()
            val newRoom = ChatRoom(
                id = newChatId,
                participants = sortedParticipants,
                productId = productId,
                productTitle = productTitle,
                productImageUrl = productImageUrl,
                lastMessage = "Sohbet basladi",
                lastMessageTimestamp = System.currentTimeMillis()
            )

            chatsCollection.document(newChatId).set(newRoom).await()
            Resource.Success(newChatId)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Oda olusturulamadi")
        }
    }

    override fun getUserChatRooms(): Flow<Resource<List<ChatRoom>>> = callbackFlow {
        var chatListener: ListenerRegistration? = null

        fun startChatListener(userId: String) {
            if (chatListener != null) return

            chatListener = chatsCollection
                .whereArrayContains("participants", userId)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Resource.Error(error.localizedMessage ?: "Bilinmeyen hata"))
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val rooms = snapshot.documents
                            .mapNotNull { it.toObject(ChatRoom::class.java) }
                            .filter { room ->
                                val deletedAt = room.deletedAtByUserIds[userId] ?: 0L
                                userId !in room.deletedForUserIds || room.lastMessageTimestamp > deletedAt
                            }
                        trySend(Resource.Success(rooms))
                    }
                }
        }

        trySend(Resource.Loading())

        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            startChatListener(currentUserId)
            awaitClose { chatListener?.remove() }
            return@callbackFlow
        }

        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid ?: return@AuthStateListener
            startChatListener(uid)
        }
        auth.addAuthStateListener(authListener)

        awaitClose {
            auth.removeAuthStateListener(authListener)
            chatListener?.remove()
        }
    }

    override fun getChatRoom(chatId: String): Flow<Resource<ChatRoom>> = callbackFlow {
        if (chatId.isBlank()) {
            trySend(Resource.Error("Sohbet bilgisi eksik"))
            close()
            return@callbackFlow
        }

        trySend(Resource.Loading())

        val listener = chatsCollection.document(chatId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Sohbet yuklenemedi"))
                    return@addSnapshotListener
                }

                val room = snapshot?.toObject(ChatRoom::class.java)
                if (room != null) {
                    trySend(Resource.Success(room))
                } else {
                    trySend(Resource.Error("Sohbet bulunamadi"))
                }
            }

        awaitClose { listener.remove() }
    }

    override fun getChatMessages(chatId: String): Flow<Resource<List<Message>>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            trySend(Resource.Error("Kullanici girisi yapilmadi"))
            close()
            return@callbackFlow
        }

        val chatDocument = chatsCollection.document(chatId)
        val chatSnapshot = chatDocument.get().await()
        val deletedAt = chatSnapshot
            .toObject(ChatRoom::class.java)
            ?.deletedAtByUserIds
            ?.get(currentUserId)
            ?: 0L
        val messagesCollection = chatDocument.collection("messages")

        val listener = messagesCollection
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Bilinmeyen hata"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val msgs = snapshot.documents
                        .mapNotNull { it.toObject(Message::class.java) }
                        .filter { it.timestamp > deletedAt }
                    trySend(Resource.Success(msgs))
                }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun sendMessage(chatId: String, text: String): Resource<Unit> {
        val currentUserId = auth.currentUser?.uid ?: return Resource.Error("Kullanici girisi yapilmadi")

        return try {
            val chatDocument = chatsCollection.document(chatId)
            val room = chatDocument.get().await().toObject(ChatRoom::class.java)
            val participantsToRestore = room?.participants
                ?.takeIf { it.isNotEmpty() }
                ?: listOf(currentUserId)
            val messagesCollection = chatDocument.collection("messages")
            val messageId = UUID.randomUUID().toString()
            val timestamp = System.currentTimeMillis()

            val message = Message(
                id = messageId,
                senderId = currentUserId,
                text = text,
                timestamp = timestamp
            )

            messagesCollection.document(messageId).set(message).await()

            chatDocument.update(
                mapOf(
                    "lastMessage" to text,
                    "lastMessageTimestamp" to timestamp,
                    "deletedForUserIds" to FieldValue.arrayRemove(*participantsToRestore.toTypedArray())
                )
            ).await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Mesaj gonderilemedi")
        }
    }

    override suspend fun deleteChatRoom(chatId: String): Resource<Unit> {
        val currentUserId = auth.currentUser?.uid ?: return Resource.Error("Kullanici girisi yapilmadi")

        return try {
            val chatDocument = chatsCollection.document(chatId)
            val chatSnapshot = chatDocument.get().await()
            val room = chatSnapshot.toObject(ChatRoom::class.java)
                ?: return Resource.Error("Sohbet bulunamadi")

            if (currentUserId !in room.participants) {
                return Resource.Error("Bu sohbeti silme yetkin yok")
            }

            chatDocument.update(
                mapOf(
                    "deletedForUserIds" to FieldValue.arrayUnion(currentUserId),
                    "deletedAtByUserIds.$currentUserId" to System.currentTimeMillis()
                )
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Sohbet silinemedi")
        }
    }
}
