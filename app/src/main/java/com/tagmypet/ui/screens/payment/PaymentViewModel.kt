package com.tagmypet.ui.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagmypet.data.remote.SocketManager
import com.tagmypet.data.repository.PaymentRepository
import com.tagmypet.data.repository.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val socketManager: SocketManager,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _paymentClientSecret = MutableStateFlow<String?>(null)
    val paymentClientSecret: StateFlow<String?> = _paymentClientSecret.asStateFlow()

    private val _paymentSuccess = MutableStateFlow(false)
    val paymentSuccess: StateFlow<Boolean> = _paymentSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        socketManager.connect()
        listenToPaymentUpdates()
    }

    private fun listenToPaymentUpdates() {
        viewModelScope.launch {
            socketManager.paymentFlow.collect { data ->
                val status = data.optString("status")
                if (status == "success") {
                    _isLoading.value = false
                    _paymentSuccess.value = true
                } else if (status == "failed") {
                    _isLoading.value = false
                    _errorMessage.value = "Pagamento reprovado pelo banco."
                }
            }
        }
    }

    fun prepareCheckout(planType: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _paymentClientSecret.value = null

            val result = paymentRepository.createPaymentIntent(planType)

            when (result) {
                is Resource.Success -> {
                    _paymentClientSecret.value = result.data?.clientSecret
                    _isLoading.value = false
                }

                is Resource.Error -> {
                    _isLoading.value = false
                    _errorMessage.value = result.message
                }

                else -> {}
            }
        }
    }

    fun onPaymentSheetResult(success: Boolean, error: String?) {
        if (success) {
            _isLoading.value = true // Aguarda webhook
        } else if (error != null) {
            _errorMessage.value = "Erro: $error"
        }
    }
}