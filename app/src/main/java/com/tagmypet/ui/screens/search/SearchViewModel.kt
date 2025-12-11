package com.tagmypet.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagmypet.data.model.Pet
import com.tagmypet.data.model.User
import com.tagmypet.data.repository.Resource
import com.tagmypet.data.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val pets: List<Pet> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }

        // Debounce: Cancela a busca anterior se o usuário continuar a digitar
        searchJob?.cancel()

        if (newQuery.isBlank()) {
            _uiState.update { it.copy(users = emptyList(), pets = emptyList(), isLoading = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // Espera 500ms após a última tecla
            performSearch(newQuery)
        }
    }

    private suspend fun performSearch(query: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        when (val result = searchRepository.search(query)) {
            is Resource.Success -> {
                val data = result.data
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        users = data?.users ?: emptyList(),
                        pets = data?.pets ?: emptyList()
                    )
                }
            }

            is Resource.Error -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }

            else -> {}
        }
    }
}