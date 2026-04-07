package com.yurtdolap.app.domain.repository

import com.yurtdolap.app.domain.model.Product
import com.yurtdolap.app.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(categoryId: String? = null): Flow<Resource<List<Product>>>
    fun getProductById(id: String): Flow<Resource<Product>>
    suspend fun addProduct(product: Product): Resource<Unit>
    suspend fun deleteProduct(productId: String): Resource<Unit>
    suspend fun uploadProductImage(imageBytes: ByteArray, fileName: String): Resource<String>
    suspend fun updateProduct(productId: String, updates: Map<String, Any>): Resource<Unit>
}
