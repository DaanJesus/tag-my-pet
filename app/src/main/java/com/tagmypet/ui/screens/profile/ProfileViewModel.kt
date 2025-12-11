package com.tagmypet.ui.screens.profile

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tagmypet.data.model.Pet
import com.tagmypet.data.model.User
import com.tagmypet.data.repository.PetRepository
import com.tagmypet.data.repository.Resource
import com.tagmypet.data.repository.UserRepository
import com.tagmypet.service.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import com.tagmypet.data.api.ApiService // Certificar-se que ApiService está disponível se fosse usar o retorno direto
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val petRepository: PetRepository,
    application: Application,
) : AndroidViewModel(application) {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _permissionRequestEvent = MutableSharedFlow<String>()
    val permissionRequestEvent: SharedFlow<String> = _permissionRequestEvent.asSharedFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val userResult = userRepository.getMe()) {
                is Resource.Success -> _user.value = userResult.data
                is Resource.Error -> _errorMessage.value = userResult.message
                else -> {}
            }
            refreshPets()
            _isLoading.value = false
        }
    }

    private suspend fun refreshPets() {
        when (val petsResult = petRepository.getMyPets()) {
            is Resource.Success -> {
                _pets.value = petsResult.data ?: emptyList()
                Log.e(
                    "PET_DEBUG",
                    "SUCESSO: ${(_pets.value.size)} pets carregados. Nome 1: ${_pets.value.firstOrNull()?.name}"
                )
            }

            is Resource.Error -> {
                _errorMessage.value = petsResult.message
                Log.e("PET_DEBUG", "ERRO AO CARREGAR PETS: ${petsResult.message}")
            }

            else -> {}
        }
    }

    fun requestPermissionOrToggle(petId: String) {
        val currentPet = _pets.value.find { it.id == petId } ?: return
        val context = getApplication<Application>().applicationContext

        if (currentPet.isLost) {
            // Se já está perdido, o clique é para MARCAR COMO ENCONTRADO (newStatus=false).
            // NUNCA precisamos de permissão para DESLIGAR o serviço.
            toggleStatusAndCallApi(petId, false)
            context.stopService(Intent(context, LocationService::class.java))
            return
        }

        // Se vai marcar como perdido (newStatus = true), checa permissão.
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permissão JÁ concedida, inicia diretamente.
            toggleStatusAndStartService(petId, true)
        } else {
            // Permissão NÃO concedida, sinaliza a UI para abrir o diálogo de permissão.
            viewModelScope.launch {
                _permissionRequestEvent.emit(petId)
            }
        }
    }

    fun onPermissionResult(petId: String, granted: Boolean) {
        if (granted) {
            // Permissão concedida, inicia o rastreamento.
            toggleStatusAndStartService(petId, true)
        } else {
            // Permissão negada, mostra erro e reverte o status otimista.
            _errorMessage.value = "Permissão de localização é essencial para rastreamento GPS."
            updateLocalPetStatus(petId, false)
        }
    }

    private fun toggleStatusAndStartService(petId: String, newStatus: Boolean) {
        val context = getApplication<Application>().applicationContext
        val serviceIntent = Intent(context, LocationService::class.java)

        // 1. Atualização Otimista
        updateLocalPetStatus(petId, newStatus)

        // 2. Inicia o Serviço
        serviceIntent.putExtra("petId", petId)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // 3. Chama API
        toggleStatusAndCallApi(petId, newStatus)
    }

    private fun toggleStatusAndCallApi(petId: String, newStatus: Boolean) {
        viewModelScope.launch {
            val result = petRepository.togglePetStatus(petId, newStatus)

            if (result is Resource.Success) {
                // CORREÇÃO CRÍTICA: Força o recarregamento da lista da API para garantir a consistência
                refreshPets()
            } else if (result is Resource.Error) {
                updateLocalPetStatus(petId, !newStatus) // Reverte
                _errorMessage.value = result.message
                // Reverte o serviço se a API falhar
                if (newStatus) getApplication<Application>().applicationContext.stopService(
                    Intent(
                        getApplication(),
                        LocationService::class.java
                    )
                )
            }
        }
    }

    fun removePet(petId: String) {
        viewModelScope.launch {
            val oldList = _pets.value
            _pets.value = oldList.filter { it.id != petId }
            val result = petRepository.deletePet(petId)
            if (result is Resource.Error) {
                _pets.value = oldList
                _errorMessage.value = result.message
            }
        }
    }

    private fun updateLocalPetStatus(petId: String, isLost: Boolean) {
        _pets.value = _pets.value.map { pet ->
            if (pet.id == petId) pet.copy(isLost = isLost) else pet
        }
    }
}