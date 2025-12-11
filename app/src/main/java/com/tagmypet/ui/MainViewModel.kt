package com.tagmypet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagmypet.data.local.TokenManager
import com.tagmypet.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first // <--- IMPORTANTE
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _startDestination = MutableStateFlow(Screen.Onboarding.route)
    val startDestination: StateFlow<String> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            // CORREÇÃO: Usamos .first() para pegar apenas o estado INICIAL do app.
            // Isso impede que salvar o onboarding durante o uso force um reinício da navegação.
            val destination = combine(
                tokenManager.getToken(),
                tokenManager.getOnboardingCompleted()
            ) { token, onboardingCompleted ->
                if (!token.isNullOrBlank()) {
                    Screen.Home.route
                } else if (onboardingCompleted) {
                    Screen.Login.route
                } else {
                    Screen.Onboarding.route
                }
            }.first() // <--- AQUI É O PULO DO GATO

            _startDestination.value = destination
            _isLoading.value = false
        }
    }
}