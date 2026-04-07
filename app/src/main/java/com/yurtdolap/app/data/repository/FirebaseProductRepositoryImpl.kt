package com.yurtdolap.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.FirebaseStorage
import com.yurtdolap.app.domain.model.Product
import com.yurtdolap.app.domain.repository.ProductRepository
import com.yurtdolap.app.domain.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject

class FirebaseProductRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ProductRepository {

    private val productsCollection = firestore.collection("products")
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
                trySend(Resource.Error(error.localizedMessage ?: "Ürünler yüklenemedi"))
                return@addSnapshotListener
            }

            val products = snapshot?.documents?.mapNotNull { doc ->
                val id = doc.id
                val title = doc.getString("title") ?: ""
                val description = doc.getString("description") ?: ""
                val price = doc.getString("price") ?: ""
                val imageUrl = doc.getString("imageUrl")
                val tag = doc.getString("tag") ?: ""
                val categoryId = doc.getString("categoryId")
                val sellerName = doc.getString("sellerName") ?: ""
                val sellerId = doc.getString("sellerId") ?: ""
                val dormitory = doc.getString("dormitory") ?: ""
                val deliveryPreference = doc.getString("deliveryPreference") ?: ""
                val isAvailable = doc.getBoolean("isAvailable") ?: true

                Product(
                    id = id,
                    title = title,
                    description = description,
                    price = price,
                    imageUrl = imageUrl,
                    tag = tag,
                    categoryId = categoryId,
                    sellerName = sellerName,
                    sellerId = sellerId,
                    dormitory = dormitory,
                    deliveryPreference = deliveryPreference,
                    isAvailable = isAvailable
                )
            } ?: emptyList()

            trySend(Resource.Success(products))
        }

        awaitClose { listener.remove() }
    }

    override fun getProductById(id: String): Flow<Resource<Product>> = callbackFlow {
        trySend(Resource.Loading())
        
        val listener = productsCollection.document(id).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "Ürün yüklenemedi"))
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val title = snapshot.getString("title") ?: ""
                val description = snapshot.getString("description") ?: ""
                val price = snapshot.getString("price") ?: ""
                val imageUrl = snapshot.getString("imageUrl")
                val tag = snapshot.getString("tag") ?: ""
                val categoryId = snapshot.getString("categoryId")
                val sellerName = snapshot.getString("sellerName") ?: ""
                val sellerId = snapshot.getString("sellerId") ?: ""
                val dormitory = snapshot.getString("dormitory") ?: ""
                val deliveryPreference = snapshot.getString("deliveryPreference") ?: ""
                val isAvailable = snapshot.getBoolean("isAvailable") ?: true

                val product = Product(
                    id = snapshot.id,
                    title = title,
                    description = description,
                    price = price,
                    imageUrl = imageUrl,
                    tag = tag,
                    categoryId = categoryId,
                    sellerName = sellerName,
                    sellerId = sellerId,
                    dormitory = dormitory,
                    deliveryPreference = deliveryPreference,
                    isAvailable = isAvailable
                )
                trySend(Resource.Success(product))
            } else {
                trySend(Resource.Error("Ürün bulunamadı"))
            }
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
                "isAvailable" to product.isAvailable
            )
            
            // Eğer id boş gelirse Firestore'un auto-id üretmesini sağlıyoruz.
            if (product.id.isEmpty()) {
                productsCollection.add(productMap).await()
            } else {
                productsCollection.document(product.id).set(productMap).await()
            }
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Ürün eklenirken hata oluştu")
        }
    }

    override suspend fun deleteProduct(productId: String): Resource<Unit> {
        return try {
            productsCollection.document(productId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Ürün silinirken hata oluştu")
        }
    }

    override suspend fun uploadProductImage(imageBytes: ByteArray, fileName: String): Resource<String> {
        return try {
            val imageRef = storageRef.child(fileName)
            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()
            
            // Suspend coroutine until upload succeeds to avoid UploadTask.await() compiler issues
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
            Resource.Error(e.localizedMessage ?: "Resim yüklenirken hata oluştu")
        }
    }

    override suspend fun updateProduct(productId: String, updates: Map<String, Any>): Resource<Unit> {
        return try {
            productsCollection.document(productId).update(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Ürün güncellenirken hata oluştu")
        }
    }
}
