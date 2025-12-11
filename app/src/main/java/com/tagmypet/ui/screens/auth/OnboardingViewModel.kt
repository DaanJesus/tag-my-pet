package com.tagmypet.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagmypet.data.local.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val tokenManager: TokenManager,
) : ViewModel() {

    fun completeOnboarding() {
        viewModelScope.launch {
            tokenManager.saveOnboardingCompleted()
        }
    }
}