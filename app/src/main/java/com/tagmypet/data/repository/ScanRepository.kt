package com.tagmypet.data.repository

import com.tagmypet.data.api.ApiService
import com.tagmypet.data.model.Pet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanRepository @Inject constructor(
    private val apiService: ApiService,
) {
    suspend fun getPetByTag(tagId: String): Resource<Pet> {
        // Garante que a entrada (que é o ID) não tem espaços em branco indesejados.
        val petId = tagId.trim()

        if (petId.isBlank()) {
            return Resource.Error("ID do Pet não fornecido.")
        }

        return try {
            // O ApiService já está configurado para usar este ID no path: /pets/tag/{id}
            val response = apiService.getPetByTag(petId)

            if (response.isSuccessful && response.body() != null) {
                val petData = response.body()!!.data.pet
                Resource.Success(petData)
            } else {
                // Se der 404 (Não Encontrado) ou outro erro da API
                Resource.Error("Pet não encontrado ou código inválido.")
            }
        } catch (e: Exception) {
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }
}