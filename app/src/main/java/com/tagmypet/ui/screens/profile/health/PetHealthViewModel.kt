package com.tagmypet.ui.screens.profile.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagmypet.data.model.Vaccine
import com.tagmypet.data.repository.PetRepository
import com.tagmypet.data.repository.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Mapeia o modelo da API para o da UI se necessário, ou usa direto
typealias VaccineModel = Vaccine

@HiltViewModel
class PetHealthViewModel @Inject constructor(
    private val petRepository: PetRepository,
) : ViewModel() {

    private val _vaccines = MutableStateFlow<List<VaccineModel>>(emptyList())
    val vaccines: StateFlow<List<VaccineModel>> = _vaccines.asStateFlow()

    private val _petName = MutableStateFlow("")
    val petName: StateFlow<String> = _petName.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentPetId: String = ""

    fun loadData(petId: String) {
        currentPetId = petId
        viewModelScope.launch {
            _isLoading.value = true
            // Reutiliza getMyPets para achar o pet (cache simples)
            // Idealmente teria getPetDetails(id)
            val result = petRepository.getMyPets()
            if (result is Resource.Success) {
                val pet = result.data?.find { it.id == petId }
                if (pet != null) {
                    _petName.value = pet.name
                    _vaccines.value = pet.vaccines
                }
            }
            _isLoading.value = false
        }
    }

    fun addVaccine(name: String, date: String, nextDose: String?) {
        viewModelScope.launch {
            val result = petRepository.addVaccine(currentPetId, name, date, nextDose, true)
            if (result is Resource.Success) {
                _vaccines.value = result.data ?: emptyList()
            }
        }
    }

    fun removeVaccine(vaccineId: String) {
        viewModelScope.launch {
            val result = petRepository.removeVaccine(currentPetId, vaccineId)
            if (result is Resource.Success) {
                _vaccines.value = result.data ?: emptyList()
            }
        }
    }
}