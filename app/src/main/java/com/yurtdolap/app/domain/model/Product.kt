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
    val isAvailable: Boolean
)

object ProductTags {
    const val FOR_SALE = "Satılık"
    const val FOR_RENT = "Kiralık"
    const val NEED_REQUEST = "İhtiyacım Var"
}

fun Product.isNeedRequest(): Boolean = tag == ProductTags.NEED_REQUEST
