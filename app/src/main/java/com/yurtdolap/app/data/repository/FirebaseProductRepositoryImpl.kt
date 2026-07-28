package com.yurtdolap.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.yurtdolap.app.domain.model.Product
import com.yurtdolap.app.domain.model.ProductPaymentStatus
import com.yurtdolap.app.domain.model.ProductReview
import com.yurtdolap.app.domain.model.ProductTransaction
import com.yurtdolap.app.domain.model.ProductTransactionStatus
import com.yurtdolap.app.domain.model.isForRent
import com.yurtdolap.app.domain.model.isNeedRequest
import com.yurtdolap.app.domain.repository.ProductRepository
import com.yurtdolap.app.domain.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseProductRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : ProductRepository {

    private val productsCollection = firestore.collection("products")
    private val usersCollection = firestore.collection("users")
    private val storageRef = storage.reference.child("product_images")

    override fun getProducts(categoryId: String?): Flow<Resource<List<Product>>> = callbackFlow {
        trySend(Resource.Loading())

        val query = if (categoryId != null) {
            productsCollection.whereEqualTo("categoryId", categoryId)
        } else {
            productsCollection
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "Urunler yuklenemedi"))
                return@addSnapshotListener
            }

            val products = snapshot?.documents?.map(::mapProduct) ?: emptyList()
            trySend(Resource.Success(products))
        }

        awaitClose { listener.remove() }
    }

    override fun getProductById(id: String): Flow<Resource<Product>> = callbackFlow {
        trySend(Resource.Loading())

        val listener = productsCollection.document(id).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "Urun yuklenemedi"))
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                trySend(Resource.Success(mapProduct(snapshot)))
            } else {
                trySend(Resource.Error("Urun bulunamadi"))
            }
        }

        awaitClose { listener.remove() }
    }

    override fun getProductReviews(productId: String): Flow<Resource<List<ProductReview>>> = callbackFlow {
        trySend(Resource.Loading())

        val listener = productsCollection.document(productId)
            .collection("reviews")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Degerlendirmeler yuklenemedi"))
                    return@addSnapshotListener
                }

                val reviews = snapshot?.documents?.map { doc ->
                    ProductReview(
                        id = doc.id,
                        productId = productId,
                        reviewerId = doc.getString("reviewerId") ?: "",
                        reviewerName = doc.getString("reviewerName") ?: "",
                        sellerId = doc.getString("sellerId") ?: "",
                        sellerRating = doc.getIntValue("sellerRating"),
                        rentalRating = doc.getNullableIntValue("rentalRating"),
                        comment = doc.getString("comment") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                } ?: emptyList()

                trySend(Resource.Success(reviews))
            }

        awaitClose { listener.remove() }
    }

    override fun getProductTransactions(productId: String): Flow<Resource<List<ProductTransaction>>> = callbackFlow {
        trySend(Resource.Loading())

        val listener = productsCollection.document(productId)
            .collection("transactions")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Islem talepleri yuklenemedi"))
                    return@addSnapshotListener
                }

                val transactions = snapshot?.documents?.map { doc ->
                    ProductTransaction(
                        id = doc.id,
                        productId = productId,
                        buyerId = doc.getString("buyerId") ?: "",
                        buyerName = doc.getString("buyerName") ?: "",
                        sellerId = doc.getString("sellerId") ?: "",
                        status = doc.getString("status") ?: ProductTransactionStatus.REQUESTED,
                        paymentStatus = doc.getString("paymentStatus") ?: ProductPaymentStatus.NOT_STARTED,
                        amount = doc.getLong("amount") ?: 0L,
                        currency = doc.getString("currency") ?: "TRY",
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        paidAt = doc.getLong("paidAt"),
                        completedAt = doc.getLong("completedAt")
                    )
                } ?: emptyList()

                trySend(Resource.Success(transactions))
            }

        awaitClose { listener.remove() }
    }

    override suspend fun addProduct(product: Product): Resource<Unit> {
        return try {
            val productMap = hashMapOf(
                "title" to product.title,
                "description" to product.description,
                "price" to product.price,
                "imageUrl" to product.imageUrl,
                "tag" to product.tag,
                "categoryId" to product.categoryId,
                "sellerName" to product.sellerName,
                "sellerId" to product.sellerId,
                "dormitory" to product.dormitory,
                "deliveryPreference" to product.deliveryPreference,
                "isAvailable" to product.isAvailable,
                "sellerRatingAverage" to product.sellerRatingAverage,
                "sellerRatingCount" to product.sellerRatingCount,
                "rentalRatingAverage" to product.rentalRatingAverage,
                "rentalRatingCount" to product.rentalRatingCount,
                "rentalCount" to product.rentalCount,
                "lastRentedAt" to product.lastRentedAt
            )

            if (product.id.isEmpty()) {
                productsCollection.add(productMap).await()
            } else {
                productsCollection.document(product.id).set(productMap).await()
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Urun eklenirken hata olustu")
        }
    }

    override suspend fun deleteProduct(productId: String): Resource<Unit> {
        return try {
            productsCollection.document(productId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Urun silinirken hata olustu")
        }
    }

    override suspend fun uploadProductImage(imageBytes: ByteArray, fileName: String): Resource<String> {
        return try {
            val imageRef = storageRef.child(fileName)
            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()

            suspendCancellableCoroutine<Unit> { continuation ->
                imageRef.putBytes(imageBytes, metadata)
                    .addOnSuccessListener {
                        if (continuation.isActive) continuation.resume(Unit)
                    }
                    .addOnFailureListener {
                        if (continuation.isActive) continuation.resumeWithException(it)
                    }
            }

            val downloadUrl = imageRef.downloadUrl.await()
            Resource.Success(downloadUrl.toString())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Resim yuklenirken hata olustu")
        }
    }

    override suspend fun updateProduct(productId: String, updates: Map<String, Any>): Resource<Unit> {
        return try {
            productsCollection.document(productId).update(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Urun guncellenirken hata olustu")
        }
    }

    override suspend fun requestProductTransaction(product: Product): Resource<Unit> {
        val currentUserId = auth.currentUser?.uid ?: return Resource.Error("Kullanici girisi yapilmadi")
        if (currentUserId == product.sellerId) {
            return Resource.Error("Kendi ilanin icin islem talebi acamazsin")
        }

        return try {
            val buyerSnapshot = usersCollection.document(currentUserId).get().await()
            val buyerName = buyerSnapshot.getString("name")
                ?.takeIf { it.isNotBlank() }
                ?: "YurtDolap Kullanici"
            val transactionRef = productsCollection
                .document(product.id)
                .collection("transactions")
                .document(currentUserId)

            transactionRef.set(
                mapOf(
                    "productId" to product.id,
                    "buyerId" to currentUserId,
                    "buyerName" to buyerName,
                    "sellerId" to product.sellerId,
                    "status" to ProductTransactionStatus.REQUESTED,
                    "createdAt" to System.currentTimeMillis(),
                    "completedAt" to null
                ),
                SetOptions.merge()
            ).await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Islem talebi olusturulamadi")
        }
    }

    override suspend fun simulateProductPayment(
        productId: String,
        shouldSucceed: Boolean,
        cardLast4: String
    ): Resource<Product> {
        val currentUserId = auth.currentUser?.uid ?: return Resource.Error("Kullanici girisi yapilmadi")

        return try {
            val productRef = productsCollection.document(productId)
            val productSnapshot = productRef.get().await()
            if (!productSnapshot.exists()) {
                return Resource.Error("Urun bulunamadi")
            }

            val product = mapProduct(productSnapshot)
            if (currentUserId == product.sellerId) {
                return Resource.Error("Kendi ilanin icin odeme simulasyonu yapamazsin")
            }
            if (product.isNeedRequest()) {
                return Resource.Error("Talep ilanlari icin odeme simulasyonu yok")
            }

            val buyerSnapshot = usersCollection.document(currentUserId).get().await()
            val buyerName = buyerSnapshot.getString("name")
                ?.takeIf { it.isNotBlank() }
                ?: "YurtDolap Kullanici"
            val now = System.currentTimeMillis()
            val transactionRef = productRef.collection("transactions").document(currentUserId)
            val status = if (shouldSucceed) {
                ProductTransactionStatus.PAID
            } else {
                ProductTransactionStatus.PAYMENT_FAILED
            }
            val paymentStatus = if (shouldSucceed) {
                ProductPaymentStatus.SIMULATED_SUCCESS
            } else {
                ProductPaymentStatus.SIMULATED_FAILED
            }

            transactionRef.set(
                mapOf(
                    "productId" to product.id,
                    "buyerId" to currentUserId,
                    "buyerName" to buyerName,
                    "sellerId" to product.sellerId,
                    "status" to status,
                    "paymentStatus" to paymentStatus,
                    "amount" to parsePriceToKurus(product.price),
                    "currency" to "TRY",
                    "demoCardLast4" to cardLast4,
                    "createdAt" to now,
                    "paidAt" to if (shouldSucceed) now else null,
                    "completedAt" to null
                ),
                SetOptions.merge()
            ).await()

            Resource.Success(product)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Odeme simulasyonu tamamlanamadi")
        }
    }

    override suspend fun completeProductTransaction(product: Product, buyerId: String): Resource<Unit> {
        val currentUserId = auth.currentUser?.uid ?: return Resource.Error("Kullanici girisi yapilmadi")
        if (currentUserId != product.sellerId) {
            return Resource.Error("Sadece satici islemi tamamlayabilir")
        }
        if (buyerId.isBlank()) {
            return Resource.Error("Alici bilgisi eksik")
        }

        return try {
            val transactionRef = productsCollection
                .document(product.id)
                .collection("transactions")
                .document(buyerId)
            val snapshot = transactionRef.get().await()
            if (!snapshot.exists()) {
                return Resource.Error("Tamamlanacak islem talebi bulunamadi")
            }
            val status = snapshot.getString("status")
            if (status == ProductTransactionStatus.PAYMENT_FAILED) {
                return Resource.Error("Basarisiz odeme simulasyonu tamamlanamaz")
            }

            transactionRef.update(
                mapOf(
                    "status" to ProductTransactionStatus.COMPLETED,
                    "completedAt" to System.currentTimeMillis()
                )
            ).await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Islem tamamlanamadi")
        }
    }

    override suspend fun submitProductReview(
        product: Product,
        sellerRating: Int,
        rentalRating: Int?,
        comment: String
    ): Resource<Unit> {
        val currentUserId = auth.currentUser?.uid ?: return Resource.Error("Kullanici girisi yapilmadi")

        if (currentUserId == product.sellerId) {
            return Resource.Error("Kendi ilanina degerlendirme birakamazsin")
        }
        if (sellerRating !in 1..5) {
            return Resource.Error("Satici puani 1 ile 5 arasinda olmali")
        }
        if (product.isForRent() && rentalRating !in 1..5) {
            return Resource.Error("Kiralik urun puani 1 ile 5 arasinda olmali")
        }
        if (!product.isForRent() && rentalRating != null) {
            return Resource.Error("Satilik ilanlarda urun puani verilmez")
        }

        return try {
            val transactionRef = productsCollection
                .document(product.id)
                .collection("transactions")
                .document(currentUserId)
            val transactionSnapshot = transactionRef.get().await()
            val transactionStatus = transactionSnapshot.getString("status")
            if (transactionStatus != ProductTransactionStatus.COMPLETED) {
                return Resource.Error("Degerlendirme icin once islemin satici tarafindan tamamlanmasi gerekir")
            }

            val sellerStats = firestore.runTransaction { transaction ->
                val now = System.currentTimeMillis()
                val productRef = productsCollection.document(product.id)
                val reviewRef = productRef.collection("reviews").document(currentUserId)
                val reviewerRef = usersCollection.document(currentUserId)
                val sellerRef = usersCollection.document(product.sellerId)

                val productSnapshot = transaction.get(productRef)
                if (!productSnapshot.exists()) {
                    throw IllegalStateException("Ilan bulunamadi")
                }

                if (transaction.get(reviewRef).exists()) {
                    throw IllegalStateException("Bu ilan icin zaten degerlendirme biraktin")
                }

                val reviewerSnapshot = transaction.get(reviewerRef)
                val reviewerName = reviewerSnapshot.getString("name")
                    ?.takeIf { it.isNotBlank() }
                    ?: "YurtDolap Kullanici"

                val sellerSnapshot = transaction.get(sellerRef)
                val previousSellerAverage = sellerSnapshot.getDoubleValue("sellerRatingAverage")
                val previousSellerCount = sellerSnapshot.getIntValue("sellerRatingCount")
                val newSellerCount = previousSellerCount + 1
                val newSellerAverage = ((previousSellerAverage * previousSellerCount) + sellerRating) / newSellerCount

                transaction.set(
                    reviewRef,
                    mapOf(
                        "productId" to product.id,
                        "reviewerId" to currentUserId,
                        "reviewerName" to reviewerName,
                        "sellerId" to product.sellerId,
                        "sellerRating" to sellerRating,
                        "rentalRating" to rentalRating,
                        "comment" to comment.trim(),
                        "createdAt" to now
                    )
                )

                transaction.set(
                    sellerRef,
                    mapOf(
                        "sellerRatingAverage" to newSellerAverage,
                        "sellerRatingCount" to newSellerCount
                    ),
                    SetOptions.merge()
                )

                if (product.isForRent()) {
                    val previousRentalAverage = productSnapshot.getDoubleValue("rentalRatingAverage")
                    val previousRentalCount = productSnapshot.getIntValue("rentalRatingCount")
                    val newRentalCount = previousRentalCount + 1
                    val safeRentalRating = rentalRating ?: sellerRating
                    val newRentalAverage = ((previousRentalAverage * previousRentalCount) + safeRentalRating) / newRentalCount
                    val previousCompletedRentals = productSnapshot.getIntValue("rentalCount")

                    transaction.update(
                        productRef,
                        mapOf(
                            "rentalRatingAverage" to newRentalAverage,
                            "rentalRatingCount" to newRentalCount,
                            "rentalCount" to previousCompletedRentals + 1,
                            "lastRentedAt" to now
                        )
                    )
                }

                SellerStats(newSellerAverage, newSellerCount)
            }.await()

            val sellerProducts = productsCollection
                .whereEqualTo("sellerId", product.sellerId)
                .get()
                .await()
            if (!sellerProducts.isEmpty) {
                val batch = firestore.batch()
                sellerProducts.documents.forEach { sellerProductDoc ->
                    batch.update(
                        sellerProductDoc.reference,
                        mapOf(
                            "sellerRatingAverage" to sellerStats.average,
                            "sellerRatingCount" to sellerStats.count
                        )
                    )
                }
                batch.commit().await()
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Degerlendirme kaydedilemedi")
        }
    }

    private fun mapProduct(doc: DocumentSnapshot): Product {
        return Product(
            id = doc.id,
            title = doc.getString("title") ?: "",
            description = doc.getString("description") ?: "",
            price = doc.getString("price") ?: "",
            imageUrl = doc.getString("imageUrl"),
            tag = doc.getString("tag") ?: "",
            categoryId = doc.getString("categoryId"),
            sellerName = doc.getString("sellerName") ?: "",
            sellerId = doc.getString("sellerId") ?: "",
            dormitory = doc.getString("dormitory") ?: "",
            deliveryPreference = doc.getString("deliveryPreference") ?: "",
            isAvailable = doc.getBoolean("isAvailable") ?: true,
            sellerRatingAverage = doc.getDoubleValue("sellerRatingAverage"),
            sellerRatingCount = doc.getIntValue("sellerRatingCount"),
            rentalRatingAverage = doc.getDoubleValue("rentalRatingAverage"),
            rentalRatingCount = doc.getIntValue("rentalRatingCount"),
            rentalCount = doc.getIntValue("rentalCount"),
            lastRentedAt = doc.getLong("lastRentedAt")
        )
    }
}

private data class SellerStats(
    val average: Double,
    val count: Int
)

private fun DocumentSnapshot.getDoubleValue(field: String): Double {
    return when (val value = get(field)) {
        is Number -> value.toDouble()
        is String -> value.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }
}

private fun DocumentSnapshot.getIntValue(field: String): Int {
    return when (val value = get(field)) {
        is Number -> value.toInt()
        is String -> value.toIntOrNull() ?: 0
        else -> 0
    }
}

private fun DocumentSnapshot.getNullableIntValue(field: String): Int? {
    return when (val value = get(field)) {
        is Number -> value.toInt()
        is String -> value.toIntOrNull()
        else -> null
    }
}

private fun parsePriceToKurus(raw: String): Long {
    val normalized = raw
        .replace("TL", "", ignoreCase = true)
        .replace("\u20BA", "")
        .replace(".", "")
        .replace(",", ".")
        .trim()
    return ((normalized.toDoubleOrNull() ?: 0.0) * 100).toLong()
}
