package com.tagmypet.ui.screens.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagmypet.data.model.Notification
import com.tagmypet.data.repository.NotificationRepository
import com.tagmypet.data.repository.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationUiState(
    val isLoading: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = notificationRepository.getNotifications()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notifications = result.data ?: emptyList()
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }

                else -> {}
            }
        }
    }

    fun markAsRead(notificationId: String) {
        // Atualização Otimista
        val currentList = _uiState.value.notifications.map {
            if (it.id == notificationId) it.copy(isRead = true) else it
        }
        _uiState.update { it.copy(notifications = currentList) }

        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        val currentList = _uiState.value.notifications.map { it.copy(isRead = true) }
        _uiState.update { it.copy(notifications = currentList) }

        viewModelScope.launch {
            notificationRepository.markAllAsRead()
        }
    }
}