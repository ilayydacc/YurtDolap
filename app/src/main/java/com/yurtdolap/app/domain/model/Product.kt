package com.yurtdolap.app.domain.model

data class Product(
    val id: String,
    val title: String,
    val description: String = "",
    val price: String,
    val imageUrl: String?,
    val tag: String, // e.g. "Satılık", "Kiralık" or "İhtiyacım Var"
    val categoryId: String? = null, // e.g. "1" for Elektronik
    val sellerName: String,
    val sellerId: String,
    val dormitory: String,
    val deliveryPreference: String = "",
    val isAvailable: Boolean,
    val sellerRatingAverage: Double = 0.0,
    val sellerRatingCount: Int = 0,
    val rentalRatingAverage: Double = 0.0,
    val rentalRatingCount: Int = 0,
    val rentalCount: Int = 0,
    val lastRentedAt: Long? = null
)

object ProductTags {
    const val FOR_SALE = "Satılık"
    const val FOR_RENT = "Kiralık"
    const val NEED_REQUEST = "İhtiyacım Var"
}

fun Product.isNeedRequest(): Boolean = tag == ProductTags.NEED_REQUEST
fun Product.isForSale(): Boolean = tag == ProductTags.FOR_SALE
fun Product.isForRent(): Boolean = tag == ProductTags.FOR_RENT
fun Product.hasSellerRating(): Boolean = sellerRatingAverage > 0.0 && sellerRatingCount > 0
fun Product.hasRentalRating(): Boolean = rentalRatingAverage > 0.0 && rentalRatingCount > 0
