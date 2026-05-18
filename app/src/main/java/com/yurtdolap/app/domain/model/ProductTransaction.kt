package com.yurtdolap.app.domain.model

data class ProductTransaction(
    val id: String = "",
    val productId: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val sellerId: String = "",
    val status: String = ProductTransactionStatus.REQUESTED,
    val createdAt: Long = 0L,
    val completedAt: Long? = null
)

object ProductTransactionStatus {
    const val REQUESTED = "requested"
    const val COMPLETED = "completed"
}
