package com.yurtdolap.app.domain.repository

import com.yurtdolap.app.domain.model.ChatRoom
import com.yurtdolap.app.domain.model.Message
import com.yurtdolap.app.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    // 1) Find an existing room between two users for a specific product, or create it if not exists.
    // Returns the ChatRoom ID.
    suspend fun createOrGetChatRoom(
        otherUserId: String,
        productId: String,
        productTitle: String,
        productImageUrl: String
    ): Resource<String>

    // 2) Listen to all chat rooms a user is part of
    fun getUserChatRooms(): Flow<Resource<List<ChatRoom>>>

    // 3) Listen to messages inside a specific room
    fun getChatMessages(chatId: String): Flow<Resource<List<Message>>>

    // 4) Send a message
    suspend fun sendMessage(chatId: String, text: String): Resource<Unit>

    // 5) Delete a chat room and all messages inside it
    suspend fun deleteChatRoom(chatId: String): Resource<Unit>
}
