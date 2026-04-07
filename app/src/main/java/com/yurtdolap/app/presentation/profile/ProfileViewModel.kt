package com.yurtdolap.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yurtdolap.app.domain.model.Product
import com.yurtdolap.app.domain.repository.AuthRepository
import com.yurtdolap.app.domain.repository.ProductRepository
import com.yurtdolap.app.domain.util.Resource
import com.yurtdolap.app.presentation.designsystem.components.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.yurtdolap.app.domain.repository.UserRepository

data class UserProfile(
    val name: String,
    val dormitory: String,
    val activeListings: List<Product>
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UIState<UserProfile>>(UIState.Idle)
    val uiState: StateFlow<UIState<UserProfile>> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = UIState.Loading

            if (!authRepository.isUserAuthenticatedInFirebase) {
                _uiState.value = UIState.Error("Profili görüntülemek için giriş yapın.")
                return@launch
            }
            
            var userName = "Anonim"
            var userDormitory = "Bilinmeyen Yurt"
            
            val profileResource = userRepository.getUserProfile()
            if (profileResource is Resource.Success) {
                val data = profileResource.data
                if (data != null) {
                    if (data.name.isNotBlank()) userName = data.name
                    if (data.dormitory.isNotBlank()) userDormitory = data.dormitory
                }
            }

            productRepository.getProducts().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val userId = authRepository.currentUserId ?: "Anonim"
                        
                        val myProducts = resource.data?.filter { it.sellerId == userId } ?: emptyList()

                        val profile = UserProfile(
                            name = userName,
                            dormitory = userDormitory,
                            activeListings = myProducts
                        )
                        _uiState.value = UIState.Success(profile)
                    }
                    is Resource.Error -> {
                        _uiState.value = UIState.Error(resource.message ?: "Profil bilgileri alınamadı.")
                    }
                    else -> {}
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            productRepository.deleteProduct(productId)
            // Listeners in getProducts() will automatically trigger an update and refresh the list
        }
    }
}
