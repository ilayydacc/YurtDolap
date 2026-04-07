package com.yurtdolap.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yurtdolap.app.domain.model.Category
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

data class HomeState(
    val categories: List<Category> = emptyList(),
    val featuredProducts: List<Product> = emptyList(),
    val favoriteIds: List<String> = emptyList(),
    val selectedCategoryId: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UIState<HomeState>>(UIState.Idle)
    val uiState: StateFlow<UIState<HomeState>> = _uiState.asStateFlow()
    private var loadHomeDataJob: Job? = null

    init {
        loadHomeData()
    }

    fun loadHomeData(categoryId: String? = null) {
        loadHomeDataJob?.cancel()
        loadHomeDataJob = viewModelScope.launch {
            _uiState.value = UIState.Loading

            combine(
                productRepository.getProducts(categoryId),
                userRepository.getFavoriteProductIds()
            ) { productsResource, favoritesResource ->
                if (productsResource is Resource.Error) {
                    return@combine UIState.Error(productsResource.message ?: "Bilinmeyen Hata")
                }

                val products = if (productsResource is Resource.Success) productsResource.data ?: emptyList() else emptyList()
                val favorites = if (favoritesResource is Resource.Success) favoritesResource.data ?: emptyList() else emptyList()

                val categories = listOf(
                    Category("1", "Elektronik", "ic_monitor"),
                    Category("2", "Kitap", "ic_book"),
                    Category("3", "Mutfak", "ic_kitchen"),
                    Category("4", "Kırtasiye", "ic_pencil"),
                    Category("5", "Giyim", "ic_shirt")
                )

                UIState.Success(HomeState(categories, products, favorites, categoryId))
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

    fun selectCategory(categoryId: String?) {
        loadHomeData(categoryId)
    }
}
