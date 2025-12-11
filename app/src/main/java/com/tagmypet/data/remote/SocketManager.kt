// java/com/tagmypet/data/remote/SocketManager.kt
package com.tagmypet.data.remote

import android.util.Log
import com.tagmypet.data.local.TokenManager
import com.tagmypet.utils.Constants
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketManager @Inject constructor(
    private val tokenManager: TokenManager,
) {
    private var socket: Socket? = null

    // Flows para emitir eventos para a UI
    private val _messageFlow = MutableSharedFlow<JSONObject>()
    val messageFlow: SharedFlow<JSONObject> = _messageFlow

    private val _notificationFlow = MutableSharedFlow<JSONObject>()
    val notificationFlow: SharedFlow<JSONObject> = _notificationFlow

    private val _paymentFlow = MutableSharedFlow<JSONObject>()
    val paymentFlow: SharedFlow<JSONObject> = _paymentFlow

    fun connect() {
        // Se já estiver conectado, não faz nada
        if (socket?.connected() == true) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Pega o Token salvo (Bloqueante aqui pois estamos na Coroutine IO)
                val token = tokenManager.getToken().first()

                if (token.isNullOrBlank()) {
                    Log.e("SocketManager", "Tentativa de conexão sem token. Abortando.")
                    return@launch
                }

                // 2. Configura as opções com Autenticação
                val options = IO.Options().apply {
                    reconnection = true
                    forceNew = true
                    // ENVIA O TOKEN PARA O SERVIDOR VALIDAR
                    auth = mapOf("token" to token)
                }

                // Conecta na URL base (ex: http://10.0.2.2:3000)
                val socketUrl = Constants.BASE_URL.replace("/api/v1/", "")
                socket = IO.socket(socketUrl, options)

                setupListeners()

                socket?.connect()

            } catch (e: Exception) {
                Log.e("SocketManager", "Erro de conexão", e)
            }
        }
    }

    private fun setupListeners() {
        socket?.on(Socket.EVENT_CONNECT) {
            Log.d("SocketManager", "Conectado e Autenticado!")
        }

        socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
            Log.e("SocketManager", "Erro de conexão: ${args.firstOrNull()}")
        }

        socket?.on("new_message") { args ->
            if (args.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    _messageFlow.emit(args[0] as JSONObject)
                }
            }
        }

        socket?.on("new_notification") { args ->
            if (args.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    _notificationFlow.emit(args[0] as JSONObject)
                }
            }
        }

        socket?.on("payment_update") { args ->
            if (args.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    _paymentFlow.emit(args[0] as JSONObject)
                }
            }
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
    }
}