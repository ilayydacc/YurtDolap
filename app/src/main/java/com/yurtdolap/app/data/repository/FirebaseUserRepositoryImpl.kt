package com.yurtdolap.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.yurtdolap.app.domain.model.UserProfileData
import com.yurtdolap.app.domain.repository.UserRepository
import com.yurtdolap.app.domain.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseUserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    override fun getFavoriteProductIds(): Flow<Resource<List<String>>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            trySend(Resource.Error("Kullanici girisi yapilmadi"))
            close()
            return@callbackFlow
        }

        trySend(Resource.Loading())

        val listener = usersCollection.document(currentUserId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "Favoriler yuklenemedi"))
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                @Suppress("UNCHECKED_CAST")
                val favorites = snapshot.get("favoriteProductIds") as? List<String> ?: emptyList()
                trySend(Resource.Success(favorites))
            } else {
                trySend(Resource.Success(emptyList()))
            }
        }

        awaitClose { listener.remove() }
    }

    override suspend fun toggleFavorite(productId: String): Resource<Unit> {
        val currentUserId = auth.currentUser?.uid ?: return Resource.Error("Kullanici girisi yapilmadi")

        return try {
            val userDocRef = usersCollection.document(currentUserId)
            val snapshot = userDocRef.get().await()

            if (!snapshot.exists()) {
                userDocRef.set(mapOf("favoriteProductIds" to listOf(productId))).await()
            } else {
                @Suppress("UNCHECKED_CAST")
                val favorites = snapshot.get("favoriteProductIds") as? List<String> ?: emptyList()
                if (favorites.contains(productId)) {
                    userDocRef.update("favoriteProductIds", FieldValue.arrayRemove(productId)).await()
                } else {
                    userDocRef.update("favoriteProductIds", FieldValue.arrayUnion(productId)).await()
                }
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Favori islemi basarisiz oldu")
        }
    }

    override suspend fun getUserProfile(): Resource<UserProfileData> {
        val currentUserId = auth.currentUser?.uid ?: return Resource.Error("Kullanici girisi yapilmadi")

        return try {
            val snapshot = usersCollection.document(currentUserId).get().await()
            if (!snapshot.exists()) {
                return Resource.Error("Kullanici profili bulunamadi")
            }

            val rawIsAdmin = snapshot.get("isAdmin")
            val isAdmin = when (rawIsAdmin) {
                is Boolean -> rawIsAdmin
                is String -> rawIsAdmin.equals("true", ignoreCase = true)
                is Number -> rawIsAdmin.toInt() != 0
                else -> false
            }

            Resource.Success(
                UserProfileData(
                    id = snapshot.getString("id") ?: currentUserId,
                    name = snapshot.getString("name") ?: "",
                    email = snapshot.getString("email") ?: "",
                    city = snapshot.getString("city") ?: "",
                    dormitory = snapshot.getString("dormitory") ?: "",
                    createdAt = snapshot.getLong("createdAt") ?: 0L,
                    isAdmin = isAdmin
                )
            )
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Profil bilgileri alinamadi")
        }
    }

    override suspend fun updateUserProfile(name: String, city: String, dormitory: String): Resource<Unit> {
        val currentUserId = auth.currentUser?.uid ?: return Resource.Error("Kullanici girisi yapilmadi")

        if (name.isBlank() || city.isBlank() || dormitory.isBlank()) {
            return Resource.Error("Isim, sehir ve yurt bilgisi bos olamaz")
        }

        return try {
            usersCollection.document(currentUserId).set(
                mapOf(
                    "name" to name.trim(),
                    "city" to city.trim(),
                    "dormitory" to dormitory.trim()
                ),
                SetOptions.merge()
            ).await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Profil guncellenemedi")
        }
    }
}
