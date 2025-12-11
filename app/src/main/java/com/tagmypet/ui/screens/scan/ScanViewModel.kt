package com.tagmypet.ui.screens.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagmypet.data.local.TokenManager
import com.tagmypet.data.model.Pet
import com.tagmypet.data.repository.Resource
import com.tagmypet.data.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _isScanning = MutableStateFlow(true)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _foundPet = MutableStateFlow<Pet?>(null)
    val foundPet: StateFlow<Pet?> = _foundPet.asStateFlow()

    private val _isOwner = MutableStateFlow(false)
    val isOwner: StateFlow<Boolean> = _isOwner.asStateFlow()

    // --- RESTAURADO: Mensagem de Feedback para a UI ---
    private val _scanMessage = MutableStateFlow<String?>(null)
    val scanMessage: StateFlow<String?> = _scanMessage.asStateFlow()

    private val _navigateToError = MutableStateFlow(false)
    val navigateToError: StateFlow<Boolean> = _navigateToError.asStateFlow()

    fun onBarcodeDetected(code: String) {
        viewModelScope.launch {
            if (!_isScanning.value) return@launch

            // Pausa scanner para não processar códigos duplicados rapidamente
            _isScanning.value = false

            when (val result = scanRepository.getPetByTag(code)) {
                is Resource.Success -> {
                    val pet = result.data
                    if (pet != null) {
                        _foundPet.value = pet

                        val myId = tokenManager.getUserId().first()
                        val isUserOwner = pet.ownerId == myId
                        _isOwner.value = isUserOwner

                        // --- LÓGICA DE MENSAGENS (Restaurada) ---
                        if (isUserOwner && pet.isLost) {
                            _scanMessage.value =
                                "Pet identificado! Status atualizado para 'Em Casa'."
                            // TODO: Opcional - Chamar API para atualizar status automaticamente aqui
                        } else if (pet.isLost) {
                            _scanMessage.value = "Atenção: Este pet consta como desaparecido!"
                        } else if (isUserOwner) {
                            _scanMessage.value = "Olá! Este é o perfil do seu pet."
                        }
                    }
                }

                is Resource.Error -> {
                    _navigateToError.value = true
                }

                else -> {}
            }
        }
    }

    fun clearMessage() {
        _scanMessage.value = null
    }

    fun resetScan() {
        _isScanning.value = true
        _foundPet.value = null
        _navigateToError.value = false
        _scanMessage.value = null
    }

    fun onNavigationHandled() {
        _navigateToError.value = false
    }
}