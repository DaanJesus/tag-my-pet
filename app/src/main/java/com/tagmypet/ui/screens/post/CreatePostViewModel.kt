package com.tagmypet.ui.screens.post

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagmypet.data.repository.PostRepository
import com.tagmypet.data.repository.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val postRepository: PostRepository,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // O avatar e nome agora devem vir do UserSession/Repository,
    // mas por enquanto podemos deixar fixo ou carregar do UserRepository
    private val _userAvatar = MutableStateFlow("")
    val userAvatar: StateFlow<String> = _userAvatar.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    fun createPost(content: String, imageUri: Uri?, onSuccess: () -> Unit) {
        if (content.isBlank() && imageUri == null) return

        viewModelScope.launch {
            _isLoading.value = true

            // Envia para API (IsLostAlert false por padrão aqui, pode adicionar checkbox na UI depois)
            val result = postRepository.createPost(content, imageUri, isLostAlert = false)

            _isLoading.value = false

            if (result is Resource.Success) {
                onSuccess()
            }
        }
    }
}