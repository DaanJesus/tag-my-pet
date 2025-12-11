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
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun register(
        name: String,
        email: String,
        pass: String,
        confirmPass: String,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            _errorMessage.value = null

            // Validações Locais
            if (name.isBlank() || email.isBlank() || pass.isBlank()) {
                _errorMessage.value = "Preencha todos os campos."
                return@launch
            }

            if (pass != confirmPass) {
                _errorMessage.value = "As senhas não coincidem."
                return@launch
            }

            if (pass.length < 6) {
                _errorMessage.value = "A senha deve ter pelo menos 6 caracteres."
                return@launch
            }

            _isLoading.value = true

            // Chamada Real à API
            val result = authRepository.register(name, email, pass)

            _isLoading.value = false

            when (result) {
                is Resource.Success -> {
                    // Registro com sucesso já salva o token e loga o usuário
                    onSuccess()
                }

                is Resource.Error -> {
                    _errorMessage.value = result.message ?: "Erro ao criar conta."
                }

                else -> {}
            }
        }
    }
}