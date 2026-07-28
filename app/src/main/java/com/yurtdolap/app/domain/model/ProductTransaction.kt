package com.yurtdolap.app.domain.model

data class ProductTransaction(
    val id: String = "",
    val productId: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val sellerId: String = "",
    val status: String = ProductTransactionStatus.REQUESTED,
    val paymentStatus: String = ProductPaymentStatus.NOT_STARTED,
    val amount: Long = 0L,
    val currency: String = "TRY",
    val createdAt: Long = 0L,
    val paidAt: Long? = null,
    val completedAt: Long? = null
)

object ProductTransactionStatus {
    const val REQUESTED = "requested"
    const val PAYMENT_PENDING = "payment_pending"
    const val PAID = "paid"
    const val PAYMENT_FAILED = "payment_failed"
    const val COMPLETED = "completed"
}

object ProductPaymentStatus {
    const val NOT_STARTED = "not_started"
    const val SIMULATED_SUCCESS = "simulated_success"
    const val SIMULATED_FAILED = "simulated_failed"
}
