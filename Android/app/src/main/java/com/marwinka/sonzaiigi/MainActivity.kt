package com.marwinka.sonzaiigi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.core.view.WindowCompat
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.marwinka.sonzaiigi.ui.theme.SonzaiigiTheme

// ✨ Специальный класс для хранения данных просмотрщика
data class ViewerState(val messages: List<Message>, val initialId: Int, val initialBounds: Rect?)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AuthManager.init(applicationContext)
        val imageLoader = ImageLoader.Builder(applicationContext)
            .memoryCache {
                MemoryCache.Builder(applicationContext)
                    .maxSizePercent(0.25) // Отдаем под картинки четверть оперативной памяти
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(applicationContext.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(512L * 1024L * 1024L) // Выделяем 512 МБ на диске под фото
                    .build()
            }
            .respectCacheHeaders(false) // Чтобы картинки кешировались, даже если сервер "молчит" об этом
            .build()

        coil.Coil.setImageLoader(imageLoader)
        setContent {
            SonzaiigiTheme {
                var currentScreen by remember {
                    mutableStateOf(if (AuthManager.token != null) "chats" else "login")
                }
                var selectedChat by remember { mutableStateOf<Chat?>(null) }

                // ✨ Глобальное состояние для картинки (если не null, рисуем поверх всего)
                var imageViewerState by remember { mutableStateOf<ViewerState?>(null) }
                WindowCompat.setDecorFitsSystemWindows(window, false)
                // Оборачиваем всё в Box, чтобы можно было наслаивать экраны
                Box(modifier = Modifier.fillMaxSize()) {

                    // Твое основное приложение
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        when (currentScreen) {
                            "login" -> {
                                LoginScreen(onNavigateToChats = { currentScreen = "chats" })
                            }
                            "chats" -> {
                                ChatListScreen(
                                    onChatClick = { clickedChat ->
                                        selectedChat = clickedChat
                                        currentScreen = "messages"
                                    }
                                )
                            }
                            "messages" -> {
                                if (selectedChat != null) {
                                    ChatMessagesScreen(
                                        chat = selectedChat!!,
                                        onBack = { currentScreen = "chats" },
                                        // ✨ Передаем команду "открыть картинку" на самый верх
                                        onImageClick = { msgs, initialId, bounds ->
                                            imageViewerState = ViewerState(msgs, initialId, bounds)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // ✨ ПРОСМОТРЩИК КАРТИНОК! Он рисуется в самом низу кода,
                    // а значит в приложении он будет поверх всего остального!
                    if (imageViewerState != null) {
                        ImageViewerScreen(
                            messages = imageViewerState!!.messages,
                            initialMessageId = imageViewerState!!.initialId,
                            initialBounds = imageViewerState!!.initialBounds, // Передаем координаты!
                            onClose = { imageViewerState = null }
                        )
                    }
                }
            }
        }
    }
}