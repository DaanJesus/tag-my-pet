package com.tagmypet.data.repository

import android.content.Context
import android.net.Uri
import com.tagmypet.data.api.ApiService
import com.tagmypet.ui.components.PostVO
import com.tagmypet.utils.FileUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context,
) {
    // 1. Feed Principal
    suspend fun getFeed(): Resource<List<PostVO>> {
        return try {
            val response = apiService.getFeed()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.data.posts)
            } else {
                Resource.Error("Erro ao carregar feed")
            }
        } catch (e: Exception) {
            Resource.Error("Erro de conexão: ${e.localizedMessage}")
        }
    }

    // 2. Posts de um Usuário (Perfil Público)
    suspend fun getUserPosts(userId: String): Resource<List<PostVO>> {
        return try {
            val response = apiService.getUserPosts(userId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.data.posts)
            } else {
                Resource.Error("Erro ao buscar posts")
            }
        } catch (e: Exception) {
            Resource.Error("Erro: ${e.message}")
        }
    }

    // 3. Criar Post
    suspend fun createPost(content: String, imageUri: Uri?, isLostAlert: Boolean): Resource<Unit> {
        return try {
            val contentPart = content.toRequestBody("text/plain".toMediaTypeOrNull())
            val imagePart = imageUri?.let { FileUtils.getFilePartFromUri(context, it) }

            val response = apiService.createPost(contentPart, imagePart, isLostAlert)

            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Erro ao publicar")
            }
        } catch (e: Exception) {
            Resource.Error("Erro: ${e.message}")
        }
    }

    // 4. Curtir / Descurtir
    suspend fun toggleLike(postId: String): Resource<Unit> {
        return try {
            val response = apiService.toggleLike(postId)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Erro ao curtir")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro")
        }
    }
}