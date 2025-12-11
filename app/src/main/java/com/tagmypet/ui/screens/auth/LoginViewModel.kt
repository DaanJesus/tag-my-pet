package com.tagmypet.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagmypet.data.repository.AuthRepository
import com.tagmypet.data.repository.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Preencha todos os campos."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // Chamada Real à API
            val result = authRepository.login(email, pass)

            _isLoading.value = false

            when (result) {
                is Resource.Success -> {
                    // O token já foi salvo automaticamente pelo Repository
                    onSuccess()
                }

                is Resource.Error -> {
                    _errorMessage.value = result.message ?: "Erro ao fazer login."
                }

                else -> {}
            }
        }
    }
}