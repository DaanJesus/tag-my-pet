// java/com/tagmypet/ui/screens/settings/SettingsViewModel.kt
package com.tagmypet.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagmypet.data.repository.AuthRepository
import com.tagmypet.data.repository.Resource
import com.tagmypet.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _logoutEvent = MutableStateFlow(false)
    val logoutEvent: StateFlow<Boolean> = _logoutEvent.asStateFlow()

    // --- Preferências (ADICIONADAS AS VARIÁVEIS QUE FALTAVAM) ---
    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _emailUpdatesEnabled = MutableStateFlow(true)
    val emailUpdatesEnabled: StateFlow<Boolean> = _emailUpdatesEnabled.asStateFlow()

    private val _darkModeEnabled = MutableStateFlow(false)
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()

    fun toggleNotification(value: Boolean) {
        _notificationsEnabled.value = value
    }

    fun toggleEmailUpdates(value: Boolean) {
        _emailUpdatesEnabled.value = value
    }

    fun toggleDarkMode(value: Boolean) {
        _darkModeEnabled.value = value
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _logoutEvent.value = true
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = userRepository.deleteAccount()
            if (result is Resource.Success) {
                authRepository.logout()
                _logoutEvent.value = true
            }
            _isLoading.value = false
        }
    }
}