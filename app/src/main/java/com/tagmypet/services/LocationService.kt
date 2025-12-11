// java/com/tagmypet/services/LocationService.kt
package com.tagmypet.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.tagmypet.R
import com.tagmypet.data.local.TokenManager
import com.tagmypet.utils.Constants // Importante para pegar URL correta
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {

    @Inject
    lateinit var tokenManager: TokenManager

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var socket: Socket? = null
    private var petIdToTrack: String? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // A conexão agora é feita dentro do onStartCommand ou numa coroutine iniciada no onCreate
        // para garantir que temos tempo de pegar o token
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        petIdToTrack = intent?.getStringExtra("petId")

        // Inicia Socket com Autenticação
        setupSocketAuthenticated()

        // Cria a notificação persistente
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "gps_channel")
            .setContentTitle("Rastreamento Ativo")
            .setContentText("Monitorando localização do seu pet...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        startForeground(1, notification)

        // Configura updates de GPS (Intervalo ajustado para bateria: 10s)
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    sendLocationToSocket(location.latitude, location.longitude)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        return START_STICKY
    }

    private fun setupSocketAuthenticated() {
        CoroutineScope(Dispatchers.IO).launch {
            val token = tokenManager.getToken().first()
            if (token.isNullOrBlank()) return@launch

            try {
                val opts = IO.Options().apply {
                    forceNew = true
                    // Auth Token obrigatório agora
                    auth = mapOf("token" to token)
                }

                // URL dinâmica baseada no Constants
                val socketUrl = Constants.BASE_URL.replace("/api/v1/", "")
                socket = IO.socket(socketUrl, opts)
                socket?.connect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sendLocationToSocket(lat: Double, lng: Double) {
        // Envia apenas se o socket estiver conectado
        if (socket?.connected() == true && petIdToTrack != null) {
            val data = JSONObject()
            data.put("petId", petIdToTrack)
            data.put("lat", lat)
            data.put("lng", lng)
            // ownerId não é mais necessário no payload, o server pega do token!

            socket?.emit("update_pet_location", data)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        socket?.disconnect()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "gps_channel",
                "Rastreamento Pet",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}