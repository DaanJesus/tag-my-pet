package com.tagmypet.ui.screens.profile.public_profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagmypet.data.api.UserStats
import com.tagmypet.data.model.Pet
import com.tagmypet.data.model.User
import com.tagmypet.data.repository.PostRepository
import com.tagmypet.data.repository.Resource
import com.tagmypet.data.repository.UserRepository
import com.tagmypet.ui.components.PostVO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PublicProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
) : ViewModel() {

    private val userId: String = savedStateHandle["userId"] ?: ""

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    private val _stats = MutableStateFlow(UserStats(0, 0, 0))
    val stats: StateFlow<UserStats> = _stats.asStateFlow()

    private val _posts = MutableStateFlow<List<PostVO>>(emptyList())
    val posts: StateFlow<List<PostVO>> = _posts.asStateFlow()

    // Nota: A API atual não retorna lista de pets no perfil público ainda (endpoint getPublicProfile retorna user + stats).
    // Se quiser exibir pets aqui, precisaria de um endpoint GET /users/{id}/pets no backend.
    // Por enquanto deixaremos a lista vazia ou podemos remover a aba de pets da UI.
    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets.asStateFlow()

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        if (userId.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true

            // 1. Dados do Perfil e Stats
            when (val result = userRepository.getPublicProfile(userId)) {
                is Resource.Success -> {
                    val data = result.data
                    _userProfile.value = data?.user
                    _stats.value = data?.stats ?: UserStats(0, 0, 0)
                    _isFollowing.value = data?.isFollowing ?: false
                }

                is Resource.Error -> { /* Tratar erro */
                }

                else -> {}
            }

            // 2. Posts do Usuário
            when (val postsResult = postRepository.getUserPosts(userId)) {
                is Resource.Success -> {
                    _posts.value = postsResult.data ?: emptyList()
                }

                else -> {}
            }

            _isLoading.value = false
        }
    }

    fun toggleFollow() {
        viewModelScope.launch {
            // Update Otimista
            val oldState = _isFollowing.value
            _isFollowing.value = !oldState

            // Chama API
            val result = userRepository.toggleFollow(userId)

            if (result is Resource.Error) {
                // Reverte em caso de erro
                _isFollowing.value = oldState
            } else if (result is Resource.Success) {
                // Atualiza com a verdade do servidor (opcional)
                _isFollowing.value = result.data ?: !oldState
            }
        }
    }
}