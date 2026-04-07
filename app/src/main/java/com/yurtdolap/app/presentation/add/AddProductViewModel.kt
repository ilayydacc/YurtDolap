package com.yurtdolap.app.presentation.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yurtdolap.app.domain.model.Category
import com.yurtdolap.app.domain.model.Product
import com.yurtdolap.app.domain.model.ProductTags
import com.yurtdolap.app.domain.repository.AuthRepository
import com.yurtdolap.app.domain.repository.ProductRepository
import com.yurtdolap.app.domain.repository.UserRepository
import com.yurtdolap.app.domain.util.Resource
import com.yurtdolap.app.presentation.designsystem.components.UIState
import android.content.Context
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddProductState(
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val deliveryPreference: String = "",
    val imageUri: Uri? = null,
    val selectedCategoryId: String? = null,
    val selectedTag: String = ProductTags.FOR_SALE,
    val categories: List<Category> = listOf(
        Category("1", "Elektronik", "ic_monitor"),
        Category("2", "Kitap", "ic_book"),
        Category("3", "Mutfak", "ic_kitchen"),
        Category("4", "Kırtasiye", "ic_pencil"),
        Category("5", "Giyim", "ic_shirt")
    )
)

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UIState<Unit>>(UIState.Idle)
    val uiState: StateFlow<UIState<Unit>> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(AddProductState())
    val formState: StateFlow<AddProductState> = _formState.asStateFlow()

    fun onTitleChange(title: String) {
        _formState.value = _formState.value.copy(title = title)
    }

    fun onDescriptionChange(description: String) {
        _formState.value = _formState.value.copy(description = description)
    }

    fun onPriceChange(price: String) {
        _formState.value = _formState.value.copy(price = price)
    }

    fun onDeliveryPreferenceChange(deliveryPreference: String) {
        _formState.value = _formState.value.copy(deliveryPreference = deliveryPreference)
    }

    fun onImageSelect(uri: Uri?) {
        _formState.value = _formState.value.copy(imageUri = uri)
    }

    fun onCategorySelect(categoryId: String) {
        _formState.value = _formState.value.copy(selectedCategoryId = categoryId)
    }

    fun onTagSelect(tag: String) {
        _formState.value = _formState.value.copy(selectedTag = tag)
    }

    fun addProduct(context: Context) {
        val currentState = _formState.value
        
        val isNeedRequest = currentState.selectedTag == ProductTags.NEED_REQUEST
        if (currentState.title.isBlank() || currentState.selectedCategoryId == null || (!isNeedRequest && currentState.price.isBlank())) {
            _uiState.value = UIState.Error(
                if (isNeedRequest) {
                    "Lütfen başlık ve kategori alanlarını doldurun."
                } else {
                    "Lütfen başlık, fiyat ve kategori alanlarını doldurun."
                }
            )
            return
        }

        _uiState.value = UIState.Loading

        viewModelScope.launch {
            // Get current user details to attach context
            val currentUserId = authRepository.currentUserId
            if (currentUserId != null) {
                
                var uploadedImageUrl: String? = null
                
                val uri = currentState.imageUri
                if (uri != null) {
                    try {
                        val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                        if (bytes != null) {
                            val fileName = "img_${UUID.randomUUID()}.jpg"
                            val uploadResult = productRepository.uploadProductImage(bytes, fileName)
                            if (uploadResult is Resource.Success) {
                                uploadedImageUrl = uploadResult.data
                            } else {
                                _uiState.value = UIState.Error(uploadResult.message ?: "Resim yüklenemedi.")
                                return@launch
                            }
                        }
                    } catch (e: Exception) {
                        _uiState.value = UIState.Error("Resim okunurken hata oluştu: ${e.localizedMessage}")
                        return@launch
                    }
                }
                
                var userName = "Anonim Kullanıcı"
                var userDormitory = "Belirtilmemiş Yurt"
                
                val profileResource = userRepository.getUserProfile()
                if (profileResource is Resource.Success) {
                    val data = profileResource.data
                    if (data != null) {
                        if (data.name.isNotBlank()) userName = data.name
                        if (data.dormitory.isNotBlank()) userDormitory = data.dormitory
                    }
                }

                val product = Product(
                    id = "", // Let Firestore generate ID
                    title = currentState.title,
                    description = currentState.description.trim(),
                    price = if (isNeedRequest && currentState.price.isBlank()) "Bütçe belirtilmedi" else currentState.price,
                    imageUrl = uploadedImageUrl,
                    tag = currentState.selectedTag,
                    categoryId = currentState.selectedCategoryId,
                    sellerName = userName,
                    sellerId = currentUserId,
                    dormitory = userDormitory,
                    deliveryPreference = currentState.deliveryPreference.trim(),
                    isAvailable = true
                )

                val result = productRepository.addProduct(product)
                if (result is Resource.Success) {
                    _uiState.value = UIState.Success(Unit)
                    // Reset form logic after success if needed
                    _formState.value = AddProductState()
                } else {
                    _uiState.value = UIState.Error(result.message ?: "Ürün eklenirken bir hata oluştu.")
                }
            } else {
                _uiState.value = UIState.Error("Oturum süreniz dolmuş olabilir. Lütfen tekrar giriş yapın.")
            }
        }
    }
}
