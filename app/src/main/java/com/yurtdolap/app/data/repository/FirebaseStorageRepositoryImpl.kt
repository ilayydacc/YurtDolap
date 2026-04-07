package com.yurtdolap.app.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.yurtdolap.app.domain.repository.StorageRepository
import com.yurtdolap.app.domain.util.Resource
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class FirebaseStorageRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage
) : StorageRepository {

    override suspend fun uploadProductImage(imageUri: Uri, productId: String): Resource<String> {
        return try {
            // Benzersiz bir dosya adı oluşturuyoruz
            val fileName = UUID.randomUUID().toString()
            val storageRef = storage.reference.child("products").child(productId).child("$fileName.jpg")
            
            // Resmi Firebase Storage'a yüklüyoruz
            storageRef.putFile(imageUri).await()
            
            // Yüklenen resmin "indirme (download) linkini" alıyoruz
            val downloadUrl = storageRef.downloadUrl.await()
            
            Resource.Success(downloadUrl.toString())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Görsel yüklenirken bir hata oluştu.")
        }
    }
}
