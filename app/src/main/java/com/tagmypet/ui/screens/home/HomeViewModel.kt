package com.tagmypet.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagmypet.data.repository.PostRepository
import com.tagmypet.data.repository.Resource
import com.tagmypet.ui.components.PostVO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FeedItem {
    data class Post(val data: PostVO) : FeedItem()
    data class Ad(
        val title: String,
        val description: String,
        val imageUrl: String,
        val actionUrl: String,
    ) : FeedItem()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository,
) : ViewModel() {

    private val _feedItems = MutableStateFlow<List<FeedItem>>(emptyList())
    val feedItems: StateFlow<List<FeedItem>> = _feedItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = postRepository.getFeed()) {
                is Resource.Success -> {
                    val posts = result.data ?: emptyList()
                    val mixedList = mutableListOf<FeedItem>()

                    posts.forEachIndexed { index, post ->
                        mixedList.add(FeedItem.Post(post))
                        // A cada 5 posts, um anúncio (exemplo)
                        if ((index + 1) % 5 == 0) mixedList.add(createMockAd())
                    }
                    _feedItems.value = mixedList
                }

                is Resource.Error -> { /* Tratar erro (ex: toast) */
                }

                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            // Update Otimista na UI
            updateLocalLike(postId)
            // Chama API
            postRepository.toggleLike(postId)
        }
    }

    private fun updateLocalLike(postId: String) {
        val currentList = _feedItems.value.map { item ->
            if (item is FeedItem.Post && item.data.id == postId) {
                val p = item.data
                val newLikeState = !p.isLiked
                val newCount = if (newLikeState) p.likesCount + 1 else p.likesCount - 1
                FeedItem.Post(p.copy(isLiked = newLikeState, likesCount = newCount))
            } else {
                item
            }
        }
        _feedItems.value = currentList
    }

    private fun createMockAd() = FeedItem.Ad(
        title = "Ração Premium",
        description = "A melhor nutrição com 20% OFF.",
        imageUrl = "https://images.unsplash.com/photo-1589924691195-41432c84c161?w=500",
        actionUrl = "https://loja.exemplo.com"
    )
}