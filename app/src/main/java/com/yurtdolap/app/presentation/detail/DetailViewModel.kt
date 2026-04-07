package com.yurtdolap.app.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yurtdolap.app.domain.model.Product
import com.yurtdolap.app.domain.repository.ChatRepository
import com.yurtdolap.app.domain.repository.ProductRepository
import com.yurtdolap.app.domain.repository.UserRepository
import com.yurtdolap.app.domain.util.Resource
import com.yurtdolap.app.presentation.designsystem.components.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val productId: String? = savedStateHandle["productId"]

    private val _uiState = MutableStateFlow<UIState<Product>>(UIState.Idle)
    val uiState: StateFlow<UIState<Product>> = _uiState.asStateFlow()

    private var loadProductJob: Job? = null
    private var isDeletingProduct: Boolean = false

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _navigateToChatEvent = MutableSharedFlow<String>()
    val navigateToChatEvent = _navigateToChatEvent.asSharedFlow()

    private val _productDeletedEvent = MutableSharedFlow<Unit>()
    val productDeletedEvent = _productDeletedEvent.asSharedFlow()

    init {
        loadCurrentUserRole()
        loadProduct()
    }

    private fun loadCurrentUserRole() {
        viewModelScope.launch {
            repeat(3) { attempt ->
                val profile = userRepository.getUserProfile()
                if (profile is Resource.Success) {
                    _isAdmin.value = profile.data?.isAdmin == true
                    return@launch
                }
                if (attempt < 2) delay(300)
            }
        }
    }

    fun loadProduct() {
        if (productId == null) {
            _uiState.value = UIState.Error("Urun bulunamadi")
            return
        }

        loadProductJob?.cancel()
        loadProductJob = viewModelScope.launch {
            productRepository.getProductById(productId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = UIState.Loading
                    }

                    is Resource.Success -> {
                        val data = resource.data
                        if (data != null) {
                            _uiState.value = UIState.Success(data)
                        } else {
                            _uiState.value = UIState.Error("Urun verisi bos")
                        }
                    }

                    is Resource.Error -> {
                        if (!isDeletingProduct) {
                            _uiState.value = UIState.Error(resource.message ?: "Bilinmeyen bir hata olustu")
                        }
                    }
                }
            }
        }
    }

    fun onMessageSellerClicked() {
        val currentProduct = (uiState.value as? UIState.Success)?.data ?: return

        viewModelScope.launch {
            val result = chatRepository.createOrGetChatRoom(
                otherUserId = currentProduct.sellerId,
                productId = currentProduct.id,
                productTitle = currentProduct.title,
                productImageUrl = currentProduct.imageUrl ?: ""
            )
            if (result is Resource.Success && result.data != null) {
                _navigateToChatEvent.emit(result.data)
            }
        }
    }

    fun deleteProductAsAdmin() {
        val currentProduct = (uiState.value as? UIState.Success)?.data ?: return
        if (!_isAdmin.value) return

        viewModelScope.launch {
            isDeletingProduct = true
            val result = productRepository.deleteProduct(currentProduct.id)
            if (result is Resource.Success) {
                loadProductJob?.cancel()
                _productDeletedEvent.emit(Unit)
            } else {
                isDeletingProduct = false
            }
        }
    }
}
