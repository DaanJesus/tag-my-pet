package com.tagmypet.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagmypet.data.repository.Resource
import com.tagmypet.data.repository.UserRepository
import com.tagmypet.data.api.ApiService // Para upload direto se Repository não tiver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val bio: String = "", // Nota: API User model não tem bio ainda, apenas name e photoUrl
    val email: String = "",
    val phone: String = "",
    val currentPhotoUrl: String = "",
    val photoUri: Uri? = null,
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val apiService: ApiService, // Injetando ApiService para upload rápido ou criar UploadRepository
    private val application: android.app.Application,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = userRepository.getMe()) {
                is Resource.Success -> {
                    val user = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = user?.name ?: "",
                            email = user?.email ?: "",
                            currentPhotoUrl = user?.photoUrl ?: ""
                        )
                    }
                }

                else -> _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onNameChange(text: String) {
        _uiState.update { it.copy(name = text) }
    }

    fun onBioChange(text: String) {
        _uiState.update { it.copy(bio = text) }
    }

    fun onPhotoSelected(uri: Uri?) {
        _uiState.update { it.copy(photoUri = uri) }
    }

    fun saveProfile(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            var photoUrlToSend: String? = null

            // 1. Upload da foto se houver nova
            if (_uiState.value.photoUri != null) {
                // Lógica de upload (simplificada aqui, idealmente em Repository)
                val uri = _uiState.value.photoUri!!
                val filePart = getFilePart(uri)
                if (filePart != null) {
                    try {
                        val response = apiService.uploadImage(filePart)
                        if (response.isSuccessful) {
                            photoUrlToSend = response.body()?.data?.url
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // 2. Atualizar Perfil
            val result = userRepository.updateProfile(
                name = _uiState.value.name,
                photoUrl = photoUrlToSend
            )

            _uiState.update { it.copy(isLoading = false) }

            if (result is Resource.Success) {
                onSuccess()
            }
        }
    }

    // Helper de arquivo (duplicado do FileUtils para acesso ao Context do Application)
    private fun getFilePart(uri: Uri): MultipartBody.Part? {
        try {
            val context = application.applicationContext
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("profile_", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream.use { it.copyTo(outputStream) }
            val reqFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            return MultipartBody.Part.createFormData("image", tempFile.name, reqFile)
        } catch (e: Exception) {
            return null
        }
    }
}