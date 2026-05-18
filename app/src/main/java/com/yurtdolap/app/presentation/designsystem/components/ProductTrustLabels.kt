package com.yurtdolap.app.presentation.designsystem.components

import com.yurtdolap.app.domain.model.Product
import com.yurtdolap.app.domain.model.hasRentalRating
import com.yurtdolap.app.domain.model.hasSellerRating
import com.yurtdolap.app.domain.model.isForRent
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

fun Product.sellerRatingLabel(): String? {
    if (!hasSellerRating()) return null
    return "★ ${formatRatingValue(sellerRatingAverage)} satici puani"
}

fun Product.rentalSummaryLabel(): String? {
    if (!isForRent() || !hasRentalRating()) return null

    val parts = buildList {
        add("${formatRatingValue(rentalRatingAverage)} urun puani")
        if (rentalCount > 0) {
            add("$rentalCount kiralama")
        }
        formatRelativeRentalTime(lastRentedAt)?.let(::add)
    }

    return parts.joinToString(" · ")
}

fun formatRatingValue(value: Double): String {
    return ((value * 10).roundToInt() / 10.0).toString()
}

fun formatRelativeRentalTime(lastRentedAt: Long?): String? {
    if (lastRentedAt == null || lastRentedAt <= 0L) return null

    val elapsedMillis = (System.currentTimeMillis() - lastRentedAt).coerceAtLeast(0L)
    val days = TimeUnit.MILLISECONDS.toDays(elapsedMillis)

    return when {
        days <= 0 -> "son kiralama bugun"
        days == 1L -> "son kiralama 1 gun once"
        days < 7L -> "son kiralama $days gun once"
        days < 30L -> "son kiralama ${(days / 7).coerceAtLeast(1)} hafta once"
        days < 365L -> "son kiralama ${(days / 30).coerceAtLeast(1)} ay once"
        else -> "son kiralama ${(days / 365).coerceAtLeast(1)} yil once"
    }
}
