package com.tagmypet.data.repository

import com.tagmypet.data.api.ApiService
import com.tagmypet.data.model.Pet
import com.tagmypet.data.model.User
import javax.inject.Inject
import javax.inject.Singleton

data class SearchResults(val users: List<User>, val pets: List<Pet>)

@Singleton
class SearchRepository @Inject constructor(
    private val apiService: ApiService,
) {
    suspend fun search(query: String): Resource<SearchResults> {
        return try {
            val response = apiService.searchGlobal(query)

            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.data
                // A API retorna objetos separados para users e pets
                Resource.Success(SearchResults(data.users, data.pets))
            } else {
                Resource.Error("Erro ao realizar a busca.")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro de conexão")
        }
    }
}