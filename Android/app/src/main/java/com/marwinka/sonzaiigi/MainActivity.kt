package com.marwinka.sonzaiigi

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.marwinka.sonzaiigi.ui.theme.SonzaiigiTheme
import kotlinx.coroutines.flow.MutableStateFlow

// ✨ Специальный класс для хранения данных просмотрщика
data class ViewerState(val messages: List<Message>, val initialId: Int, val initialBounds: Rect?)

class MainActivity : ComponentActivity() {
    private val incomingJoinToken = MutableStateFlow<String?>(null)

    // ✨ ДОБАВИЛИ: Стейт для хранения данных из пуш-уведомления (chat_id, message_id)
    private val incomingPush = MutableStateFlow<Pair<Int, Int?>?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AuthManager.init(applicationContext)
        EmojiManager.init(applicationContext)
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
        handleIntents(intent)
        setContent {
            SonzaiigiTheme {
                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (!isGranted) {
                        Toast.makeText(context, "Без разрешения вы не будете получать пуши :(", Toast.LENGTH_SHORT).show()
                    }
                }
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED

                        if (!hasPermission) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }
                var currentScreen by remember {
                    mutableStateOf(if (AuthManager.token != null) "chats" else "login")
                }
                var selectedChat by remember { mutableStateOf<Chat?>(null) }

                // ✨ Состояние просмотрщика
                var imageViewerState by remember { mutableStateOf<ViewerState?>(null) }

                // ✨ СИГНАЛЫ ДЛЯ ЧАТА (чтобы команды из просмотрщика дошли до экрана сообщений)
                var signalReplyMessage by remember { mutableStateOf<Message?>(null) }
                var signalForwardMessage by remember { mutableStateOf<Message?>(null) }
                var signalScrollToId by remember { mutableStateOf<Int?>(null) }

                WindowCompat.setDecorFitsSystemWindows(window, false)

                val tokenToJoin by incomingJoinToken.collectAsState()
                LaunchedEffect(tokenToJoin) {
                    tokenToJoin?.let { token ->
                        // Если пользователь не залогинен, не пускаем его
                        if (AuthManager.token == null) {
                            Toast.makeText(context, "Сначала войдите в аккаунт", Toast.LENGTH_SHORT).show()
                            incomingJoinToken.value = null
                            return@LaunchedEffect
                        }

                        try {
                            val api = ApiService.create()
                            val res = api.joinGroup(token)
                            if (res.isSuccessful) {
                                Toast.makeText(context, "Вы успешно присоединились!", Toast.LENGTH_SHORT).show()
                                // Принудительно открываем список чатов, чтобы увидеть новую группу
                                currentScreen = "chats"
                            } else {
                                Toast.makeText(context, "Ошибка вступления или вы уже там", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Ошибка сети", Toast.LENGTH_SHORT).show()
                        } finally {
                            incomingJoinToken.value = null // Очищаем токен, чтобы не вступать дважды
                        }
                    }
                }
                val pushData by incomingPush.collectAsState()
                LaunchedEffect(pushData) {
                    pushData?.let { (chatId, msgId) ->
                        if (AuthManager.token != null) {
                            try {

                                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                notificationManager.cancelAll()


                                val api = ApiService.create()
                                // Загружаем список чатов, чтобы найти нужный объект Chat
                                val res = api.getChats()
                                val chatToOpen = res.chats.find { it.id == chatId }

                                if (chatToOpen != null) {
                                    selectedChat = chatToOpen
                                    currentScreen = "messages"
                                    // Сигналим чату, к какому сообщению скроллить
                                    if (msgId != null) {
                                        signalScrollToId = msgId
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        incomingPush.value = null // Очищаем после обработки
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        when (currentScreen) {
                            "login" -> {
                                LoginScreen(
                                    onNavigateToChats = { currentScreen = "chats" },
                                    onNavigateToRegister = { currentScreen = "register" } // ✨ Переход на регистрацию
                                )
                            }
                            "register" -> {
                                RegisterScreen(
                                    onNavigateToLogin = { currentScreen = "login" }, // ✨ Переход обратно на логин
                                    onNavigateToChats = { currentScreen = "chats" }
                                )
                            }
                            "chats" -> {
                                ChatListScreen(
                                    onChatClick = { clickedChat ->
                                        selectedChat = clickedChat
                                        currentScreen = "messages"
                                    },
                                    onLogout = { currentScreen = "login" }
                                )
                            }
                            "messages" -> {
                                if (selectedChat != null) {
                                    ChatMessagesScreen(
                                        chat = selectedChat!!,
                                        onBack = { currentScreen = "chats" },
                                        onImageClick = { msgs, initialId, bounds ->
                                            imageViewerState = ViewerState(msgs, initialId, bounds)
                                        },
                                        // ✨ ПЕРЕДАЕМ СИГНАЛЫ В ЧАТ
                                        externalReply = signalReplyMessage,
                                        onReplyHandled = { signalReplyMessage = null }, // Сбрасываем, когда чат принял

                                        externalForward = signalForwardMessage,
                                        onForwardHandled = { signalForwardMessage = null },

                                        externalScrollId = signalScrollToId,
                                        onScrollHandled = { signalScrollToId = null },


                                    )
                                }
                            }
                        }
                    }

                    // ✨ ИСПРАВЛЕННЫЙ ВЫЗОВ ПРОСМОТРЩИКА
                    if (imageViewerState != null) {
                        ImageViewerScreen(
                            messages = imageViewerState!!.messages,
                            initialMessageId = imageViewerState!!.initialId,
                            initialBounds = imageViewerState!!.initialBounds,
                            onClose = { imageViewerState = null },

                            // Когда нажали "Ответить" в просмотре фото
                            onReply = { msg ->
                                signalReplyMessage = msg
                                imageViewerState = null
                            },

                            // Когда нажали "Переслать"
                            onForward = { msg ->
                                signalForwardMessage = msg
                                imageViewerState = null
                            },

                            // Когда нажали "Показать в чате"
                            onShowInChat = { messageId ->
                                signalScrollToId = messageId
                                imageViewerState = null
                            }
                        )
                    }
                }
            }
        }
    }
    // ✨ ПРОВЕРЯЕМ ССЫЛКУ, ЕСЛИ ПРИЛОЖЕНИЕ РАЗВЕРНУЛИ ИЗ ФОНА
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntents(intent)
    }

    // ✨ ОБНОВЛЕННАЯ ФУНКЦИЯ ДЛЯ ПУШЕЙ И ССЫЛОК
    private fun handleIntents(intent: Intent?) {
        // 1. Обработка Deep Links (вступление в группу)
        val uri = intent?.data
        if (uri != null && uri.host == "sonzaiigi.com" && uri.path?.startsWith("/join/") == true) {
            val token = uri.path?.substringAfter("/join/")
            if (!token.isNullOrBlank()) {
                incomingJoinToken.value = token
            }
        }

        // 2. Обработка Firebase Push (клика по уведомлению)
        // Когда приложение в фоне, FCM кладет data-поля в extras интента
        val chatIdStr = intent?.extras?.getString("chat_id")
        val msgIdStr = intent?.extras?.getString("message_id")

        if (chatIdStr != null) {
            incomingPush.value = Pair(chatIdStr.toInt(), msgIdStr?.toIntOrNull())
        }
    }
}