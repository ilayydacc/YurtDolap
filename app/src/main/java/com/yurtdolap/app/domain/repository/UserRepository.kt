package com.yurtdolap.app.domain.repository

import com.yurtdolap.app.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getFavoriteProductIds(): Flow<Resource<List<String>>>
    suspend fun toggleFavorite(productId: String): Resource<Unit>
    suspend fun getUserProfile(): Resource<com.yurtdolap.app.domain.model.UserProfileData>
    suspend fun updateUserProfile(name: String, city: String, dormitory: String): Resource<Unit>
}
