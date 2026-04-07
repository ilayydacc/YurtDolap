package com.yurtdolap.app.presentation.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yurtdolap.app.domain.model.Product
import com.yurtdolap.app.domain.repository.ProductRepository
import com.yurtdolap.app.domain.repository.UserRepository
import com.yurtdolap.app.domain.util.Resource
import com.yurtdolap.app.presentation.designsystem.components.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavedState(
    val savedProducts: List<Product> = emptyList(),
    val favoriteIds: List<String> = emptyList()
)

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UIState<SavedState>>(UIState.Idle)
    val uiState: StateFlow<UIState<SavedState>> = _uiState.asStateFlow()
    private var loadSavedProductsJob: Job? = null

    init {
        loadSavedProducts()
    }

    fun loadSavedProducts() {
        loadSavedProductsJob?.cancel()
        loadSavedProductsJob = viewModelScope.launch {
            _uiState.value = UIState.Loading

            combine(
                productRepository.getProducts(),
                userRepository.getFavoriteProductIds()
            ) { productsResource, favoritesResource ->
                if (productsResource is Resource.Error) {
                    return@combine UIState.Error(productsResource.message ?: "Bilinmeyen Hata")
                }
                
                if (favoritesResource is Resource.Error) {
                    return@combine UIState.Error(favoritesResource.message ?: "Favoriler yüklenemedi")
                }

                val allProducts = if (productsResource is Resource.Success) productsResource.data ?: emptyList() else emptyList()
                val favorites = if (favoritesResource is Resource.Success) favoritesResource.data ?: emptyList() else emptyList()

                val savedProducts = allProducts.filter { favorites.contains(it.id) }

                UIState.Success(SavedState(savedProducts, favorites))
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            userRepository.toggleFavorite(productId)
        }
    }
}
