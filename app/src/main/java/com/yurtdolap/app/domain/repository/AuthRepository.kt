package com.yurtdolap.app.domain.repository

import com.yurtdolap.app.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isUserAuthenticatedInFirebase: Boolean
    val currentUserId: String?

    suspend fun signInAnonymously(): Resource<Unit>
    suspend fun signInWithEmailAndPassword(email: String, password: String): Resource<Unit>
    suspend fun createUserWithEmailAndPassword(email: String, password: String, name: String, city: String, dormitory: String): Resource<Unit>
    suspend fun signOut(): Resource<Unit>
}
