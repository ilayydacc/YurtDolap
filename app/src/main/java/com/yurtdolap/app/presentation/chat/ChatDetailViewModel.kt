package com.yurtdolap.app.presentation.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yurtdolap.app.domain.model.ChatRoom
import com.yurtdolap.app.domain.model.Message
import com.yurtdolap.app.domain.model.Product
import com.yurtdolap.app.domain.model.ProductTransaction
import com.yurtdolap.app.domain.model.ProductTransactionStatus
import com.yurtdolap.app.domain.repository.AuthRepository
import com.yurtdolap.app.domain.repository.ChatRepository
import com.yurtdolap.app.domain.repository.ProductRepository
import com.yurtdolap.app.domain.util.Resource
import com.yurtdolap.app.presentation.designsystem.components.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val chatId: String? = savedStateHandle["chatId"]

    private val _uiState = MutableStateFlow<UIState<List<Message>>>(UIState.Loading)
    val uiState: StateFlow<UIState<List<Message>>> = _uiState.asStateFlow()

    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    private val _approvalState = MutableStateFlow(TransactionApprovalUiState())
    val approvalState: StateFlow<TransactionApprovalUiState> = _approvalState.asStateFlow()

    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent = _messageEvent.asSharedFlow()

    val currentUserId: String = authRepository.currentUserId ?: ""

    private var room: ChatRoom? = null
    private var product: Product? = null
    private var transactions: List<ProductTransaction> = emptyList()
    private var productJob: Job? = null
    private var transactionsJob: Job? = null

    init {
        if (chatId.isNullOrBlank()) {
            _uiState.value = UIState.Error("Sohbet bilgisi eksik.")
        } else {
            listenForChatRoom()
            listenForMessages()
        }
    }

    private fun listenForChatRoom() {
        val safeChatId = chatId ?: return
        viewModelScope.launch {
            chatRepository.getChatRoom(safeChatId).collect { resource ->
                if (resource is Resource.Success && resource.data != null) {
                    val nextRoom = resource.data
                    room = nextRoom
                    updateApprovalState()
                    if (nextRoom.productId.isNotBlank() && nextRoom.productId != product?.id) {
                        listenForProduct(nextRoom.productId)
                        listenForTransactions(nextRoom.productId)
                    }
                } else if (resource is Resource.Error) {
                    _messageEvent.emit(resource.message ?: "Sohbet bilgisi alinamadi")
                }
            }
        }
    }

    private fun listenForProduct(productId: String) {
        productJob?.cancel()
        productJob = viewModelScope.launch {
            productRepository.getProductById(productId).collect { resource ->
                if (resource is Resource.Success && resource.data != null) {
                    product = resource.data
                    updateApprovalState()
                } else if (resource is Resource.Error) {
                    _messageEvent.emit(resource.message ?: "Ilan bilgisi alinamadi")
                }
            }
        }
    }

    private fun listenForTransactions(productId: String) {
        transactionsJob?.cancel()
        transactionsJob = viewModelScope.launch {
            productRepository.getProductTransactions(productId).collect { resource ->
                if (resource is Resource.Success) {
                    transactions = resource.data ?: emptyList()
                    updateApprovalState()
                } else if (resource is Resource.Error) {
                    _messageEvent.emit(resource.message ?: "Talep bilgisi alinamadi")
                }
            }
        }
    }

    private fun listenForMessages() {
        val safeChatId = chatId ?: return
        viewModelScope.launch {
            chatRepository.getChatMessages(safeChatId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.value = UIState.Success(resource.data ?: emptyList())
                    }
                    is Resource.Error -> {
                        _uiState.value = UIState.Error(resource.message ?: "Mesajlar alınamadı")
                    }
                    else -> {}
                }
            }
        }
    }

    private fun updateApprovalState() {
        val currentRoom = room
        val currentProduct = product
        if (currentRoom == null || currentProduct == null || currentUserId != currentProduct.sellerId) {
            _approvalState.value = TransactionApprovalUiState()
            return
        }

        val buyerId = currentRoom.participants.firstOrNull { it != currentProduct.sellerId }.orEmpty()
        if (buyerId.isBlank()) {
            _approvalState.value = TransactionApprovalUiState()
            return
        }

        val transaction = transactions.firstOrNull { it.buyerId == buyerId }
        _approvalState.value = when (transaction?.status) {
            ProductTransactionStatus.REQUESTED -> TransactionApprovalUiState(
                isVisible = true,
                buyerId = buyerId,
                buyerName = transaction.buyerName.ifBlank { "Alici" },
                productTitle = currentProduct.title,
                isApproved = false,
                isApproving = _approvalState.value.isApproving
            )
            ProductTransactionStatus.COMPLETED -> TransactionApprovalUiState(
                isVisible = true,
                buyerId = buyerId,
                buyerName = transaction.buyerName.ifBlank { "Alici" },
                productTitle = currentProduct.title,
                isApproved = true
            )
            else -> TransactionApprovalUiState()
        }
    }

    fun approveTransactionRequest() {
        val safeChatId = chatId ?: return
        val currentProduct = product ?: return
        val buyerId = approvalState.value.buyerId
        if (buyerId.isBlank() || approvalState.value.isApproving || approvalState.value.isApproved) return

        viewModelScope.launch {
            _approvalState.value = approvalState.value.copy(isApproving = true)
            val result = productRepository.completeProductTransaction(currentProduct, buyerId)
            when (result) {
                is Resource.Success -> {
                    chatRepository.sendMessage(
                        chatId = safeChatId,
                        text = "\"${currentProduct.title}\" ilani icin kiralama talebini onayladim."
                    )
                    _messageEvent.emit("Kiralama talebi onaylandi")
                }
                is Resource.Error -> {
                    _messageEvent.emit(result.message ?: "Talep onaylanamadi")
                }
                is Resource.Loading -> Unit
            }
            _approvalState.value = approvalState.value.copy(isApproving = false)
            updateApprovalState()
        }
    }

    fun updateMessageText(text: String) {
        _messageText.value = text
    }

    fun sendMessage() {
        val safeChatId = chatId ?: return
        val text = _messageText.value.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            _messageText.value = "" // clear input quickly for UX
            val result = chatRepository.sendMessage(safeChatId, text)
            if (result is Resource.Error) {
                // optionally handle error, e.g., show to user
                _messageText.value = text // put back if failed
            }
        }
    }
}

data class TransactionApprovalUiState(
    val isVisible: Boolean = false,
    val buyerId: String = "",
    val buyerName: String = "",
    val productTitle: String = "",
    val isApproved: Boolean = false,
    val isApproving: Boolean = false
)
