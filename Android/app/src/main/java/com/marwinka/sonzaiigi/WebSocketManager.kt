package com.marwinka.sonzaiigi

import android.util.Log
import okhttp3.*
import org.json.JSONObject

class WebSocketManager(private val userId: Int, private val onMessageReceived: (JSONObject) -> Unit) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    fun connect() {
        // ✨ УКАЖИ ТУТ АДРЕС СВОЕГО ВЕБСОКЕТА
        val wsUrl = "wss://sonzaiigi.com/ws?user_id=$userId"
        val request = Request.Builder().url(wsUrl).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WS", "🚀 Соединение установлено!")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WS", "📩 Получено: $text")
                try {
                    val json = JSONObject(text)
                    onMessageReceived(json) // Передаем на экран
                } catch (e: Exception) { e.printStackTrace() }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WS", "🔴 Соединение закрыто")
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WS", "❌ Ошибка: ${t.message}")
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "User left")
    }
}