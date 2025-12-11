package com.tagmypet.ui.screens.profile.pet_form

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagmypet.data.repository.PetRepository
import com.tagmypet.data.repository.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PetFormUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val species: String = "Dog",
    val breed: String = "",
    val age: String = "",
    val weight: String = "",
    val contactPhone: String = "",
    val allergies: List<String> = emptyList(),
    val medications: List<String> = emptyList(),
    val photoUri: Uri? = null,
    val currentPhotoUrl: String = "", // String não-nula
)

@HiltViewModel
class PetFormViewModel @Inject constructor(
    private val petRepository: PetRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetFormUiState())
    val uiState: StateFlow<PetFormUiState> = _uiState.asStateFlow()

    fun loadPet(petId: String) {
        if (petId == "new") return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Busca dados REAIS do pet para edição
            when (val result = petRepository.getPetById(petId)) {
                is Resource.Success -> {
                    val pet = result.data!!
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = pet.name,
                            species = pet.species,
                            breed = pet.breed,
                            age = pet.age.toString(),
                            weight = pet.weight.toString(),
                            contactPhone = pet.contactPhone,
                            // CORREÇÃO: Usa o Elvis Operator (?: "") para converter String? em String.
                            currentPhotoUrl = pet.photoUrl ?: "",
                            allergies = pet.allergies,
                            medications = pet.medications
                        )
                    }
                }

                else -> _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // --- Inputs Básicos ---
    fun onNameChange(v: String) {
        _uiState.update { it.copy(name = v) }
    }

    fun onSpeciesChange(v: String) {
        _uiState.update { it.copy(species = v) }
    }

    fun onBreedChange(v: String) {
        _uiState.update { it.copy(breed = v) }
    }

    fun onAgeChange(v: String) {
        _uiState.update { it.copy(age = v) }
    }

    fun onWeightChange(v: String) {
        _uiState.update { it.copy(weight = v) }
    }

    fun onPhotoSelected(uri: Uri?) {
        _uiState.update { it.copy(photoUri = uri) }
    }

    // --- Listas (Alergias e Medicamentos) ---
    fun addAllergy(item: String) {
        _uiState.update { it.copy(allergies = it.allergies + item) }
    }

    fun removeAllergy(item: String) {
        _uiState.update { it.copy(allergies = it.allergies - item) }
    }

    fun addMedication(item: String) {
        _uiState.update { it.copy(medications = it.medications + item) }
    }

    fun removeMedication(item: String) {
        _uiState.update { it.copy(medications = it.medications - item) }
    }

    // --- Salvar ---
    fun savePet(onSuccess: () -> Unit) {
        val s = _uiState.value
        if (s.name.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Lógica para determinar se é criação ou atualização
            val isNew = true // Se for petId = "new" (ajuste a lógica do form se precisar de update)

            val result = petRepository.createPet( // Usando createPet para novo registro
                name = s.name,
                species = s.species,
                breed = s.breed,
                age = s.age,
                weight = s.weight,
                contact = "11999999999", // Placeholder
                allergies = s.allergies,
                medications = s.medications,
                photoUri = s.photoUri
            )

            _uiState.update { it.copy(isLoading = false) }

            if (result is Resource.Success) {
                // CORREÇÃO CRÍTICA: Limpar o estado do formulário para forçar a saída
                _uiState.value = PetFormUiState()
                onSuccess()
            }
        }
    }
}