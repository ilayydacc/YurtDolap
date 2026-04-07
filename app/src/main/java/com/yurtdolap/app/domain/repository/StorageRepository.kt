package com.yurtdolap.app.domain.repository

import android.net.Uri
import com.yurtdolap.app.domain.util.Resource

interface StorageRepository {
    suspend fun uploadProductImage(imageUri: Uri, productId: String): Resource<String>
}
