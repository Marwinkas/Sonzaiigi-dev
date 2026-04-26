package com.marwinka.sonzaiigi

import android.Manifest
import android.R.attr.maxHeight
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.math.abs
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import kotlinx.coroutines.coroutineScope
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.window.Dialog
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

import android.provider.MediaStore
import android.content.ContentUris
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.Alignment.Companion
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.invoke
import coil.decode.ImageDecoderDecoder
import coil.decode.GifDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun formatMessageTime(timeString: String?): String {
    if (timeString.isNullOrEmpty()) return ""
    return try {
        // Очищаем миллисекунды, если они есть (C# иногда их шлет)
        val cleanString = timeString.substringBefore(".").substringBefore("Z") + "Z"

        // Парсим UTC время с сервера
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(cleanString)

        // Форматируем в локальное время телефона пользователя (только Часы:Минуты)
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        formatter.timeZone = TimeZone.getDefault() // Берет часовой пояс телефона

        date?.let { formatter.format(it) } ?: ""
    } catch (e: Exception) {
        // Если что-то пошло не так, просто вытаскиваем кусок времени из строки
        timeString.substringAfter("T").take(5)
    }
}
// ✨ ГЛОБАЛЬНЫЕ ПЕРЕМЕННЫЕ ДЛЯ СТЕКЛА
val GlassBackground = Color(0xFF242629).copy(alpha = 0.85f)
val GlassBackground3 = Color(0xFF242629).copy(alpha = 0.85f)
    val GlassBackground2 = Color(0xFF242629)
val GlassBorder = Color.White.copy(alpha = 0.15f)

suspend fun uploadFileInChunks(
    context: Context,
    uri: Uri,
    chatId: Int,
    api: ApiService,
    onProgress: (Float) -> Unit // Чтобы показывать процент загрузки
): Triple<String, String, String>? { // Возвращает: UploadId, Имя файла, Тип (MimeType)

    var fileName = "file"
    var mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

    // Достаем оригинальное имя файла
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) fileName = cursor.getString(nameIndex)
        }
    }

    val uploadId = UUID.randomUUID().toString().replace("-", "")
    val chunkSize = 2 * 1024 * 1024 // 2 МБ на один кусок (можешь изменить)
    val buffer = ByteArray(chunkSize)
    var chunkIndex = 0

    try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val totalBytes = inputStream.available().toFloat() // Примерный размер для прогресса
        var uploadedBytes = 0f

        inputStream.use { stream ->
            var bytesRead: Int
            while (stream.read(buffer).also { bytesRead = it } != -1) {
                // Если считали меньше, чем 2МБ (например, последний кусок файла), обрезаем массив
                val actualBytes = if (bytesRead == chunkSize) buffer else buffer.copyOf(bytesRead)

                val requestBody = actualBytes.toRequestBody("application/octet-stream".toMediaTypeOrNull())
                val chunkPart = MultipartBody.Part.createFormData("chunk", "chunk_$chunkIndex", requestBody)

                val chunkIndexBody = chunkIndex.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val uploadIdBody = uploadId.toRequestBody("text/plain".toMediaTypeOrNull())

                // Отправляем чанк на бэкенд
                api.uploadChunk(chatId, chunkPart, chunkIndexBody, uploadIdBody)

                chunkIndex++
                uploadedBytes += bytesRead
                onProgress(uploadedBytes / totalBytes) // Обновляем полоску загрузки
            }
        }
        return Triple(uploadId, fileName, mimeType)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}


data class MediaItem(
    val uri: Uri,
    val isVideo: Boolean,
    val durationStr: String? = null,
    val dateAdded: Long
)
fun getRecentMedia(context: Context, limit: Int = 200): List<MediaItem> {
    val mediaList = mutableListOf<MediaItem>()
    val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC"

    // ✨ СЕКРЕТ: Используем VOLUME_EXTERNAL для доступа ко всему хранилищу
    val imageUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    val videoUri = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)

    // Запрос картинок
    val imageProjection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATE_ADDED,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME // Это имя папки (для отладки)
    )

    context.contentResolver.query(imageUri, imageProjection, null, null, sortOrder)?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
        val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

        while (cursor.moveToNext() && mediaList.size < limit) {
            val id = cursor.getLong(idCol)
            val date = cursor.getLong(dateCol)
            val bucketName = cursor.getString(bucketCol) // Если хочешь, можно вывести в лог

            val uri = ContentUris.withAppendedId(imageUri, id)
            mediaList.add(MediaItem(uri, isVideo = false, dateAdded = date))
        }
    }

    // Запрос видео
    val videoProjection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DATE_ADDED,
        MediaStore.Video.Media.DURATION
    )

    context.contentResolver.query(videoUri, videoProjection, null, null, sortOrder)?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
        val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

        while (cursor.moveToNext() && mediaList.size < limit) {
            val id = cursor.getLong(idCol)
            val date = cursor.getLong(dateCol)
            val durationMs = cursor.getLong(durCol)
            val uri = ContentUris.withAppendedId(videoUri, id)

            val totalSeconds = durationMs / 1000
            val durStr = String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60)

            mediaList.add(MediaItem(uri, isVideo = true, durationStr = durStr, dateAdded = date))
        }
    }

    return mediaList.sortedByDescending { it.dateAdded }.take(limit)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomMediaPickerSheet(
    onDismiss: () -> Unit,
    onMediaSelected: (Uri, Rect?) -> Unit,
    onOpenFileManager: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // Запрашиваем медиа в фоновом потоке, чтобы не тормозить UI
    var mediaItems by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.Dispatchers.IO.invoke {
            mediaItems = getRecentMedia(context)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E293B), // Цвет твоей панели
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp)) {

            // 1. Заголовок и кнопка Файлы
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Галерея", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onOpenFileManager) {
                    Text("Файлы...", color = Color(0xFF38BDF8), fontSize = 16.sp)
                }
            }

            // 2. Сетка с фотографиями
            if (mediaItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF38BDF8))
                }
            } else {
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3), // По 3 фотки в ряд
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(mediaItems.size) { index ->
                        val item = mediaItems[index]
                        var bounds by remember { mutableStateOf<Rect?>(null) }
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .onGloballyPositioned { bounds = it.boundsInWindow() } // ✨ Ловим координаты ячейки
                                .clickable { onMediaSelected(item.uri, bounds) }
                        ) {
                            AsyncImage(
                                model = item.uri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Если это видео, рисуем значок камеры и длительность
                            if (item.isVideo) {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_video), contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(item.durationStr ?: "00:00", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatMessagesScreen(
    chat: Chat,
    onBack: () -> Unit,
    onImageClick: (List<Message>, Int, Rect?) -> Unit
) {
    val api = remember { ApiService.create() }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var highlightedMessageId by remember { mutableStateOf<Int?>(null) }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    var menuExpanded by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }

    var replyToMessage by remember { mutableStateOf<Message?>(null) }
    var editingMessage by remember { mutableStateOf<Message?>(null) }

    var deletingMessageId by remember { mutableStateOf<Int?>(null) }

    // ✨ НОВОЕ: Список сообщений, которые сейчас находятся в процессе плавного исчезновения
    var fadingOutMessages by remember { mutableStateOf<Set<Int>>(emptySet()) }

    val clipboardManager = LocalClipboardManager.current



    val context = LocalContext.current
    var isForwardModalOpen by remember { mutableStateOf(false) }
    var forwardMessageId by remember { mutableStateOf<Int?>(null) }
    var selectedChatsForForward by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var includeAuthor by remember { mutableStateOf(true) }
    var availableChats by remember { mutableStateOf<List<Chat>>(emptyList()) }
    var showMediaPicker by remember { mutableStateOf(false) }
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    var previewMediaUri by remember { mutableStateOf<Uri?>(null) }
    var previewMediaBounds by remember { mutableStateOf<Rect?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {

        } else {
            Toast.makeText(context, "Нужен доступ к фото и видео", Toast.LENGTH_SHORT).show()
        }
    }
    // Состояния для файла
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }

// Состояния для выбранного файла (черновик)
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedMimeType by remember { mutableStateOf<String?>(null) }

// Твой лаунчер теперь просто запоминает файл, а не начинает загрузку сразу
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            // Достаем имя файла для отображения
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) selectedFileName = cursor.getString(nameIndex)
                }
            }
            selectedMimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
        }
    }
// Запрос прав специально для микрофона
    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Без доступа к микрофону запись невозможна", Toast.LENGTH_SHORT).show()
        }
    }
    val reversedMessages = remember(messages) { messages.reversed() }

    LaunchedEffect(isForwardModalOpen) {
        if (isForwardModalOpen && availableChats.isEmpty()) {
            try {
                val res = api.getChats()
                availableChats = res.chats
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    LaunchedEffect(chat.id) {
        try {
            val response = api.getMessages(chat.id)
            messages = response.messages
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    var expandedMessageId by remember { mutableStateOf<Int?>(null) }

    DisposableEffect(chat.id) {
        val wsManager = WebSocketManager(userId = AuthManager.userId) { json ->
            val type = json.optString("type")
            val targetChatId = json.optInt("chat_id")

            if (targetChatId == chat.id) {
                when (type) {
                    "new_message" -> {
                        val msgObj = json.getJSONObject("message")
                        val replyToObj = msgObj.optJSONObject("reply_to")
                        val parsedReplyTo = if (replyToObj != null) {
                            ReplyToInfo(
                                id = replyToObj.optInt("id", 0),
                                name = replyToObj.optString("name", "Кто-то"),
                                text = replyToObj.optString("text", "Вложение")
                            )
                        } else null

                        val fwdFrom = msgObj.optString("forwarded_from", "").takeIf { it.isNotBlank() && it != "null" }

                        val newMsg = Message(
                            id = msgObj.getInt("id"),
                            text = msgObj.optString("text", "").takeIf { it.isNotBlank() },
                            senderId = msgObj.getInt("senderId"),
                            senderName = msgObj.optString("senderName", "Кто-то"),
                            senderAvatar = msgObj.optString("senderAvatar", null),
                            time = msgObj.optString("time", ""),
                            image_thumb = msgObj.optString("image_thumb", null).takeIf { it != "null" },
                            image_view = msgObj.optString("image_view", null).takeIf { it != "null" },
                            image_master = msgObj.optString("image_master", null).takeIf { it != "null" },
                            is_edited = msgObj.optBoolean("is_edited", false),
                            gif_url = msgObj.optString("gif_url", ""),
                            forwarded_from = fwdFrom,
                            reply_to = parsedReplyTo
                        )

                        if (!messages.any { it.id == newMsg.id }) {
                            messages = messages + newMsg
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(100)
                                listState.animateScrollToItem(0)
                            }
                        }
                    }
                    "message_updated" -> {
                        val msgId = json.getInt("message_id")
                        val newText = json.getString("text")
                        messages = messages.map {
                            if (it.id == msgId) it.copy(text = newText, is_edited = true) else it
                        }
                    }
                    "message_deleted" -> {
                        val msgId = json.getInt("message_id")
                        // ✨ Если сообщение удалил кто-то другой, оно тоже плавно исчезнет!
                        if (!fadingOutMessages.contains(msgId)) {
                            fadingOutMessages = fadingOutMessages + msgId
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(500)
                                messages = messages.filter { it.id != msgId }
                                fadingOutMessages = fadingOutMessages - msgId
                            }
                        }
                    }
                }
            }
        }
        wsManager.connect()
        onDispose { wsManager.disconnect() }
    }
// Переменные для записи голоса
    var isRecording by remember { mutableStateOf(false) }
    var isRecordingLocked by remember { mutableStateOf(false) }
    var recordDuration by remember { mutableIntStateOf(0) }
    val voiceRecorder = remember { VoiceRecorder(context) }

    // Таймер для голосовых
    LaunchedEffect(isRecording) {
        recordDuration = 0
        while (isRecording) {
            delay(1000)
            recordDuration++
        }
    }

    // Лаунчер для выбора любых файлов (включая музыку)
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) selectedFileName = cursor.getString(nameIndex)
                }
            }
            selectedMimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
        }
    }

    val sendAction = {
        val textToSend = inputText
        val currentReplyToId = replyToMessage?.id
        val fileUri = selectedFileUri
        val mime = selectedMimeType
        val fName = selectedFileName

        inputText = ""
        replyToMessage = null
        selectedFileUri = null

        coroutineScope.launch {
            try {
                var uploadedFileId: String? = null
                if (fileUri != null) {
                    isUploading = true
                    val uploadResult = uploadFileInChunks(context, fileUri, chat.id, api) { progress ->
                        uploadProgress = progress
                    }
                    uploadedFileId = uploadResult?.first
                    isUploading = false
                }

                if (editingMessage != null) {
                    val msgId = editingMessage!!.id
                    editingMessage = null
                    val resMsg = api.editMessage(msgId, UpdateMsgDto(textToSend))
                    messages = messages.map { if (it.id == msgId) it.copy(text = resMsg.text, is_edited = true) else it }
                } else {
                    val sentMsg = api.sendMessage(
                        conversationId = chat.id,
                        text = textToSend.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        parentId = currentReplyToId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        uploadedFileId = uploadedFileId?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        originalFileName = fName?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        contentType = mime?.toRequestBody("text/plain".toMediaTypeOrNull())
                    )
                    if (!messages.any { it.id == sentMsg.id }) {
                        messages = messages + sentMsg
                        listState.animateScrollToItem(0)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isUploading = false
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFF16161a)
            )
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .layout { measurable, constraints ->
                            // overflowX скрывает бока, overflowY скрывает низ
                            val overflowX = 4.dp.roundToPx()
                            val overflowY = 2.dp.roundToPx()

                            val placeable = measurable.measure(
                                constraints.copy(
                                    maxWidth = constraints.maxWidth + overflowX,
                                    maxHeight = constraints.maxHeight + overflowY
                                )
                            )
                            // Оставляем видимую высоту на overflowY меньше реальной
                            layout(placeable.width - overflowX, placeable.height - overflowY) {
                                // Смещаем влево на половину X и вниз на весь Y
                                placeable.placeRelative(-overflowX / 2, -overflowY / 2)
                            }
                        }
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .background( Color(0xFF242629))
                        .border(1.dp, GlassBorder, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .padding(horizontal = 8.dp).padding(bottom = 4.dp),

                )
                {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                    ) {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (chat.avatar != null) {
                                        AsyncImage(
                                            model = chat.avatar,
                                            contentDescription = "Аватар",
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (chat.is_group) Color(0xFFF59E0B) else Color(
                                                        0xFF38BDF8
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = chat.name.take(1).uppercase(),
                                                color = Color(0xFF0F172A),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = chat.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (chat.is_group) {
                                            Text(text = "Группа", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                        }
                                    }
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = onBack) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                                }
                            },
                            actions = {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Меню", tint = Color.White)
                                }

                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false },
                                    modifier = Modifier.background(Color.Transparent)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(GlassBackground)
                                            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                                    ) {
                                        Column {
                                            DropdownMenuItem(
                                                text = { Text(if (isMuted) "Включить" else "Выключить уведомления", color = Color.White) },
                                                leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF94A3B8)) },
                                                onClick = { isMuted = !isMuted; menuExpanded = false }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Поиск", color = Color.White) },
                                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8)) },
                                                onClick = { menuExpanded = false }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Выйти из чата", color = Color(0xFFEF4444)) },
                                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color(0xFFEF4444)) },
                                                onClick = { menuExpanded = false; onBack() }
                                            )
                                        }
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                        )
                    }
                }
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .layout { measurable, constraints ->
                            val overflowX = 4.dp.roundToPx()
                            val overflowY = 2.dp.roundToPx()
                            val placeable = measurable.measure(
                                constraints.copy(maxWidth = constraints.maxWidth + overflowX, maxHeight = constraints.maxHeight + overflowY)
                            )
                            layout(placeable.width - overflowX, placeable.height - overflowY) {
                                placeable.placeRelative(-overflowX / 2, 0)
                            }
                        }
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(Color(0xFF242629))
                        .border(1.dp, GlassBorder, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .padding(horizontal = 8.dp)
                        .padding(top = 4.dp)
                        .animateContentSize() // ✨ ИСПРАВЛЕНО: Плавно раздвигает панель при открытии галереи!
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(8.dp)
                    ) {
                        // 1. ПЛАШКА ОТВЕТА
                        if (replyToMessage != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.05f)).border(1.dp, GlassBorder, RoundedCornerShape(16.dp)).padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Create, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.padding(end = 8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Ответ ${replyToMessage!!.senderName}", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(replyToMessage!!.text ?: "Вложение", color = Color.White, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                IconButton(onClick = { replyToMessage = null }) { Icon(Icons.Default.Close, contentDescription = "Отмена", tint = Color.Gray) }
                            }
                        }

                        // 2. ПЛАШКА РЕДАКТИРОВАНИЯ
                        if (editingMessage != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.05f)).border(1.dp, GlassBorder, RoundedCornerShape(16.dp)).padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.padding(end = 8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Редактирование", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(editingMessage!!.text ?: "", color = Color.White, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                IconButton(onClick = { editingMessage = null; inputText = "" }) { Icon(Icons.Default.Close, contentDescription = "Отмена", tint = Color.Gray) }
                            }
                        }

                        // ✨ 3. НОВАЯ ПЛАШКА ВЫБРАННОГО ФАЙЛА
                        if (selectedFileUri != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF7f5af0).copy(alpha = 0.1f))
                                    .border(1.dp, Color(0xFF7f5af0).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_file),
                                    contentDescription = null,
                                    tint = Color(0xFF7f5af0),
                                    modifier = Modifier.padding(end = 8.dp).size(20.dp)
                                )
                                Text(
                                    text = selectedFileName ?: "Файл",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    selectedFileUri = null
                                    selectedFileName = null
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Убрать файл", tint = Color.Gray, modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        // 4. СТРОКА ВВОДА
                        // 4. СТРОКА ВВОДА
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isUploading) {
                                CircularProgressIndicator(
                                    progress = { uploadProgress },
                                    modifier = Modifier.size(24.dp),
                                    color = Color(0xFF38BDF8),
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            } else if (!isRecording) {
                                // Кнопка Скрепка (видна, только если не записываем)
                                IconButton(onClick = {
                                    val permissions =
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            arrayOf(
                                                Manifest.permission.READ_MEDIA_IMAGES,
                                                Manifest.permission.READ_MEDIA_VIDEO
                                            )
                                        } else {
                                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                                        }
                                    permissionLauncher.launch(permissions)

                                    if (showMediaPicker) {
                                        showMediaPicker = false
                                        focusRequester.requestFocus()
                                    } else {
                                        showMediaPicker = true
                                    }
                                }) {
                                    Icon(
                                        painter = painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_paperclip),
                                        contentDescription = "Прикрепить",
                                        tint = if (showMediaPicker || selectedFileUri != null) Color(
                                            0xFF7f5af0
                                        ) else Color.White
                                    )
                                }
                            }

                            // Поле ввода ИЛИ панель записи голоса
                            if (isRecording) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Мигающая красная точка
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (recordDuration % 2 == 0) Color(
                                                    0xFFEF4444
                                                ) else Color.Transparent
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = String.format(
                                            "%02d:%02d",
                                            recordDuration / 60,
                                            recordDuration % 60
                                        ),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.weight(1f))

                                    // Кнопка отмены записи
                                    IconButton(onClick = {
                                        isRecording = false
                                        isRecordingLocked = false
                                        voiceRecorder.cancelRecording()
                                    }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Отмена",
                                            tint = Color(0xFFEF4444)
                                        )
                                    }
                                }
                            } else {
                                OutlinedTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    placeholder = {
                                        Text(
                                            "Сообщение...",
                                            color = Color(0XFF72757e)
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF7f5af0).copy(alpha = 0.7f),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                        cursorColor = Color(0xFF7f5af0)
                                    ),
                                    shape = RoundedCornerShape(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Кнопка Отправить / Микрофон
                            val showMic =
                                inputText.isBlank() && selectedFileUri == null && !isUploading

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isUploading) Color.Gray else Color(0xFF7f5af0))
                                    .pointerInput(showMic, isRecordingLocked) {
                                        if (showMic && !isRecordingLocked) {
                                            awaitPointerEventScope {
                                                while (true) {
                                                    val down = awaitFirstDown()

                                                    // ✨ ПРОВЕРЯЕМ ПРАВА НА МИКРОФОН!
                                                    val hasMicPerm =
                                                        androidx.core.content.ContextCompat.checkSelfPermission(
                                                            context,
                                                            Manifest.permission.RECORD_AUDIO
                                                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                                    if (!hasMicPerm) {
                                                        // Если прав нет, просим и игнорируем текущее нажатие
                                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                        // Ждем, пока пользователь отпустит палец
                                                        while (awaitPointerEvent().changes.any { it.pressed }) {
                                                        }
                                                        continue
                                                    }

                                                    // Если права есть, начинаем запись
                                                    isRecording = true
                                                    voiceRecorder.startRecording()
                                                    var isCanceled = false

                                                    do {
                                                        val event = awaitPointerEvent()
                                                        val dragY =
                                                            event.changes.firstOrNull()?.position?.y
                                                                ?: 0f

                                                        // Свайп вверх -> Замок
                                                        if (dragY < -100f) {
                                                            isRecordingLocked = true
                                                        }

                                                        // Свайп влево -> Отмена
                                                        val dragX =
                                                            event.changes.firstOrNull()?.position?.x
                                                                ?: 0f
                                                        if (dragX < -150f) {
                                                            isCanceled = true
                                                            isRecording = false
                                                            voiceRecorder.cancelRecording()
                                                        }
                                                    } while (event.changes.any { it.pressed })

                                                    // Палец отпущен
                                                    if (!isCanceled && !isRecordingLocked) {
                                                        isRecording = false
                                                        voiceRecorder.stopRecording()

                                                        // ✨ Важно: если запись слишком короткая (доля секунды), отменяем
                                                        if (recordDuration < 1) {
                                                            voiceRecorder.cancelRecording()
                                                        } else {
                                                            voiceRecorder.outputFile?.let { file ->
                                                                selectedFileUri = Uri.fromFile(file)
                                                                selectedFileName = file.name
                                                                selectedMimeType = "audio/mp4"
                                                                sendAction()
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    .clickable(enabled = !showMic || isRecordingLocked) {
                                        if (isRecordingLocked) {
                                            isRecording = false
                                            isRecordingLocked = false
                                            voiceRecorder.stopRecording()

                                            if (recordDuration < 1) {
                                                voiceRecorder.cancelRecording()
                                            } else {
                                                voiceRecorder.outputFile?.let { file ->
                                                    selectedFileUri = Uri.fromFile(file)
                                                    selectedFileName = file.name
                                                    selectedMimeType = "audio/mp4"
                                                }
                                            }
                                        }
                                        // Если выбрали отмену, selectedFileUri будет null, и пустое сообщение не уйдет
                                        if (inputText.isNotBlank() || selectedFileUri != null) {
                                            sendAction()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = if (showMic) painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_mic)
                                    else painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_send),
                                    contentDescription = "Действие",
                                    tint = Color.White
                                )
                            }
                        }
                        Spacer(Modifier.imePadding())
                    }

                    androidx.compose.animation.AnimatedVisibility(visible = showMediaPicker,
                        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically(
                            spring(
                            stiffness = Spring.StiffnessMediumLow,
                            visibilityThreshold = IntSize.VisibilityThreshold
                        ),
                            Alignment.Top)) {
                        InlineMediaGallery(
                            onMediaSelected = { uri, bounds ->
                                previewMediaUri = uri
                                previewMediaBounds = bounds
                                // Мы НЕ делаем showMediaPicker = false, галерея остается открытой!
                            },
                            onOpenFileManager = {
                                showMediaPicker = false
                                filePickerLauncher.launch("*/*")
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier
                .fillMaxSize()
                ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF38BDF8))
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(
                            top = innerPadding.calculateTopPadding() + 8.dp,
                            bottom = innerPadding.calculateBottomPadding() + 8.dp
                        ),
                        reverseLayout = true
                    ){
                        // ✨ 1. ДОБАВЛЕН КЛЮЧ! Это полностью убирает глюки с прыгающей шириной соседних сообщений
                        itemsIndexed(reversedMessages, key = { _, msg -> msg.id }, contentType = { _, _ -> "message" }) { index, msg ->
                            val isFadingOut = fadingOutMessages.contains(msg.id)
                            val prevMsg = reversedMessages.getOrNull(index + 1)
                            val nextMsg = reversedMessages.getOrNull(index - 1)

                            val isConnectedToOlder = prevMsg != null &&
                                    prevMsg.senderId == msg.senderId &&
                                    formatMessageTime(prevMsg.time) == formatMessageTime(msg.time)

                            val isConnectedToNewer = nextMsg != null &&
                                    nextMsg.senderId == msg.senderId &&
                                    formatMessageTime(nextMsg.time) == formatMessageTime(msg.time)

                            // ✨ 2. ПЛАВНОЕ ИСЧЕЗНОВЕНИЕ: Сначала растворяется (300мс), потом сжимается (300мс)
                            Box(modifier = Modifier.animateItem(
                                // Настраиваем плавное перемещение, когда сообщения "раздвигаются"
                                placementSpec = tween(
                                    durationMillis = 500,
                                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                                ),
                                // Анимация появления нового сообщения
                                fadeInSpec = tween(durationMillis = 400),
                                // Анимация исчезновения
                                fadeOutSpec = tween(durationMillis = 300)
                            )){
                            androidx.compose.animation.AnimatedVisibility(
                                visible = !isFadingOut,
                                enter = androidx.compose.animation.EnterTransition.None,
                                exit = androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(300)) +
                                        androidx.compose.animation.shrinkVertically(
                                            animationSpec = androidx.compose.animation.core.tween(300, delayMillis = 200)
                                        )
                            ) {
                                MessageBubble(
                                    message = msg,
                                    isMe = msg.senderId == AuthManager.userId,
                                    // ✨ ПЕРЕДАЕМ НОВЫЕ ПАРАМЕТРЫ
                                    isConnectedToOlder = isConnectedToOlder,
                                    isConnectedToNewer = isConnectedToNewer,
                                    showTime = !isConnectedToNewer, // Время только у последнего в группе
                                    showName = !isConnectedToOlder, // Имя только у первого в группе
                                    onImageClick = { clickedMsg, bounds -> onImageClick(messages, clickedMsg.id, bounds) },
                                    onReply = { replyToMessage = it },
                                    onEdit = { editingMessage = it; inputText = it.text ?: "" },
                                    onForward = {
                                        forwardMessageId = it.id
                                        selectedChatsForForward = emptySet()
                                        isForwardModalOpen = true
                                    },
                                    isExpanded = expandedMessageId == msg.id,
                                    onToggleExpand = {
                                        expandedMessageId = if (expandedMessageId == msg.id) null else msg.id
                                    },
                                    onReplyClick = { targetId ->
                                        val targetIndex = messages.indexOfFirst { it.id == targetId }
                                        if (targetIndex != -1) {
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(targetIndex)
                                                highlightedMessageId = targetId
                                                kotlinx.coroutines.delay(1500)
                                                highlightedMessageId = null
                                            }
                                        } else {
                                            Toast.makeText(context, "Сообщение слишком старое", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    isDeleting = deletingMessageId == msg.id,
                                    onDeleteStart = { deletingMessageId = msg.id },
                                    onCancelDelete = { deletingMessageId = null },
                                    onConfirmDelete = { deleteForEveryone ->
                                        deletingMessageId = null
                                        fadingOutMessages = fadingOutMessages + msg.id
                                        coroutineScope.launch {
                                            try {
                                                api.deleteMessage(msg.id, deleteForEveryone)
                                                kotlinx.coroutines.delay(500)
                                                messages = messages.filter { it.id != msg.id }
                                                fadingOutMessages = fadingOutMessages - msg.id
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                fadingOutMessages = fadingOutMessages - msg.id
                                            }
                                        }
                                    }
                                )
                            }
                                }
                        }
                    }
                }
            }
        }
    }

// ✨ ВЫЗОВ ЭКРАНА ПРЕДПРОСМОТРА
    if (previewMediaUri != null) {
        MediaPreviewScreen(
            uri = previewMediaUri!!,
            initialBounds = previewMediaBounds,
            onClose = {
                previewMediaUri = null
                previewMediaBounds = null
            },
            onSend = { uri, caption ->
                // 1. Закрываем предпросмотр
                previewMediaUri = null
                previewMediaBounds = null

                // 2. Имитируем, что пользователь ввел этот текст и выбрал этот файл в главном окне
                inputText = caption
                selectedFileUri = uri
                selectedMimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(selectedMimeType) ?: "jpg"
                selectedFileName = "media_${System.currentTimeMillis()}.$ext"

                val textToSend = inputText
                val currentReplyToId = replyToMessage?.id
                val fileUri = selectedFileUri

                // Очищаем интерфейс сразу
                inputText = ""
                replyToMessage = null
                selectedFileUri = null

                coroutineScope.launch {
                    try {
                        var uploadedFileId: String? = null

                        // ✨ Если есть файл - сначала грузим его чанками
                        // ✨ Если есть файл - сначала грузим его чанками
                        if (fileUri != null) {
                            isUploading = true
                            val uploadResult = uploadFileInChunks(context, fileUri, chat.id, api) { progress ->
                                uploadProgress = progress
                            }
                            // ✨ ЗАЩИТА: Если загрузка упала, прерываемся!
                            if (uploadResult == null) {
                                isUploading = false
                                Toast.makeText(context, "Ошибка при загрузке файла", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            uploadedFileId = uploadResult.first
                            isUploading = false
                        }

                        if (editingMessage != null) {
                            val msgId = editingMessage!!.id
                            editingMessage = null
                            val resMsg = api.editMessage(msgId, UpdateMsgDto(textToSend))
                            messages = messages.map { if (it.id == msgId) it.copy(text = resMsg.text, is_edited = true) else it }
                        } else {
                            // Используем наш универсальный метод sendMessage
                            val sentMsg = api.sendMessage(
                                conversationId = chat.id,
                                text = textToSend.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull()),
                                parentId = currentReplyToId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
                                uploadedFileId = uploadedFileId?.toRequestBody("text/plain".toMediaTypeOrNull()),
                                originalFileName = selectedFileName?.toRequestBody("text/plain".toMediaTypeOrNull()),
                                contentType = selectedMimeType?.toRequestBody("text/plain".toMediaTypeOrNull())
                            )

                            if (!messages.any { it.id == sentMsg.id }) {
                                messages = messages + sentMsg
                                kotlinx.coroutines.delay(100)
                                listState.animateScrollToItem(0)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Ошибка отправки", Toast.LENGTH_SHORT).show()
                    } finally {
                        isUploading = false
                        selectedFileName = null
                    }
                }
            }
        )
    }
    // ✨ СТЕКЛЯННАЯ МОДАЛКА ПЕРЕСЫЛКИ
    if (isForwardModalOpen) {
        Dialog(onDismissRequest = { isForwardModalOpen = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(GlassBackground)
                    .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text("Переслать сообщение", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(availableChats) { c ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        selectedChatsForForward =
                                            if (selectedChatsForForward.contains(c.id)) {
                                                selectedChatsForForward - c.id
                                            } else {
                                                selectedChatsForForward + c.id
                                            }
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (c.is_group) Color(0xFFF59E0B) else Color(
                                                0xFF38BDF8
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = c.name.take(1).uppercase(), color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(c.name, color = Color.White, modifier = Modifier.weight(1f))
                                Checkbox(
                                    checked = selectedChatsForForward.contains(c.id),
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF38BDF8), uncheckedColor = Color.White.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { includeAuthor = !includeAuthor }) {
                        Checkbox(
                            checked = includeAuthor,
                            onCheckedChange = { includeAuthor = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF38BDF8), uncheckedColor = Color.White.copy(alpha = 0.5f))
                        )
                        Text("Показывать автора", color = Color.White, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { isForwardModalOpen = false }) { Text("Отмена", color = Color(0xFF94A3B8)) }
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        api.forwardMessage(ForwardDto(forwardMessageId!!, selectedChatsForForward.toList(), includeAuthor))
                                        isForwardModalOpen = false
                                        Toast.makeText(context, "Сообщение переслано", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) { e.printStackTrace() }
                                }
                            },
                            enabled = selectedChatsForForward.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8), disabledContainerColor = Color.White.copy(alpha = 0.1f))
                        ) {
                            Text("Переслать (${selectedChatsForForward.size})", color = if (selectedChatsForForward.isNotEmpty()) Color(0xFF0F172A) else Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
data class MenuAction(
    val label: String,
    val iconRes: Int,
    val color: Color = Color.Gray,
    val onClick: () -> Unit
)
object GlobalAudioPlayer {
    var mediaPlayer: MediaPlayer? = null
    var currentPlayingId by mutableStateOf<Int?>(null)
    var progress by mutableFloatStateOf(0f)
    var trackDuration by mutableStateOf(0)
    var isPlaying by mutableStateOf(false)

    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun playPause(messageId: Int, url: String) {
        // Если нажали на тот же трек, который сейчас играет
        if (currentPlayingId == messageId) {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                isPlaying = false
                progressJob?.cancel()
            } else {
                mediaPlayer?.start()
                isPlaying = true
                startProgressTracker()
            }
            return
        }

        // Если нажали на другой трек — выключаем старый, включаем новый
        stop()

        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepare()
            trackDuration = duration
            start()
        }
        currentPlayingId = messageId
        isPlaying = true
        startProgressTracker()

        mediaPlayer?.setOnCompletionListener {
            isPlaying = false
            progress = 0f
            mediaPlayer?.seekTo(0)
            progressJob?.cancel()
        }
    }

    fun stop() {
        mediaPlayer?.release()
        mediaPlayer = null
        currentPlayingId = null
        isPlaying = false
        progressJob?.cancel()
        progress = 0f
        trackDuration = 0
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
        progress = position.toFloat() / (trackDuration.takeIf { it > 0 } ?: 1)
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && mediaPlayer?.isPlaying == true) {
                val pos = mediaPlayer?.currentPosition ?: 0
                val dur = trackDuration.takeIf { it > 0 } ?: 1
                progress = pos.toFloat() / dur.toFloat()
                delay(50) // Плавное обновление ползунка
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerBubble(message: Message, isMe: Boolean) {
    val isThisTrackActive = GlobalAudioPlayer.currentPlayingId == message.id
    val isPlaying = isThisTrackActive && GlobalAudioPlayer.isPlaying
    val progress = if (isThisTrackActive) GlobalAudioPlayer.progress else 0f
    val duration = if (isThisTrackActive) GlobalAudioPlayer.trackDuration else 0
    val currentPosition = (progress * duration).toInt()

    var isDragging by remember { mutableStateOf(false) }
    var localDragPosition by remember { mutableIntStateOf(0) }
    Row(
        modifier = Modifier
            .fillMaxWidth(0.85f).padding(8.dp)
           ,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(64.dp))
                // Если обложки нет, красим фон цветом
                .then(
                    if (message.audio_cover == null) {
                        Modifier.background(if (isMe) Color.White.copy(alpha = 0.2f) else Color(0xFF38BDF8))
                    } else {
                        Modifier // Если есть обложка, фон не нужен (она его закроет)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (message.audio_cover != null) {
                AsyncImage(
                    model = message.audio_cover,
                    contentDescription = "Обложка",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                            colorFilter = ColorFilter.tint(
                            Color.Black.copy(alpha = 0.4f),
                    blendMode = BlendMode.Darken
                )
                )
            }
            IconButton(
                onClick = {
                    val url = message.audio_stream ?: message.audio_master
                    if (url != null) {
                        GlobalAudioPlayer.playPause(message.id, url)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
            ) {
                Icon(
                    painter = if (isPlaying) painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_pause)
                    else painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_play),
                    contentDescription = "Play/Pause",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = message.audio_title ?: "Голосовое сообщение",
                color = Color.White,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,

                )

            // ✨ ИНТЕРАКТИВНЫЙ ПОЛЗУНОК
            val displayProgress = if (isDragging) {
                if (duration > 0) localDragPosition.toFloat() / duration.toFloat() else 0f
            } else {
                progress
            }
            //if (!isPlaying && message.audio_artist != null) {
            //    Text(
            //        text = message.audio_artist,
            //        color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
            //        fontSize = 12.sp,
            //         maxLines = 1,
            //        overflow = TextOverflow.Ellipsis
            //   )
            //}
            //else
            Slider(
                value = progress.coerceIn(0f, 1f),
                onValueChange = { newProgress ->
                    isDragging = true
                    localDragPosition = (newProgress * duration).toInt()
                },
                onValueChangeFinished = {
                    isDragging = false
                    if (isThisTrackActive) {
                        GlobalAudioPlayer.seekTo(localDragPosition)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(20.dp),
                // Используем SliderDefaults.Thumb и Track, чтобы убрать лишние зазоры
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(12.dp) // Размер твоего кружка
                            .padding(top = 2.dp)
                            .background(
                                color = if (isMe) Color.White else Color(0xFF38BDF8),
                                shape = CircleShape
                            )
                    )
                },
                track = { sliderState ->
                    SliderDefaults.Track(
                        sliderState = sliderState,
                        modifier = Modifier.height(3.dp), // Тонкая линия
                        thumbTrackGapSize = 0.dp, // Обязательно: совпадает с размером thumb
                        drawStopIndicator = null, // Убирает тот самый "глупый белый кружок" в конце
                        colors = SliderDefaults.colors(
                            activeTrackColor = if (isMe) Color.White else Color(0xFF38BDF8),
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                }
            )


            // Отображение времени 00:00 / 00:00
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                val timeToShow = if (isDragging) localDragPosition else currentPosition
                Text(
                    formatTime(timeToShow),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
                Text(text = "/", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                Text(
                    text = if (duration > 0) formatTime(duration) else message.audio_duration
                        ?: "00:00",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
        }
        }

    }


// Вспомогательная функция для красивого времени
fun formatTime(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun MessageBubble(
    message: Message,
    isMe: Boolean,
    isConnectedToOlder: Boolean, // ✨ НОВЫЕ ПАРАМЕТРЫ
    isConnectedToNewer: Boolean,
    showTime: Boolean,
    showName: Boolean,
    onImageClick: (Message, Rect?) -> Unit,
    onReply: (Message) -> Unit,
    onEdit: (Message) -> Unit,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onForward: (Message) -> Unit,
    onReplyClick: (Int) -> Unit,
    isDeleting: Boolean,
    onDeleteStart: () -> Unit,
    onCancelDelete: () -> Unit,
    onConfirmDelete: (Boolean) -> Unit
) {
    val baseBubbleColor = if (isMe) Color(0xFF7f5af0) else Color(0xFF1E293B)
    val baseDeleteColor =  Color(0xFF242629)
    // ✨ ПУЗЫРЕК: СТЕКЛО
    val glassColor = baseBubbleColor.copy(alpha = 0.85f)
    val deleteColor = baseDeleteColor.copy(alpha = 0.85f)
    val textColor = if (isMe) Color(0xFFfffffe) else Color.White

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var imageBounds by remember { mutableStateOf<Rect?>(null) }
    var deleteForEveryone by remember { mutableStateOf(false) }

    val actions = remember(message, isMe) {
        mutableListOf<MenuAction>().apply {
            add(MenuAction("Reply", com.composables.icons.lucide.R.drawable.lucide_ic_reply) {
                onReply(message); onToggleExpand()
            })
            add(MenuAction("Forward", com.composables.icons.lucide.R.drawable.lucide_ic_forward) {
                onForward(message); onToggleExpand()
            })

            if (!message.text.isNullOrEmpty()) {
                add(MenuAction("Copy", com.composables.icons.lucide.R.drawable.lucide_ic_copy) {
                    clipboardManager.setText(AnnotatedString(message.text))
                    Toast.makeText(context, "Текст скопирован", Toast.LENGTH_SHORT).show()
                    onToggleExpand()
                })
            }

            if (isMe && !message.text.isNullOrEmpty()) {
                add(MenuAction("Edit", com.composables.icons.lucide.R.drawable.lucide_ic_pencil) {
                    onEdit(message); onToggleExpand()
                })
            }

            add(MenuAction("Delete", com.composables.icons.lucide.R.drawable.lucide_ic_trash_2, Color(0xFFEF4444)) {
                onDeleteStart(); onToggleExpand()
            })
        }
    }

    val configuration = LocalConfiguration.current
    val maxHeight = configuration.screenHeightDp.dp * 0.5f // Вычисляем 50% высоты экрана
// ✨ ВЫЧИСЛЯЕМ ФОРМУ ПУЗЫРЯ (Делаем прямые углы, если сообщения "слиплись")
    val bubbleShape = if (isMe) {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = if (isConnectedToOlder) 4.dp else 16.dp,
            bottomEnd = if (isConnectedToNewer) 4.dp else 16.dp,
            bottomStart = 16.dp
        )
    } else {
        RoundedCornerShape(
            topStart = if (isConnectedToOlder) 4.dp else 16.dp,
            topEnd = 16.dp,
            bottomEnd = 16.dp,
            bottomStart = if (isConnectedToNewer) 4.dp else 16.dp
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = if (isConnectedToOlder) 1.dp else 4.dp,
                bottom = if (isConnectedToNewer) 1.dp else 4.dp
            )
            .clickable(enabled = !isDeleting,
                interactionSource = remember { MutableInteractionSource() },
                indication = null )
            {
                onToggleExpand()
            },
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f),
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            if (!isMe && !isDeleting && showName) {
                Text(
                    text = message.senderName ?: "Кто-то",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 2.dp, start = 8.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(bubbleShape)

                    .background(if (isDeleting) deleteColor else glassColor)
                    .border(width = 1.dp, color = GlassBorder, shape = bubbleShape)
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessHigh
                        )
                    )

            ) {
                androidx.compose.animation.AnimatedContent(
                    targetState = isDeleting,
                    transitionSpec = {
                        androidx.compose.animation.fadeIn(
                            animationSpec = androidx.compose.animation.core.tween(50)
                        ) togetherWith androidx.compose.animation.fadeOut(
                            animationSpec = androidx.compose.animation.core.tween(50)
                        )
                    },
                    label = "delete_transition"
                ) { deleting ->
                    if (deleting) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Удалить сообщение?",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(onClick = onCancelDelete) {
                                    Text("Отмена", color = Color(0xFF94A3B8))
                                }
                                Button(
                                    onClick = { onConfirmDelete(deleteForEveryone) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(
                                            0xFFEF4444
                                        )
                                    )
                                ) {
                                    Text("Удалить", color = Color.White)
                                }
                            }
                        }
                    } else {
                        Column {
                            if (message.forwarded_from != null) {
                                Text("Forwarded from ${message.forwarded_from}", color = Color(0xFFfffffe).copy(alpha = 0.8f), fontSize = 12.sp, modifier = Modifier.padding(8.dp))
                            }

                            if (message.reply_to != null) {
                                Row(
                                    modifier = Modifier.padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 6.dp).height(IntrinsicSize.Min).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.1f)).clickable { onReplyClick(message.reply_to.id) }
                                ) {
                                    Box(modifier = Modifier.width(3.dp).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(if (isMe) Color(0xFFE0F2FE) else Color(0xFF38BDF8)))
                                    Column(modifier = Modifier.padding(start = 6.dp, end = 8.dp).alpha(0.9f)) {
                                        Text(message.reply_to.name ?: "Ответ", color = Color(0xFFfffffe), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(message.reply_to.text ?: "Вложение", color = textColor, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                            val hasImage = !message.image_thumb.isNullOrEmpty()
                            val hasAudio = !message.audio_stream.isNullOrEmpty() || !message.audio_master.isNullOrEmpty()
                            val hasText = !message.text.isNullOrEmpty()
                            val isOnlyImage = hasImage && !hasAudio && !hasText

                            if (hasImage) {
                                val isGifResource = message.image_thumb?.endsWith(".gif") == true || message.gif_url != null
                                val imageUrl = message.gif_url ?: message.image_thumb

                                // ✨ Обернули в Box, чтобы наложить время
                                Box {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(imageUrl)
                                            .crossfade(false)
                                            .memoryCacheKey(imageUrl)
                                            .diskCacheKey(imageUrl)
                                            .apply {
                                                if (isGifResource) {
                                                    decoderFactory(if (Build.VERSION.SDK_INT >= 28) ImageDecoderDecoder.Factory() else GifDecoder.Factory())
                                                } else {
                                                    size(coil.size.Size.ORIGINAL)
                                                }
                                            }
                                            .build(),
                                        contentDescription = "Изображение",
                                        modifier = Modifier
                                            .wrapContentWidth()
                                            .heightIn(max = maxHeight)
                                            .clip(RoundedCornerShape(4.dp))
                                            .onGloballyPositioned { coordinates -> imageBounds = coordinates.boundsInWindow() }
                                            .clickable { onImageClick(message, imageBounds) },
                                        contentScale = ContentScale.Fit
                                    )

                                    // ✨ ВРЕМЯ НАД КАРТИНКОЙ (Если нет текста)
                                    if (isOnlyImage && showTime) {
                                        Row(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(6.dp)
                                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (message.is_edited == true) {
                                                Text("изм. ", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, modifier = Modifier.padding(end = 2.dp))
                                            }
                                            Text(formatMessageTime(message.time), color = Color.White, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                            if (hasAudio) {
                                AudioPlayerBubble(message = message, isMe = isMe)
                            }
                            if (hasText) {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(text = message.text!!, color = textColor, fontSize = 16.sp, lineHeight = 20.sp)
                                }
                            }
                            if (!isOnlyImage && showTime) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentWidth(Alignment.End)
                                        .padding(bottom = 6.dp, end = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (message.is_edited == true) {
                                        Text("изм. ", color = textColor.copy(alpha = 0.5f), fontSize = 10.sp, modifier = Modifier.padding(end = 4.dp))
                                    }
                                    Text(formatMessageTime(message.time), color = textColor.copy(alpha = 0.5f), fontSize = 10.sp)
                                }
                            }
                            androidx.compose.animation.AnimatedVisibility(
                                visible = isExpanded,
                                modifier = Modifier.align(Alignment.End),
                                enter = androidx.compose.animation.fadeIn() +
                                        androidx.compose.animation.expandIn(expandFrom = Alignment.TopCenter),
                                exit = androidx.compose.animation.fadeOut() +
                                        androidx.compose.animation.shrinkOut(shrinkTowards = Alignment.TopCenter)
                            ) {
                                Row(
                                    modifier = Modifier.padding(top = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    actions.forEach { action ->
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White.copy(alpha = 0.15f)) // Стеклянные кнопочки
                                                .border(
                                                    0.5.dp,
                                                    Color.White.copy(alpha = 0.2f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { action.onClick() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(action.iconRes),
                                                contentDescription = action.label,
                                                tint = Color(0xFFfffffe),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPreviewScreen(
    uri: Uri,
    initialBounds: Rect?,
    onClose: () -> Unit,
    onSend: (Uri, String) -> Unit // Передаем обратно файл и введенный текст
) {
    val scope = rememberCoroutineScope()
    val backgroundAlpha = remember { Animatable(0f) }
    var captionText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        backgroundAlpha.animateTo(0.95f, TweenSpec(300))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Слой фона
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = backgroundAlpha.value }
                .background(GlassBackground2)
        )

        // 2. Картинка с анимацией вылета
        DismissibleImage(
            model = uri,
            initialBounds = initialBounds,
            bgAlpha = backgroundAlpha,
            onClose = onClose
        )

        // 3. Верхняя панель (крестик)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding()
                .align(Alignment.TopCenter)
                .graphicsLayer { alpha = if (backgroundAlpha.value > 0.7f) backgroundAlpha.value else 0f },
        ) {
            IconButton(onClick = {
                scope.launch {
                    backgroundAlpha.animateTo(0f, TweenSpec(300))
                    onClose()
                }
            }) {
                Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = Color.White)
            }
        }

        // ✨ 4. СТЕКЛЯННАЯ ПАНЕЛЬ ВВОДА СНИЗУ (Как в чате)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .graphicsLayer { alpha = if (backgroundAlpha.value > 0.7f) backgroundAlpha.value else 0f }
                .navigationBarsPadding()
                .imePadding() // ✨ ИСПРАВЛЕНО: Теперь инпут едет вверх за клавиатурой!
                .padding(12.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(GlassBackground)
                .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    placeholder = { Text("Добавить подпись...", color = Color(0XFF72757e)) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF7f5af0).copy(alpha = 0.7f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        cursorColor = Color(0xFF7f5af0),
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        scope.launch {
                            backgroundAlpha.animateTo(0f, TweenSpec(200))
                            onSend(uri, captionText)
                        }
                    },
                    modifier = Modifier.clip(CircleShape).background(Color(0xFF7f5af0))
                ) {
                    Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_send), contentDescription = "Отправить", tint = Color(0xFFfffffe))
                }
            }
        }
    }
}
@Composable
fun InlineMediaGallery(
    onMediaSelected: (Uri, Rect?) -> Unit,
    onOpenFileManager: () -> Unit
) {
    val context = LocalContext.current
    var mediaItems by remember { mutableStateOf<List<MediaItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.Dispatchers.IO.invoke {
            mediaItems = getRecentMedia(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp) // ✨ Высота примерно равна высоте клавиатуры
            .padding(top = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onOpenFileManager) {
                Text("", color = Color(0xFF38BDF8), fontSize = 14.sp)
            }
        }

        if (mediaItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF38BDF8))
            }
        } else {
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(mediaItems.size) { index ->
                    val item = mediaItems[index]
                    var bounds by remember { mutableStateOf<Rect?>(null) }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .onGloballyPositioned { bounds = it.boundsInWindow() }
                            .clickable { onMediaSelected(item.uri, bounds) } // ✨ НЕ закрываем галерею при клике!
                    ) {
                        AsyncImage(model = item.uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        if (item.isVideo) {
                            Row(
                                modifier = Modifier.align(Alignment.BottomStart).padding(4.dp).clip(RoundedCornerShape(4.dp)).background(Color.Black.copy(alpha = 0.5f)).padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_video), contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(item.durationStr ?: "00:00", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ImageViewerScreen(
    messages: List<Message>,
    initialMessageId: Int,
    initialBounds: Rect?,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val imageMessages = remember(messages) { messages.filter { it.image_view != null } }
    val initialPage = remember { imageMessages.indexOfFirst { it.id == initialMessageId }.coerceAtLeast(0) }

    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { imageMessages.size })
    var menuExpanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val backgroundAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        backgroundAlpha.animateTo(0.95f, TweenSpec(300))
    }

    // ✨ ГЛАВНЫЙ КОНТЕЙНЕР (убрали отсюда graphicsLayer, теперь он не тушит картинку!)
    Box(modifier = Modifier.fillMaxSize()) {

        // ✨ ОТДЕЛЬНЫЙ СЛОЙ ФОНА (Вот он прозрачный и быстрый)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = backgroundAlpha.value }
                .background(GlassBackground2)
        )

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val msg = imageMessages[page]

            DismissibleImage(
                model = msg.image_view ?: "",
                initialBounds = if (msg.id == initialMessageId) initialBounds else null,
                bgAlpha = backgroundAlpha,
                onClose = onClose
            )
        }

        // Верхняя панелька (крестик, счетчик, меню)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding()
                .align(Alignment.TopCenter)
                // ✨ Панелька плавно исчезает вместе с черным фоном
                .graphicsLayer {
                    alpha = if (backgroundAlpha.value > 0.7f) backgroundAlpha.value else 0f
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                scope.launch {
                    backgroundAlpha.animateTo(0f, TweenSpec(300))
                    onClose()
                }
            }) {
                Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = Color.White)
            }

            Text("${pagerState.currentPage + 1} из ${imageMessages.size}", color = Color.White, style = MaterialTheme.typography.titleMedium)

            Box {
                IconButton(onClick = { menuExpanded = true }) { Icon(Icons.Default.MoreVert, contentDescription = "Меню", tint = Color.White) }
                val currentMsg = imageMessages.getOrNull(pagerState.currentPage)
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }, modifier = Modifier.background(Color(0xFF1E293B))) {
                    DropdownMenuItem(text = { Text("Сохранить (1200x)", color = Color.White) }, onClick = { menuExpanded = false; currentMsg?.image_view?.let { downloadImage(context, it, "image_${currentMsg.id}.webp") } })
                    DropdownMenuItem(text = { Text("Сохранить оригинал", color = Color.White) }, onClick = { menuExpanded = false; currentMsg?.image_master?.let { downloadImage(context, it, "original_${currentMsg.id}.jpg") } })
                }
            }
        }
    }
}

@Composable
fun DismissibleImage(
    model: Any,
    initialBounds: Rect?,
    bgAlpha: Animatable<Float, androidx.compose.animation.core.AnimationVector1D>,
    onClose: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }
    val dismissThreshold = screenHeight * 0.15f

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val dismissTranslationY = remember { Animatable(0f) }

    // ✨ 1. ТОЧНАЯ МАТЕМАТИКА РАЗМЕРОВ
    // Вычисляем реальный размер картинки на экране (чтобы отрезать углы именно у нее, а не у пустоты)
    val boxWidth: Float
    val boxHeight: Float
    val targetScale: Float
    val targetRadius: Float

    if (initialBounds != null) {
        val imageRatio = initialBounds.width / initialBounds.height
        val screenRatio = screenWidth / screenHeight

        if (imageRatio > screenRatio) {
            boxWidth = screenWidth
            boxHeight = screenWidth / imageRatio
        } else {
            boxHeight = screenHeight
            boxWidth = screenHeight * imageRatio
        }
        targetScale = initialBounds.width / boxWidth
        // Делаем углы "с запасом", чтобы при сжатии картинки они визуально превратились ровно в 12dp
        targetRadius = 4f / targetScale
    } else {
        boxWidth = screenWidth
        boxHeight = screenHeight
        targetScale = 1f
        targetRadius = 0f
    }

    val targetTranslateX = if (initialBounds != null) initialBounds.center.x - (screenWidth / 2f) else 0f
    val targetTranslateY = if (initialBounds != null) initialBounds.center.y - (screenHeight / 2f) else 0f

    val flyScale = remember { Animatable(if (initialBounds != null) targetScale else 1f) }
    val flyOffsetX = remember { Animatable(if (initialBounds != null) targetTranslateX else 0f) }
    val flyOffsetY = remember { Animatable(if (initialBounds != null) targetTranslateY else 0f) }
    val cornerRadius = remember { Animatable(if (initialBounds != null) targetRadius else 0f) }

    LaunchedEffect(Unit) {
        if (initialBounds != null) {
            launch { flyScale.animateTo(1f, TweenSpec(300)) }
            launch { flyOffsetX.animateTo(0f, TweenSpec(300)) }
            launch { flyOffsetY.animateTo(0f, TweenSpec(300)) }
            launch { cornerRadius.animateTo(0f, TweenSpec(300)) }
        }
    }

    // ✨ 2. РАЗДЕЛИЛИ СЛОИ: Внешний Box ловит жесты по всему экрану, а внутренний - обрезает углы
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitFirstDown(requireUnconsumed = false)
                        do {
                            val event = awaitPointerEvent()
                            val zoom = event.calculateZoom()
                            val pan = event.calculatePan()

                            if (scale > 1f) {
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                val maxX = (size.width * (scale - 1)) / 2
                                val maxY = (size.height * (scale - 1)) / 2
                                offsetX = (offsetX + pan.x * scale).coerceIn(-maxX, maxX)
                                offsetY = (offsetY + pan.y * scale).coerceIn(-maxY, maxY)
                                event.changes.forEach { it.consume() }
                            } else {
                                if (zoom != 1f) {
                                    scale = (scale * zoom).coerceIn(1f, 5f)
                                    event.changes.forEach { it.consume() }
                                } else if (abs(pan.y) > abs(pan.x) + 5f && event.changes.size == 1) {
                                    coroutineScope.launch {
                                        dismissTranslationY.snapTo(dismissTranslationY.value + pan.y)
                                        bgAlpha.snapTo(
                                            (1f - (abs(dismissTranslationY.value) / screenHeight * 1.5f)).coerceIn(
                                                0f,
                                                1f
                                            )
                                        )
                                    }
                                    event.changes.forEach { it.consume() }
                                }
                            }
                        } while (event.changes.any { it.pressed })

                        // Отпустили палец - решаем, что делать
                        if (scale == 1f) {
                            coroutineScope.launch {
                                if (abs(dismissTranslationY.value) > dismissThreshold) {
                                    if (initialBounds != null) {
                                        launch { bgAlpha.animateTo(0f, TweenSpec(300)) }
                                        launch { flyScale.animateTo(targetScale, TweenSpec(300)) }
                                        launch {
                                            flyOffsetX.animateTo(
                                                targetTranslateX,
                                                TweenSpec(300)
                                            )
                                        }
                                        launch {
                                            flyOffsetY.animateTo(
                                                targetTranslateY - dismissTranslationY.value,
                                                TweenSpec(300)
                                            )
                                        }
                                        launch {
                                            cornerRadius.animateTo(
                                                targetRadius,
                                                TweenSpec(300)
                                            )
                                        } // ✨ ЗАКРУГЛЯЕМ КРАЯ ИДЕАЛЬНО!

                                        kotlinx.coroutines.delay(300)
                                        onClose()
                                    } else {
                                        launch { bgAlpha.animateTo(0f, TweenSpec(200)) }
                                        launch {
                                            dismissTranslationY.animateTo(
                                                if (dismissTranslationY.value > 0) screenHeight else -screenHeight,
                                                TweenSpec(200)
                                            )
                                        }
                                        kotlinx.coroutines.delay(200)
                                        onClose()
                                    }
                                } else {
                                    launch { dismissTranslationY.animateTo(0f, TweenSpec(300)) }
                                    launch { bgAlpha.animateTo(1f, TweenSpec(300)) }
                                }
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // ✨ ВНУТРЕННИЙ БЛОК: Размером ровно с картинку
        Box(
            modifier = Modifier
                .size(
                    width = with(density) { boxWidth.toDp() },
                    height = with(density) { boxHeight.toDp() }
                )
                .graphicsLayer {
                    scaleX = scale * flyScale.value
                    scaleY = scale * flyScale.value
                    translationX = offsetX + flyOffsetX.value
                    translationY = offsetY + flyOffsetY.value + dismissTranslationY.value
                    clip = true // Обрезаем точно по краям
                    shape = RoundedCornerShape(cornerRadius.value.dp)
                }
        ) {


            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(model)
                    .apply {
                        decoderFactory(
                            if (Build.VERSION.SDK_INT >= 28) ImageDecoderDecoder.Factory()
                            else GifDecoder.Factory()
                        )
                    }
                    .build(),
                contentDescription = "Полная",
                contentScale = ContentScale.Fit, // Гарантирует, что внутри блока не будет ни одного пустого пикселя
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
// ✨ НОВОЕ: Функция для копирования текста (ссылки) в буфер обмена Android

fun downloadImage(context: Context, url: String, fileName: String) {
    try {
        val request = android.app.DownloadManager.Request(android.net.Uri.parse(url))
            .setTitle("Скачивание картинки")
            .setDescription("Загрузка из Sonzaiigi")
            .setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_PICTURES, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
        dm.enqueue(request)
        Toast.makeText(context, "Скачивание началось...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Ошибка скачивания: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}


class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    var outputFile: File? = null
        private set

    fun startRecording() {
        outputFile = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile?.absolutePath)
            try {
                prepare()
                start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopRecording() {
        try {
            recorder?.stop()
            recorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            recorder = null
        }
    }

    fun cancelRecording() {
        stopRecording()
        outputFile?.delete()
        outputFile = null
    }
}