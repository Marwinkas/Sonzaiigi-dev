package com.marwinka.sonzaiigi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
object ActiveChatManager {
    var currentChatId: Int? = null
}
class SonzaiigiMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d("FCM_LOG", "Новый токен: $token")
        // Не забудьте отправить токен на сервер при его обновлении!
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM_LOG", "Пришло сообщение! Data: ${remoteMessage.data}")

        val chatIdStr = remoteMessage.data["chat_id"]
        val messageId = remoteMessage.data["message_id"]

        // ✨ ИСПРАВЛЕНИЕ 1: Проверяем, не сидит ли пользователь прямо сейчас в этом чате
        val incomingChatId = chatIdStr?.toIntOrNull()
        if (incomingChatId != null && incomingChatId == ActiveChatManager.currentChatId) {
            Log.d("FCM_LOG", "Пользователь сейчас в этом чате, пуш НЕ показываем")
            return // Просто прерываем функцию, уведомление не появится!
        }

        val title = remoteMessage.data["title"] ?: "Sonzaiigi"
        val body = remoteMessage.data["body"] ?: "Новое сообщение"

        showNotification(title, body, chatIdStr, messageId)
    }
    private fun showNotification(title: String, body: String, chatId: String?, messageId: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Используем канал v2
        val channelId = "chat_messages_v2"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Сообщения", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Уведомления о новых сообщениях в чатах"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("chat_id", chatId)
            putExtra("message_id", messageId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            // Желательно заменить на иконку вашего приложения, например R.drawable.ic_notification
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            // Для уникальности уведомлений используем messageId, если он есть, иначе время
            val notificationId = messageId?.hashCode() ?: System.currentTimeMillis().toInt()
            notificationManager.notify(notificationId, notification)
            Log.d("FCM_LOG", "Уведомление успешно показано: $title - $body")
        } catch (e: Exception) {
            Log.e("FCM_LOG", "Ошибка показа уведомления: ${e.message}")
        }
    }
}