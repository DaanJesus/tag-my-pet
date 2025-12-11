package com.tagmypet

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TagMyPetApp : Application() {
    // Aqui é onde inicializamos coisas globais no futuro,
    // como Analytics, Logs de erro, etc.
    // Por enquanto, só a anotação @HiltAndroidApp já faz a mágica.
}