package com.yurtdolap.app.domain.model

data class ChatRoom(
    val id: String = "",
    val participants: List<String> = emptyList(), // User IDs (buyer, seller)
    val productId: String = "",
    val productTitle: String = "",
    val productImageUrl: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0L,
    val deletedForUserIds: List<String> = emptyList(),
    val deletedAtByUserIds: Map<String, Long> = emptyMap()
)
