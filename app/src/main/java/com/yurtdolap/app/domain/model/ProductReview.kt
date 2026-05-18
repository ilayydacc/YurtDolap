package com.yurtdolap.app.domain.model

data class ProductReview(
    val id: String = "",
    val productId: String = "",
    val reviewerId: String = "",
    val reviewerName: String = "",
    val sellerId: String = "",
    val sellerRating: Int = 0,
    val rentalRating: Int? = null,
    val comment: String = "",
    val createdAt: Long = 0L
)
