package com.tagmypet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.firebase.messaging.FirebaseMessaging
import com.tagmypet.data.repository.AuthRepository
import com.tagmypet.ui.MainScreen
import com.tagmypet.ui.MainViewModel
import com.tagmypet.ui.navigation.Screen
import com.tagmypet.ui.theme.Primary600
import com.tagmypet.ui.theme.TagMyPetTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TagMyPetTheme {
                val isLoading by mainViewModel.isLoading.collectAsState()
                val startDestination by mainViewModel.startDestination.collectAsState()

                // ⚡ F C M   T O K E N   L O G I C ⚡
                LaunchedEffect(startDestination) {
                    // Só tenta enviar o token se o usuário estiver logado
                    if (startDestination == Screen.Home.route) {
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                return@addOnCompleteListener
                            }
                            val token = task.result
                            if (token != null) {
                                launch {
                                    authRepository.updateFcmToken(token)
                                }
                            }
                        }
                    }
                }
                // ⚡ F I M   F C M   L O G I C ⚡

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary600)
                    }
                } else {
                    MainScreen(startDestination = startDestination)
                }
            }
        }
    }
}