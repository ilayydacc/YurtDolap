package com.yurtdolap.app.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yurtdolap.app.domain.model.Product
import com.yurtdolap.app.domain.model.ProductReview
import com.yurtdolap.app.domain.model.ProductTransaction
import com.yurtdolap.app.domain.model.ProductTransactionStatus
import com.yurtdolap.app.domain.model.isForRent
import com.yurtdolap.app.domain.model.isNeedRequest
import com.yurtdolap.app.domain.repository.AuthRepository
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
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val productId: String? = savedStateHandle["productId"]

    private val _uiState = MutableStateFlow<UIState<Product>>(UIState.Idle)
    val uiState: StateFlow<UIState<Product>> = _uiState.asStateFlow()

    private val _reviewsState = MutableStateFlow<UIState<List<ProductReview>>>(UIState.Loading)
    val reviewsState: StateFlow<UIState<List<ProductReview>>> = _reviewsState.asStateFlow()

    private val _transactionsState = MutableStateFlow<UIState<List<ProductTransaction>>>(UIState.Loading)
    val transactionsState: StateFlow<UIState<List<ProductTransaction>>> = _transactionsState.asStateFlow()

    private val _isSubmittingReview = MutableStateFlow(false)
    val isSubmittingReview: StateFlow<Boolean> = _isSubmittingReview.asStateFlow()
    private val _isSubmittingTransaction = MutableStateFlow(false)
    val isSubmittingTransaction: StateFlow<Boolean> = _isSubmittingTransaction.asStateFlow()

    private var loadProductJob: Job? = null
    private var loadReviewsJob: Job? = null
    private var loadTransactionsJob: Job? = null
    private var isDeletingProduct: Boolean = false

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _currentUserId = MutableStateFlow(authRepository.currentUserId)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _navigateToChatEvent = MutableSharedFlow<String>()
    val navigateToChatEvent = _navigateToChatEvent.asSharedFlow()

    private val _productDeletedEvent = MutableSharedFlow<Unit>()
    val productDeletedEvent = _productDeletedEvent.asSharedFlow()

    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent = _messageEvent.asSharedFlow()

    init {
        loadCurrentUserRole()
        loadProduct()
        loadReviews()
        loadTransactions()
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
                    is Resource.Loading -> _uiState.value = UIState.Loading
                    is Resource.Success -> {
                        val data = resource.data
                        _uiState.value = if (data != null) {
                            UIState.Success(data)
                        } else {
                            UIState.Error("Urun verisi bos")
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

    fun loadReviews() {
        if (productId == null) {
            _reviewsState.value = UIState.Error("Degerlendirme bilgisi bulunamadi")
            return
        }

        loadReviewsJob?.cancel()
        loadReviewsJob = viewModelScope.launch {
            productRepository.getProductReviews(productId).collect { resource ->
                _reviewsState.value = when (resource) {
                    is Resource.Loading -> UIState.Loading
                    is Resource.Success -> UIState.Success(resource.data ?: emptyList())
                    is Resource.Error -> UIState.Error(resource.message ?: "Degerlendirmeler yuklenemedi")
                }
            }
        }
    }

    fun loadTransactions() {
        if (productId == null) {
            _transactionsState.value = UIState.Error("Islem bilgisi bulunamadi")
            return
        }

        loadTransactionsJob?.cancel()
        loadTransactionsJob = viewModelScope.launch {
            productRepository.getProductTransactions(productId).collect { resource ->
                _transactionsState.value = when (resource) {
                    is Resource.Loading -> UIState.Loading
                    is Resource.Success -> UIState.Success(resource.data ?: emptyList())
                    is Resource.Error -> UIState.Error(resource.message ?: "Islem talepleri yuklenemedi")
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

    fun submitReview(
        sellerRating: Int,
        rentalRating: Int?,
        comment: String
    ) {
        val currentProduct = (uiState.value as? UIState.Success)?.data ?: return
        if (_isSubmittingReview.value) return

        viewModelScope.launch {
            _isSubmittingReview.value = true
            val result = productRepository.submitProductReview(
                product = currentProduct,
                sellerRating = sellerRating,
                rentalRating = rentalRating,
                comment = comment
            )
            _isSubmittingReview.value = false

            when (result) {
                is Resource.Success -> _messageEvent.emit("Degerlendirmen kaydedildi")
                is Resource.Error -> _messageEvent.emit(result.message ?: "Degerlendirme kaydedilemedi")
                is Resource.Loading -> Unit
            }
        }
    }

    fun requestTransaction() {
        val currentProduct = (uiState.value as? UIState.Success)?.data ?: return
        if (_isSubmittingTransaction.value) return

        viewModelScope.launch {
            _isSubmittingTransaction.value = true
            val result = productRepository.requestProductTransaction(currentProduct)
            when (result) {
                is Resource.Success -> {
                    val chatResult = chatRepository.createOrGetChatRoom(
                        otherUserId = currentProduct.sellerId,
                        productId = currentProduct.id,
                        productTitle = currentProduct.title,
                        productImageUrl = currentProduct.imageUrl ?: ""
                    )
                    if (chatResult is Resource.Success && chatResult.data != null) {
                        val notificationResult = chatRepository.sendMessage(
                            chatId = chatResult.data,
                            text = buildTransactionRequestMessage(currentProduct)
                        )
                        if (notificationResult is Resource.Success) {
                            _messageEvent.emit("Islem talebin saticinin mesaj kutusuna gonderildi")
                        } else {
                            _messageEvent.emit(
                                (notificationResult as? Resource.Error)?.message
                                    ?: "Talep olusturuldu ama mesaj kutusuna dusurulemedi"
                            )
                        }
                    } else {
                        _messageEvent.emit(
                            (chatResult as? Resource.Error)?.message
                                ?: "Talep olusturuldu ama sohbet baslatilamadi"
                        )
                    }
                }
                is Resource.Error -> _messageEvent.emit(result.message ?: "Islem talebi gonderilemedi")
                is Resource.Loading -> Unit
            }
            _isSubmittingTransaction.value = false
        }
    }

    private fun buildTransactionRequestMessage(product: Product): String {
        val requestType = if (product.isForRent()) "kiralama" else "islem"
        return "Merhaba, \"${product.title}\" ilani icin $requestType talebi gonderdim. Detaylari buradan konusalim."
    }

    fun completeTransaction(buyerId: String) {
        val currentProduct = (uiState.value as? UIState.Success)?.data ?: return
        if (_isSubmittingTransaction.value) return

        viewModelScope.launch {
            _isSubmittingTransaction.value = true
            val result = productRepository.completeProductTransaction(currentProduct, buyerId)
            _isSubmittingTransaction.value = false
            when (result) {
                is Resource.Success -> _messageEvent.emit("Islem tamamlandi olarak isaretlendi")
                is Resource.Error -> _messageEvent.emit(result.message ?: "Islem tamamlanamadi")
                is Resource.Loading -> Unit
            }
        }
    }

    fun reviewEligibility(
        product: Product,
        currentUserId: String?,
        reviews: List<ProductReview>,
        transactions: List<ProductTransaction>
    ): ReviewEligibility {
        val hasReviewed = currentUserId != null && reviews.any { it.reviewerId == currentUserId }
        if (product.isNeedRequest()) {
            return ReviewEligibility(false, hasReviewed, "Talep ilanlari icin degerlendirme yok")
        }
        if (currentUserId == null) {
            return ReviewEligibility(false, hasReviewed, "Degerlendirme icin giris yapman gerekir")
        }
        if (currentUserId == product.sellerId) {
            return ReviewEligibility(false, hasReviewed, "Satici kendi ilanini degerlendiremez")
        }
        if (hasReviewed) {
            return ReviewEligibility(false, true, "Bu ilan icin degerlendirmeni zaten biraktin.")
        }

        val transaction = transactions.firstOrNull { it.buyerId == currentUserId }
        return when {
            transaction == null -> ReviewEligibility(false, false, "Once saticiya islem talebi gondermelisin.")
            transaction.status != ProductTransactionStatus.COMPLETED ->
                ReviewEligibility(false, false, "Satici islemi tamamlandi olarak onayladiginda yorum acilir.")
            else -> ReviewEligibility(true, false, null)
        }
    }

    fun deleteProductAsAdmin() {
        val currentProduct = (uiState.value as? UIState.Success)?.data ?: return
        if (!_isAdmin.value) return

        viewModelScope.launch {
            isDeletingProduct = true
            val result = productRepository.deleteProduct(currentProduct.id)
            when (result) {
                is Resource.Success -> {
                    loadProductJob?.cancel()
                    loadReviewsJob?.cancel()
                    _transactionsState.value = UIState.Idle
                    _productDeletedEvent.emit(Unit)
                }
                is Resource.Error -> {
                    isDeletingProduct = false
                }
                is Resource.Loading -> Unit
            }
        }
    }
}

data class ReviewEligibility(
    val canReview: Boolean,
    val hasReviewed: Boolean,
    val blockedReason: String?
)
