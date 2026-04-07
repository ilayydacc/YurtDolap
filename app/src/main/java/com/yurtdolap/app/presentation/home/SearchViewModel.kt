package com.yurtdolap.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yurtdolap.app.domain.model.Product
import com.yurtdolap.app.domain.repository.ProductRepository
import com.yurtdolap.app.domain.util.Resource
import com.yurtdolap.app.presentation.designsystem.components.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<UIState<List<Product>>>(UIState.Idle)
    val uiState: StateFlow<UIState<List<Product>>> = _uiState.asStateFlow()

    private var allProducts: List<Product> = emptyList()

    init {
        loadAllProducts()
    }

    private fun loadAllProducts() {
        viewModelScope.launch {
            _uiState.value = UIState.Loading
            productRepository.getProducts().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        allProducts = resource.data ?: emptyList()
                        _uiState.value = UIState.Success(allProducts)
                    }
                    is Resource.Error -> {
                        _uiState.value = UIState.Error(resource.message ?: "Ürünler yüklenirken hata oluştu.")
                    }
                    else -> {}
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterProducts(query)
    }

    private fun filterProducts(query: String) {
        if (query.isBlank()) {
            _uiState.value = UIState.Success(allProducts)
            return
        }

        val filtered = allProducts.filter { product ->
            product.title.contains(query, ignoreCase = true) ||
                product.tag.contains(query, ignoreCase = true) ||
                product.dormitory.contains(query, ignoreCase = true) ||
                product.price.contains(query, ignoreCase = true)
        }
        
        _uiState.value = UIState.Success(filtered)
    }
}
