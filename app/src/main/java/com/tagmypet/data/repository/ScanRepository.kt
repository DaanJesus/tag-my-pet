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
        return try {
            val response = apiService.getPetByTag(tagId)

            if (response.isSuccessful && response.body() != null) {
                val petData = response.body()!!.data.pet
                Resource.Success(petData)
            } else {
                // Se der 404 ou outro erro
                Resource.Error("Pet não encontrado ou código inválido.")
            }
        } catch (e: Exception) {
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }
}