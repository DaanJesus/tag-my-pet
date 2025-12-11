package com.tagmypet.data.repository

import android.content.Context
import android.net.Uri
import com.tagmypet.data.api.ApiService
import com.tagmypet.data.model.Pet
import com.tagmypet.data.model.Vaccine
import com.tagmypet.utils.FileUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetRepository @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context,
) {
    // Cache em memória simples para evitar re-fetch constante na edição
    private var cachedPets: List<Pet>? = null

    suspend fun getMyPets(): Resource<List<Pet>> {
        // CORREÇÃO: Forçar API se o cache estiver nulo ou se for necessário um refresh
        // Por padrão, chamamos a API, mas você pode usar o cache se a UI pedir explicitamente.
        // Já que o problema é a falta de refresh, vamos sempre à API por enquanto:
        return try {
            val response = apiService.getMyPets()
            if (response.isSuccessful && response.body() != null) {
                val pets = response.body()!!.data.pets
                cachedPets = pets // Atualiza o cache
                Resource.Success(pets)
            } else {
                Resource.Error("Erro ao buscar pets")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro desconhecido")
        }
    }

    // NOVO: Obter Pet por ID (para edição)
    suspend fun getPetById(petId: String): Resource<Pet> {
        // Tenta pegar do cache primeiro
        val cached = cachedPets?.find { it.id == petId }
        if (cached != null) return Resource.Success(cached)

        // Se não tiver, busca tudo de novo (pois a API não tem endpoint GET /pets/:id privado ainda)
        return when (val result = getMyPets()) {
            is Resource.Success -> {
                val pet = result.data?.find { it.id == petId }
                if (pet != null) Resource.Success(pet) else Resource.Error("Pet não encontrado")
            }

            is Resource.Error -> Resource.Error(result.message ?: "Erro")
            else -> Resource.Error("Erro desconhecido")
        }
    }

    suspend fun createPet(
        name: String, species: String, breed: String, age: String, weight: String, contact: String,
        allergies: List<String>, medications: List<String>, photoUri: Uri?,
    ): Resource<Pet> {
        return try {
            val namePart = createPartFromString(name)
            val speciesPart = createPartFromString(species)
            val breedPart = createPartFromString(breed)
            val agePart = createPartFromString(age)
            val weightPart = createPartFromString(weight)
            val contactPart = createPartFromString(contact)
            val allergiesParts =
                allergies.map { MultipartBody.Part.createFormData("allergies", it) }
            val medicationsParts =
                medications.map { MultipartBody.Part.createFormData("medications", it) }
            val imagePart = photoUri?.let { FileUtils.getFilePartFromUri(context, it) }

            val response = apiService.createPet(
                namePart, speciesPart, breedPart, agePart, weightPart, contactPart,
                allergiesParts, medicationsParts, imagePart
            )

            if (response.isSuccessful && response.body() != null) {
                // CORREÇÃO CRÍTICA: Limpar cache na criação para forçar o ProfileScreen a recarregar
                cachedPets = null
                Resource.Success(response.body()!!.data.pet)
            } else {
                Resource.Error("Falha ao criar pet")
            }
        } catch (e: Exception) {
            Resource.Error("Erro: ${e.message}")
        }
    }

    suspend fun togglePetStatus(petId: String, isLost: Boolean): Resource<Pet> {
        return try {
            val response = apiService.togglePetStatus(petId, mapOf("isLost" to isLost))
            if (response.isSuccessful && response.body() != null) Resource.Success(response.body()!!.data.pet)
            else Resource.Error("Erro ao atualizar status")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro")
        }
    }

    suspend fun deletePet(petId: String): Resource<Unit> {
        return try {
            val response = apiService.deletePet(petId)
            if (response.isSuccessful) {
                // Invalida cache na remoção
                cachedPets = null
                Resource.Success(Unit)
            } else Resource.Error("Erro ao remover pet")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro")
        }
    }

    suspend fun addVaccine(
        petId: String,
        name: String,
        dateApplied: String,
        nextDoseDate: String?,
        isApplied: Boolean,
    ): Resource<List<Vaccine>> {
        return try {
            val request = mapOf(
                "name" to name,
                "dateApplied" to dateApplied,
                "nextDoseDate" to (nextDoseDate ?: ""),
                "isApplied" to isApplied
            )
            val response = apiService.addVaccine(petId, request)
            if (response.isSuccessful && response.body() != null) Resource.Success(response.body()!!.data.vaccines)
            else Resource.Error("Erro ao adicionar vacina")
        } catch (e: Exception) {
            Resource.Error("Erro: ${e.message}")
        }
    }

    suspend fun removeVaccine(petId: String, vaccineId: String): Resource<List<Vaccine>> {
        return try {
            val response = apiService.removeVaccine(petId, vaccineId)
            if (response.isSuccessful && response.body() != null) Resource.Success(response.body()!!.data.vaccines)
            else Resource.Error("Erro ao remover vacina")
        } catch (e: Exception) {
            Resource.Error("Erro: ${e.message}")
        }
    }

    private fun createPartFromString(value: String): RequestBody {
        return value.toRequestBody("text/plain".toMediaTypeOrNull())
    }
}