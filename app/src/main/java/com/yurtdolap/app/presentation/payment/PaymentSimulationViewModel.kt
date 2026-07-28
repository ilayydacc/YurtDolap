package com.yurtdolap.app.presentation.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yurtdolap.app.domain.model.Product
import com.yurtdolap.app.domain.repository.ChatRepository
import com.yurtdolap.app.domain.repository.ProductRepository
import com.yurtdolap.app.domain.util.Resource
import com.yurtdolap.app.presentation.designsystem.components.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

private const val SuccessfulDemoCard = "4242424242424242"
private const val FailedDemoCard = "4000000000000002"

@HiltViewModel
class PaymentSimulationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val productId: String? = savedStateHandle["productId"]

    private val _uiState = MutableStateFlow<UIState<Product>>(UIState.Loading)
    val uiState: StateFlow<UIState<Product>> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(PaymentSimulationFormState())
    val formState: StateFlow<PaymentSimulationFormState> = _formState.asStateFlow()

    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent = _messageEvent.asSharedFlow()

    private val _paymentSuccessEvent = MutableSharedFlow<Unit>()
    val paymentSuccessEvent = _paymentSuccessEvent.asSharedFlow()

    init {
        loadProduct()
    }

    fun loadProduct() {
        val safeProductId = productId
        if (safeProductId.isNullOrBlank()) {
            _uiState.value = UIState.Error("Urun bilgisi eksik")
            return
        }

        viewModelScope.launch {
            productRepository.getProductById(safeProductId).collect { resource ->
                _uiState.value = when (resource) {
                    is Resource.Loading -> UIState.Loading
                    is Resource.Success -> {
                        val product = resource.data
                        if (product != null) UIState.Success(product) else UIState.Error("Urun bulunamadi")
                    }
                    is Resource.Error -> UIState.Error(resource.message ?: "Urun yuklenemedi")
                }
            }
        }
    }

    fun updateCardHolder(value: String) {
        _formState.value = _formState.value.copy(
            cardHolder = value.filter { it.isLetter() || it.isWhitespace() }.take(40),
            errorMessage = null
        )
    }

    fun updateCardNumber(value: String) {
        _formState.value = _formState.value.copy(
            cardNumber = value.filter { it.isDigit() }.take(16),
            errorMessage = null
        )
    }

    fun updateExpiry(value: String) {
        val currentState = _formState.value
        val digits = value.filter { it.isDigit() }.take(4)
        val safeDigits = when {
            digits.length < 2 -> digits
            digits.take(2).toIntOrNull() in 1..12 -> digits
            else -> digits.take(1)
        }
        val expiry = if (safeDigits.hasPastYear()) currentState.expiry else safeDigits
        _formState.value = currentState.copy(expiry = expiry, errorMessage = null)
    }

    fun updateCvv(value: String) {
        _formState.value = _formState.value.copy(
            cvv = value.filter { it.isDigit() }.take(3),
            errorMessage = null
        )
    }

    fun submitPayment() {
        val product = (uiState.value as? UIState.Success)?.data ?: return
        val currentForm = formState.value
        val validationError = currentForm.validationError()
        if (validationError != null || currentForm.isSubmitting) {
            _formState.value = currentForm.copy(errorMessage = validationError)
            return
        }

        viewModelScope.launch {
            _formState.value = currentForm.copy(isSubmitting = true, errorMessage = null)
            val shouldSucceed = currentForm.cardNumber == SuccessfulDemoCard
            val result = productRepository.simulateProductPayment(
                productId = product.id,
                shouldSucceed = shouldSucceed,
                cardLast4 = currentForm.cardNumber.takeLast(4)
            )

            when (result) {
                is Resource.Success -> {
                    val paidProduct = result.data ?: product
                    sendDemoPaymentMessage(paidProduct, shouldSucceed)
                    if (shouldSucceed) {
                        _messageEvent.emit("Demo odeme basarili. Saticiya bilgi gonderildi.")
                        _paymentSuccessEvent.emit(Unit)
                    } else {
                        _messageEvent.emit("Demo odeme basarisiz olarak kaydedildi.")
                    }
                }
                is Resource.Error -> _messageEvent.emit(result.message ?: "Odeme simulasyonu basarisiz")
                is Resource.Loading -> Unit
            }

            _formState.value = _formState.value.copy(isSubmitting = false)
        }
    }

    private suspend fun sendDemoPaymentMessage(product: Product, shouldSucceed: Boolean) {
        val chatResult = chatRepository.createOrGetChatRoom(
            otherUserId = product.sellerId,
            productId = product.id,
            productTitle = product.title,
            productImageUrl = product.imageUrl ?: ""
        )
        if (chatResult is Resource.Success && chatResult.data != null) {
            val text = if (shouldSucceed) {
                "\"${product.title}\" ilani icin test odeme simulasyonu basarili oldu. Teslimat sonrasi islemi tamamlandi olarak isaretleyebilirsin."
            } else {
                "\"${product.title}\" ilani icin test odeme simulasyonu basarisiz oldu."
            }
            chatRepository.sendMessage(chatResult.data, text)
        }
    }

}

data class PaymentSimulationFormState(
    val cardHolder: String = "",
    val cardNumber: String = "",
    val expiry: String = "",
    val cvv: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
) {
    fun validationError(): String? {
        return when {
            cardHolder.isBlank() -> "Kart sahibi gir"
            cardNumber.length != 16 -> "16 haneli test kart numarasi gir"
            cardNumber != SuccessfulDemoCard && cardNumber != FailedDemoCard -> {
                "Test icin 4242 4242 4242 4242 veya 4000 0000 0000 0002 kullan"
            }
            expiry.length != 4 -> "Son kullanma tarihini AA/YY formatinda gir"
            !expiry.hasValidMonth() -> "Ay 01 ile 12 arasinda olmali"
            expiry.hasPastYear() -> "Yil bu yil veya sonrasi olmali"
            expiry.isExpired() -> "Son kullanma tarihi gecerli ay veya sonrasi olmali"
            cvv.length != 3 -> "3 haneli CVV gir"
            else -> null
        }
    }
}

private fun String.hasValidMonth(): Boolean {
    val month = take(2).toIntOrNull() ?: return false
    return month in 1..12
}

private fun String.hasPastYear(): Boolean {
    if (length < 4) return false
    val yearSuffix = takeLast(2).toIntOrNull() ?: return true
    val currentYearSuffix = Calendar.getInstance().get(Calendar.YEAR) % 100
    return yearSuffix < currentYearSuffix
}

private fun String.isExpired(): Boolean {
    val month = take(2).toIntOrNull() ?: return true
    val yearSuffix = takeLast(2).toIntOrNull() ?: return true
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH) + 1
    val currentYearSuffix = calendar.get(Calendar.YEAR) % 100
    return yearSuffix < currentYearSuffix ||
        (yearSuffix == currentYearSuffix && month < currentMonth)
}
