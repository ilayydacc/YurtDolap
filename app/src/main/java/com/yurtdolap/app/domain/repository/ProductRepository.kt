package com.yurtdolap.app.domain.repository

import com.yurtdolap.app.domain.model.Product
import com.yurtdolap.app.domain.model.ProductReview
import com.yurtdolap.app.domain.model.ProductTransaction
import com.yurtdolap.app.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(categoryId: String? = null): Flow<Resource<List<Product>>>
    fun getProductById(id: String): Flow<Resource<Product>>
    fun getProductReviews(productId: String): Flow<Resource<List<ProductReview>>>
    fun getProductTransactions(productId: String): Flow<Resource<List<ProductTransaction>>>
    suspend fun addProduct(product: Product): Resource<Unit>
    suspend fun deleteProduct(productId: String): Resource<Unit>
    suspend fun uploadProductImage(imageBytes: ByteArray, fileName: String): Resource<String>
    suspend fun updateProduct(productId: String, updates: Map<String, Any>): Resource<Unit>
    suspend fun requestProductTransaction(product: Product): Resource<Unit>
    suspend fun simulateProductPayment(
        productId: String,
        shouldSucceed: Boolean,
        cardLast4: String
    ): Resource<Product>
    suspend fun completeProductTransaction(product: Product, buyerId: String): Resource<Unit>
    suspend fun submitProductReview(
        product: Product,
        sellerRating: Int,
        rentalRating: Int?,
        comment: String
    ): Resource<Unit>
}
