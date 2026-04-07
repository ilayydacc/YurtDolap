package com.yurtdolap.app.presentation.edit

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yurtdolap.app.domain.model.Category
import com.yurtdolap.app.domain.model.ProductTags
import com.yurtdolap.app.domain.repository.ProductRepository
import com.yurtdolap.app.domain.util.Resource
import com.yurtdolap.app.presentation.designsystem.components.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class EditProductState(
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val deliveryPreference: String = "",
    val imageUri: Uri? = null,
    val existingImageUrl: String? = null,
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
class EditProductViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val productId: String? = savedStateHandle["productId"]

    private val _uiState = MutableStateFlow<UIState<Unit>>(UIState.Idle)
    val uiState: StateFlow<UIState<Unit>> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(EditProductState())
    val formState: StateFlow<EditProductState> = _formState.asStateFlow()

    init {
        loadProduct()
    }

    private fun loadProduct() {
        if (productId == null) {
            _uiState.value = UIState.Error("Ürün ID bulunamadı.")
            return
        }

        viewModelScope.launch {
            _uiState.value = UIState.Loading
            productRepository.getProductById(productId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> { } // Already loading
                    is Resource.Success -> {
                        resource.data?.let { product ->
                            _formState.value = _formState.value.copy(
                                title = product.title,
                                description = product.description,
                                price = product.price,
                                deliveryPreference = product.deliveryPreference,
                                existingImageUrl = product.imageUrl,
                                selectedCategoryId = product.categoryId,
                                selectedTag = product.tag
                            )
                            _uiState.value = UIState.Idle // Ready for user to edit
                        } ?: run {
                            _uiState.value = UIState.Error("Ürün bulunamadı.")
                        }
                    }
                    is Resource.Error -> {
                        _uiState.value = UIState.Error(resource.message ?: "Yükleme hatası")
                    }
                }
            }
        }
    }

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

    fun updateProduct(context: Context) {
        if (productId == null) return
        val currentState = _formState.value

        val isNeedRequest = currentState.selectedTag == ProductTags.NEED_REQUEST
        if (currentState.title.isBlank() || currentState.selectedCategoryId == null || (!isNeedRequest && currentState.price.isBlank())) {
            _uiState.value = UIState.Error(
                if (isNeedRequest) {
                    "Lütfen başlık ve kategori alanlarını doldurun."
                } else {
                    "Lütfen gerekli alanları doldurun."
                }
            )
            return
        }

        _uiState.value = UIState.Loading

        viewModelScope.launch {
            var finalImageUrl: String? = currentState.existingImageUrl

            val uri = currentState.imageUri
            if (uri != null) {
                try {
                    val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                    if (bytes != null) {
                        val fileName = "img_${UUID.randomUUID()}.jpg"
                        val uploadResult = productRepository.uploadProductImage(bytes, fileName)
                        if (uploadResult is Resource.Success) {
                            finalImageUrl = uploadResult.data
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

            val updates = mapOf(
                "title" to currentState.title,
                "description" to currentState.description.trim(),
                "price" to if (isNeedRequest && currentState.price.isBlank()) "Bütçe belirtilmedi" else currentState.price,
                "deliveryPreference" to currentState.deliveryPreference.trim(),
                "tag" to currentState.selectedTag,
                "categoryId" to currentState.selectedCategoryId as Any,
                "imageUrl" to (finalImageUrl ?: "")
            )

            val result = productRepository.updateProduct(productId, updates)
            if (result is Resource.Success) {
                _uiState.value = UIState.Success(Unit)
            } else {
                _uiState.value = UIState.Error(result.message ?: "Güncellenirken hata oluştu.")
            }
        }
    }
}
