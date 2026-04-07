package com.yurtdolap.app.di

import com.yurtdolap.app.data.repository.FirebaseAuthRepositoryImpl
import com.yurtdolap.app.data.repository.FirebaseProductRepositoryImpl
import com.yurtdolap.app.data.repository.FirebaseStorageRepositoryImpl
import com.yurtdolap.app.data.repository.FirebaseUserRepositoryImpl
import com.yurtdolap.app.domain.repository.AuthRepository
import com.yurtdolap.app.domain.repository.ChatRepository
import com.yurtdolap.app.domain.repository.ProductRepository
import com.yurtdolap.app.domain.repository.StorageRepository
import com.yurtdolap.app.domain.repository.UserRepository
import com.yurtdolap.app.data.repository.FirebaseChatRepositoryImpl
import com.yurtdolap.app.data.repository.FirebaseLocationRepositoryImpl
import com.yurtdolap.app.domain.repository.LocationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: FirebaseAuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        productRepositoryImpl: FirebaseProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindStorageRepository(
        storageRepositoryImpl: FirebaseStorageRepositoryImpl
    ): StorageRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: FirebaseChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: FirebaseUserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        locationRepositoryImpl: FirebaseLocationRepositoryImpl
    ): LocationRepository
}
