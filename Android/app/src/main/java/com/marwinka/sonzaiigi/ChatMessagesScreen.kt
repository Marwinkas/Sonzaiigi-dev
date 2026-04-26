    package com.marwinka.sonzaiigi
    import androidx.activity.compose.BackHandler
    import android.Manifest
    import android.app.DownloadManager
    import android.content.BroadcastReceiver
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
    import kotlin.math.abs
    import androidx.compose.ui.geometry.Rect
    import androidx.compose.ui.layout.boundsInWindow
    import androidx.compose.ui.layout.onGloballyPositioned
    import androidx.compose.foundation.gestures.detectTapGestures
    import androidx.compose.foundation.lazy.rememberLazyListState
    import androidx.compose.material.icons.filled.Create
    import androidx.compose.material.icons.filled.Delete
    import androidx.compose.material.icons.filled.Edit
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.draw.alpha
    import androidx.compose.ui.graphics.Brush
    import androidx.compose.ui.layout.layout
    import androidx.compose.ui.platform.LocalClipboardManager
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.text.style.TextAlign
    import androidx.compose.ui.window.Dialog
    import okhttp3.MediaType.Companion.toMediaTypeOrNull
    import okhttp3.MultipartBody
    import okhttp3.RequestBody.Companion.toRequestBody
    import java.util.UUID
    
    import android.provider.MediaStore
    import android.content.ContentUris
    import android.content.Intent
    import android.content.IntentFilter
    import android.media.MediaPlayer
    import android.media.MediaRecorder
    import android.os.Build
    import android.webkit.MimeTypeMap
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.compose.animation.core.VisibilityThreshold
    import androidx.compose.foundation.interaction.MutableInteractionSource
    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.lazy.itemsIndexed
    import androidx.compose.material.icons.filled.KeyboardArrowUp
    import androidx.compose.material.icons.filled.Lock
    import androidx.compose.ui.focus.focusRequester
    import androidx.compose.ui.focus.onFocusChanged
    import androidx.compose.ui.geometry.Offset
    import androidx.compose.ui.graphics.BlendMode
    import androidx.compose.ui.graphics.ColorFilter
    import androidx.compose.ui.unit.IntSize
    import kotlinx.coroutines.invoke
    import coil.decode.ImageDecoderDecoder
    import coil.decode.GifDecoder
    import coil.request.ImageRequest
    import coil.request.videoFrameMicros
    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.Job
    import kotlinx.coroutines.delay
    import kotlinx.coroutines.isActive
    import kotlinx.coroutines.withContext
    import java.io.File
    import java.text.SimpleDateFormat
    import java.util.Locale
    import java.util.TimeZone
    import android.graphics.Bitmap
    import android.graphics.BitmapFactory
    import android.graphics.Paint
    import android.graphics.Typeface
    import android.os.Environment
    import android.util.Log
    import android.util.Patterns
    import androidx.compose.animation.expandIn
    import androidx.compose.animation.fadeIn
    import androidx.compose.animation.fadeOut
    import androidx.compose.animation.scaleIn
    import androidx.compose.animation.scaleOut
    import androidx.compose.animation.shrinkOut
    import androidx.compose.animation.slideInHorizontally
    import androidx.compose.animation.slideOutHorizontally
    import androidx.compose.foundation.BorderStroke
    import androidx.compose.foundation.combinedClickable
    import androidx.compose.foundation.gestures.calculateRotation
    import androidx.compose.foundation.gestures.detectDragGestures
    import androidx.compose.foundation.gestures.detectVerticalDragGestures
    import androidx.compose.foundation.lazy.LazyListState
    import androidx.compose.foundation.lazy.LazyRow
    import androidx.compose.foundation.text.ClickableText
    import androidx.compose.material.icons.filled.Add
    import androidx.compose.material.icons.filled.ArrowBack
    import androidx.compose.material.icons.filled.Check
    import androidx.compose.material.icons.filled.KeyboardArrowDown
    import androidx.compose.material.icons.filled.Menu
    import androidx.compose.material.icons.filled.Settings
    import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
    import androidx.compose.ui.graphics.asImageBitmap
    import androidx.compose.ui.graphics.nativeCanvas
    import androidx.compose.ui.platform.LocalUriHandler
    import androidx.compose.ui.text.AnnotatedString
    import androidx.compose.ui.text.SpanStyle
    import androidx.compose.ui.text.buildAnnotatedString
    import androidx.compose.ui.text.input.TextFieldValue
    import androidx.compose.ui.text.style.TextDecoration
    import androidx.compose.ui.text.withStyle
    import androidx.compose.ui.window.DialogProperties
    import androidx.core.content.ContextCompat
    import androidx.lifecycle.Lifecycle
    import androidx.lifecycle.LifecycleEventObserver
    import androidx.lifecycle.compose.LocalLifecycleOwner
    import com.google.android.datatransport.BuildConfig
    import kotlinx.coroutines.coroutineScope
    import kotlinx.coroutines.flow.collectLatest
    import okhttp3.OkHttpClient
    import okhttp3.Request
    import org.json.JSONObject
    import java.io.FileOutputStream
    import java.util.Calendar
    
    fun formatFileSize(sizeInBytes: Long?): String {
        if (sizeInBytes == null || sizeInBytes == 0L) return ""
        val kb = sizeInBytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
            kb >= 1.0 -> String.format(Locale.US, "%.0f KB", kb)
            else -> "$sizeInBytes B"
        }
    }
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
    
    enum class MediaType { IMAGE, VIDEO, AUDIO }
    
    data class MediaItem(
        val uri: Uri,
        val type: MediaType, // Теперь файл знает, кто он такой
        val durationStr: String? = null,
        val dateAdded: Long,
        val name: String? = null // Добавили имя, чтобы подписывать треки
    )
    
    fun getRecentMedia(context: Context, limit: Int = 200): List<MediaItem> {
        val mediaList = mutableListOf<MediaItem>()
        val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC"
    
        // ✨ ВОТ ОНО! Условие: файл должен весить больше 0 байт (исключаем "призраков" и битые файлы)
        val selection = "${MediaStore.MediaColumns.SIZE} > 0"
    
        val imageUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val videoUri = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val audioUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    
        // Запрос картинок
        context.contentResolver.query(imageUri, arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED), selection, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            while (cursor.moveToNext() && mediaList.size < limit) {
                val id = cursor.getLong(idCol)
                val date = cursor.getLong(dateCol)
                val uri = ContentUris.withAppendedId(imageUri, id)
                mediaList.add(MediaItem(uri, MediaType.IMAGE, dateAdded = date))
            }
        }
    
        // Запрос видео
        context.contentResolver.query(videoUri, arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DATE_ADDED, MediaStore.Video.Media.DURATION), selection, null, sortOrder)?.use { cursor ->
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
                mediaList.add(MediaItem(uri, MediaType.VIDEO, durationStr = durStr, dateAdded = date))
            }
        }
    
        // Запрос музыки
        context.contentResolver.query(audioUri, arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATE_ADDED, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DISPLAY_NAME), selection, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            while (cursor.moveToNext() && mediaList.size < limit) {
                val id = cursor.getLong(idCol)
                val date = cursor.getLong(dateCol)
                val durationMs = cursor.getLong(durCol)
                val name = cursor.getString(nameCol)
                val uri = ContentUris.withAppendedId(audioUri, id)
                val totalSeconds = durationMs / 1000
                val durStr = String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60)
                mediaList.add(MediaItem(uri, MediaType.AUDIO, durationStr = durStr, dateAdded = date, name = name))
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
                                if (item.type == MediaType.VIDEO) {
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
    // ✨ ФИЛЬТРЫ МЕДИА ДЛЯ ПОИСКА
    enum class SearchMediaType { ALL, IMAGE, VIDEO, AUDIO, FILE, LINK }
    
    // ✨ ФУНКЦИЯ ФОРМАТИРОВАНИЯ ДАТЫ (Сегодня, Вчера, 12 августа)
    fun getMessageDateSeparator(timeString: String?): String {
        if (timeString.isNullOrEmpty()) return ""
        return try {
            val cleanString = timeString.substringBefore(".").substringBefore("Z") + "Z"
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val msgDate = parser.parse(cleanString) ?: return ""
    
            val msgCal = Calendar.getInstance().apply { time = msgDate }
            val nowCal = Calendar.getInstance()
    
            val isCurrentYear = msgCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
    
            // Сбрасываем часы для точного сравнения дней
            msgCal.set(Calendar.HOUR_OF_DAY, 0); msgCal.set(Calendar.MINUTE, 0); msgCal.set(Calendar.SECOND, 0)
            val todayCal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }
    
            val diffDays = (todayCal.timeInMillis - msgCal.timeInMillis) / (1000 * 60 * 60 * 24)
    
            when (diffDays) {
                0L -> "Сегодня"
                1L -> "Вчера"
                else -> {
                    val formatPattern = if (isCurrentYear) "d MMMM" else "d MMMM yyyy"
                    SimpleDateFormat(formatPattern, Locale("ru")).format(msgDate)
                }
            }
        } catch (e: Exception) { "" }
    }
    
    // ✨ КОМПОНЕНТ ДЛЯ КЛИКАБЕЛЬНЫХ ССЫЛОК
    @Composable
    fun MessageTextWithLinks(
        text: String,
        textColor: Color,
        modifier: Modifier = Modifier,
        onJoinGroup: ((String) -> Unit)? = null, // ✨ Новый параметр для перехвата
        onNormalClick: () -> Unit = {},
    ) {
        val uriHandler = LocalUriHandler.current
    
        val annotatedString = buildAnnotatedString {
            val matcher = Patterns.WEB_URL.matcher(text)
            var lastIndex = 0
    
            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                val url = text.substring(start, end)
    
                append(text.substring(lastIndex, start))
                pushStringAnnotation(tag = "URL", annotation = url)
                withStyle(style = SpanStyle(color = Color(0xFF38BDF8), textDecoration = TextDecoration.Underline)) {
                    append(url)
                }
                pop()
                lastIndex = end
            }
            append(text.substring(lastIndex))
        }
    
        ClickableText(
            text = annotatedString,
            style = LocalTextStyle.current.copy(color = textColor, fontSize = 16.sp, lineHeight = 20.sp),
            modifier = modifier,
            onClick = {
                offset ->
                val annotations = annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)

                        if (annotations.isEmpty()) {
                            // ✨ Если нажали НЕ на ссылку, вызываем открытие меню
                            onNormalClick()
                        }
                        else {
                            // Логика открытия ссылки (остается прежней)
                            val url = annotations.first().item
                            if (url.contains("sonzaiigi.com/join/") && onJoinGroup != null) {
                                val token =
                                    url.substringAfter("join/").substringBefore(" ").trim('/', ' ')
                                onJoinGroup(token)
                            } else {
                                try {
                                    var finalUrl = url
                                    if (!finalUrl.startsWith("http")) finalUrl = "https://$finalUrl"
                                    uriHandler.openUri(finalUrl)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

            }
        )
    }
    fun downloadFile(context: Context, url: String, fileName: String) {
        try {
            val request = android.app.DownloadManager.Request(android.net.Uri.parse(url))
                .setTitle(fileName)
                .setDescription("Загрузка файла...")
                .setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
    
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
            dm.enqueue(request)
            Toast.makeText(context, "Скачивание началось...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка скачивания", Toast.LENGTH_SHORT).show()
        }
    }

    object ApkUpdater {
        private val client = okhttp3.OkHttpClient()

        fun downloadAndInstall(context: Context, apkUrl: String, version: String) {
            val scope = CoroutineScope(Dispatchers.IO)
            Toast.makeText(context, "Загрузка обновления v$version...", Toast.LENGTH_SHORT).show()

            scope.launch {
                try {
                    // 1. Скачиваем файл во внутренний кэш
                    val request = okhttp3.Request.Builder().url(apkUrl).build()
                    val response = client.newCall(request).execute()

                    if (!response.isSuccessful) throw Exception("Ошибка сервера: ${response.code}")

                    val apkFile = File(context.cacheDir, "update.apk")

                    // Если старый файл остался — удаляем
                    if (apkFile.exists()) apkFile.delete()

                    response.body?.byteStream()?.use { input ->
                        apkFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    // 2. Когда скачивание завершено — запускаем установку
                    withContext(Dispatchers.Main) {
                        installApk(context, apkFile)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Ошибка при обновлении: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        private fun installApk(context: Context, file: File) {
            // Получаем безопасный URI через FileProvider
            val contentUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("APK_INSTALL", "Error: ${e.message}")
                Toast.makeText(context, "Не удалось запустить установку", Toast.LENGTH_LONG).show()
            }
        }
    }
    @Composable
    fun ReactionChip(
        reaction: ReactionInfo,
        onClick: () -> Unit
    ) {
        Box(
            modifier = Modifier
                .padding(end = 4.dp, bottom = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (reaction.reacted_by_me) Color(0xFF38BDF8).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                .border(
                    width = 1.dp,
                    color = if (reaction.reacted_by_me) Color(0xFF38BDF8).copy(alpha = 0.5f) else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { onClick() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = reaction.emoji, fontSize = 14.sp)
                if (reaction.count > 1) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = reaction.count.toString(),
                        color = if (reaction.reacted_by_me) Color(0xFF38BDF8) else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChatMessagesScreen(
        chat: Chat,
        onBack: () -> Unit,
        onImageClick: (List<Message>, Int, Rect?) -> Unit,
        externalReply: Message? = null,
        onReplyHandled: () -> Unit = {},
        externalForward: Message? = null,
        onForwardHandled: () -> Unit = {},
        externalScrollId: Int? = null,
        onScrollHandled: () -> Unit = {},
    ) {
        val api = remember { ApiService.create() }
        val coroutineScope = rememberCoroutineScope()
        val listState = rememberLazyListState()
        var isScreenActive by remember { mutableStateOf(true) }
        var canLoadMoreUp by remember { mutableStateOf(true) }      // Можно ли грузить историю
        var canLoadMoreDown by remember { mutableStateOf(false) }  // Можно ли грузить новые (true только в режиме контекста)
        var isLoadingMoreDown by remember { mutableStateOf(false) }
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(Unit) {
            ActiveChatManager.currentChatId = chat.id
            onDispose {
                // Когда уходим в список чатов (или закрываем окно), сбрасываем ID
                if (ActiveChatManager.currentChatId == chat.id) {
                    ActiveChatManager.currentChatId = null
                }
            }
        }

        var highlightedMessageId by remember { mutableStateOf<Int?>(null) }
        var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
        var inputText by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(true) }
        var isLoadingMore by remember { mutableStateOf(false) }
        var canLoadMore by remember { mutableStateOf(true) } // Фл
        var menuExpanded by remember { mutableStateOf(false) }
        var isMuted by remember { mutableStateOf(false) }
    
        var replyToMessage by remember { mutableStateOf<Message?>(null) }
        var editingMessage by remember { mutableStateOf<Message?>(null) }
    
        var deletingMessageId by remember { mutableStateOf<Int?>(null) }
    
        // ✨ НОВОЕ: Список сообщений, которые сейчас находятся в процессе плавного исчезновения
        var fadingOutMessages by remember { mutableStateOf<Set<Int>>(emptySet()) }
    
        val clipboardManager = LocalClipboardManager.current
        var dragYOffset by remember { mutableFloatStateOf(0f) }
    
    
        val context = LocalContext.current
        var isForwardModalOpen by remember { mutableStateOf(false) }
        var forwardMessageId by remember { mutableStateOf<Int?>(null) }
        var selectedChatsForForward by remember { mutableStateOf<Set<Int>>(emptySet()) }
        var includeAuthor by remember { mutableStateOf(true) }
        var availableChats by remember { mutableStateOf<List<Chat>>(emptyList()) }
        var showMediaPicker by remember { mutableStateOf(false) }
        val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
        val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
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
        var showGroupProfile by remember { mutableStateOf(false) }
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
        val shouldLoadMore by remember {
            derivedStateOf {
                val totalItems = listState.layoutInfo.totalItemsCount
                val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
    
                // Если мы пролистали почти до верха (осталось 5 сообщений) и элементы вообще есть
                totalItems > 0 && lastVisibleItem >= totalItems - 5
            }
        }
    
    // ✨ Сам процесс подгрузки
        LaunchedEffect(shouldLoadMore, canLoadMore, isLoadingMore) {
            if (shouldLoadMore && canLoadMore && !isLoadingMore) {
                isLoadingMore = true
                try {
                    // Передаем текущее количество сообщений как отступ (offset)
                    // Чтобы пропустить те 20, что мы уже загрузили
                    val response = api.getMessages(chat.id, offset = messages.size)
                    val olderMessages = response.messages
    
                    // Если пришло меньше 20 или вообще пусто, значит история закончилась
                    if (olderMessages.isEmpty() || olderMessages.size < 20) {
                        canLoadMore = false
                    }
    
                    if (olderMessages.isNotEmpty()) {
                        // Добавляем старые сообщения в НАЧАЛО нашего списка
                        // Оставляем только уникальные по id, чтобы избежать дублей на стыке
                        val combined = olderMessages + messages
                        messages = combined.distinctBy { it.id }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoadingMore = false
                }
            }
        }
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

                // Подтягиваем мут
                if (chat.is_group) {
                    val gInfo = api.getGroupInfo(chat.id)
                    isMuted = gInfo.isMuted
                } else {
                    val otherId = messages.firstOrNull { it.senderId != AuthManager.userId }?.senderId
                    if (otherId != null) {
                        val rel = api.getUserRelations(otherId)
                        isMuted = rel.is_muted
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // ✨ 1. СНАЧАЛА выключаем загрузку, чтобы LazyColumn появился на экране
                isLoading = false
            }

            // ✨ 2. ТОЛЬКО ПОТОМ запускаем вычисление и скролл
            val oldestUnreadMsg = messages.firstOrNull { it.senderId != AuthManager.userId && !it.is_read }
            if (oldestUnreadMsg != null) {
                val targetIndex = messages.reversed().indexOfFirst { it.id == oldestUnreadMsg.id }
                if (targetIndex != -1) {
                    // Запускаем асинхронно, чтобы не блокировать UI
                    coroutineScope.launch {
                        // Даем Compose 100 миллисекунд, чтобы он успел отрисовать элементы списка
                        kotlinx.coroutines.delay(100)
                        listState.scrollToItem(targetIndex)
                    }
                }
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

                            val attachmentsArray = msgObj.optJSONArray("attachments")
                            val parsedAttachments = mutableListOf<Attachment>()
                            if (attachmentsArray != null) {
                                for (i in 0 until attachmentsArray.length()) {
                                    val attObj = attachmentsArray.getJSONObject(i)
                                    parsedAttachments.add(
                                        Attachment(
                                            type = attObj.optString("type", ""),
                                            url = attObj.optString("url", ""),
                                            thumb = attObj.optString("thumb", null).takeIf { it != "null" },
                                            name = attObj.optString("name", null).takeIf { it != "null" },
                                            size = attObj.optString("size", null).takeIf { it != "null" },
                                            duration = attObj.optString("duration", null).takeIf { it != "null" },
                                            extra_info = attObj.optString("extra_info", null).takeIf { it != "null" },
                                            height = attObj.optInt("height"),
                                            width = attObj.optInt("width")
                                        )
                                    )
                                }
                            }

                            val replyToObj = msgObj.optJSONObject("reply_to")
                            val parsedReplyTo = if (replyToObj != null) {
                                ReplyToInfo(
                                    id = replyToObj.optInt("id", 0),
                                    name = replyToObj.optString("name", "Кто-то"),
                                    text = replyToObj.optString("text", "Вложение"),
                                    // Читаем медиа из ответа:
                                    image_thumb = replyToObj.optString("image_thumb", null).takeIf { it != "null" },
                                    video_master = replyToObj.optString("video_master", null).takeIf { it != "null" },
                                    audio_title = replyToObj.optString("audio_title", null).takeIf { it != "null" },
                                    audio_stream = replyToObj.optString("audio_stream", null).takeIf { it != "null" },
                                    audio_master = replyToObj.optString("audio_master", null).takeIf { it != "null" }
                                )
                            } else null
    
                            val fwdFrom = msgObj.optString("forwarded_from", "").takeIf { it.isNotBlank() && it != "null" }
    
                            val newMsg = Message(
                                id = msgObj.getInt("id"),
                                text = msgObj.getNullableString("text"),
                                senderId = msgObj.getInt("senderId"),
                                senderName = msgObj.optString("senderName", "Кто-то"),
                                senderAvatar = msgObj.getNullableString("senderAvatar")?.toFullUrl(),
                                time = msgObj.optString("time", ""),
                                image_thumb = msgObj.getNullableString("image_thumb")?.toFullUrl(),
                                image_view = msgObj.getNullableString("image_view")?.toFullUrl(),
                                image_master = msgObj.getNullableString("image_master")?.toFullUrl(),
                                video_master = msgObj.getNullableString("video_master")?.toFullUrl(),
    
                                // ✨ ВОТ ОНИ! Добавляем имя и размер документа
                                document_url = msgObj.getNullableString("document_url")?.toFullUrl(),
                                document_name = msgObj.getNullableString("document_name"),
                                document_size = msgObj.getNullableString("document_size"),
    
                                audio_stream = msgObj.getNullableString("audio_stream")?.toFullUrl(),
                                audio_master = msgObj.getNullableString("audio_master")?.toFullUrl(),
                                is_edited = msgObj.optBoolean("is_edited", false),
                                gif_url = msgObj.getNullableString("gif_url"),
                                forwarded_from = fwdFrom,
                                reply_to = parsedReplyTo,
    
                                audio_title = msgObj.getNullableString("audio_title"),
                                audio_duration = msgObj.getNullableString("audio_duration"),
                                audio_cover = msgObj.getNullableString("audio_cover")?.toFullUrl(),
                                audio_artist = msgObj.getNullableString("audio_artist"),
                                attachments = parsedAttachments
                            )

                            if (!messages.any { it.id == newMsg.id }) {
                                messages = messages + newMsg

                                // ✨ ИСПРАВЛЕНИЕ ТЕЛЕПОРТА ✨
                                // 1. Проверяем, смотрим ли мы сейчас в самый низ чата (индекс 0, 1 или 2)
                                val isAtBottom = listState.firstVisibleItemIndex <= 2
                                // 2. Проверяем, наше ли это сообщение
                                val isMyMessage = newMsg.senderId == AuthManager.userId

                                // Прыгаем вниз ТОЛЬКО если мы внизу ИЛИ если мы сами это написали
                                if (isMyMessage || isAtBottom) {
                                    coroutineScope.launch {
                                        kotlinx.coroutines.delay(100)
                                        listState.animateScrollToItem(0)
                                    }
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
                        "messages_read" -> {
                            val readerId = json.getInt("reader_id")
                            val maxId = json.getInt("max_id")

                            messages = messages.map {
                                // Если кто-то другой прочитал мое сообщение, ИЛИ я прочитал чужое
                                if ((readerId != AuthManager.userId && it.senderId == AuthManager.userId && it.id <= maxId) ||
                                    (readerId == AuthManager.userId && it.senderId != AuthManager.userId && it.id <= maxId)) {
                                    it.copy(is_read = true)
                                } else it
                            }
                        }
                        "reaction_update" -> {
                            val msgId = json.getInt("message_id")
                            val reactionsArray = json.getJSONArray("reactions")

                            // Парсим массив реакций из JSON
                            val updatedReactions = mutableListOf<ReactionInfo>()
                            for (i in 0 until reactionsArray.length()) {
                                val obj = reactionsArray.getJSONObject(i)
                                updatedReactions.add(ReactionInfo(
                                    emoji = obj.getString("emoji"),
                                    count = obj.getInt("count"),
                                    // Проверяем, есть ли наш ID в списке проголосовавших (приходит из C#)
                                    reacted_by_me = obj.getJSONArray("userIds").let { ids ->
                                        var found = false
                                        for (j in 0 until ids.length()) { if (ids.getInt(j) == AuthManager.userId) found = true }
                                        found
                                    }
                                ))
                            }

                            // Обновляем состояние сообщений
                            messages = messages.map {
                                if (it.id == msgId) it.copy(reactions = updatedReactions) else it
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
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager


        // Таймер для голосовых
        LaunchedEffect(isRecording) {
            recordDuration = 0
            while (isRecording) {
                delay(1000)
                recordDuration++
            }
        }
        BackHandler {
            if(showMediaPicker){
                showMediaPicker = false;
            }
            else {
            onBack()
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

    
    // ✨ СТЕЙТЫ ПОИСКА
        var isSearchMode by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        var searchUserId by remember { mutableStateOf<Int?>(null) }
        var searchMediaType by remember { mutableStateOf(SearchMediaType.ALL) } // ✨ НОВЫЙ ФИЛЬТР
        // ✨ СТЕЙТЫ ПРОФИЛЯ
        var showUserProfileId by remember { mutableStateOf<Int?>(null) }
        val displayedMessages = remember(messages, searchQuery, searchUserId, isSearchMode, searchMediaType) {
            if (!isSearchMode) messages.reversed()
            else {
                messages.reversed().filter { msg ->
                    val matchesText = if (searchQuery.isBlank()) true else msg.text?.contains(searchQuery, ignoreCase = true) == true
                    val matchesUser = if (searchUserId == null) true else msg.senderId == searchUserId
    
                    val matchesMedia = when (searchMediaType) {
                        SearchMediaType.ALL -> true
                        SearchMediaType.IMAGE -> msg.image_thumb != null || msg.image_master != null
                        SearchMediaType.VIDEO -> msg.video_master != null
                        SearchMediaType.AUDIO -> msg.audio_stream != null || msg.audio_master != null
                        // Если есть ссылки на файлы, проверяем их
                        SearchMediaType.FILE -> msg.document_url != null || msg.document_size != null
                        SearchMediaType.LINK -> msg.text?.contains(Regex("https?://")) == true
                    }
    
                    matchesText && matchesUser && matchesMedia
                }
            }
        }
    
        // ✨ ДОСТАЕМ УНИКАЛЬНЫХ ПОЛЬЗОВАТЕЛЕЙ ДЛЯ ФИЛЬТРА В ГРУППЕ
        val chatUsers = remember(messages) {
            messages.map { it.senderId to it.senderAvatar }.distinctBy { it.first }
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
                        // ✨ УПАКОВЫВАЕМ ДАННЫЕ В МАССИВЫ ДЛЯ C#
                        val idsPart = uploadedFileId?.let { listOf(MultipartBody.Part.createFormData("uploaded_file_ids", it)) }
                        val namesPart = fName?.let { listOf(MultipartBody.Part.createFormData("original_file_names", it)) }
                        val mimePart = mime?.let { listOf(MultipartBody.Part.createFormData("content_types", it)) }

                        val sentMsg = api.sendMessage(
                            conversationId = chat.id,
                            text = textToSend.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull()),
                            parentId = currentReplyToId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
                            uploadedFileIds = idsPart,
                            originalFileNames = namesPart,
                            contentTypes = mimePart
                        )

                        if (!messages.any { it.id == sentMsg.id }) {
                            messages = messages + sentMsg
                        }
                        listState.animateScrollToItem(0)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isUploading = false
                }
            }
        }
        LaunchedEffect(externalReply) {
            if (externalReply != null) {
                // Закрываем все профили, если они открыты
                showGroupProfile = false
                showUserProfileId = null
    
                // Устанавливаем сообщение для ответа
                replyToMessage = externalReply
    
                // Сообщаем MainActivity, что всё ок, можно обнулять сигнал
                onReplyHandled()
            }
        }
    
        // ✨ 2. СЛУШАЕМ СИГНАЛ НА ПЕРЕСЫЛКУ
        LaunchedEffect(externalForward) {
            if (externalForward != null) {
                showGroupProfile = false
                showUserProfileId = null
    
                // Открываем модалку пересылки
                forwardMessageId = externalForward.id
                selectedChatsForForward = emptySet()
                isForwardModalOpen = true
    
                onForwardHandled()
            }
        }
    
        // ✨ 3. СЛУШАЕМ СИГНАЛ НА ТЕЛЕПОРТ (ПЕРЕХОД К СООБЩЕНИЮ)
        LaunchedEffect(externalScrollId) {
            if (externalScrollId != null) {
                showGroupProfile = false
                showUserProfileId = null
    
                val targetId = externalScrollId
                val index = messages.indexOfFirst { it.id == targetId }
    
                if (index != -1) {
                    // Вычисляем индекс в реверсивном списке
                    val reversedIndex = messages.size - 1 - index
                    listState.animateScrollToItem(reversedIndex)
    
                    // Подсвечиваем сообщение
                    highlightedMessageId = targetId
                    delay(2000)
                    highlightedMessageId = null
                }
                onScrollHandled()
            }
        }
        // Внутри ChatMessagesScreen, где-то после объявления listState
        // Следим за скроллом и отмечаем прочитанным
        // ✨ ИСПРАВЛЕННОЕ АВТОПРОЧТЕНИЕ
        // ✨ УМНОЕ АВТОПРОЧТЕНИЕ
        val visibleItemsInfo = listState.layoutInfo.visibleItemsInfo
        // ✨ БРОНЕБОЙНОЕ АВТОПРОЧТЕНИЕ (Как в Telegram)
        LaunchedEffect(listState) {
            // snapshotFlow непрерывно следит за тем, какие элементы сейчас на экране
            androidx.compose.runtime.snapshotFlow { listState.layoutInfo.visibleItemsInfo }
                .collectLatest { visibleItems ->
                    if (ActiveChatManager.currentChatId != chat.id) return@collectLatest

                    // 1. Достаем ID всех сообщений, которые сейчас видны на экране
                    val visibleMsgIds = visibleItems.mapNotNull { it.key as? Int }

                    // 2. Ищем среди них максимальный ID непрочитанного чужого сообщения.
                    // Берем данные напрямую из стейта messages, чтобы они всегда были на 100% свежими!
                    val maxVisibleUnreadId = messages
                        .filter { it.id in visibleMsgIds && it.senderId != AuthManager.userId && !it.is_read }
                        .maxOfOrNull { it.id }

                    if (maxVisibleUnreadId != null) {
                        // 3. Задержка полсекунды.
                        // collectLatest устроен так: если ты продолжаешь скроллить чат,
                        // он ОТМЕНИТ текущую паузу и начнет заново для новых элементов.
                        // Это полностью исключает спам запросами на сервер!
                        kotlinx.coroutines.delay(500)

                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            try {
                                // Сообщаем серверу
                                val res = api.markAsRead(chat.id, maxVisibleUnreadId)
                                if (res.isSuccessful) {
                                    notificationManager.cancel(maxVisibleUnreadId.toString().hashCode())

                                    // Мгновенно перерисовываем галочки у себя на экране
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        messages = messages.map {
                                            if (it.senderId != AuthManager.userId && it.id <= maxVisibleUnreadId && !it.is_read)
                                                it.copy(is_read = true)
                                            else it
                                        }
                                    }
                                }
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    }
                }
        }
        val isAtBottomForDownLoad = remember {
            derivedStateOf {
                listState.firstVisibleItemIndex < 2 // Мы у самого низа (начала списка)
            }
        }

        LaunchedEffect(isAtBottomForDownLoad.value, canLoadMoreDown) {
            if (isAtBottomForDownLoad.value && canLoadMoreDown && !isLoadingMoreDown) {
                isLoadingMoreDown = true
                try {
                    val lastMsgId = messages.lastOrNull()?.id ?: return@LaunchedEffect
                    val response = api.loadMoreDown(chat.id, lastMsgId)

                    if (response.messages.isNotEmpty()) {
                        messages = (messages + response.messages).distinctBy { it.id }
                        canLoadMoreDown = response.has_more_down
                    } else {
                        canLoadMoreDown = false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoadingMoreDown = false
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
                topBar = @androidx.compose.runtime.Composable {
                    val otherUserId = chat.user?.id ?: messages.firstOrNull { it.senderId != AuthManager.userId }?.senderId
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)).background(Color(0xFF242629)).border(1.dp, GlassBorder, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)).padding(bottom = 4.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
    
                            // ✨ РЕЖИМ ПОИСКА ИЛИ ОБЫЧНАЯ ШАПКА
                            if (isSearchMode) {
                                TopAppBar(
                                    title = {
                                        OutlinedTextField(
                                            value = searchQuery,
                                            onValueChange = { searchQuery = it },
                                            placeholder = { Text("Поиск...", color = Color.Gray) },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent,
                                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                                cursorColor = Color(0xFF38BDF8)
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = { isSearchMode = false; searchQuery = ""; searchUserId = null }) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                                )
                                // Сортировка по юзерам (только для групп)
                                if (chat.is_group && chatUsers.isNotEmpty()) {
                                    androidx.compose.foundation.lazy.LazyRow(
                                        modifier = Modifier.fillMaxWidth().padding(end = 16.dp,start = 16.dp, bottom = 12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        item {
                                            Box(
                                                modifier = Modifier.size(40.dp).clip(CircleShape).background(if (searchUserId == null) Color(0xFF38BDF8) else Color(0xFF38BDF8).copy(alpha = 0.2f)).clickable { searchUserId = null },
                                                contentAlignment = Alignment.Center
                                            ) { Text("Все", color = if (searchUserId == null) Color.Black else Color.White, fontSize = 12.sp) }
                                        }
                                        items(chatUsers) { (userId, avatar) ->
                                            Box(
                                                modifier = Modifier.size(40.dp).clip(CircleShape).border(2.dp, if (searchUserId == userId) Color(0xFF38BDF8) else Color.Transparent, CircleShape).clickable { searchUserId = userId }
                                            ) { AsyncImage(model = avatar, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().background(Color.Gray)) }
                                        }
    
                                    }
                                }
                                androidx.compose.foundation.lazy.LazyRow(
                                    modifier = Modifier.fillMaxWidth().padding(end = 16.dp,start = 16.dp, bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val filters = listOf(
                                        SearchMediaType.ALL to "Все",
                                        SearchMediaType.IMAGE to "Фото",
                                        SearchMediaType.VIDEO to "Видео",
                                        SearchMediaType.AUDIO to "Аудио",
                                        SearchMediaType.FILE to "Файлы",
                                        SearchMediaType.LINK to "Ссылки"
                                    )
                                    items(filters.size) { index ->
                                        val (type, label) = filters[index]
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(if (searchMediaType == type) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.1f))
                                                .clickable { searchMediaType = type }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(label, color = if (searchMediaType == type) Color.Black else Color.White, fontSize = 13.sp)
                                        }
                                    }
                                }
                            } else {
                                // --- ОБЫЧНАЯ ШАПКА ---
                                TopAppBar(
                                    title = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                // ✨ ИСПРАВЛЕННЫЙ КЛИК ПО ШАПКЕ
                                                .clickable {
                                                    if (chat.is_group) {
                                                        showGroupProfile = true
                                                    } else {
                                                        // Теперь профиль откроется даже в пустом чате!
                                                        if (otherUserId != null) {
                                                            showUserProfileId = otherUserId
                                                        } else {
                                                            Toast.makeText(context, "Данные пользователя загружаются...", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                                .padding(4.dp)
                                        ) {
                                            if (chat.avatar != null) {
                                                AsyncImage(model = chat.avatar, contentDescription = "Аватар", modifier = Modifier.size(40.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                                            } else {
                                                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(if (chat.is_group) Color(0xFFF59E0B) else Color(0xFF38BDF8)), contentAlignment = Alignment.Center) {
                                                    Text(text = chat.name.take(1).uppercase(), color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(text = chat.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                if (chat.is_group) Text(text = "Группа", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                            }
                                        }
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White) }
                                    },
                                    actions = {
                                        Box {
                                            IconButton(onClick = { menuExpanded = true }) {
                                                Icon(Icons.Default.MoreVert, contentDescription = "Меню", tint = Color.White)
                                            }
                                            DropdownMenu(
                                                expanded = menuExpanded,
                                                onDismissRequest = { menuExpanded = false },
                                                modifier = Modifier.background(Color(0xFF242629))
                                            ) {
                                                if (chat.is_group) {
                                                    DropdownMenuItem(
                                                        text = { Text(if (isMuted) "Включить звук" else "Выключить уведомления", color = Color.White) },
                                                        leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF94A3B8)) },
                                                        onClick = {
                                                            menuExpanded = false
                                                            coroutineScope.launch { try { isMuted = api.toggleGroupMute(chat.id).is_muted } catch(e:Exception){} }
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text("Поиск", color = Color.White) },
                                                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8)) },
                                                        onClick = { menuExpanded = false; isSearchMode = true }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text("Выйти из чата", color = Color.White) },
                                                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color(0xFFEF4444)) },
                                                        onClick = {
                                                            menuExpanded = false
                                                            coroutineScope.launch { try { api.leaveGroup(chat.id); onBack() } catch(e:Exception){} }
                                                        }
                                                    )
                                                } else {
                                                    // ✨ ИСПРАВЛЕННОЕ МЕНЮ ЛИЧНЫХ СООБЩЕНИЙ
                                                    DropdownMenuItem(
                                                        text = { Text(if (isMuted) "Включить звук" else "Замутить пользователя", color = Color.White) },
                                                        leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF94A3B8)) },
                                                        onClick = {
                                                            menuExpanded = false
                                                            if (otherUserId != null) {
                                                                coroutineScope.launch {
                                                                    try {
                                                                        isMuted = api.toggleUserMute(otherUserId).is_muted
                                                                    } catch(e:Exception){}
                                                                }
                                                            }
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text("Поиск", color = Color.White) },
                                                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8)) },
                                                        onClick = { menuExpanded = false; isSearchMode = true }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text("Очистить чат", color = Color(0xFFEF4444)) },
                                                        leadingIcon = { Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_trash_2), contentDescription = null, tint = Color(0xFFEF4444)) },
                                                        onClick = {
                                                            menuExpanded = false
                                                            coroutineScope.launch {
                                                                try {
                                                                    // ✨ Вызываем правильный метод удаления чата!
                                                                    val res = api.deleteConversation(chat.id)
                                                                    if (res.isSuccessful) {
                                                                        onBack() // Выходим из чата после удаления
                                                                    }
                                                                } catch(e:Exception){
                                                                    Toast.makeText(context, "Ошибка при очистке чата", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                                )
                        }
                    }
                }
                },
                bottomBar = {
                    if (!chat.is_group && !chat.can_reply) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Вы не можете писать в этот чат, пока не подпишетесь друг на друга.",
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                        }
                    } else {
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
                            .clip(RoundedCornerShape(topStart = 24.dp))
                            .background(Color(0xFF242629))
                            .border(1.dp, GlassBorder, RoundedCornerShape(topStart = 24.dp))
                            .padding(horizontal = 8.dp)
                            .padding(top = 4.dp)
                            .animateContentSize() // ✨ ИСПРАВЛЕНО: Плавно раздвигает панель при открытии галереи!
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                        ) {
    
                            // Внутренняя часть с отступами для поля ввода и плашек
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                            // 1. ПЛАШКА ОТВЕТА
                            // 1. ПЛАШКА ОТВЕТА
                            if (replyToMessage != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(16.dp))
                                        .background(Color.White.copy(alpha = 0.05f)).border(1.dp, GlassBorder, RoundedCornerShape(16.dp)).padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Проверяем, есть ли медиафайлы
                                    val hasImage = !replyToMessage!!.image_thumb.isNullOrEmpty() || !replyToMessage!!.image_master.isNullOrEmpty()
                                    val hasVideo = !replyToMessage!!.video_master.isNullOrEmpty()
                                    val hasAudio = !replyToMessage!!.audio_stream.isNullOrEmpty() || !replyToMessage!!.audio_master.isNullOrEmpty()
    
                                    // Показываем мини-картинку или стандартную иконку
                                    if (hasImage || hasVideo || hasAudio) {
                                        val mediaUrl = replyToMessage!!.image_thumb ?: replyToMessage!!.image_master ?: replyToMessage!!.video_master ?: replyToMessage!!.audio_cover
                                        AsyncImage(
                                            model = mediaUrl,
                                            contentDescription = null,
                                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(4.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    } else {
                                        Icon(Icons.Default.Create, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.padding(end = 8.dp))
                                    }
    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Ответ ${replyToMessage!!.senderName}", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    
                                        // Подбираем правильный текст
                                        val replyText = when {
                                            !replyToMessage!!.text.isNullOrEmpty() -> replyToMessage!!.text
                                            hasImage -> "Изображение"
                                            hasVideo -> "Видео"
                                            hasAudio -> replyToMessage!!.audio_title ?: "Голосовое сообщение"
                                            else -> "Вложение"
                                        }
    
                                        Text(replyText ?: "", color = Color.White, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                                        } else {
                                            showMediaPicker = true
                                            focusManager.clearFocus() // ✨ Прячем клавиатуру, чтобы она не толкала галерею вверх!
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
                                            // ✨ Легкая фиолетовая подсветка, когда запись закреплена
                                            .background(if (isRecordingLocked) Color(0xFF7f5af0).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                                            .animateContentSize()
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Мигающая красная точка
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(if (recordDuration % 2 == 0) Color(0xFFEF4444) else Color.Transparent)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = String.format("%02d:%02d", recordDuration / 60, recordDuration % 60),
                                            color = Color.White
                                        )
    
                                        Spacer(modifier = Modifier.width(16.dp))
    
                                        // ✨ АНИМИРОВАННАЯ ИНДИКАЦИЯ ЗАМКА
                                        androidx.compose.animation.AnimatedContent(
                                            targetState = isRecordingLocked,
                                            label = "lock_animation"
                                        ) { locked ->
                                            if (locked) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Lock, contentDescription = "Закреплено", tint = Color(0xFF7f5af0), modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Закреплено", color = Color(0xFF7f5af0), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.alpha(0.5f)
                                                ) {
                                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                    Text("для замка", color = Color.White, fontSize = 12.sp)
                                                }
                                            }
                                        }
    
                                        Spacer(modifier = Modifier.weight(1f))
    
                                        // Кнопка отмены записи
                                        IconButton(onClick = {
                                            isRecording = false
                                            isRecordingLocked = false
                                            dragYOffset = 0f // Сбрасываем сдвиг на всякий случай
                                            voiceRecorder.cancelRecording()
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Отмена", tint = Color(0xFFEF4444))
                                        }
                                    }
                                } else {
                                    OutlinedTextField(
                                        value = inputText,
                                        onValueChange = { inputText = it },
                                        textStyle = LocalTextStyle.current.copy(
                                            lineHeight = 20.sp // Здесь можно настроить высоту строки (например, 20 или 22)
                                        ),
                                        placeholder = {
                                            Text(
                                                "Сообщение...",
                                                color = Color(0XFF72757e),
                                                // Для красоты можно добавить такой же интервал и в подсказку
                                                style = LocalTextStyle.current.copy(lineHeight = 20.sp)
                                            )
                                        },
                                        modifier = Modifier.weight(1f).focusRequester(focusRequester) // Привязываем реквестер
                                            .onFocusChanged { focusState ->
                                                // Если пользователь тапнул в поле ввода — автоматически закрываем галерею
                                                if (focusState.isFocused && showMediaPicker) {
                                                    showMediaPicker = false
                                                }
                                            },
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
                                        // 1. ПЕРЕНОСИМ clickable ВЫШЕ pointerInput!
                                        // Теперь pointerInput будет получать события первым.
                                        .clickable(enabled = !showMic || isRecordingLocked) {
                                            if (isRecordingLocked) {
                                                isRecording = false
                                                isRecordingLocked = false
                                                voiceRecorder.stopRecording()
    
                                                // Больше не смотрим на кривой recordDuration, проверяем наличие файла
                                                voiceRecorder.outputFile?.let { file ->
                                                    if (file.length() > 0) {
                                                        selectedFileUri = Uri.fromFile(file)
                                                        selectedFileName = file.name
                                                        selectedMimeType = "audio/mp4"
                                                    }
                                                }
                                            }
                                            if (inputText.isNotBlank() || selectedFileUri != null) {
                                                sendAction()
                                            }
                                        }
                                        .pointerInput(showMic, isRecordingLocked) {
                                            if (showMic && !isRecordingLocked) {
                                                awaitPointerEventScope {
                                                    while (true) {
                                                        // 2. Читаем событие, даже если оно кем-то "съедено"
                                                        val down = awaitFirstDown(requireUnconsumed = false)
    
                                                        val hasMicPerm = androidx.core.content.ContextCompat.checkSelfPermission(
                                                            context, Manifest.permission.RECORD_AUDIO
                                                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    
                                                        if (!hasMicPerm) {
                                                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                            while (awaitPointerEvent().changes.any { it.pressed }) {}
                                                            continue
                                                        }
    
                                                        // 3. Используем миллисекунды для точного подсчета времени
                                                        val startTime = System.currentTimeMillis()
                                                        isRecording = true
                                                        voiceRecorder.startRecording()
                                                        var isCanceled = false
    
                                                        do {
                                                            val event = awaitPointerEvent()
                                                            val dragY = event.changes.firstOrNull()?.position?.y ?: 0f
    
                                                            // Свайп вверх -> Замок
                                                            if (dragY < -100f) {
                                                                isRecordingLocked = true
                                                            }
    
                                                            // Свайп влево -> Отмена
                                                            val dragX = event.changes.firstOrNull()?.position?.x ?: 0f
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
    
                                                            val elapsed = System.currentTimeMillis() - startTime
                                                            // 4. Защита от случайных микро-тапов: если меньше 300 мс — отменяем
                                                            if (elapsed < 300) {
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
                        }}
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
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = innerPadding.calculateTopPadding() + 8.dp, bottom = innerPadding.calculateBottomPadding() + 8.dp),
                            reverseLayout = true // Лента идет снизу вверх!
                        ){
                            itemsIndexed(displayedMessages, key = { _, msg -> msg.id }, contentType = { _, _ -> "message" }) { index, msg ->
                                val isFadingOut = fadingOutMessages.contains(msg.id)
    
                                // ✨ ЛОГИКА ДАТ И СОСЕДЕЙ
                                val prevMsg = displayedMessages.getOrNull(index + 1) // В reversedLayout это БОЛЕЕ СТАРОЕ сообщение (выше на экране)
                                val nextMsg = displayedMessages.getOrNull(index - 1) // БОЛЕЕ НОВОЕ (ниже на экране)
    
                                val currentMsgDateStr = getMessageDateSeparator(msg.time)
                                val olderMsgDateStr = prevMsg?.time?.let { getMessageDateSeparator(it) }
    
                                val isSameDateAsOlder = currentMsgDateStr == olderMsgDateStr
                                val isSameDateAsNewer = currentMsgDateStr == nextMsg?.time?.let { getMessageDateSeparator(it) }
    
                                val isConnectedToOlder = prevMsg != null && prevMsg.senderId == msg.senderId && isSameDateAsOlder && formatMessageTime(prevMsg.time) == formatMessageTime(msg.time)
                                val isConnectedToNewer = nextMsg != null && nextMsg.senderId == msg.senderId && isSameDateAsNewer && formatMessageTime(nextMsg.time) == formatMessageTime(msg.time)
                                Column {
                                        androidx.compose.animation.AnimatedVisibility(visible = !isFadingOut) {

                                            val hasDeletePerm = chat.permissions?.deleteOthersMessages ?: false
                                            val canDeleteForEveryone = msg.senderId == AuthManager.userId || hasDeletePerm // Разрешено автору ИЛИ модератору


                                            MessageBubble(
                                                message = msg,
                                                isMe = msg.senderId == AuthManager.userId,
                                                repliedMessage = msg.reply_to?.let { reply -> messages.find { it.id == reply.id } },
                                                canDeleteForEveryone = canDeleteForEveryone,
                                                isConnectedToOlder = isConnectedToOlder,
                                                isConnectedToNewer = isConnectedToNewer,
                                                showTime = !isConnectedToNewer,
                                                showName = !isConnectedToOlder,
                                                onImageClick = { clickedMsg, bounds ->
                                                    onImageClick(
                                                        messages,
                                                        clickedMsg.id,
                                                        bounds
                                                    )
                                                },
                                                onReply = { replyToMessage = it },
                                                onEdit = {
                                                    editingMessage = it; inputText = it.text ?: ""
                                                },
                                                onForward = {
                                                    forwardMessageId = it.id
                                                    selectedChatsForForward = emptySet()
                                                    isForwardModalOpen = true
                                                },
                                                isExpanded = expandedMessageId == msg.id,
                                                onToggleExpand = {
                                                    expandedMessageId =
                                                        if (expandedMessageId == msg.id) null else msg.id
                                                },
                                                onReplyClick = { targetId ->
                                                    val index = messages.indexOfFirst { it.id == targetId }
                                                    if (index != -1) {
                                                        // 1. Сообщение уже в списке
                                                        val reversedIndex = messages.reversed().indexOfFirst { it.id == targetId }
                                                        coroutineScope.launch {
                                                            listState.animateScrollToItem(reversedIndex)
                                                            highlightedMessageId = targetId
                                                            delay(2000)
                                                            highlightedMessageId = null
                                                        }
                                                    } else {
                                                        coroutineScope.launch {
                                                            isLoading = true
                                                            try {
                                                                val res = api.loadContext(chat.id, targetId)
                                                                messages = res.messages

                                                                // ✨ Обновляем возможности прокрутки
                                                                canLoadMoreUp = res.has_more_up
                                                                canLoadMoreDown = res.has_more_down // Если за нами есть сообщения, включаем загрузку вниз

                                                                delay(100)
                                                                val newReversedIndex = messages.reversed().indexOfFirst { it.id == targetId }
                                                                if (newReversedIndex != -1) {
                                                                    listState.scrollToItem(newReversedIndex)
                                                                    highlightedMessageId = targetId
                                                                    delay(2000)
                                                                    highlightedMessageId = null
                                                                }
                                                            } catch (e: Exception) { /* error */ } finally { isLoading = false }
                                                        }
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
                                                            messages =
                                                                messages.filter { it.id != msg.id }
                                                        } catch (e: Exception) {
                                                            e.printStackTrace()
                                                        } finally {
                                                            fadingOutMessages =
                                                                fadingOutMessages - msg.id
                                                        }
                                                    }
                                                },
                                                // ✨ ОТКРЫВАЕМ ПРОФИЛЬ ПРИ КЛИКЕ ПО АВЕ
                                                onAvatarClick = { userId -> showUserProfileId = userId },
                                                api = api
                                            )
                                        }
                                        if (currentMsgDateStr.isNotEmpty() && currentMsgDateStr != olderMsgDateStr) {
                                            Box(
                                                modifier = Modifier.fillMaxWidth()
                                                    .padding(vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = currentMsgDateStr,
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier
                                                        .background(
                                                            Color.Black.copy(alpha = 0.3f),
                                                            RoundedCornerShape(12.dp)
                                                        )
                                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }

                            }
                            if (isLoadingMore) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = Color(0xFF38BDF8), modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                    }
                                }
                            }
                        }
                        val showScrollToBottom by remember {
                            derivedStateOf {
                                // Показываем кнопку, только если мы пролистали вверх как минимум на 4 сообщения
                                listState.firstVisibleItemIndex > 3
                            }
                        }
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showScrollToBottom,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut(),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                // Поднимаем кнопку над полем ввода
                                .padding(bottom = innerPadding.calculateBottomPadding() + 16.dp, end = 16.dp)
                        ) {
                            FloatingActionButton(
                                onClick = {
                                    if (canLoadMoreDown) {
                                        // ✨ Если мы в "прошлом", кнопка не просто скроллит, а ПЕРЕЗАГРУЖАЕТ чат
                                        coroutineScope.launch {
                                            isLoading = true
                                            try {
                                                val response = api.getMessages(chat.id) // Загружаем самые свежие 30 штук
                                                messages = response.messages
                                                canLoadMoreUp = true
                                                canLoadMoreDown = false // Мы снова в настоящем
                                                delay(100)
                                                listState.scrollToItem(0)
                                            } catch (e: Exception) {} finally { isLoading = false }
                                        }
                                    } else {
                                        // Если мы и так в настоящем — просто скроллим вниз
                                        coroutineScope.launch { listState.animateScrollToItem(0) }
                                    }
                                },
                                containerColor = Color(0xFF242629).copy(alpha = 0.9f),
                                contentColor = Color(0xFF38BDF8),
                                modifier = Modifier.size(44.dp).border(1.dp, GlassBorder, CircleShape),
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Вниз")
                            }
                        }
                    }
                }
            }
            if (showGroupProfile) {
                GroupProfileScreen(chatId = chat.id, api = api, onBack = { showGroupProfile = false }, onExitChat = { showGroupProfile = false; onBack() },
                    // ✨ ПЕРЕДАЕМ ДАЛЬШЕ
                    onImageClick = onImageClick)
            }
    
            if (showUserProfileId != null) {
                UserProfileScreen(
                    userId = showUserProfileId!!,
                    api = api,
                    onBack = { showUserProfileId = null },
                    onOpenChat = { showUserProfileId = null }
                )
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
                    // 1. Закрываем предпросмотр сразу
                    previewMediaUri = null
                    previewMediaBounds = null
                    showMediaPicker = false
                    // 2. Сохраняем нужные данные в локальные переменные,
                    // чтобы они не стерлись при очистке глобального стейта
                    val currentReplyToId = replyToMessage?.id
                    val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                    val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime) ?: "jpg"
                    val fName = "media_${System.currentTimeMillis()}.$ext"

                    // Очищаем черновики в главном окне
                    inputText = ""
                    replyToMessage = null
                    selectedFileUri = null

                    coroutineScope.launch {
                        try {
                            isUploading = true
                            // ✨ Грузим файл чанками
                            val uploadResult = uploadFileInChunks(context, uri, chat.id, api) { progress ->
                                uploadProgress = progress
                            }

                            if (uploadResult == null) {
                                isUploading = false
                                Toast.makeText(context, "Ошибка при загрузке файла", Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            val uploadedFileId = uploadResult.first
                            isUploading = false

                            // ✨ ФИКС: Упаковываем данные в List<MultipartBody.Part> для нового API
                            val idsPart = listOf(MultipartBody.Part.createFormData("uploaded_file_ids", uploadedFileId))
                            val namesPart = listOf(MultipartBody.Part.createFormData("original_file_names", fName))
                            val mimePart = listOf(MultipartBody.Part.createFormData("content_types", mime))

                            // Отправляем сообщение
                            val sentMsg = api.sendMessage(
                                conversationId = chat.id,
                                text = caption.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull()),
                                parentId = currentReplyToId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
                                uploadedFileIds = idsPart,
                                originalFileNames = namesPart,
                                contentTypes = mimePart
                            )

                            // ✨ Обновляем список сообщений
                            // Сервер теперь возвращает сообщение с заполненным массивом attachments
                            if (!messages.any { it.id == sentMsg.id }) {
                                messages = messages + sentMsg
                                kotlinx.coroutines.delay(100)
                                listState.animateScrollToItem(0)
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                            Log.e("UploadError", "Failed to send media: ${e.message}")
                            Toast.makeText(context, "Ошибка отправки", Toast.LENGTH_SHORT).show()
                        } finally {
                            isUploading = false
                        }
                    }
                }
            )
        }
        if (showGroupProfile) {
            GroupProfileScreen(
                chatId = chat.id,
                api = api,
                onBack = { showGroupProfile = false },
                onExitChat = {
                    showGroupProfile = false
                    onBack() // Возвращаемся в список чатов
                },
                // ✨ ПЕРЕДАЕМ ДАЛЬШЕ
                onImageClick = onImageClick
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
    object AudioCacheHelper {
        private val client = OkHttpClient()
    
        fun getPlayableUrl(context: Context, url: String, messageId: Int): String {
            // Проверяем, есть ли уже файл в кэше телефона
            val fileName = "audio_cache_$messageId.mp3" // Можно юзать .m4a
            val cachedFile = File(context.cacheDir, fileName)
    
            // Если файл есть и он не пустой — отдаем локальный путь! (Загрузится моментально)
            if (cachedFile.exists() && cachedFile.length() > 0) {
                return cachedFile.absolutePath
            }
    
            // Если файла нет, запускаем тихую загрузку в фоне
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()
    
                    if (response.isSuccessful) {
                        // Сохраняем во временный файл, чтобы плеер не попытался прочитать недокачанное
                        val tempFile = File(context.cacheDir, "$fileName.tmp")
                        response.body?.byteStream()?.use { input ->
                            tempFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        // Переименовываем в нормальный файл
                        tempFile.renameTo(cachedFile)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    
            // Пока файл качается в фоне, возвращаем сетевую ссылку (плеер сам справится с потоковым воспроизведением)
            return url
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AudioPlayerBubble(message: Message, isMe: Boolean) {
        val context = LocalContext.current // ✨ Нужен для скачивания
        val isThisTrackActive = GlobalAudioPlayer.currentPlayingId == message.id
        val isPlaying = isThisTrackActive && GlobalAudioPlayer.isPlaying
        val progress = if (isThisTrackActive) GlobalAudioPlayer.progress else 0f
        val duration = if (isThisTrackActive) GlobalAudioPlayer.trackDuration else 0
        val currentPosition = (progress * duration).toInt()
    
        var isDragging by remember { mutableStateOf(false) }
        var localDragPosition by remember { mutableIntStateOf(0) }
        Row(
            modifier = Modifier
                .fillMaxWidth().padding(8.dp)
               ,
            verticalAlignment = Alignment.CenterVertically
        ) {
    
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(64.dp)).then(if (message.audio_cover == null) Modifier.background(if (isMe) Color.White.copy(alpha = 0.2f) else Color(0xFF38BDF8)) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                if (message.audio_cover != null) {
                    AsyncImage(model = message.audio_cover, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.4f), blendMode = BlendMode.Darken))
                }
    
                // ✨ ОБНОВЛЕННАЯ КНОПКА PLAY С КЭШИРОВАНИЕМ
                IconButton(
                    onClick = {
                        val rawUrl = message.audio_stream ?: message.audio_master
                        if (rawUrl != null) {
                            // Спрашиваем наш кэш: дать сетевую ссылку или локальную?
                            val playableUrl = AudioCacheHelper.getPlayableUrl(context, rawUrl, message.id)
                            GlobalAudioPlayer.playPause(message.id, playableUrl)
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(painter = if (isPlaying) painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_pause) else painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_play), contentDescription = null, tint = Color.White)
                }
            }
    
            Spacer(modifier = Modifier.width(12.dp))
    
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message.audio_title ?: "Голосовое сообщение",
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
    
                val displayProgress = if (isDragging) {
                    if (duration > 0) localDragPosition.toFloat() / duration.toFloat() else 0f
                } else progress
    
                Slider(
                    value = displayProgress.coerceIn(0f, 1f),
                    onValueChange = { newProgress ->
                        isDragging = true; localDragPosition = (newProgress * duration).toInt()
                    },
                    onValueChangeFinished = {
                        isDragging = false; if (isThisTrackActive) GlobalAudioPlayer.seekTo(
                        localDragPosition
                    )
                    },
                    modifier = Modifier.fillMaxWidth().height(20.dp),
                    thumb = {
                        Box(
                            modifier = Modifier.size(12.dp).padding(top = 2.dp).background(
                                color = if (isMe) Color.White else Color(0xFF38BDF8),
                                shape = CircleShape
                            )
                        )
                    },
                    track = { sliderState ->
                        SliderDefaults.Track(
                            sliderState = sliderState,
                            modifier = Modifier.height(3.dp),
                            thumbTrackGapSize = 0.dp,
                            drawStopIndicator = null,
                            colors = SliderDefaults.colors(
                                activeTrackColor = if (isMe) Color.White else Color(0xFF38BDF8),
                                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                            )
                        )
                    }
                )
    
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    val timeToShow = if (isDragging) localDragPosition else currentPosition
                    Text(
                        formatTime(timeToShow),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                    Text(text = " / ", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                    Text(
                        text = if (duration > 0) formatTime(duration) else message.audio_duration
                            ?: "00:00", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp
                    )
                }
            }
    
            // Кнопка скачивания
            IconButton(
                onClick = {
                    val urlToDownload = message.audio_master ?: message.audio_stream
                    if (urlToDownload != null) downloadFile(
                        context,
                        urlToDownload,
                        message.audio_title ?: "audio_${message.id}.mp3"
                    )
                },
                modifier = Modifier.padding(start = 8.dp).size(32.dp)
            ) {
                Icon(
                    painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_download),
                    contentDescription = "Скачать",
                    tint = Color.White
                )
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
    fun JSONObject.getNullableString(key: String): String? {
        return if (this.isNull(key)) null else this.optString(key, null).takeIf { it != "null" && it != "" }
    }
    object EmojiManager {
        private const val KEY_EMOJI_HISTORY = "emoji_history"
        private var prefs: android.content.SharedPreferences? = null

        // Кэш для полного списка, чтобы не генерировать его каждый раз
        private var allEmojisCache: List<String>? = null

        fun init(context: android.content.Context) {
            prefs = context.getSharedPreferences("emoji_prefs", android.content.Context.MODE_PRIVATE)
        }

        // Генерируем список всех эмодзи по стандартам Unicode
        private fun generateAllEmojis(): List<String> {
            val emojis = mutableListOf<String>()
            val ranges = listOf(
                0x1F600..0x1F64F, // Смайлы
                0x1F680..0x1F6C5, // Транспорт
                0x1F400..0x1F4D9, // Животные и природа
                0x1F300..0x1F3FA, // Еда, напитки, спорт
                0x1F900..0x1F9FF, // Новые эмодзи (Unicode 9.0+)
                0x1FAB0..0x1FABB, // Насекомые, природа
                0x1F600..0x1F637, // Лица
                0x1F466..0x1F487, // Люди
                0x2702..0x27B0,   // Символы
                0x1F1E6..0x1F1FF  // Флаги (региональные индикаторы)
            )

            for (range in ranges) {
                for (unicode in range) {
                    // Превращаем код Unicode в строку-эмодзи
                    emojis.add(String(Character.toChars(unicode)))
                }
            }
            return emojis
        }

        fun getEmojis(): List<String> {
            if (allEmojisCache == null) {
                allEmojisCache = generateAllEmojis()
            }

            val history = prefs?.getString(KEY_EMOJI_HISTORY, "")
                ?.split(",")
                ?.filter { it.isNotBlank() } ?: emptyList()

            // Возвращаем: История + Все остальные (без дубликатов истории)
            return (history + allEmojisCache!!).distinct()
        }

        fun onEmojiUsed(emoji: String) {
            val currentHistory = prefs?.getString(KEY_EMOJI_HISTORY, "")
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?.toMutableList() ?: mutableListOf()

            currentHistory.remove(emoji)
            currentHistory.add(0, emoji)

            // Храним последние 50 использованных
            val newHistory = currentHistory.take(50).joinToString(",")
            prefs?.edit()?.putString(KEY_EMOJI_HISTORY, newHistory)?.apply()
        }
    }
    @OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class,
        ExperimentalMaterial3Api::class
    )
    @Composable
    fun MessageBubble(
        message: Message,
        repliedMessage: Message? = null,
        isMe: Boolean,
        canDeleteForEveryone: Boolean,
        isConnectedToOlder: Boolean,
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
        onConfirmDelete: (Boolean) -> Unit,
        onAvatarClick: (Int) -> Unit,
        api : ApiService
    ) {
        val baseBubbleColor = if (isMe) Color(0xFF7f5af0) else Color(0xFF1E293B)
        val baseDeleteColor =  Color(0xFF242629)
        val glassColor = baseBubbleColor.copy(alpha = 0.85f)
        val deleteColor = baseDeleteColor.copy(alpha = 0.85f)
        val textColor = if (isMe) Color(0xFFfffffe) else Color.White

        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current
        var imageBounds by remember { mutableStateOf<Rect?>(null) }
        var deleteForEveryone by remember { mutableStateOf(false) }
        var isEmojiMode by remember { mutableStateOf(false) }
        var showFullEmojiPicker by remember { mutableStateOf(false) } // Состояние большой сетки
        val dynamicEmojis = remember(isExpanded) { EmojiManager.getEmojis() }
        // ✨ СБРОС: если сообщение закрылось, возвращаем обычный вид
        LaunchedEffect(isExpanded) {
            if (!isExpanded) isEmojiMode = false
        }


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
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Текст сообщения", message.text)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Текст скопирован", Toast.LENGTH_SHORT).show()
                        onToggleExpand()
                    })
                }

                if (isMe) {
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
        val maxHeight = configuration.screenHeightDp.dp * 0.5f

        val bubbleShape = if (isMe) {
            if(message.text.isNullOrEmpty() && !message.attachments.isNullOrEmpty()){
                RoundedCornerShape(
                    topStart = 8.dp,
                    topEnd = if (isConnectedToOlder) 8.dp else 8.dp,
                    bottomEnd = if (isConnectedToNewer) 8.dp else 8.dp,
                    bottomStart = 8.dp
                )
            }
            else if(!message.text.isNullOrEmpty() && !message.attachments.isNullOrEmpty()) {
                RoundedCornerShape(
                    topStart = 8.dp,
                    topEnd = if (isConnectedToOlder) 8.dp else 8.dp,
                    bottomEnd = if (isConnectedToNewer) 8.dp else 16.dp,
                    bottomStart = 16.dp
                )
            }
            else{
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = if (isConnectedToOlder) 8.dp else 16.dp,
                    bottomEnd = if (isConnectedToNewer) 8.dp else 16.dp,
                    bottomStart = 16.dp
                )
            }
        } else {
            RoundedCornerShape(
                topStart = if (isConnectedToOlder) 4.dp else 16.dp,
                topEnd = 16.dp,
                bottomEnd = 16.dp,
                bottomStart = if (isConnectedToNewer) 4.dp else 16.dp
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = if (isConnectedToOlder) 1.dp else 4.dp,
                    bottom = if (isConnectedToNewer) 1.dp else 4.dp
                ),
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            if (!isMe) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .padding(end = 8.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (!isConnectedToNewer) {
                        AsyncImage(
                            model = message.senderAvatar,
                            contentDescription = null,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                                .clickable { onAvatarClick(message.senderId) },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
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
                    modifier = Modifier.fillMaxWidth(0.8f),
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
                    if (!isMe && !isDeleting && showName) {
                        val nameColor = remember(message.senderColor) {
                            try {
                                message.senderColor?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color(0xFF94A3B8)
                            } catch (e: Exception) { Color(0xFF94A3B8) }
                        }
                        Text(
                            text = message.senderName ?: "Кто-то",
                            color = nameColor,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 2.dp, start = 8.dp)
                        )
                    }
                    val isOnlyMedia = (message.attachments?.isNotEmpty() == true) && message.text.isNullOrEmpty()

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
                                androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(50)) togetherWith
                                        androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(50))
                            },
                            label = "delete_transition"
                        ) { deleting ->
                            if (deleting) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Удалить сообщение?",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    if (canDeleteForEveryone) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { deleteForEveryone = !deleteForEveryone }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Checkbox(
                                                checked = deleteForEveryone,
                                                onCheckedChange = null,
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = Color(0xFFEF4444),
                                                    uncheckedColor = Color.White.copy(alpha = 0.5f),
                                                    checkmarkColor = Color.White
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Удалить для всех", color = Color.White, fontSize = 14.sp)
                                        }
                                    } else {
                                        // Если прав нет, принудительно ставим false
                                        deleteForEveryone = false
                                        Text("Сообщение будет удалено только у вас.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(onClick = onCancelDelete) {
                                            Text("Отмена", color = Color(0xFF94A3B8))
                                        }
                                        Button(
                                            onClick = { onConfirmDelete(deleteForEveryone) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
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
                                            modifier = Modifier.padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 6.dp)
                                                .height(IntrinsicSize.Min).clip(RoundedCornerShape(8.dp))
                                                .background(Color.White.copy(alpha = 0.1f))
                                                .clickable { onReplyClick(message.reply_to.id) }
                                        ) {
                                            Box(modifier = Modifier.width(3.dp).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(if (isMe) Color(0xFFE0F2FE) else Color(0xFF38BDF8)))

                                            val replyData = message.reply_to
                                            val hasReplyImage = (repliedMessage != null && (!repliedMessage.image_thumb.isNullOrEmpty() || !repliedMessage.image_master.isNullOrEmpty())) || !replyData.image_thumb.isNullOrEmpty()
                                            val hasReplyVideo = (repliedMessage != null && !repliedMessage.video_master.isNullOrEmpty()) || !replyData.video_master.isNullOrEmpty()
                                            val hasReplyAudio = (repliedMessage != null && (!repliedMessage.audio_stream.isNullOrEmpty() || !repliedMessage.audio_master.isNullOrEmpty())) || !replyData.audio_stream.isNullOrEmpty() || !replyData.audio_master.isNullOrEmpty()

                                            if (hasReplyImage || hasReplyVideo) {
                                                val mediaUrl = repliedMessage?.image_thumb ?: repliedMessage?.video_master ?: replyData.image_thumb ?: replyData.video_master

                                                Box(
                                                    modifier = Modifier
                                                        .padding(start = 6.dp, top = 4.dp, bottom = 4.dp)
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                ) {
                                                    AsyncImage(
                                                        model = coil.request.ImageRequest.Builder(LocalContext.current)
                                                            .data(mediaUrl)
                                                            .apply {
                                                                // Если это видео - заставляем Coil достать первый кадр!
                                                                if (hasReplyVideo || mediaUrl?.endsWith(".mp4") == true || mediaUrl?.endsWith(".webm") == true) {
                                                                    decoderFactory(coil.decode.VideoFrameDecoder.Factory())
                                                                    videoFrameMicros(1000000)
                                                                }
                                                            }
                                                            .build(),
                                                        contentDescription = null,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )

                                                    // Если это видео, рисуем полупрозрачную черную пленку и треугольник Play
                                                    if (hasReplyVideo) {
                                                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                                            Icon(
                                                                painter = painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_play),
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                                modifier = Modifier.size(16.dp).padding(start = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            Column(modifier = Modifier.padding(start = 6.dp, end = 8.dp, top = 4.dp, bottom = 4.dp).alpha(0.9f)) {
                                                Text(replyData.name ?: "Ответ", color = Color(0xFFfffffe), fontSize = 14.sp, fontWeight = FontWeight.Bold)

                                                val replyText = when {
                                                    !replyData.text.isNullOrEmpty() && replyData.text != "Вложение" -> replyData.text
                                                    repliedMessage?.text?.isNotEmpty() == true -> repliedMessage.text
                                                    hasReplyImage -> "Изображение"
                                                    hasReplyVideo -> "Видео"
                                                    hasReplyAudio -> repliedMessage?.audio_title ?: replyData.audio_title ?: "Голосовое сообщение"
                                                    else -> replyData.text ?: "Вложение"
                                                }

                                                Text(replyText ?: "", color = textColor, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }

                                    // ✨ ЧИСТАЯ ЛОГИКА ОТОБРАЖЕНИЯ МАССИВА ВЛОЖЕНИЙ (БЕЗ ДУБЛИКАТОВ)
                                    val hasText = !message.text.isNullOrEmpty()
                                    val attachments = message.attachments ?: emptyList()
                                    val isOnlyMedia = attachments.isNotEmpty() && !hasText

                                    if (attachments.isNotEmpty()) {
                                        Column(modifier = Modifier.fillMaxWidth()) {

                                            attachments.forEach { att ->
                                                when (att.type) {
                                                    "image", "gif" -> {
                                                        Box{
                                                            val hasDimensions = att.width != null && att.height != null && att.width > 0 && att.height > 0
                                                            val aspectRatio = if (hasDimensions) att.width!!.toFloat() / att.height!!.toFloat() else null

                                                            AsyncImage(
                                                                model = ImageRequest.Builder(LocalContext.current)
                                                                    .data(att.url)
                                                                    .memoryCacheKey(att.url)
                                                                    .diskCacheKey(att.url)
                                                                    .crossfade(true)
                                                                    .apply {
                                                                        if (att.type == "gif") {
                                                                            decoderFactory(if (Build.VERSION.SDK_INT >= 28) coil.decode.ImageDecoderDecoder.Factory() else coil.decode.GifDecoder.Factory())
                                                                        } else if (hasDimensions) {
                                                                            // ✨ Указываем Coil точный размер для декодирования (экономит память)
                                                                            size(att.width!!, att.height!!)
                                                                        }
                                                                    }
                                                                    .build(),
                                                                contentDescription = "Изображение",
                                                                modifier = Modifier.then(
                                                                    if (aspectRatio != null) Modifier.aspectRatio(aspectRatio)
                                                                    else Modifier.wrapContentWidth()
                                                                )
                                                                    .wrapContentWidth()
                                                                    .heightIn(max = maxHeight)
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .onGloballyPositioned { coordinates -> imageBounds = coordinates.boundsInWindow() }
                                                                    .clickable { onImageClick(message, imageBounds) },
                                                                contentScale = ContentScale.Fit
                                                            )

                                                            if (isOnlyMedia && showTime && att == attachments.last()) {
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

                                                    "video" -> {
                                                        Box(
                                                            modifier = Modifier
                                                                .onGloballyPositioned { coordinates -> imageBounds = coordinates.boundsInWindow() }
                                                                .clickable { onImageClick(message, imageBounds) }
                                                        ) {
                                                            val hasDimensions = att.width != null && att.height != null && att.width > 0 && att.height > 0
                                                            val aspectRatio = if (hasDimensions) att.width!!.toFloat() / att.height!!.toFloat() else 1.77f // 16:9

                                                            AsyncImage(
                                                                model = ImageRequest.Builder(LocalContext.current)
                                                                    .data(att.thumb)
                                                                    .memoryCacheKey(att.thumb)
                                                                    .diskCacheKey(att.thumb)
                                                                    .crossfade(true)
                                                                    .apply {
                                                                        if (hasDimensions) {
                                                                            size(att.width!!, att.height!!) // Вызываем только если не null
                                                                        }
                                                                    }
                                                                    .build(),
                                                                contentDescription = "Видео",
                                                                modifier = Modifier.then(
                                                                    if (aspectRatio != null) Modifier.aspectRatio(aspectRatio)
                                                                    else Modifier.wrapContentWidth()
                                                                )
                                                                    .wrapContentWidth()
                                                                    .heightIn(max = maxHeight)
                                                                    .clip(RoundedCornerShape(4.dp))
                                                                    .onGloballyPositioned { coordinates -> imageBounds = coordinates.boundsInWindow() }
                                                                    .clickable { onImageClick(message, imageBounds) },
                                                                contentScale = ContentScale.Fit)


                                                            Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.2f)))

                                                            Row(modifier = Modifier.align(Alignment.BottomStart).padding(4.dp).clip(RoundedCornerShape(4.dp)).background(Color.Black.copy(alpha = 0.5f)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                                                                Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_video), null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text(att.duration ?: "0:00", color = Color.White, fontSize = 10.sp)
                                                                if (att.size != null) {
                                                                    Text(" • ${att.size}", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                                                                }
                                                            }

                                                            Box(modifier = Modifier.align(Alignment.Center).size(44.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                                                                Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_play), contentDescription = "Play", tint = Color.White, modifier = Modifier.size(20.dp).padding(start = 2.dp))
                                                            }

                                                            if (isOnlyMedia && showTime && att == attachments.last()) {
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

                                                    "file" -> {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(0.85f).padding(bottom = 4.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.1f)).border(1.dp, GlassBorder, RoundedCornerShape(12.dp)).padding(12.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(if(isMe) Color.White.copy(0.2f) else Color(0xFF38BDF8)), contentAlignment = Alignment.Center) {
                                                                Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_file), contentDescription = null, tint = Color.White)
                                                            }
                                                            Spacer(modifier = Modifier.width(12.dp))
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Text(att.name ?: "Файл", color = Color.White, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                                Text(att.size ?: "Неизвестный размер", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                                            }
                                                            IconButton(onClick = { downloadFile(context, att.url, att.name ?: "file") }) {
                                                                Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_download), contentDescription = "Скачать", tint = Color.White)
                                                            }
                                                        }
                                                    }

                                                    "audio" -> {
                                                        AudioPlayerBubble(message = message, isMe = isMe)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // ✨ ТЕКСТ СООБЩЕНИЯ
                                    if (hasText) {
                                        Row(
                                            verticalAlignment = Alignment.Bottom,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            MessageTextWithLinks(
                                                text = message.text!!,
                                                textColor = textColor,
                                                modifier = Modifier.weight(1f, fill = false),
                                                onNormalClick = { onToggleExpand() },
                                                onJoinGroup = { token ->
                                                    coroutineScope.launch {
                                                        try {
                                                            api.joinGroup(token)
                                                            Toast.makeText(context, "Вы вступили в группу!", Toast.LENGTH_SHORT).show()
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "Ошибка или вы уже в группе", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            )

                                            if (showTime) {
                                                Row(
                                                    modifier = Modifier.padding(start = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    if (message.is_edited == true) {
                                                        Text("изм. ", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, modifier = Modifier.padding(end = 2.dp))
                                                    }
                                                    Text(
                                                        text = formatMessageTime(message.time),
                                                        color = Color.White.copy(alpha = 0.7f),
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (!message.reactions.isNullOrEmpty()) {
                                        // Используем FlowRow, чтобы реакции переносились на новую строку, если их много
                                        androidx.compose.foundation.layout.ContextualFlowRow(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            itemCount = message.reactions.size
                                        ) { index ->
                                            val reaction = message.reactions[index]
                                            ReactionChip(
                                                reaction = reaction,
                                                onClick = {
                                                    // Вызываем API. Бэкенд сам поймет: добавить реакцию или убрать
                                                    coroutineScope.launch {
                                                        try {
                                                            api.react(message.id, ReactDto(reaction.emoji))
                                                        } catch (e: Exception) {
                                                            e.printStackTrace()
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    }
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = isExpanded,
                                        modifier = Modifier.align(Alignment.End),
                                        enter = fadeIn() + expandIn(expandFrom = Alignment.TopCenter),
                                        exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.TopCenter)
                                    ) {
                                        Row(
                                            modifier = Modifier

                                                .fillMaxWidth(0.9f), // Ограничиваем общую ширину меню, чтобы оно не прилипало к краям
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            // ✨ КОНТЕЙНЕР ДЛЯ КНОПОК ИЛИ ЭМОДЗИ
                                            Box(
                                                modifier = Modifier.weight(1f, fill = false), // Позволяет контенту занимать место слева от смайла
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                androidx.compose.animation.AnimatedContent(
                                                    targetState = isEmojiMode,
                                                    transitionSpec = {
                                                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                                                    },
                                                    label = "mode_switch"
                                                ) { emojiMode ->
                                                    if (emojiMode) {
                                                        androidx.compose.foundation.lazy.LazyRow(
                                                            horizontalArrangement = Arrangement.spacedBy(14.dp), // Чуть больше места для красоты
                                                            modifier = Modifier
                                                                .padding(end = 8.dp)
                                                                .fillMaxWidth(), // Занимает всё доступное пространство слева от смайла
                                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                                        ) {
                                                            items(dynamicEmojis) { emoji ->
                                                                Text(
                                                                    text = emoji,
                                                                    fontSize = 26.sp,
                                                                    modifier = Modifier
                                                                        .clip(CircleShape)
                                                                        // ✨ НОВЫЙ СПОСОБ КЛИКА
                                                                        .combinedClickable(
                                                                            onClick = {
                                                                                EmojiManager.onEmojiUsed(emoji)
                                                                                coroutineScope.launch { try { api.react(message.id, ReactDto(emoji)) } catch (e: Exception) {} }
                                                                                onToggleExpand()
                                                                            },
                                                                            onLongClick = {
                                                                                showFullEmojiPicker = true // Открываем сетку 10x10
                                                                            }
                                                                        )
                                                                )
                                                            }

                                                        }
                                                    } else {
                                                        // --- СТАНДАРТНЫЕ КНОПКИ (Reply, Forward...) ---
                                                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                                            actions.forEach { action ->
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(36.dp)
                                                                        .clip(RoundedCornerShape(8.dp))
                                                                        .background(Color.White.copy(alpha = 0.15f))
                                                                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                                                        .clickable { action.onClick() },
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Icon(
                                                                        painter = painterResource(action.iconRes),
                                                                        contentDescription = action.label,
                                                                        tint = Color.White,
                                                                        modifier = Modifier.size(20.dp)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            // ✨ ФИКСИРОВАННАЯ КНОПКА СМАЙЛА (Всегда справа)
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isEmojiMode) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.15f))
                                                    .border(0.5.dp, if (isEmojiMode) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                                    .combinedClickable(
                                                        onClick = { isEmojiMode = !isEmojiMode },
                                                        onLongClick = { showFullEmojiPicker = true } // ✨ Зажал смайл — открыл всё
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_smile),
                                                    contentDescription = "Emojis",
                                                    tint = if (isEmojiMode) Color(0xFF38BDF8) else Color.White,
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
        if (showFullEmojiPicker) {
            ModalBottomSheet(
                onDismissRequest = { showFullEmojiPicker = false },
                containerColor = Color(0xFF1E293B), // Цвет твоих панелей
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
            ) {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
                    Text(
                        "Все реакции",
                        color = Color.White,
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold
                    )

                    // Сетка эмодзи (примерно 9-10 в ряд)
                    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(9),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(dynamicEmojis.size) { index ->
                            val emoji = dynamicEmojis[index]
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        EmojiManager.onEmojiUsed(emoji)
                                        coroutineScope.launch {
                                            try { api.react(message.id, ReactDto(emoji)) } catch (e: Exception) {}
                                        }
                                        showFullEmojiPicker = false
                                        onToggleExpand()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 28.sp)
                            }
                        }
                    }
                }
            }
        }
    }
    // --- СТЕК ДЕЙСТВИЙ (ЧТОБЫ НЕ БЫЛО КРАША ПО ПАМЯТИ) ---
    sealed class EditAction {
        data class Draw(val path: android.graphics.Path, val color: Int, val strokeWidth: Float) : EditAction()
        data class TextOverlay(val text: String, val x: Float, val y: Float, val color: Int, val size: Float) : EditAction()
        // data class Crop(val rect: android.graphics.Rect) : EditAction() // Задел для обрезки
    }
    
    enum class EditMode { NONE, DRAW, TEXT, CROP }
    
    // Действия рисования (для кнопки Отмена)
    data class DrawPath(val path: android.graphics.Path, val color: Int, val strokeWidth: Float)
    
    // "Живой" текст, который можно двигать
    // "Живой" текст, который можно двигать и изменять
    // "Живой" текст, который можно двигать, крутить и изменять
    // --- СПИСОК ВСТРОЕННЫХ ШРИФТОВ ---
    val EditorFonts = listOf(
        "Стандартный" to Typeface.DEFAULT_BOLD,
        "С засечками" to Typeface.create(Typeface.SERIF, Typeface.BOLD),
        "Моноширинный" to Typeface.create(Typeface.MONOSPACE, Typeface.BOLD),
        "Курсив" to Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
    )
    
    // "Живой" текст
    // "Живой" текст
    class EditableText(
        val id: String = UUID.randomUUID().toString(),
        initialText: String = "Текст",
        initialColor: Int = 0xFFFFFFFF.toInt(),
        initialBmpX: Float = 0f,
        initialBmpY: Float = 0f,
        initialScale: Float = 1f,
        initialRotation: Float = 0f,
        initialHasStroke: Boolean = false,
        initialStrokeColor: Int = 0xFF000000.toInt(),
        initialFontIndex: Int = 0
    ) {
        var text by mutableStateOf(initialText)
        var color by mutableIntStateOf(initialColor)
        var hasStroke by mutableStateOf(initialHasStroke)
        var strokeColor by mutableIntStateOf(initialStrokeColor)
        var fontIndex by mutableIntStateOf(initialFontIndex)
    
        var bmpX by mutableFloatStateOf(initialBmpX)
        var bmpY by mutableFloatStateOf(initialBmpY)
        var scale by mutableFloatStateOf(initialScale)
        var rotation by mutableFloatStateOf(initialRotation)
    
        fun copy() = EditableText(
            id = id,
            initialText = text,
            initialColor = color,
            initialBmpX = bmpX,
            initialBmpY = bmpY,
            initialScale = scale,
            initialRotation = rotation,
            initialHasStroke = hasStroke,
            initialStrokeColor = strokeColor,
            initialFontIndex = fontIndex
        )
    }
    
    val EditorColors = listOf(
        0xFFFFFFFF.toInt(), 0xFF000000.toInt(), 0xFFEF4444.toInt(),
        0xFFF59E0B.toInt(), 0xFF10B981.toInt(), 0xFF3B82F6.toInt(), 0xFF8B5CF6.toInt()
    )
    
    object EditorIcons {
        val Undo = Icons.Default.ArrowBack
        val Draw = Icons.Default.Create      // Иконка кисточки
        val Text = Icons.Default.Create // Иконка буквы 'T'
        val Crop = Icons.Default.Create       // Иконка обрезки
    }
    
    // Добавим вспомогательный класс для управления жестами обрезки
    enum class CropHandle { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, LEFT, RIGHT, TOP, BOTTOM, CENTER, NONE }
    
    // Умная история изменений
    sealed class HistoryAction {
        data class Draw(val path: DrawPath) : HistoryAction()
        data class AddText(val textId: String) : HistoryAction()
        data class Crop(val oldBitmap: android.graphics.Bitmap, val oldDraws: List<DrawPath>, val oldTexts: List<EditableText>) : HistoryAction()
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChatListScreen(
        onChatClick: (Chat) -> Unit,
        onLogout: () -> Unit
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val api = remember { ApiService.create() }
    
        // Стейты экрана
        var chats by remember { mutableStateOf<List<Chat>>(emptyList()) }
        var myProfile by remember { mutableStateOf<UserData?>(null) }
        var isLoading by remember { mutableStateOf(true) }
    
        // Стейты меню и модалок
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        var showMyProfile by remember { mutableStateOf(false) }
        var showCreateGroupDialog by remember { mutableStateOf(false) }
        var showSettings by remember { mutableStateOf(false) } // ✨ Стейт для настроек
        var showEditProfile by remember { mutableStateOf(false) } // ✨ Стейт для редактора профиля

        var latestVersion by remember { mutableStateOf("") }
        var updateUrl by remember { mutableStateOf("") }

        // ✨ Получаем текущую версию и чистим её
        val currentAppVersion = remember {
            try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                pInfo.versionName ?: "unknown"
            } catch (e: Exception) {
                "unknown"
            }
        }


        val isUpdateAvailable = remember(latestVersion, updateUrl) {
            val latest = latestVersion.replace("\"", "").trim()
            val current = currentAppVersion.trim()

            Log.d("UPDATE_CHECK", "SERVER: '$latest' | PHONE: '$current'")

            latest.isNotEmpty() && current != "unknown" && latest != current && updateUrl.isNotBlank()
        }
        fun loadData() {
            scope.launch {
                isLoading = true
                try {
                    // ✨ ДОБАВЛЯЕМ ОБНОВЛЕНИЕ ТОКЕНА ПРИ ЗАГРУЗКЕ
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            scope.launch {
                                try { api.updateFcmToken(ApiService.FcmTokenDto(task.result)) }
                                catch (e: Exception) {}
                            }
                        }
                    }

                    val chatsRes = api.getChats()
                    chats = chatsRes.chats
                    val profileRes = api.getUserProfile(AuthManager.userId)
                    myProfile = profileRes.user
                } catch (e: Exception) {
                    Toast.makeText(context, "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                } finally {
                    isLoading = false
                }
            }
        }
        val lifecycleOwner = LocalLifecycleOwner.current

        LaunchedEffect(Unit) {
            loadData()
            try {
                val updateRes = api.checkUpdate()
                latestVersion = updateRes.version ?: ""
                updateUrl = updateRes.url ?: ""
            } catch (e: Exception) {
                Log.e("UPDATE_CHECK", "Ошибка: ${e.message}")
            }
        }
        DisposableEffect(Unit) {
            val wsManager = WebSocketManager(userId = AuthManager.userId) { json ->
                val type = json.optString("type")
                // Если пришло или удалилось сообщение — обновляем список чатов!
                if (type == "new_message" || type == "message_deleted" || type == "messages_read") {
                    scope.launch {
                        try {
                            val chatsRes = api.getChats()
                            chats = chatsRes.chats
                        } catch (e: Exception) {}
                    }
                }
            }
            wsManager.connect()
            // Слушаем сворачивание / разворачивание приложения
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    // Экран включили/развернули!
                    // 1. Принудительно переподключаем вебсокет
                    wsManager.disconnect()
                    wsManager.connect()

                    // 2. Делаем быстрый запрос, чтобы подтянуть то, что прислали пока экран был выключен
                    scope.launch {
                        try {
                            val chatsRes = api.getChats()
                            chats = chatsRes.chats
                        } catch (e: Exception) {}
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                wsManager.disconnect()
            }
        }
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    // Приложение развернули — запрашиваем свежие чаты
                    scope.launch {
                        try {
                            val chatsRes = api.getChats()
                            chats = chatsRes.chats
                        } catch (e: Exception) {}
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
        // Загрузка данных при входе на экран


    
        // ✨ БОКОВОЕ МЕНЮ (БУРГЕР)
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = Color(0xFF1E293B),
                    modifier = Modifier.width(300.dp)
                ) {
                    // ✨ ШАПКА МЕНЮ (Имя и тег теперь СПРАВА от аватарки)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch { drawerState.close() }
                                showMyProfile = true
                            }
                            .padding(start = 16.dp, end = 16.dp, top = 48.dp, bottom = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = myProfile?.avatar,
                                contentDescription = "Мой аватар",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = myProfile?.name ?: "Загрузка...",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (!myProfile?.username.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "@${myProfile!!.username}",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
    
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(8.dp))
    
                    // КНОПКА: Создать группу
                    NavigationDrawerItem(
                        label = { Text("Создать группу", color = Color.White) },
                        icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            showCreateGroupDialog = true
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
    
                    // ✨ КНОПКА: Настройки (Теперь работает!)
                    NavigationDrawerItem(
                        label = { Text("Настройки", color = Color.White) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            showSettings = true
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
    
                    Spacer(modifier = Modifier.weight(1f))
    
                    // КНОПКА: Выйти
                    NavigationDrawerItem(
                        label = { Text("Выйти из аккаунта", color = Color(0xFFEF4444)) },
                        icon = { Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_log_out), contentDescription = null, tint = Color(0xFFEF4444)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            AuthManager.clearAuth()
                            onLogout()
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Sonzaiigi", color = Color.White, fontWeight = FontWeight.Bold) },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E293B)),
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Меню", tint = Color.White)
                            }
                        },
                        actions = {
                            IconButton(onClick = { loadData() }) {
                                Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_refresh_cw), contentDescription = "Обновить", tint = Color(0xFF94A3B8))
                            }
                        }
                    )
                },
                containerColor = Color(0xFF0F172A)
            ) { innerPadding ->
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color(0xFF38BDF8))
                    } else if (chats.isEmpty()) {
                        Text(text = "У вас пока нет чатов", color = Color.Gray)
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(chats) { chat ->
                                ChatItemRow(chat = chat, onClick = { onChatClick(chat) })
                            }
                        }
                    }
                    // ✨ ПЛАШКА ОБНОВЛЕНИЯ
                    if (latestVersion != currentAppVersion && updateUrl.isNotBlank()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF38BDF8))
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Доступно обновление!", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Версия: $latestVersion", color = Color.Black.copy(0.7f), fontSize = 14.sp)
                                }
                                Button(
                                    onClick = { ApkUpdater.downloadAndInstall(context, updateUrl, latestVersion) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                                ) {
                                    Text("Обновить", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    
        // --- МОДАЛКИ ---
    
        if (showMyProfile) {
            UserProfileScreen(userId = AuthManager.userId, api = api, onBack = { showMyProfile = false }, onOpenChat = {})
        }
    
        // ✨ Вызов экрана настроек
        if (showSettings) {
            SettingsScreen(
                api = api,
                onBack = { showSettings = false },
                onEditProfile = {
                    showSettings = false
                    showEditProfile = true
                },
                onLogout = {
                    showSettings = false
                    AuthManager.clearAuth()
                    onLogout()
                }
            )
        }
    
        // ✨ Вызов экрана редактирования профиля
        if (showEditProfile && myProfile != null) {
            EditProfileScreen(
                user = myProfile!!,
                api = api,
                onBack = { showEditProfile = false },
                onProfileUpdated = { loadData() }
            )
        }
    
        // 2. Создание группы
        if (showCreateGroupDialog) {
            var groupName by remember { mutableStateOf("") }
            var groupDesc by remember { mutableStateOf("") } // ✨ Новое поле
            var avatarUri by remember { mutableStateOf<Uri?>(null) }
            val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> avatarUri = uri }
    
            var isCreating by remember { mutableStateOf(false) }
    
            AlertDialog(
                onDismissRequest = { showCreateGroupDialog = false },
                containerColor = Color(0xFF1E293B),
                title = { Text("Создание группы", color = Color.White) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Выбор аватарки
                        Box(
                            modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.DarkGray).clickable { picker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarUri != null) {
                                AsyncImage(model = avatarUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            } else {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
    
                        OutlinedTextField(
                            value = groupName, onValueChange = { groupName = it },
                            label = { Text("Название группы", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
    
                        OutlinedTextField(
                            value = groupDesc, onValueChange = { groupDesc = it },
                            label = { Text("Описание (необязательно)", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                isCreating = true
                                try {
                                    val namePart = groupName.toRequestBody("text/plain".toMediaTypeOrNull())
                                    val descPart = groupDesc.toRequestBody("text/plain".toMediaTypeOrNull())
                                    var avatarPart: MultipartBody.Part? = null
    
                                    avatarUri?.let { uri ->
                                        val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                                        if (bytes != null) {
                                            val reqFile = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                                            avatarPart = MultipartBody.Part.createFormData("Avatar", "group_avatar.jpg", reqFile)
                                        }
                                    }
    
                                    // Обнови метод в ApiService, чтобы он принимал Description!
                                    val res = api.createGroup(namePart, descPart, avatarPart)
                                    if (res.isSuccessful) {
                                        showCreateGroupDialog = false
                                        loadData()
                                    }
                                } catch (e: Exception) { /* ошибка */ } finally { isCreating = false }
                            }
                        },
                        enabled = groupName.isNotBlank() && !isCreating
                    ) { Text("Создать") }
                }
            )
        }
    }
    fun String?.toFullUrl(): String? {
        if (this.isNullOrBlank() || this == "null" || this == "undefined") return null
        var url = this.trim()
        if (url.startsWith("http://")) url = url.replace("http://", "https://") // Coil не любит http
        if (url.startsWith("https://")) return url
        return "https://cdn.sonzaiigi.com/${url.trimStart('/')}"
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SettingsScreen(
        onBack: () -> Unit,
        onEditProfile: () -> Unit,
        onLogout: () -> Unit,
        api: ApiService
    ) {
        var showPrivacy by remember { mutableStateOf(false) }

        if (showPrivacy) {
            PrivacySecurityScreen(onBack = { showPrivacy = false }, api = api)
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Настройки", color = Color.White) },
                        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E293B))
                    )
                },
                containerColor = Color(0xFF0F172A)
            ) { innerPadding ->
                Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
    
                    // Секция аккаунта
                    SettingsItem(title = "Редактировать профиль", icon = Icons.Default.Edit, onClick = onEditProfile)
                    SettingsItem(title = "Конфиденциальность и безопасность", icon = Icons.Default.Lock, onClick = { showPrivacy = true })
    
                    Spacer(modifier = Modifier.weight(1f))
    
                    // Выход
                    SettingsItem(
                        title = "Выйти из аккаунта",
                        icon = painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_log_out),
                        color = Color(0xFFEF4444),
                        onClick = onLogout
                    )

                    if (AuthManager.userId == 3) { // 1 - это ID админа
                        var showAdminPanel by remember { mutableStateOf(false) }

                        SettingsItem(title = "Панель администратора (APK)", icon = Icons.Default.Add, color = Color(0xFF10B981), onClick = { showAdminPanel = true })

                        if (showAdminPanel) {
                            var apkVersion by remember { mutableStateOf("") }
                            var apkUri by remember { mutableStateOf<Uri?>(null) }
                            val apkPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> apkUri = uri }
                            var isUploadingApk by remember { mutableStateOf(false) }
                            val context = LocalContext.current
                            val scope = rememberCoroutineScope()

                            Dialog(onDismissRequest = { showAdminPanel = false }) {
                                Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xFF1E1E1E)).padding(16.dp)) {
                                    Column {
                                        Text("Загрузить новую версию", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        OutlinedTextField(
                                            value = apkVersion, onValueChange = { apkVersion = it },
                                            label = { Text("Версия (например: 1.0.1)", color = Color.Gray) },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(onClick = { apkPicker.launch("application/vnd.android.package-archive") }) {
                                            Text(if (apkUri == null) "Выбрать APK файл" else "APK выбран!")
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                            TextButton(onClick = { showAdminPanel = false }) { Text("Отмена", color = Color.Gray) }
                                            Button(
                                                onClick = {
                                                    scope.launch {
                                                        isUploadingApk = true
                                                        try {
                                                            // 1. Пытаемся открыть файл
                                                            val pfd = try {
                                                                context.contentResolver.openFileDescriptor(apkUri!!, "r")
                                                            } catch (e: Exception) {
                                                                Log.e("APK_UPLOAD", "File access error: ${e.message}")
                                                                Toast.makeText(context, "Android запретил доступ к файлу", Toast.LENGTH_SHORT).show()
                                                                null
                                                            } ?: return@launch

                                                            val fileInputStream = java.io.FileInputStream(pfd.fileDescriptor)
                                                            val totalSize = context.contentResolver.openFileDescriptor(apkUri!!, "r")?.use {
                                                                it.statSize
                                                            } ?: -1L

                                                            val requestBody = object : okhttp3.RequestBody() {
                                                                override fun contentType() = "application/vnd.android.package-archive".toMediaTypeOrNull()
                                                                override fun contentLength() = totalSize

                                                                override fun writeTo(sink: okio.BufferedSink) {
                                                                    // ✨ ОТКРЫВАЕМ поток заново внутри метода
                                                                    // Это гарантирует, что если OkHttp вызовет writeTo дважды,
                                                                    // он оба раза получит открытый поток.
                                                                    context.contentResolver.openInputStream(apkUri!!)?.use { input ->
                                                                        val buffer = ByteArray(8192)
                                                                        var read: Int
                                                                        while (input.read(buffer).also { read = it } != -1) {
                                                                            sink.write(buffer, 0, read)
                                                                        }
                                                                    }
                                                                }
                                                            }


                                                            val apkPart = MultipartBody.Part.createFormData("apk", "update.apk", requestBody)
                                                            val versionPart = apkVersion.toRequestBody("text/plain".toMediaTypeOrNull())

                                                            // 2. Отправка на сервер
                                                            val res = try {
                                                                api.uploadUpdate(versionPart, apkPart)
                                                            } catch (e: Exception) {
                                                                Log.e("APK_UPLOAD", "Network error: ${e.message}")
                                                                Toast.makeText(context, "Ошибка сети: ${e.message}", Toast.LENGTH_LONG).show()
                                                                null
                                                            }

                                                            if (res != null) {
                                                                if (res.isSuccessful) {
                                                                    Toast.makeText(context, "Успешно загружено!", Toast.LENGTH_SHORT).show()
                                                                    showAdminPanel = false
                                                                } else {
                                                                    val errorMsg = res.errorBody()?.string() ?: ""
                                                                    Log.e("APK_UPLOAD", "Server error ${res.code()}: $errorMsg")

                                                                    // Если ошибка 403 - значит вы не админ или не залогинены
                                                                    // Если ошибка 404 - значит путь api/app/upload не найден
                                                                    Toast.makeText(context, "Сервер: ${res.code()} $errorMsg", Toast.LENGTH_LONG).show()
                                                                }
                                                            }
                                                            pfd.close()

                                                        } catch (e: Exception) {
                                                            Log.e("APK_UPLOAD", "Global error: ${e.message}")
                                                            Toast.makeText(context, "Критическая ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                                                        } finally {
                                                            isUploadingApk = false
                                                        }
                                                    }
                                                },
                                                enabled = apkVersion.isNotBlank() && apkUri != null && !isUploadingApk
                                            ) {
                                                if (isUploadingApk) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                                else Text("Опубликовать")
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
    
    @Composable
    fun SettingsItem(title: String, icon: Any, color: Color = Color.White, onClick: () -> Unit) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (icon) {
                is androidx.compose.ui.graphics.vector.ImageVector -> Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
                is androidx.compose.ui.graphics.painter.Painter -> Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, color = color, fontSize = 16.sp)
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PrivacySecurityScreen(onBack: () -> Unit, api: ApiService) {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
    
        var email by remember { mutableStateOf("") }
        var currentPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
    
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Безопасность", color = Color.White) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E293B))
                )
            },
            containerColor = Color(0xFF0F172A)
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
    
                Text("Смена Email", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Новый Email") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = { /* Вызов API обновления email */ }, modifier = Modifier.padding(top = 8.dp)) { Text("Обновить почту") }
    
                Spacer(modifier = Modifier.height(32.dp))
    
                Text("Смена пароля", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                OutlinedTextField(value = currentPassword, onValueChange = { currentPassword = it }, label = { Text("Текущий пароль") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("Новый пароль") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = {
                    scope.launch {
                        // Используем твой существующий метод api.updatePassword
                        // Убедись, что DTO соответствует бэкенду
                    }
                }, modifier = Modifier.padding(top = 8.dp)) { Text("Сменить пароль") }
            }
        }
    }
    // ✨ Обновленный компонент элемента списка
    @Composable
    fun ChatItemRow(chat: Chat, onClick: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✨ Добавлена поддержка реальной аватарки
            if (chat.avatar != null) {
                AsyncImage(
                    model = chat.avatar,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(Color.Gray)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (chat.is_group) Color(0xFFF59E0B) else Color(0xFF38BDF8)),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = chat.name.takeIf { it.isNotEmpty() }?.take(1)?.uppercase() ?: "?"
                    Text(text = initial, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
    
            Spacer(modifier = Modifier.width(16.dp))
    
            Column(modifier = Modifier.weight(1f)) {
                Text(text = chat.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = chat.lastMessage ?: "Вложение", color = Color(0xFF94A3B8), fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
    
            Column(horizontalAlignment = Alignment.End) {
                Text(text = formatMessageTime(chat.time) ?: "", color = Color(0xFF64748B), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                if (chat.unread_count > 0) {
                    Box(
                        modifier = Modifier.clip(CircleShape).background(Color(0xFF38BDF8)).padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = chat.unread_count.toString(), color = Color(0xFF0F172A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    fun GroupProfileScreen(
        chatId: Int,
        api: ApiService,
        onBack: () -> Unit,
        onExitChat: () -> Unit,
        onImageClick: (List<Message>, Int, Rect?) -> Unit
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val clipboardManager = LocalClipboardManager.current
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    
        var groupInfo by remember { mutableStateOf<GroupProfileResponse?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var isMuted by remember { mutableStateOf(false) }
    
        val tabs = listOf("Members", "Media", "Files", "Links", "Music", "GIFs")
    
        // ✨ СТЕЙТЫ ДЛЯ СВАЙПОВ И СКРОЛЛА
        val pagerState = rememberPagerState(pageCount = { tabs.size })
        // Создаем отдельный скролл-стейт для каждой вкладки, чтобы они не конфликтовали
        val listStates = remember { List(tabs.size) { LazyListState() } }
    
        var showEditScreen by remember { mutableStateOf(false) }
        var selectedUserId by remember { mutableStateOf<Int?>(null) } // Для открытия профиля юзера
    
        val expandedHeight = 380.dp
        val collapsedHeight = 64.dp
        val tabsHeight = 48.dp
        val maxOffsetPx = with(LocalDensity.current) { (expandedHeight - collapsedHeight).toPx() }
    
        // Слушаем скролл ТОЛЬКО текущей активной вкладки
        val currentListState = listStates[pagerState.currentPage]
        val firstVisibleIndex by remember { derivedStateOf { currentListState.firstVisibleItemIndex } }
        val scrollOffset by remember { derivedStateOf { currentListState.firstVisibleItemScrollOffset } }
    
        val collapseFraction by remember {
            derivedStateOf {
                if (firstVisibleIndex > 0) 1f
                else (scrollOffset / maxOffsetPx).coerceIn(0f, 1f)
            }
        }
        var groupMessages by remember { mutableStateOf<List<Message>>(emptyList()) }
        var isLoadingMore by remember { mutableStateOf(false) }
    
        // ✨ ИЗМЕНЕНИЕ 1: Теперь мы храним смещение (offset), чтобы точно знать, откуда грузить
        var messagesOffset by remember { mutableStateOf(0) }
        var canLoadMore by remember { mutableStateOf(true) }
    
        var searchUserId by remember { mutableStateOf<Int?>(null) }
        val allUsersForFilter = remember(groupInfo?.members, groupMessages) {
            val members = groupInfo?.members?.map { it.id to it.avatar } ?: emptyList()
            val authors = groupMessages.map { it.senderId to it.senderAvatar }
            (members + authors).distinctBy { it.first }
        }
    
        LaunchedEffect(chatId) {
            isLoading = true
            canLoadMore = true
            messagesOffset = 0
            try {
                val info = api.getGroupInfo(chatId)
                groupInfo = info
                isMuted = info.isMuted
    
                val res = api.getMessages(chatId, 0)
                groupMessages = res.messages
                messagesOffset = res.messages.size
                if (res.messages.size < 20) canLoadMore = false
            } catch (e: Exception) { e.printStackTrace() }
            finally { isLoading = false }
        }
        LaunchedEffect(pagerState.currentPage, chatId) {
            // Следим за состоянием списка текущей вкладки
            snapshotFlow { listStates[pagerState.currentPage].layoutInfo }
                .collect { layoutInfo ->
                    val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull() ?: return@collect
                    val totalItems = layoutInfo.totalItemsCount
    
                    // Если до конца осталось 5 элементов
                    val isAtEnd = lastVisibleItem.index >= totalItems - 5
    
                    // Проверяем условия загрузки
                    if (isAtEnd && canLoadMore && !isLoadingMore && !isLoading) {
                        isLoadingMore = true
    
                        // Запускаем сетевой запрос в IO потоке
                        withContext(Dispatchers.IO) {
                            try {
                                val res = api.getMessages(chatId, messagesOffset)
    
                                if (res.messages.isEmpty()) {
                                    canLoadMore = false
                                } else {
                                    withContext(Dispatchers.Main) {
                                        // Обновляем сообщения и сдвигаем offset
                                        groupMessages = (groupMessages + res.messages).distinctBy { it.id }
                                        messagesOffset += res.messages.size
    
                                        // Если пришло мало — значит это был последний кусок
                                        if (res.messages.size < 20) canLoadMore = false
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                isLoadingMore = false
                            }
                        }
                    }
                }
            }
        // ✨ ИЗМЕНЕНИЕ 3: Вычисляем, нужно ли грузить еще, ДЛЯ КАЖДОЙ ВКЛАДКИ ОТДЕЛЬНО
        val shouldLoadMore by remember(pagerState.currentPage, listStates) {
            derivedStateOf {
                val currentState = listStates[pagerState.currentPage]
                val total = currentState.layoutInfo.totalItemsCount
                val last = currentState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
    
                // Если доскроллили почти до конца (осталось 2-3 элемента) и есть что грузить
                total > 0 && last >= total - 3
            }
        }
    
        // ✨ ИЗМЕНЕНИЕ 4: Мощная и правильная подгрузка
        LaunchedEffect(shouldLoadMore, canLoadMore, isLoadingMore) {
            // Запускаем только если нужно грузить, можно грузить и сейчас не грузим
            if (shouldLoadMore && canLoadMore && !isLoadingMore) {
                isLoadingMore = true
                kotlinx.coroutines.Dispatchers.IO.invoke {
                    try {
                        var fetched = 0
                        val newMsgs = mutableListOf<Message>()
    
                        // Пытаемся вытянуть до 60 сообщений за раз (3 запроса по 20)
                        while (fetched < 60 && canLoadMore) {
                            // Используем НАШ offset, а не размер массива (размер массива может быть неточным из-за фильтрации)
                            val res = api.getMessages(chatId, messagesOffset)
    
                            if (res.messages.isEmpty()) {
                                canLoadMore = false
                                break
                            }
    
                            newMsgs.addAll(res.messages)
                            fetched += res.messages.size
                            messagesOffset += res.messages.size // Увеличиваем offset
    
                            if (res.messages.size < 20) canLoadMore = false
                        }
    
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            // Объединяем старые и новые, убирая дубликаты
                            groupMessages = (groupMessages + newMsgs).distinctBy { it.id }
                            isLoadingMore = false
                        }
                    } catch (e: Exception) {
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { isLoadingMore = false }
                    }
                }
            }
        }
    
        LaunchedEffect(pagerState.currentPage) { searchUserId = null }
        val chatUsers = remember(groupMessages) {
            groupMessages.map { it.senderId to it.senderAvatar }.distinctBy { it.first }
        }
        androidx.compose.ui.window.Dialog(
            onDismissRequest = onBack,
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F0F0F))
                    .statusBarsPadding()
                    .pointerInput(Unit) { detectTapGestures {} }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF38BDF8))
                } else if (groupInfo != null) {
                    val group = groupInfo!!
    
                    // --- 1. ПЕЙДЖЕР СО СПИСКАМИ (НА ЗАДНЕМ ФОНЕ) ---
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        LazyColumn(
                            state = listStates[page],
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item { Spacer(modifier = Modifier.height(expandedHeight + tabsHeight)) }
    
                            // ✨ 1. ВЫДЕЛЯЕМ СООБЩЕНИЯ ТОЛЬКО ДЛЯ ТЕКУЩЕЙ ВКЛАДКИ
                            val tabMessages = when (page) {
                                1 -> groupMessages.filter { it.image_thumb != null || it.image_master != null || it.video_master != null }
                                2 -> groupMessages.filter { it.document_url != null }
                                3 -> groupMessages.filter { it.text?.let { txt -> Patterns.WEB_URL.matcher(txt).find() } == true }
                                4 -> groupMessages.filter { it.audio_stream != null || it.audio_master != null }
                                5 -> groupMessages.filter { it.gif_url != null || it.image_thumb?.endsWith(".gif") == true }
                                else -> emptyList()
                            }
    
                            // ✨ 2. ПОЛУЧАЕМ АВАТАРКИ ЛЮДЕЙ, КОТОРЫЕ ЧТО-ТО ПОСТИЛИ ИМЕННО СЮДА
                            // (Даже если они уже вышли из чата, их данные остались в сообщениях!)
                            val tabUsers = tabMessages.map { it.senderId to it.senderAvatar }.distinctBy { it.first }
    
                            // ✨ 3. РИСУЕМ ФИЛЬТР ТОЛЬКО ЕСЛИ ТУТ ЕСТЬ КОНТЕНТ И ЭТО НЕ ВКЛАДКА MEMBERS
                            // ✨ РИСУЕМ ФИЛЬТР СО ВСЕМИ УЧАСТНИКАМИ
                            if (page > 0 && allUsersForFilter.isNotEmpty()) {
                                item {
                                    androidx.compose.foundation.lazy.LazyRow(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        item {
                                            Box(
                                                modifier = Modifier.size(40.dp).clip(CircleShape).background(if (searchUserId == null) Color(0xFF38BDF8) else Color(0xFF38BDF8).copy(alpha = 0.2f)).clickable { searchUserId = null },
                                                contentAlignment = Alignment.Center
                                            ) { Text("Все", color = if (searchUserId == null) Color.Black else Color.White, fontSize = 12.sp) }
                                        }
                                        items(allUsersForFilter) { (userId, avatar) ->
                                            Box(
                                                modifier = Modifier.size(40.dp).clip(CircleShape).border(2.dp, if (searchUserId == userId) Color(0xFF38BDF8) else Color.Transparent, CircleShape).clickable { searchUserId = userId }
                                            ) { AsyncImage(model = avatar, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().background(Color.Gray)) }
                                        }
                                    }
                                }
                            }
    
                            // ✨ 4. ФИЛЬТРУЕМ КОНТЕНТ ПО ВЫБРАННОМУ ЮЗЕРУ
                            val filteredMsgs = tabMessages.filter { searchUserId == null || it.senderId == searchUserId }
    
                            when (page) {
                                0 -> { // Members
                                    items(group.members) { member ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().clickable { selectedUserId = member.id }.padding(horizontal = 16.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AsyncImage(model = member.avatar, contentDescription = null, modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF1E1E1E)), contentScale = ContentScale.Crop)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(member.name, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                                                Text("online", color = Color(0xFF38BDF8), fontSize = 13.sp)
                                            }
                                            if (!member.nickname.isNullOrBlank()) Text(member.nickname, color = Color(0xFF38BDF8), fontSize = 13.sp)
                                            else if (member.isOwner) Text("владелец", color = Color.Gray, fontSize = 13.sp)
                                        }
                                    }
                                }
    
                                1 -> { // Media
                                    if (filteredMsgs.isEmpty()) item { Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { Text("Нет медиа", color = Color.Gray) } }
                                    else {
                                        items(filteredMsgs.chunked(3)) { rowItems ->
                                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                rowItems.forEach { msg ->
                                                    val url = msg.image_thumb ?: msg.video_master ?: msg.image_master
                                                    var bounds by remember { mutableStateOf<Rect?>(null) }
    
                                                    Box(
                                                        modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(Color(0xFF1E1E1E))
                                                            .onGloballyPositioned { bounds = it.boundsInWindow() }
                                                            // ✨ КЛИКАБЕЛЬНОСТЬ И ВЫЗОВ ВЬЮВЕРА
                                                            .clickable {
                                                                // ✨ ВЫЗЫВАЕМ ВЬЮВЕР
                                                                // Передаем все сообщения этой вкладки, чтобы можно было листать
                                                                onImageClick(filteredMsgs, msg.id, bounds)
                                                            }
                                                    ) {
                                                        // Парсим кадр видео или картинку
                                                        AsyncImage(
                                                            model = ImageRequest.Builder(LocalContext.current).data(url).apply { if (msg.video_master != null) { decoderFactory(coil.decode.VideoFrameDecoder.Factory()); videoFrameMicros(1000000) } }.build(),
                                                            contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                                                        )
    
                                                        // ✨ ЕСЛИ ВИДЕО - РИСУЕМ ПРЕВЬЮ КАК В ЧАТЕ
                                                        if (msg.video_master != null) {
                                                            Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.2f)))
    
                                                            // Плашка с таймером в левом нижнем углу
                                                            Row(
                                                                modifier = Modifier.align(Alignment.BottomStart).padding(4.dp).clip(RoundedCornerShape(4.dp)).background(Color.Black.copy(0.5f)).padding(horizontal = 4.dp, vertical = 2.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(msg.audio_duration ?: "0:00", color = Color.White, fontSize = 10.sp)
                                                            }
    
                                                            // Кнопка Play по центру
                                                            Box(modifier = Modifier.align(Alignment.Center).size(24.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                                                                Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_play), contentDescription = "Play", tint = Color.White, modifier = Modifier.size(12.dp).padding(start = 1.dp))
                                                            }
                                                        }
                                                    }
                                                }
                                                repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                                            }
                                        }
                                    }
                                }
    
                                2 -> { // Files
                                    if (filteredMsgs.isEmpty()) item { Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { Text("Нет файлов", color = Color.Gray) } }
                                    items(filteredMsgs) { msg ->
                                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF38BDF8).copy(0.2f)), contentAlignment = Alignment.Center) { Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_file), contentDescription = null, tint = Color(0xFF38BDF8)) }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(msg.document_name ?: "Файл", color = Color.White, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(msg.document_size ?: "", color = Color.Gray, fontSize = 12.sp)
                                            }
                                            IconButton(onClick = { downloadFile(context, msg.document_url!!, msg.document_name ?: "file") }) { Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_download), contentDescription = null, tint = Color.Gray) }
                                        }
                                    }
                                }
    
                                3 -> { // Links
                                    if (filteredMsgs.isEmpty()) item { Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { Text("Нет ссылок", color = Color.Gray) } }
                                    items(filteredMsgs) { msg ->
                                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).background(Color.White.copy(0.05f), RoundedCornerShape(12.dp)).padding(12.dp)) {
                                            Text(msg.senderName ?: "Кто-то", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                                            MessageTextWithLinks(text = msg.text!!, textColor = Color.White)
                                        }
                                    }
                                }
    
                                4 -> { // Music
                                    if (filteredMsgs.isEmpty()) item { Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { Text("Нет аудио", color = Color.Gray) } }
                                    items(filteredMsgs) { msg -> AudioPlayerBubble(message = msg, isMe = false) }
                                }
    
                                5 -> { // GIFs
                                    if (filteredMsgs.isEmpty()) item { Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { Text("Нет GIF", color = Color.Gray) } }
                                    items(filteredMsgs.chunked(3)) { rowItems ->
                                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            rowItems.forEach { msg ->
                                                val url = msg.gif_url ?: msg.image_thumb
                                                Box(modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(Color(0xFF1E1E1E))) {
                                                    AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                                }
                                            }
                                            repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                                        }
                                    }
                                }
                            }
                            if (isLoadingMore) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.navigationBarsPadding()) }
                        }
                    }
    
                    // --- 2. ШАПКА И ВКЛАДКИ (ПОВЕРХ СПИСКОВ) ---
                    val currentHeaderHeight = androidx.compose.ui.unit.lerp(expandedHeight, collapsedHeight, collapseFraction)
    
                    Column(modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth()) {
    
                        // АНИМИРОВАННАЯ ШАПКА
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(currentHeaderHeight)
                                .background(Color(0xFF1E1E1E).copy(alpha = collapseFraction))
                        ) {
                            val avatarSize = androidx.compose.ui.unit.lerp(screenWidth, 44.dp, collapseFraction)
                            val avatarRadius = androidx.compose.ui.unit.lerp(0.dp, 22.dp, collapseFraction)
                            val avatarPaddingEnd = androidx.compose.ui.unit.lerp(0.dp, 16.dp, collapseFraction)
                            val avatarPaddingTop = androidx.compose.ui.unit.lerp(0.dp, 10.dp, collapseFraction)
    
                            AsyncImage(
                                model = group.avatar,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = avatarPaddingTop, end = avatarPaddingEnd)
                                    .size(width = avatarSize, height = if (collapseFraction == 0f) expandedHeight else avatarSize)
                                    .clip(RoundedCornerShape(avatarRadius))
                            )
    
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(1f - collapseFraction)
                                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF0F0F0F)), startY = 200f))
                            )
    
                            // ✨ КНОПКИ СЛЕВА СВЕРХУ (Назад и Редактировать)
                            Row(
                                modifier = Modifier.align(Alignment.TopStart).padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = onBack) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                                }
                                // Кнопка редактирования теперь тут, а три точки убрали
                                IconButton(onClick = { showEditScreen = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Редактировать", tint = Color.White)
                                }
                            }
    
                            // Сжатый текст (появляется при скролле)
                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(start = 96.dp, top = 12.dp) // Сдвинули правее, чтобы не наезжало на две кнопки
                                    .alpha(collapseFraction)
                            ) {
                                Text(group.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${group.members.size} members", color = Color.Gray, fontSize = 13.sp)
                            }
    
                            // Развернутый текст и кнопки
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .alpha((1f - collapseFraction * 2).coerceIn(0f, 1f))
                                    .padding(horizontal = 16.dp, vertical = 16.dp)
                            ) {
                                Text(group.name, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                Text("${group.members.size} members", color = Color.White.copy(0.7f), fontSize = 14.sp)

                                if (!group.description.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(group.description, color = Color.White, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
    
                                Spacer(modifier = Modifier.height(16.dp))
    
                                // Кнопки (Edit убрали отсюда)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ProfileActionButton(icon = com.composables.icons.lucide.R.drawable.lucide_ic_message_circle, text = "Message", onClick = onBack)
                                    ProfileActionButton(
                                        icon = if (isMuted) com.composables.icons.lucide.R.drawable.lucide_ic_bell_off else com.composables.icons.lucide.R.drawable.lucide_ic_bell,
                                        text = if (isMuted) "Unmute" else "Mute",
                                        onClick = {
                                            scope.launch { try { isMuted = api.toggleGroupMute(chatId).is_muted } catch (e: Exception) {} }
                                        }
                                    )
                                    ProfileActionButton(
                                        icon = com.composables.icons.lucide.R.drawable.lucide_ic_link,
                                        text = "Copy Link",
                                        onClick = {
                                            group.inviteToken?.let {
                                                clipboardManager.setText(AnnotatedString("https://sonzaiigi.com/join/$it"))
                                                Toast.makeText(context, "Ссылка скопирована", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                    ProfileActionButton(
                                        icon = com.composables.icons.lucide.R.drawable.lucide_ic_log_out,
                                        text = "Leave",
                                        onClick = {
                                            scope.launch {
                                                if (api.leaveGroup(chatId).isSuccessful) {
                                                    Toast.makeText(context, "Вы вышли", Toast.LENGTH_SHORT).show()
                                                    onExitChat()
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
    
                        // ВКЛАДКИ
                        Box(modifier = Modifier.fillMaxWidth().height(tabsHeight).background(Color(0xFF0F0F0F))) {
                            ScrollableTabRow(
                                selectedTabIndex = pagerState.currentPage,
                                containerColor = Color.Transparent,
                                contentColor = Color.White,
                                edgePadding = 16.dp,
                                divider = { HorizontalDivider(color = Color.White.copy(0.1f)) },
                                indicator = { tabPositions ->
                                    TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]), color = Color(0xFF38BDF8))
                                }
                            ) {
                                tabs.forEachIndexed { index, title ->
                                    Tab(
                                        selected = pagerState.currentPage == index,
                                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                        text = {
                                            Text(title, color = if (pagerState.currentPage == index) Color(0xFF38BDF8) else Color.Gray, fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    )
                                }
                            }
                        }
                    }
    
                    // --- 3. МОДАЛКИ ---
                    if (showEditScreen && groupInfo != null) {
                        GroupEditScreen(
                            group = groupInfo!!,
                            api = api,
                            onBack = { showEditScreen = false },
                            onGroupUpdated = { scope.launch { try { groupInfo = api.getGroupInfo(chatId) } catch (e: Exception) {} } }
                        )
                    }
    
                    if (selectedUserId != null) {
                        UserProfileScreen(
                            userId = selectedUserId!!,
                            api = api,
                            onBack = { selectedUserId = null },
                            onOpenChat = { selectedUserId = null }
                        )
                    }
                }
            }
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun UserProfileScreen(
        userId: Int,
        api: ApiService,
        onBack: () -> Unit,
        onOpenChat: (Int) -> Unit
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val isMe = userId == AuthManager.userId
    
        var userData by remember { mutableStateOf<UserData?>(null) }
        var isLoading by remember { mutableStateOf(true) }
    
        var isFollowing by remember { mutableStateOf(false) }
        var isMutual by remember { mutableStateOf(false) }
        var isMuted by remember { mutableStateOf(false) }
        var isBlocking by remember { mutableStateOf(false) } // ✨ Стейт блокировки
        var isActionLoading by remember { mutableStateOf(false) }
    
        var showEditScreen by remember { mutableStateOf(false) } // Для открытия редактора
    
        fun loadProfile() {
            scope.launch {
                isLoading = true
                try {
                    val profileRes = api.getUserProfile(userId)
                    userData = profileRes.user
    
                    if (!isMe) {
                        val relRes = api.getUserRelations(userId)
                        isFollowing = relRes.is_following
                        isMutual = relRes.is_mutual
                        isMuted = relRes.is_muted
                        isBlocking = relRes.is_blocking // ✨ Читаем из API
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Ошибка", Toast.LENGTH_SHORT).show()
                } finally {
                    isLoading = false
                }
            }
        }
    
        LaunchedEffect(userId) { loadProfile() }
    
        Dialog(onDismissRequest = onBack, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F)).pointerInput(Unit) { detectTapGestures {} }) {
    
                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFF38BDF8), modifier = Modifier.align(Alignment.Center))
                } else if (userData != null) {
                    val user = userData!!
    
                    Column(modifier = Modifier.fillMaxSize()) {
                        // --- ШАПКА ПРОФИЛЯ ---
                        Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                            AsyncImage(model = user.avatar, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E)))
                            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF0F0F0F)), startY = 150f)))
                            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(8.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                            }
    
                            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                                Text(user.name ?: "Без имени", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                if (!user.username.isNullOrBlank()) Text("@${user.username}", color = Color(0xFF38BDF8), fontSize = 16.sp)
                                Text(if (isMe) "Это вы" else "был(а) в сети недавно", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
    
                        // --- КНОПКИ ДЕЙСТВИЙ ---
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (isMe) {
                                Button(
                                    onClick = { showEditScreen = true }, // ✨ Открываем редактор
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Редактировать профиль", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                // Подписка
                                if (!isBlocking) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            isActionLoading = true
                                            try {
                                                val res = api.toggleFollow(user.id)
                                                isFollowing = res.is_following
                                                isMutual = res.is_mutual
                                            } catch (e: Exception) {} finally { isActionLoading = false }
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isFollowing) Color(0xFF1E1E1E) else Color(0xFF38BDF8), contentColor = if (isFollowing) Color.White else Color.Black),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isActionLoading
                                ) { Text(if (isFollowing) "Отписаться" else "Подписаться", fontWeight = FontWeight.Bold) }
                            } else {
                            // Выводим подсказку
                            Text("Вы заблокировали пользователя", color = Color.Gray, fontSize = 12.sp)
                        }
                                // Написать
                                androidx.compose.animation.AnimatedVisibility(visible = isMutual && !isBlocking) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                isActionLoading = true
                                                try {
                                                    val res = api.startConversation(user.id)
                                                    onOpenChat(res.chatId)
                                                    onBack()
                                                } catch (e: Exception) {} finally { isActionLoading = false }
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(12.dp),
                                        enabled = !isActionLoading
                                    ) {
                                        Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_message_circle), contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Написать", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
    
                                // ✨ БЛОКИРОВКА (ЧС)
                                Button(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                val res = api.toggleBlock(user.id)
                                                isBlocking = res.is_blocked
                                                // Если мы заблокировали, подписка обычно слетает (зависит от твоей логики бэка)
                                                if (isBlocking) { isFollowing = false; isMutual = false }
                                            } catch (e: Exception) {}
                                        }
                                    },
                                    modifier = Modifier.size(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isBlocking) Color(0xFFEF4444).copy(0.2f) else Color(0xFF1E1E1E)),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) { Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_ban), contentDescription = "Бан", tint = if (isBlocking) Color(0xFFEF4444) else Color.Gray) }
    
                                // МУТ
                                Button(
                                    onClick = { scope.launch { try { isMuted = api.toggleUserMute(user.id).is_muted } catch (e: Exception) {} } },
                                    modifier = Modifier.size(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isMuted) Color(0xFF38BDF8).copy(0.2f) else Color(0xFF1E1E1E)),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) { Icon(painterResource(if (isMuted) com.composables.icons.lucide.R.drawable.lucide_ic_bell_off else com.composables.icons.lucide.R.drawable.lucide_ic_bell), contentDescription = "Мут", tint = if (isMuted) Color(0xFF38BDF8) else Color.White) }
                            }
                        }
    
                        // --- О СЕБЕ (БИО) ---
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                            Text("О себе", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (user.bio.isNullOrBlank()) "Пользователь пока ничего не рассказал о себе." else user.bio,
                                color = if (user.bio.isNullOrBlank()) Color.Gray else Color.White,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )
                        }
                    }
    
                    // ✨ ОТКРЫТИЕ РЕДАКТОРА ПРОФИЛЯ
                    if (showEditScreen && isMe) {
                        EditProfileScreen(
                            user = user,
                            api = api,
                            onBack = { showEditScreen = false },
                            onProfileUpdated = { loadProfile() } // Перезагружаем профиль после сохранения
                        )
                    }
                }
            }
        }
    }
    // Кнопка стала компактной, иконка и текст сгруппированы красиво
    @Composable
    fun RowScope.ProfileActionButton(
        icon: Int,
        text: String,
        onClick: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF242629)) // Темно-серый цвет, как на фото
                .clickable { onClick() }
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(painterResource(icon), contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
    // Финальная склейка рисунков, текста и фото перед отправкой
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun EditProfileScreen(
        user: UserData,
        api: ApiService,
        onBack: () -> Unit,
        onProfileUpdated: () -> Unit
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
    
        var name by remember { mutableStateOf(user.name ?: "") }
        var username by remember { mutableStateOf(user.username ?: "") }
        var bio by remember { mutableStateOf(user.bio ?: "") }
    
        var newAvatarUri by remember { mutableStateOf<Uri?>(null) }
        val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) newAvatarUri = uri
        }
    
        var isSaving by remember { mutableStateOf(false) }
    
        fun saveProfile() {
            scope.launch {
                isSaving = true
                try {
                    val nameReq = name.toRequestBody("text/plain".toMediaTypeOrNull())
                    val userReq = username.toRequestBody("text/plain".toMediaTypeOrNull())
                    val bioReq = bio.toRequestBody("text/plain".toMediaTypeOrNull())
    
                    var avatarPart: MultipartBody.Part? = null
                    if (newAvatarUri != null) {
                        val stream = context.contentResolver.openInputStream(newAvatarUri!!)
                        val bytes = stream?.readBytes()
                        stream?.close()
                        if (bytes != null) {
                            val reqFile = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                            avatarPart = MultipartBody.Part.createFormData("Avatar", "avatar.jpg", reqFile)
                        }
                    }
    
                    // Вызываем без emailReq
                    val response = api.updateProfile(nameReq, userReq, bioReq, avatarPart)
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Профиль обновлен", Toast.LENGTH_SHORT).show()
                        onProfileUpdated()
                        onBack()
                    } else {
                        // Теперь мы точно увидим, на что ругается сервер
                        val errorStr = response.errorBody()?.string() ?: "Ошибка"
                        val msg = if (errorStr.contains("имя пользователя уже занято")) "Никнейм занят" else "Ошибка сервера"
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Ошибка сети", Toast.LENGTH_SHORT).show()
                } finally {
                    isSaving = false
                }
            }
        }
    
        Dialog(onDismissRequest = onBack, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
            Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F)).statusBarsPadding()) {
    
                // Шапка
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White) }
                        Text("Редактировать", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { saveProfile() }, enabled = !isSaving) {
                        if (isSaving) CircularProgressIndicator(color = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
                        else Icon(Icons.Default.Check, contentDescription = "Сохранить", tint = Color(0xFF38BDF8))
                    }
                }
    
                // Форма
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    // Аватар
                    Box(
                        modifier = Modifier.size(100.dp).clip(CircleShape).background(Color(0xFF1E1E1E)).clickable { imagePicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (newAvatarUri != null) {
                            AsyncImage(model = newAvatarUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        } else if (user.avatar != null) {
                            AsyncImage(model = user.avatar, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        } else {
                            Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_camera), contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
    
                    // Имя
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Имя", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF38BDF8), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
    
                    // Юзернейм
                    OutlinedTextField(
                        value = username, onValueChange = { username = it },
                        label = { Text("Имя пользователя (никнейм)", color = Color.Gray) },
                        leadingIcon = { Text("@", color = Color.Gray, modifier = Modifier.padding(start = 12.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF38BDF8), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
    
                    // О себе
                    OutlinedTextField(
                        value = bio, onValueChange = { bio = it },
                        label = { Text("О себе", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF38BDF8), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Text("Любые подробности о вас: хобби, работа, интересы.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start).padding(top = 4.dp))
                }
            }
        }
    }
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    fun GroupEditScreen(
        group: GroupProfileResponse,
        api: ApiService,
        onBack: () -> Unit,
        onGroupUpdated: () -> Unit
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val clipboardManager = LocalClipboardManager.current
    
        val tabs = listOf("Основное", "Пользователи", "Роли", "Прочее")
        val pagerState = rememberPagerState(pageCount = { tabs.size })
        var isLoading by remember { mutableStateOf(false) }
    
        // --- ЛОКАЛЬНЫЕ ДАННЫЕ ДЛЯ БЕСШОВНОГО ОБНОВЛЕНИЯ ---
        var currentMembers by remember { mutableStateOf(group.members) }
        var localRoles by remember { mutableStateOf(group.roles ?: emptyList()) }
        var bannedUsers by remember { mutableStateOf(group.bannedUsers ?: emptyList()) }
        var isPrivate by remember { mutableStateOf(group.isPrivate) }
        val myMember = remember(currentMembers) { currentMembers.find { it.id == AuthManager.userId } }
        val myHighestHierarchy = remember(myMember, localRoles) {
            if (myMember?.isOwner == true) -1
            else myMember?.roleIds?.mapNotNull { rId -> localRoles.find { it.id == rId }?.hierarchy }?.minOrNull() ?: 999
        }
    // --- СТЕЙТЫ ПОДТВЕРЖДЕНИЙ ---
        var roleToDelete by remember { mutableStateOf<GroupRole?>(null) }
        var showDeleteGroupDialog by remember { mutableStateOf(false) }
        var isDeletingGroup by remember { mutableStateOf(false) }
        // --- СТЕЙТЫ ОСНОВНОЙ ВКЛАДКИ ---
        var name by remember { mutableStateOf(group.name) }
        var description by remember { mutableStateOf(group.description ?: "") }
        var inviteToken by remember { mutableStateOf(group.inviteToken ?: "") }
        var newAvatarUri by remember { mutableStateOf<Uri?>(null) }
        val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) newAvatarUri = uri
        }
    
        // --- СТЕЙТЫ "ПРОЧЕЕ" ---
        var sysMsgJoin by remember { mutableStateOf(group.sysMsgs?.join ?: false) }
        var sysMsgLeave by remember { mutableStateOf(group.sysMsgs?.leave ?: false) }
        var slowMode by remember { mutableIntStateOf(group.slowMode ?: 0) }
        var expandBans by remember { mutableStateOf(false) }
        var expandLogs by remember { mutableStateOf(false) }
    
        // --- СТЕЙТЫ РЕДАКТИРОВАНИЯ ---
        var editingMember by remember { mutableStateOf<GroupMemberProfile?>(null) }
        var editingRole by remember { mutableStateOf<GroupRole?>(null) }
        var isCreatingNewRole by remember { mutableStateOf(false) }
    
        // ✨ Функция для тихой подгрузки данных, чтобы экран не моргал
        fun reloadSilently() {
            scope.launch {
                try {
                    val updatedGroup = api.getGroupInfo(group.id)
                    currentMembers = updatedGroup.members
                    localRoles = updatedGroup.roles ?: emptyList()
                    bannedUsers = updatedGroup.bannedUsers ?: emptyList()
                    onGroupUpdated() // Сообщаем родителю, что данные изменились
                } catch (e: Exception) {}
            }
        }
    
        fun saveChanges() {
            scope.launch {
                isLoading = true
                try {
                    val nameReq = name.toRequestBody("text/plain".toMediaTypeOrNull())
                    val descReq = description.toRequestBody("text/plain".toMediaTypeOrNull())
                    val tokenReq = inviteToken.toRequestBody("text/plain".toMediaTypeOrNull())
                    val slowModeReq = slowMode.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val joinReq = sysMsgJoin.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val leaveReq = sysMsgLeave.toString().toRequestBody("text/plain".toMediaTypeOrNull())
    
                    var avatarPart: MultipartBody.Part? = null
                    if (newAvatarUri != null) {
                        val stream = context.contentResolver.openInputStream(newAvatarUri!!)
                        val bytes = stream?.readBytes()
                        stream?.close()
                        if (bytes != null) {
                            val reqFile = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                            avatarPart = MultipartBody.Part.createFormData("Avatar", "avatar.jpg", reqFile)
                        }
                    }
                    val isPrivReq = isPrivate.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val res = api.updateGroup(group.id, nameReq, descReq, tokenReq, slowModeReq, joinReq, leaveReq, isPrivate = isPrivReq,avatarPart)
                    if (res.isSuccessful) {
                        Toast.makeText(context, "Сохранено", Toast.LENGTH_SHORT).show()
                        onGroupUpdated()
                        onBack()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Ошибка сети", Toast.LENGTH_SHORT).show()
                } finally {
                    isLoading = false
                }
            }
        }
    
        fun swapRoles(index1: Int, index2: Int, editableRoles: List<GroupRole>) {
            if (index1 !in editableRoles.indices || index2 !in editableRoles.indices) return
            val role1 = editableRoles[index1]
            val role2 = editableRoles[index2]
    
            if (role1.hierarchy <= myHighestHierarchy || role2.hierarchy <= myHighestHierarchy) {
                Toast.makeText(context, "Недостаточно прав", Toast.LENGTH_SHORT).show()
                return
            }
    
            // Меняем местами во временном списке
            val newList = localRoles.toMutableList()
            val realIndex1 = newList.indexOfFirst { it.id == role1.id }
            val realIndex2 = newList.indexOfFirst { it.id == role2.id }
    
            val temp = newList[realIndex1]
            newList[realIndex1] = newList[realIndex2]
            newList[realIndex2] = temp
            localRoles = newList
    
            scope.launch {
                try {
                    api.reorderRoles(group.id, ReorderRolesDto(newList.map { it.id }))
                    reloadSilently()
                } catch (e: Exception) {}
            }
        }
    
        Dialog(onDismissRequest = onBack, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
            Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F)).statusBarsPadding()) {
    
                // --- ШАПКА ---
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White) }
                        Text("Редактирование", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { saveChanges() }, enabled = !isLoading) {
                        if (isLoading) CircularProgressIndicator(color = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
                        else Icon(Icons.Default.Check, contentDescription = "Сохранить", tint = Color(0xFF38BDF8))
                    }
                }
    
                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    edgePadding = 8.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]), color = Color(0xFF38BDF8))
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = { Text(title, color = if (pagerState.currentPage == index) Color(0xFF38BDF8) else Color.Gray) }
                        )
                    }
                }
    
                HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        when (page) {
                            0 -> {
                                // --- ВКЛАДКА 0: ОСНОВНОЕ (без изменений) ---
                                item {
                                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Color(0xFF1E1E1E)).clickable { imagePicker.launch("image/*") }, contentAlignment = Alignment.Center) {
                                            if (newAvatarUri != null) AsyncImage(model = newAvatarUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                            else if (group.avatar != null) AsyncImage(model = group.avatar, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                            else Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_camera), contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                                        }
                                        Spacer(modifier = Modifier.height(24.dp))
                                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Название группы", color = Color.Gray) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF38BDF8), focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                                        Spacer(modifier = Modifier.height(16.dp))
                                        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Описание", color = Color.Gray) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF38BDF8), focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Приватная группа", color = Color.White, fontWeight = FontWeight.Bold)
                                                Text("Скрывает и отключает ссылку-приглашение", color = Color.Gray, fontSize = 12.sp)
                                            }
                                            Switch(
                                                checked = isPrivate,
                                                onCheckedChange = { isPrivate = it },
                                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF38BDF8))
                                            )
                                        }
    
                                        Spacer(modifier = Modifier.height(16.dp))
    
                                        // ✨ ССЫЛКА ПОКАЗЫВАЕТСЯ ТОЛЬКО ЕСЛИ ГРУППА НЕ ПРИВАТНАЯ
                                        if (!isPrivate) {
                                            Text("Ссылка-приглашение", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            OutlinedTextField(value = inviteToken, onValueChange = { inviteToken = it }, leadingIcon = { Text("sonzaiigi.com/join/", color = Color.Gray, modifier = Modifier.padding(start = 12.dp)) }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF38BDF8), focusedTextColor = Color.White, unfocusedTextColor = Color.White))
    
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                TextButton(onClick = { clipboardManager.setText(AnnotatedString("https://sonzaiigi.com/join/$inviteToken")); Toast.makeText(context, "Скопировано", Toast.LENGTH_SHORT).show() }) { Text("Скопировать", color = Color.White) }
                                                TextButton(onClick = {
                                                    scope.launch { try { inviteToken = api.resetGroupLink(group.id).new_token } catch (e: Exception) {} }
                                                }) { Text("Сбросить", color = Color(0xFFEF4444)) }
                                            }
                                        }
                                        if (myMember?.isOwner == true) {
                                            Spacer(modifier = Modifier.height(32.dp))
                                            HorizontalDivider(color = Color.White.copy(0.1f), modifier = Modifier.padding(bottom = 16.dp))
    
                                            Button(
                                                onClick = { showDeleteGroupDialog = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.1f)),
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                                            ) {
                                                Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_trash_2), contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Удалить группу", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
    
                            1 -> {
                                // --- ВКЛАДКА 1: ПОЛЬЗОВАТЕЛИ ---
                                items(currentMembers) { member ->
                                    // ✨ Исключаем роль владельца (hierarchy == 0) из визуального отображения
                                    val visibleRoles = member.roleIds
                                        ?.mapNotNull { rId -> localRoles.find { it.id == rId } }
                                        ?.filter { it.hierarchy > 0 }
                                        ?.sortedBy { it.hierarchy } ?: emptyList()
    
                                    val highestRole = visibleRoles.firstOrNull()
                                    val nameColor = highestRole?.let { Color(android.graphics.Color.parseColor(it.color)) } ?: Color.White
    
                                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        AsyncImage(model = member.avatar, contentDescription = null, modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.Gray), contentScale = ContentScale.Crop)
                                        Spacer(modifier = Modifier.width(12.dp))
    
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = member.name, color = nameColor, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp)
    
                                            if (visibleRoles.isNotEmpty()) {
                                                val rolesText = androidx.compose.ui.text.buildAnnotatedString {
                                                    visibleRoles.forEachIndexed { index, role ->
                                                        withStyle(androidx.compose.ui.text.SpanStyle(color = Color(android.graphics.Color.parseColor(role.color)))) { append(role.name) }
                                                        if (index < visibleRoles.size - 1) withStyle(androidx.compose.ui.text.SpanStyle(color = Color.Gray)) { append(", ") }
                                                    }
                                                }
                                                Text(text = rolesText, fontSize = 12.sp, lineHeight = 16.sp)
                                            } else {
                                                Text("в сети", color = Color.Gray, fontSize = 12.sp, lineHeight = 16.sp)
                                            }
    
                                            if (member.isOwner) Text("владелец", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                                        }
    
                                        if (!member.isOwner || member.id == AuthManager.userId) {
                                            // ✨ Кнопку "Редактировать" теперь видят все не-владельцы, ПЛЮС сам пользователь, если он владелец (чтобы выдать себе цвет)
                                            IconButton(onClick = { editingMember = member }) { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray) }
                                        }
    
                                        if (!member.isOwner && member.id != AuthManager.userId) {
                                            // Кик и Бан показываем только для ДРУГИХ участников (не владельцев)
                                            IconButton(onClick = {
                                                scope.launch {
                                                    try {
                                                        val res = api.manageMember(group.id, member.id, "kick")
                                                        if (res.isSuccessful) reloadSilently()
                                                    } catch (e: Exception) {}
                                                }
                                            }) { Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_user_minus), contentDescription = null, tint = Color(0xFFF59E0B)) }
    
                                            IconButton(onClick = {
                                                scope.launch {
                                                    try {
                                                        val res = api.manageMember(group.id, member.id, "ban")
                                                        if (res.isSuccessful) reloadSilently()
                                                    } catch (e: Exception) {}
                                                }
                                            }) { Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_ban), contentDescription = null, tint = Color(0xFFEF4444)) }
                                        }
                                    }
                                }
                            }
    
                            2 -> {
                                // --- ВКЛАДКА 2: РОЛИ ---
                                item {
                                    OutlinedButton(onClick = { isCreatingNewRole = true }, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                        Text("Создать роль", color = Color.White)
                                    }
                                }
    
                                // ✨ Фильтруем роль владельца (hierarchy == 0), чтобы она не отображалась в списке
                                val editableRoles = localRoles.filter { it.hierarchy > 0 }.sortedBy { it.hierarchy }
    
                                itemsIndexed(editableRoles) { index, role ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { editingRole = role }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color(android.graphics.Color.parseColor(role.color))))
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(role.name, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
    
                                        // Кнопки ВВЕРХ/ВНИЗ
                                        if (role.hierarchy > myHighestHierarchy) {
                                            if (index < editableRoles.size - 1 && editableRoles[index + 1].hierarchy > myHighestHierarchy) {
                                                IconButton(onClick = { swapRoles(index, index + 1, editableRoles) }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray) }
                                            } else { Spacer(modifier = Modifier.width(32.dp)) }
    
                                            if (index > 0 && editableRoles[index - 1].hierarchy > myHighestHierarchy) {
                                                IconButton(onClick = { swapRoles(index, index - 1, editableRoles) }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = Color.Gray) }
                                            } else { Spacer(modifier = Modifier.width(32.dp)) }
                                        }
    
                                        Spacer(modifier = Modifier.width(8.dp))
    
                                        // ✨ Кнопка удаления роли
                                        // ✨ Кнопка удаления роли вызывает диалог
                                        if (role.hierarchy > myHighestHierarchy) {
                                            IconButton(onClick = { roleToDelete = role }, modifier = Modifier.size(32.dp)) {
                                                Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_trash_2), contentDescription = "Удалить", tint = Color(0xFFEF4444))
                                            }
                                        }
    
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
    
                            3 -> {
                                // --- ВКЛАДКА "ПРОЧЕЕ" ---
                                item {
                                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                        Text("Системные сообщения", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Оповещать, если человек зашел", color = Color.White)
                                            Switch(checked = sysMsgJoin, onCheckedChange = { sysMsgJoin = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF38BDF8)))
                                        }
                                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Оповещать, если человек вышел", color = Color.White)
                                            Switch(checked = sysMsgLeave, onCheckedChange = { sysMsgLeave = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF38BDF8)))
                                        }
    
                                        HorizontalDivider(color = Color.White.copy(0.1f), modifier = Modifier.padding(vertical = 16.dp))
    
                                        Text("Медленный режим", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                                        val slowModeOptions = listOf(0 to "Выкл", 10 to "10 сек", 30 to "30 сек", 60 to "1 мин", 300 to "5 мин")
                                        var expandedSlowMode by remember { mutableStateOf(false) }
                                        Box(modifier = Modifier.padding(top = 8.dp)) {
                                            OutlinedButton(onClick = { expandedSlowMode = true }, modifier = Modifier.fillMaxWidth()) {
                                                Text(slowModeOptions.find { it.first == slowMode }?.second ?: "Выкл", color = Color.White)
                                            }
                                            DropdownMenu(expanded = expandedSlowMode, onDismissRequest = { expandedSlowMode = false }, modifier = Modifier.background(Color(0xFF1E1E1E)).fillMaxWidth()) {
                                                slowModeOptions.forEach { option ->
                                                    DropdownMenuItem(text = { Text(option.second, color = Color.White) }, onClick = { slowMode = option.first; expandedSlowMode = false })
                                                }
                                            }
                                        }
    
                                        HorizontalDivider(color = Color.White.copy(0.1f), modifier = Modifier.padding(vertical = 16.dp))
    
                                        Text("Черный список (${bannedUsers.size})", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                        val visibleBans = if (expandBans) bannedUsers else bannedUsers.take(5)
                                        visibleBans.forEach { user ->
                                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                AsyncImage(model = user.avatar, contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray), contentScale = ContentScale.Crop)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(user.name, color = Color.White, lineHeight = 20.sp)
                                                    Text(user.date, color = Color.Gray, fontSize = 12.sp)
                                                }
                                                IconButton(onClick = {
                                                    scope.launch {
                                                        try {
                                                            val res = api.unbanUser(group.id, user.id)
                                                            if (res.isSuccessful) bannedUsers = bannedUsers.filter { it.id != user.id }
                                                        } catch (e: Exception) {}
                                                    }
                                                }) { Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_x), contentDescription = "Разбанить", tint = Color.Gray) }
                                            }
                                        }
                                        if (bannedUsers.size > 5) {
                                            TextButton(onClick = { expandBans = !expandBans }, modifier = Modifier.fillMaxWidth()) { Text(if (expandBans) "Скрыть" else "Показать все", color = Color(0xFF38BDF8)) }
                                        }
    
                                        HorizontalDivider(color = Color.White.copy(0.1f), modifier = Modifier.padding(vertical = 16.dp))
    
                                        Text("Журнал действий", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                                        val logs = group.auditLog ?: emptyList()
                                        val visibleLogs = if (expandLogs) logs else logs.take(5)
                                        visibleLogs.forEach { log ->
                                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(log.user, color = Color.White, fontWeight = FontWeight.Bold)
                                                    Text(log.time, color = Color.Gray, fontSize = 12.sp)
                                                }
                                                Text(log.action, color = Color.White.copy(0.8f), fontSize = 14.sp)
                                            }
                                        }
                                        if (logs.size > 5) {
                                            TextButton(onClick = { expandLogs = !expandLogs }, modifier = Modifier.fillMaxWidth()) { Text(if (expandLogs) "Скрыть" else "Показать все", color = Color(0xFF38BDF8)) }
                                        }
                                    }
                                }
                            }
                        }
                    }
    
                }
            }
            // --- ДИАЛОГ ПОДТВЕРЖДЕНИЯ УДАЛЕНИЯ РОЛИ ---
            if (roleToDelete != null) {
                Dialog(onDismissRequest = { roleToDelete = null }) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xFF1E1E1E)).padding(20.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Удаление роли", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Вы уверены, что хотите удалить роль «${roleToDelete!!.name}»? Это действие нельзя отменить.", color = Color.Gray, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                TextButton(onClick = { roleToDelete = null }) { Text("Отмена", color = Color.Gray) }
                                Button(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                val res = api.deleteRole(group.id, roleToDelete!!.id)
                                                if (res.isSuccessful) {
                                                    Toast.makeText(context, "Роль удалена", Toast.LENGTH_SHORT).show()
                                                    reloadSilently()
                                                }
                                            } catch (e: Exception) {} finally { roleToDelete = null }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                                ) { Text("Удалить", color = Color.White) }
                            }
                        }
                    }
                }
            }
    
            // --- ДИАЛОГ ПОДТВЕРЖДЕНИЯ УДАЛЕНИЯ ГРУППЫ ---
            if (showDeleteGroupDialog) {
                Dialog(onDismissRequest = { if (!isDeletingGroup) showDeleteGroupDialog = false }) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xFF1E1E1E)).padding(20.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_triangle_alert), contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Удаление группы", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Вы действительно хотите навсегда удалить группу «${group.name}»? Вся история сообщений, медиа и роли будут стерты безвозвратно.", color = Color.Gray, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(20.dp))
    
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                TextButton(onClick = { showDeleteGroupDialog = false }, enabled = !isDeletingGroup) {
                                    Text("Отмена", color = Color.Gray)
                                }
                                Button(
                                    onClick = {
                                        scope.launch {
                                            isDeletingGroup = true
                                            try {
                                                val res = api.deleteGroup(group.id)
                                                if (res.isSuccessful) {
                                                    Toast.makeText(context, "Группа удалена", Toast.LENGTH_SHORT).show()
                                                    showDeleteGroupDialog = false
                                                    onBack() // Закрываем модалку редактирования
                                                    // В идеале, если ты передал коллбэк удаления в onGroupUpdated или onExitChat, вызови его тут, чтобы закрылся сам чат.
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Ошибка", Toast.LENGTH_SHORT).show()
                                            } finally {
                                                isDeletingGroup = false
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                    enabled = !isDeletingGroup
                                ) {
                                    if (isDeletingGroup) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                    else Text("Удалить навсегда", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    
        // --- МОДАЛКА УЧАСТНИКА ---
        if (editingMember != null) {
            val member = editingMember!!
            var localRoleIds by remember { mutableStateOf(member.roleIds ?: emptyList()) }
    
            var permSend by remember { mutableStateOf(member.individualOverrides?.get("sendMessages") ?: true) }
            var permFiles by remember { mutableStateOf(member.individualOverrides?.get("attachFiles") ?: true) }
            var permReact by remember { mutableStateOf(member.individualOverrides?.get("addReactions") ?: true) }
            var isSavingMember by remember { mutableStateOf(false) }
    
            Dialog(onDismissRequest = { editingMember = null }) {
                Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xFF1E1E1E)).padding(16.dp)) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Настройки участника: ${member.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))
    
                        Text("Роли", color = Color(0xFF38BDF8), fontSize = 14.sp)
                        androidx.compose.foundation.layout.FlowRow(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // ✨ Фильтруем роль владельца при выдаче!
                            localRoles.filter { it.hierarchy > 0 }.forEach { role ->
                                val isSelected = localRoleIds.contains(role.id)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) Color(android.graphics.Color.parseColor(role.color)) else Color.DarkGray)
                                        .clickable {
                                            // Защита: нельзя выдать/забрать роль выше своей
                                            if (role.hierarchy > myHighestHierarchy) {
                                                localRoleIds = if (isSelected) localRoleIds - role.id else localRoleIds + role.id
                                            } else {
                                                Toast.makeText(context, "Недостаточно прав", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(role.name, color = Color.White, fontSize = 13.sp)
                                }
                            }
                        }
    
                        Spacer(modifier = Modifier.height(16.dp))
    
                        Text("Права", color = Color(0xFF38BDF8), fontSize = 14.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Писать сообщения", color = Color.White)
                            Switch(checked = permSend, onCheckedChange = { permSend = it })
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Отправлять файлы", color = Color.White)
                            Switch(checked = permFiles, onCheckedChange = { permFiles = it })
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Добавлять реакции", color = Color.White)
                            Switch(checked = permReact, onCheckedChange = { permReact = it })
                        }
    
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { editingMember = null }) { Text("Отмена", color = Color.Gray) }
                            Button(
                                onClick = {
                                    scope.launch {
                                        isSavingMember = true
                                        try {
                                            val overridesJson = """{"sendMessages":$permSend, "attachFiles":$permFiles, "addReactions":$permReact}"""
                                            val res = api.updateMemberRoles(group.id, member.id, UpdateMemberDto(localRoleIds, overridesJson))
                                            if (res.isSuccessful) {
                                                Toast.makeText(context, "Права обновлены", Toast.LENGTH_SHORT).show()
                                                editingMember = null
                                                reloadSilently() // Тихо подгружаем новые данные
                                            }
                                        } catch (e: Exception) {} finally { isSavingMember = false }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                            ) {
                                if (isSavingMember) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                else Text("Сохранить", color = Color(0xFF0F0F0F))
                            }
                        }
                    }
                }
            }
        }
    
        // --- МОДАЛКА РОЛИ ---
        if (editingRole != null || isCreatingNewRole) {
            RoleEditorDialog(
                groupId = group.id,
                role = editingRole,
                api = api,
                onDismiss = { editingRole = null; isCreatingNewRole = false },
                onSaved = {
                    editingRole = null
                    isCreatingNewRole = false
                    reloadSilently() // Тихо загружаем созданную/измененную роль
                }
            )
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun RoleEditorDialog(
        groupId: Int,
        role: GroupRole?,
        api: ApiService,
        onDismiss: () -> Unit,
        onSaved: () -> Unit
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
    
        var roleName by remember { mutableStateOf(role?.name ?: "Новая роль") }
        var roleColor by remember { mutableStateOf(role?.color ?: "#38BDF8") }
        var isMentionable by remember { mutableStateOf(role?.isMentionable ?: false) }
        var newIconUri by remember { mutableStateOf<Uri?>(null) }
        var isSaving by remember { mutableStateOf(false) }
    
        // ✨ Обновленные категории прав
        val permissionCategories = mapOf(
            "Управление сервером" to listOf(
                "manageRoles" to "Управлять ролями",
                "manageChat" to "Управлять чатом (имя, описание, аватар)",
                "manageLinks" to "Управлять ссылками",
                "readHistory" to "Читать историю сообщений",
                "manageSlowMode" to "Менять медленный режим",      // Новое
                "manageBlacklist" to "Управление черным списком" // Новое
            ),
            "Управление участниками" to listOf(
                "kickMembers" to "Выгонять участников",
                "banMembers" to "Банить участников",
                "muteMembers" to "Замутить участника"
            ),
            "Текстовый чат" to listOf(
                "sendMessages" to "Отправлять сообщения",
                "attachFiles" to "Прикреплять файлы",
                "sendGifs" to "Кидать гифки",
                "addReactions" to "Добавлять реакции",
                "mentionEveryone" to "Упоминания (@everyone и роли)",
                "createPolls" to "Создавать опросы",
                "bypassSlowMode" to "Обход медленного режима"
            ),
            "Модерация" to listOf(
                "pinMessages" to "Закреплять сообщения"
            )
        )
    
        val activePerms = remember { mutableStateMapOf<String, Boolean>().apply {
            role?.permissions?.forEach { (k, v) -> put(k, v) }
        }}
    
        val roleColors = listOf("#EF4444", "#F59E0B", "#10B981", "#3B82F6", "#8B5CF6", "#EC4899", "#FFFFFF", "#94A3B8")
        val iconPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) newIconUri = uri
        }
    
        fun saveRole() {
            scope.launch {
                isSaving = true
                try {
                    val jsonMap = activePerms.toMap()
                    val permissionsJsonString = org.json.JSONObject(jsonMap).toString()
    
                    val nameReq = roleName.toRequestBody("text/plain".toMediaTypeOrNull())
                    val colorReq = roleColor.toRequestBody("text/plain".toMediaTypeOrNull())
                    val mentionReq = isMentionable.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val editDesignReq = "false".toRequestBody("text/plain".toMediaTypeOrNull())
                    val permsReq = permissionsJsonString.toRequestBody("application/json".toMediaTypeOrNull())
                    val grantReq = "{}".toRequestBody("application/json".toMediaTypeOrNull())
    
                    var iconPart: MultipartBody.Part? = null
                    if (newIconUri != null) {
                        val stream = context.contentResolver.openInputStream(newIconUri!!)
                        val bytes = stream?.readBytes()
                        stream?.close()
                        if (bytes != null) {
                            val reqFile = bytes.toRequestBody("image/webp".toMediaTypeOrNull())
                            iconPart = MultipartBody.Part.createFormData("Icon", "icon.webp", reqFile)
                        }
                    }
    
                    val response = if (role == null) {
                        api.createRole(groupId, nameReq, colorReq, mentionReq, editDesignReq, permsReq, grantReq, iconPart)
                    } else {
                        api.updateRole(groupId, role.id, nameReq, colorReq, mentionReq, editDesignReq, permsReq, grantReq, iconPart)
                    }
    
                    if (response.isSuccessful) onSaved()
                    else Toast.makeText(context, "Ошибка доступа", Toast.LENGTH_SHORT).show()
    
                } catch (e: Exception) {
                    Toast.makeText(context, "Ошибка сети", Toast.LENGTH_SHORT).show()
                } finally {
                    isSaving = false
                }
            }
        }
    
        Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
            Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F)).statusBarsPadding()) {
    
                // Шапка
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White) }
                        Text(if (role == null) "Новая роль" else "Редактирование", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { saveRole() }, enabled = !isSaving) {
                        if (isSaving) CircularProgressIndicator(color = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
                        else Icon(Icons.Default.Check, contentDescription = "Сохранить", tint = Color(0xFF38BDF8))
                    }
                }
    
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // Основные настройки
                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(Color(0xFF1E1E1E)).clickable { iconPicker.launch("image/*") }, contentAlignment = Alignment.Center) {
                                    if (newIconUri != null) AsyncImage(model = newIconUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    else if (role?.icon != null) AsyncImage(model = role.icon, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    else Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_image), contentDescription = null, tint = Color.Gray)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
    
                                OutlinedTextField(
                                    value = roleName, onValueChange = { roleName = it },
                                    label = { Text("Название", color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(android.graphics.Color.parseColor(roleColor)), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    modifier = Modifier.weight(1f)
                                )
                            }
    
                            Spacer(modifier = Modifier.height(24.dp))
    
                            Text("Цвет роли", color = Color.Gray, fontSize = 14.sp)
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                roleColors.forEach { hex ->
                                    Box(
                                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(android.graphics.Color.parseColor(hex)))
                                            .border(if (roleColor == hex) 3.dp else 0.dp, Color.White, CircleShape)
                                            .clickable { roleColor = hex }
                                    )
                                }
                            }
    
                            Spacer(modifier = Modifier.height(24.dp))
    
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Упоминание", color = Color.White, fontSize = 16.sp)
                                    Text("Позволяет тегать @$roleName", color = Color.Gray, fontSize = 13.sp)
                                }
                                Switch(checked = isMentionable, onCheckedChange = { isMentionable = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color(android.graphics.Color.parseColor(roleColor))))
                            }
    
                            Spacer(modifier = Modifier.height(24.dp))
    
                            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFF59E0B).copy(alpha = 0.1f)).padding(12.dp)) {
                                Text("Ты можешь выдать этой роли только те права, которыми обладаешь сам.", color = Color(0xFFF59E0B), fontSize = 13.sp)
                            }
                        }
                    }
    
                    // Рендерим права
                    permissionCategories.forEach { (categoryName, permissionsList) ->
                        item {
                            Text(categoryName, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp), color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        items(permissionsList) { (permKey, permLabel) ->
                            val isGranted = activePerms[permKey] ?: false
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { activePerms[permKey] = !isGranted }.padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(permLabel, color = Color.White, fontSize = 16.sp)
                                Box(
                                    modifier = Modifier.size(24.dp).clip(RoundedCornerShape(6.dp)).background(if (isGranted) Color(0xFF38BDF8) else Color.Transparent).border(2.dp, if (isGranted) Color.Transparent else Color.Gray, RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isGranted) Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
    suspend fun exportEditedImage(context: Context, original: Bitmap, paths: List<DrawPath>, texts: List<EditableText>): Uri = kotlinx.coroutines.Dispatchers.IO.invoke {
        val config = original.config ?: Bitmap.Config.ARGB_8888
        val resultBitmap = Bitmap.createBitmap(original.width, original.height, config)
        val canvas = android.graphics.Canvas(resultBitmap)
        canvas.drawBitmap(original, 0f, 0f, null)
    
        val paint = Paint().apply { isAntiAlias = true; strokeJoin = Paint.Join.ROUND; strokeCap = Paint.Cap.ROUND }
    
        // Впекаем рисунки
        paths.forEach { action ->
            paint.style = Paint.Style.STROKE
            paint.color = action.color
            paint.strokeWidth = action.strokeWidth
            canvas.drawPath(action.path, paint)
        }
    
        // Впекаем текст
    // Впекаем текст
        // Впекаем текст
        texts.forEach { item ->
            canvas.save()
            canvas.translate(item.bmpX, item.bmpY)
            canvas.rotate(item.rotation)
    
            paint.textSize = 100f * item.scale
            paint.typeface = EditorFonts[item.fontIndex].second // Применяем шрифт
            paint.textAlign = Paint.Align.CENTER
    
            val fontMetrics = paint.fontMetrics
            val verticalOffset = (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent
    
            // 1. Сначала рисуем обводку (если она включена)
            if (item.hasStroke) {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 15f * item.scale // Толщина обводки
                paint.color = item.strokeColor
                canvas.drawText(item.text, 0f, verticalOffset, paint)
            }
    
            // 2. Затем рисуем сам текст (заливку) поверх обводки
            paint.style = Paint.Style.FILL
            paint.color = item.color
            canvas.drawText(item.text, 0f, verticalOffset, paint)
    
            canvas.restore()
        }
    
        val file = File(context.cacheDir, "edited_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out -> resultBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out) }
        return@invoke Uri.fromFile(file)
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MediaPreviewScreen(
        uri: Uri,
        initialBounds: Rect?,
        onClose: () -> Unit,
        onSend: (Uri, String) -> Unit
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
    
        val isVideo = remember(uri) {
            val mime = context.contentResolver.getType(uri) ?: ""
            mime.startsWith("video") || uri.toString().endsWith(".mp4", ignoreCase = true)
        }
    
        val exoPlayer = remember(uri) {
            if (isVideo) {
                androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
                    setMediaItem(androidx.media3.common.MediaItem.fromUri(uri))
                    prepare()
                    playWhenReady = true
                }
            } else null
        }
    
        DisposableEffect(exoPlayer) {
            onDispose { exoPlayer?.release() }
        }
    
        var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
        var cropRect by remember { mutableStateOf<Rect?>(null) }
        var activeCropHandle by remember { mutableStateOf(CropHandle.NONE) }
    
        // ✨ Единая история всех действий
        val actionHistory = remember { mutableStateListOf<HistoryAction>() }
    
        val drawHistory = remember { mutableStateListOf<DrawPath>() }
        val editableTexts = remember { mutableStateListOf<EditableText>() }
        var selectedTextId by remember { mutableStateOf<String?>(null) }
    
        var editMode by remember { mutableStateOf(EditMode.NONE) }
        var currentDrawPath by remember { mutableStateOf<android.graphics.Path?>(null) }
        var currentDrawColor by remember { mutableIntStateOf(EditorColors[2]) }
        var currentStrokeWidth by remember { mutableFloatStateOf(15f) }
        var textValue by remember { mutableStateOf(TextFieldValue("")) }
        var textColor by remember { mutableIntStateOf(EditorColors[0]) }
    
        val backgroundAlpha = remember { Animatable(0f) }
        var captionText by remember { mutableStateOf("") }
        var isClosing by remember { mutableStateOf(false) }
        var showDiscardDialog by remember { mutableStateOf(false) }
        var isProcessing by remember { mutableStateOf(false) }
    
        val configuration = LocalConfiguration.current
        val density = LocalDensity.current
        val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
        val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }
    
        val targetScale = if (initialBounds != null) initialBounds.width / screenWidth else 1f
        val targetTranslateX = if (initialBounds != null) initialBounds.center.x - (screenWidth / 2f) else 0f
        val targetTranslateY = if (initialBounds != null) initialBounds.center.y - (screenHeight / 2f) else 0f
        val targetRadius = if (initialBounds != null) 4f / targetScale else 0f
    
        val flyScale = remember { Animatable(if (initialBounds != null) targetScale else 1f) }
        val flyOffsetX = remember { Animatable(if (initialBounds != null) targetTranslateX else 0f) }
        val flyOffsetY = remember { Animatable(if (initialBounds != null) targetTranslateY else 0f) }
        val cornerRadius = remember { Animatable(if (initialBounds != null) targetRadius else 0f) }
    
        val dismissTranslationY = remember { Animatable(0f) }
    
        LaunchedEffect(uri) {
            if (!isVideo) {
                kotlinx.coroutines.Dispatchers.IO.invoke {
                    try { context.contentResolver.openInputStream(uri)?.use { originalBitmap = BitmapFactory.decodeStream(it) } }
                    catch (e: Exception) { e.printStackTrace() }
                }
            }
    
            if (initialBounds != null) {
                launch { flyScale.animateTo(1f, TweenSpec(300)) }
                launch { flyOffsetX.animateTo(0f, TweenSpec(300)) }
                launch { flyOffsetY.animateTo(0f, TweenSpec(300)) }
                launch { cornerRadius.animateTo(0f, TweenSpec(300)) }
            }
            launch { backgroundAlpha.animateTo(0.95f, TweenSpec(300)) }
        }
    
        fun handleExitAttempt() {
            if (actionHistory.isNotEmpty() || captionText.isNotBlank()) {
                showDiscardDialog = true
            } else {
                isClosing = true
                scope.launch {
                    if (initialBounds != null) {
                        launch { backgroundAlpha.animateTo(0f, TweenSpec(300)) }
                        launch { flyScale.animateTo(targetScale, TweenSpec(300)) }
                        launch { flyOffsetX.animateTo(targetTranslateX, TweenSpec(300)) }
                        launch { flyOffsetY.animateTo(targetTranslateY - dismissTranslationY.value, TweenSpec(300)) }
                        launch { cornerRadius.animateTo(targetRadius, TweenSpec(300)) }
                        kotlinx.coroutines.delay(300)
                        onClose()
                    } else {
                        launch { backgroundAlpha.animateTo(0f, TweenSpec(200)) }
                        launch { dismissTranslationY.animateTo(if (dismissTranslationY.value > 0) screenHeight else -screenHeight, TweenSpec(200)) }
                        kotlinx.coroutines.delay(200)
                        onClose()
                    }
                }
            }
        }
    
        // ✨ Вот то самое исправление: теперь он перехватывает "Назад" всегда, пока экран не умрет!
        BackHandler {
            if (isClosing) return@BackHandler
            handleExitAttempt()
        }
    
        Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { } } ) {
            Box(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = backgroundAlpha.value }.background(Color(0xFF242629)))
    
            Column(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = backgroundAlpha.value }) {
                // --- ВЕРХНЯЯ ПАНЕЛЬ ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp).statusBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                ) {
                    // ✨ Крестик слева (без переворотов)
                    IconButton(onClick = { handleExitAttempt() }) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = Color.White)
                    }
    
                    if (!isVideo) {
                        // ✨ Единая отмена для всего
                        IconButton(
                            onClick = {
                                if (actionHistory.isNotEmpty()) {
                                    val lastAction = actionHistory.removeAt(actionHistory.lastIndex)
                                    when (lastAction) {
                                        is HistoryAction.Draw -> drawHistory.remove(lastAction.path)
                                        is HistoryAction.AddText -> editableTexts.removeAll { it.id == lastAction.textId }
                                        is HistoryAction.Crop -> {
                                            originalBitmap = lastAction.oldBitmap
                                            drawHistory.clear()
                                            drawHistory.addAll(lastAction.oldDraws)
                                            editableTexts.clear()
                                            editableTexts.addAll(lastAction.oldTexts)
                                        }
                                    }
                                }
                            },
                            enabled = actionHistory.isNotEmpty()
                        ) {
                            Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_undo), contentDescription = "Отменить действие", tint = if (actionHistory.isNotEmpty()) Color.White else Color.Gray)
                        }
                    }
                }
    
                // --- РАБОЧАЯ ЗОНА ---
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = flyScale.value
                            scaleY = flyScale.value
                            translationX = flyOffsetX.value
                            translationY = flyOffsetY.value + dismissTranslationY.value
                            clip = true
                            shape = RoundedCornerShape(cornerRadius.value.dp)
                        }
                        .pointerInput(editMode) {
                            if (editMode == EditMode.NONE) {
                                detectVerticalDragGestures(
                                    onDragEnd = {
                                        scope.launch {
                                            if (abs(dismissTranslationY.value) > screenHeight * 0.15f) {
                                                handleExitAttempt()
                                            } else {
                                                launch { dismissTranslationY.animateTo(0f, TweenSpec(300)) }
                                                launch { backgroundAlpha.animateTo(0.95f, TweenSpec(300)) }
                                            }
                                        }
                                    },
                                    onVerticalDrag = { change, dragAmount ->
                                        scope.launch {
                                            dismissTranslationY.snapTo(dismissTranslationY.value + dragAmount)
                                            backgroundAlpha.snapTo((1f - (abs(dismissTranslationY.value) / screenHeight * 1.5f)).coerceIn(0f, 0.95f))
                                        }
                                        change.consume()
                                    }
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isVideo && exoPlayer != null) {
                        androidx.compose.ui.viewinterop.AndroidView(
                            factory = { ctx ->
                                androidx.media3.ui.PlayerView(ctx).apply {
                                    player = exoPlayer
                                    useController = true
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        originalBitmap?.let { bitmap ->
                            BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                val boxWidth = maxWidth.value * LocalDensity.current.density
                                val boxHeight = maxHeight.value * LocalDensity.current.density
    
                                val imageRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                                val screenRatio = boxWidth / boxHeight
                                val scale = if (imageRatio > screenRatio) boxWidth / bitmap.width else boxHeight / bitmap.height
    
                                val renderedWidth = bitmap.width * scale
                                val renderedHeight = bitmap.height * scale
                                val offsetX = (boxWidth - renderedWidth) / 2
                                val offsetY = (boxHeight - renderedHeight) / 2
    
                                fun toBitmapCoords(screenX: Float, screenY: Float) = Offset((screenX - offsetX) / scale, (screenY - offsetY) / scale)
    
                                androidx.compose.foundation.Image(
                                    bitmap = bitmap.asImageBitmap(), contentDescription = null,
                                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit
                                )
    
                                androidx.compose.foundation.Canvas(
                                    modifier = Modifier.fillMaxSize().pointerInput(editMode) {
                                        if (editMode == EditMode.DRAW) {
                                            detectDragGestures(
                                                onDragStart = { offset ->
                                                    val bmpOffset = toBitmapCoords(offset.x, offset.y)
                                                    if (bmpOffset.x in 0f..bitmap.width.toFloat() && bmpOffset.y in 0f..bitmap.height.toFloat()) {
                                                        currentDrawPath = android.graphics.Path().apply { moveTo(bmpOffset.x, bmpOffset.y) }
                                                    }
                                                },
                                                onDrag = { change, _ ->
                                                    currentDrawPath?.let { path ->
                                                        val bmpOffset = toBitmapCoords(change.position.x, change.position.y)
                                                        path.lineTo(bmpOffset.x, bmpOffset.y)
                                                        currentDrawPath = android.graphics.Path(path)
                                                    }
                                                },
                                                onDragEnd = {
                                                    currentDrawPath?.let { path ->
                                                        val action = DrawPath(path, currentDrawColor, currentStrokeWidth / scale)
                                                        drawHistory.add(action)
                                                        actionHistory.add(HistoryAction.Draw(action)) // Сохраняем в историю
                                                    }
                                                    currentDrawPath = null
                                                }
                                            )
                                        } else if (editMode == EditMode.TEXT) {
                                            awaitPointerEventScope {
                                                var lastTapTime = 0L
    
                                                while (true) {
                                                    val downEvent = awaitFirstDown(requireUnconsumed = false)
                                                    val bmpPos = toBitmapCoords(downEvent.position.x, downEvent.position.y)
                                                    var hitTextId: String? = null
    
                                                    for (item in editableTexts.reversed()) {
                                                        val paint = Paint().apply { textSize = 100f * item.scale; typeface = EditorFonts[item.fontIndex].second }
                                                        val width = paint.measureText(item.text)
                                                        val height = paint.fontMetrics.descent - paint.fontMetrics.ascent
    
                                                        val radius = Math.max(width, height) / 2f + 60f / scale
                                                        val distance = Math.hypot((bmpPos.x - item.bmpX).toDouble(), (bmpPos.y - item.bmpY).toDouble())
    
                                                        if (distance <= radius) {
                                                            hitTextId = item.id
                                                            break
                                                        }
                                                    }
    
                                                    val currentTime = System.currentTimeMillis()
                                                    if (hitTextId == null) {
                                                        if (currentTime - lastTapTime < 300) {
                                                            if (bmpPos.x in 0f..bitmap.width.toFloat() && bmpPos.y in 0f..bitmap.height.toFloat()) {
                                                                val newText = EditableText(initialBmpX = bmpPos.x, initialBmpY = bmpPos.y)
                                                                editableTexts.add(newText)
                                                                actionHistory.add(HistoryAction.AddText(newText.id)) // В историю
                                                                selectedTextId = newText.id
                                                                hitTextId = newText.id
                                                            }
                                                        }
                                                        lastTapTime = currentTime
                                                    }
    
                                                    if (hitTextId != selectedTextId) {
                                                        selectedTextId = hitTextId
                                                        if (hitTextId != null) {
                                                            val item = editableTexts.find { it.id == hitTextId }!!
                                                            textValue = TextFieldValue(item.text)
                                                            textColor = item.color
                                                        }
                                                    }
    
                                                    var isTransforming = hitTextId != null
                                                    while (isTransforming) {
                                                        val event = awaitPointerEvent()
                                                        val canceled = event.changes.any { it.isConsumed }
    
                                                        if (canceled || event.changes.none { it.pressed }) {
                                                            isTransforming = false
                                                        } else {
                                                            val item = editableTexts.find { it.id == hitTextId }
                                                            if (item != null) {
                                                                val zoomChange = event.calculateZoom()
                                                                val rotationChange = event.calculateRotation()
                                                                val panChange = event.calculatePan()
    
                                                                if (zoomChange != 1f || rotationChange != 0f || panChange != Offset.Zero) {
                                                                    item.scale = (item.scale * zoomChange).coerceAtLeast(0.2f)
                                                                    item.rotation += rotationChange
                                                                    item.bmpX += panChange.x / scale
                                                                    item.bmpY += panChange.y / scale
                                                                    event.changes.forEach { it.consume() }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else if (editMode == EditMode.CROP) {
                                            detectDragGestures(
                                                onDragStart = { offset ->
                                                    val bmpPos = toBitmapCoords(offset.x, offset.y)
                                                    val currentRect = cropRect ?: Rect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
                                                    val touchRadius = 60f / scale
    
                                                    // ✨ Теперь можно тянуть не только за углы, но и за грани!
                                                    activeCropHandle = when {
                                                        bmpPos.x in (currentRect.left - touchRadius)..(currentRect.left + touchRadius) && bmpPos.y in (currentRect.top - touchRadius)..(currentRect.top + touchRadius) -> CropHandle.TOP_LEFT
                                                        bmpPos.x in (currentRect.right - touchRadius)..(currentRect.right + touchRadius) && bmpPos.y in (currentRect.top - touchRadius)..(currentRect.top + touchRadius) -> CropHandle.TOP_RIGHT
                                                        bmpPos.x in (currentRect.left - touchRadius)..(currentRect.left + touchRadius) && bmpPos.y in (currentRect.bottom - touchRadius)..(currentRect.bottom + touchRadius) -> CropHandle.BOTTOM_LEFT
                                                        bmpPos.x in (currentRect.right - touchRadius)..(currentRect.right + touchRadius) && bmpPos.y in (currentRect.bottom - touchRadius)..(currentRect.bottom + touchRadius) -> CropHandle.BOTTOM_RIGHT
    
                                                        bmpPos.x in (currentRect.left - touchRadius)..(currentRect.left + touchRadius) && bmpPos.y in currentRect.top..currentRect.bottom -> CropHandle.LEFT
                                                        bmpPos.x in (currentRect.right - touchRadius)..(currentRect.right + touchRadius) && bmpPos.y in currentRect.top..currentRect.bottom -> CropHandle.RIGHT
                                                        bmpPos.y in (currentRect.top - touchRadius)..(currentRect.top + touchRadius) && bmpPos.x in currentRect.left..currentRect.right -> CropHandle.TOP
                                                        bmpPos.y in (currentRect.bottom - touchRadius)..(currentRect.bottom + touchRadius) && bmpPos.x in currentRect.left..currentRect.right -> CropHandle.BOTTOM
    
                                                        bmpPos.x in currentRect.left..currentRect.right && bmpPos.y in currentRect.top..currentRect.bottom -> CropHandle.CENTER
                                                        else -> CropHandle.NONE
                                                    }
                                                    cropRect = currentRect
                                                },
                                                onDrag = { change, dragAmount ->
                                                    if (activeCropHandle == CropHandle.NONE) return@detectDragGestures
    
                                                    val bmpDragX = dragAmount.x / scale
                                                    val bmpDragY = dragAmount.y / scale
                                                    val rect = cropRect!!
    
                                                    var newLeft = rect.left
                                                    var newTop = rect.top
                                                    var newRight = rect.right
                                                    var newBottom = rect.bottom
    
                                                    // Обработка новых граней
                                                    when (activeCropHandle) {
                                                        CropHandle.TOP_LEFT -> { newLeft += bmpDragX; newTop += bmpDragY }
                                                        CropHandle.TOP_RIGHT -> { newRight += bmpDragX; newTop += bmpDragY }
                                                        CropHandle.BOTTOM_LEFT -> { newLeft += bmpDragX; newBottom += bmpDragY }
                                                        CropHandle.BOTTOM_RIGHT -> { newRight += bmpDragX; newBottom += bmpDragY }
                                                        CropHandle.LEFT -> { newLeft += bmpDragX }
                                                        CropHandle.RIGHT -> { newRight += bmpDragX }
                                                        CropHandle.TOP -> { newTop += bmpDragY }
                                                        CropHandle.BOTTOM -> { newBottom += bmpDragY }
                                                        CropHandle.CENTER -> {
                                                            newLeft += bmpDragX; newRight += bmpDragX
                                                            newTop += bmpDragY; newBottom += bmpDragY
                                                        }
                                                        CropHandle.NONE -> {}
                                                    }
    
                                                    val maxWidth = bitmap.width.toFloat()
                                                    val maxHeight = bitmap.height.toFloat()
    
                                                    if (activeCropHandle == CropHandle.CENTER) {
                                                        val rectWidth = newRight - newLeft
                                                        val rectHeight = newBottom - newTop
                                                        if (newLeft < 0f) { newLeft = 0f; newRight = rectWidth }
                                                        if (newTop < 0f) { newTop = 0f; newBottom = rectHeight }
                                                        if (newRight > maxWidth) { newRight = maxWidth; newLeft = maxWidth - rectWidth }
                                                        if (newBottom > maxHeight) { newBottom = maxHeight; newTop = maxHeight - rectHeight }
                                                    } else {
                                                        if (newLeft < 0f) newLeft = 0f
                                                        if (newTop < 0f) newTop = 0f
                                                        if (newRight > maxWidth) newRight = maxWidth
                                                        if (newBottom > maxHeight) newBottom = maxHeight
                                                    }
    
                                                    val minSize = 100f
                                                    if (newRight - newLeft < minSize) {
                                                        if (activeCropHandle in listOf(CropHandle.TOP_LEFT, CropHandle.BOTTOM_LEFT, CropHandle.LEFT)) newLeft = newRight - minSize
                                                        else if (activeCropHandle in listOf(CropHandle.TOP_RIGHT, CropHandle.BOTTOM_RIGHT, CropHandle.RIGHT)) newRight = newLeft + minSize
                                                    }
                                                    if (newBottom - newTop < minSize) {
                                                        if (activeCropHandle in listOf(CropHandle.TOP_LEFT, CropHandle.TOP_RIGHT, CropHandle.TOP)) newTop = newBottom - minSize
                                                        else if (activeCropHandle in listOf(CropHandle.BOTTOM_LEFT, CropHandle.BOTTOM_RIGHT, CropHandle.BOTTOM)) newBottom = newTop + minSize
                                                    }
    
                                                    cropRect = Rect(newLeft, newTop, newRight, newBottom)
                                                    change.consume()
                                                },
                                                onDragEnd = {
                                                    activeCropHandle = CropHandle.NONE
                                                }
                                            )
                                        }
                                    }
                                ) {
                                    val textsToDraw = editableTexts.map { item ->
                                        object {
                                            val id = item.id; val x = item.bmpX; val y = item.bmpY; val rot = item.rotation
                                            val s = item.scale; val txt = item.text; val col = item.color
                                            val hasStr = item.hasStroke; val strCol = item.strokeColor; val fIdx = item.fontIndex
                                        }
                                    }
                                    val currentSelectedId = selectedTextId
    
                                    drawContext.canvas.nativeCanvas.apply {
                                        save()
                                        translate(offsetX, offsetY)
                                        scale(scale, scale)
    
                                        clipRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
    
                                        val paint = Paint().apply { isAntiAlias = true; strokeJoin = Paint.Join.ROUND; strokeCap = Paint.Cap.ROUND }
    
                                        drawHistory.forEach { action ->
                                            paint.style = Paint.Style.STROKE; paint.color = action.color; paint.strokeWidth = action.strokeWidth
                                            drawPath(action.path, paint)
                                        }
                                        currentDrawPath?.let {
                                            paint.style = Paint.Style.STROKE; paint.color = currentDrawColor; paint.strokeWidth = currentStrokeWidth / scale
                                            drawPath(it, paint)
                                        }
    
                                        textsToDraw.forEach { item ->
                                            save()
                                            translate(item.x, item.y)
                                            rotate(item.rot)
    
                                            paint.textSize = 100f * item.s
                                            paint.typeface = EditorFonts[item.fIdx].second
                                            paint.textAlign = Paint.Align.CENTER
    
                                            val fontMetrics = paint.fontMetrics
                                            val verticalOffset = (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent
    
                                            if (item.hasStr) {
                                                paint.style = Paint.Style.STROKE
                                                paint.strokeWidth = 15f * item.s
                                                paint.color = item.strCol
                                                drawText(item.txt, 0f, verticalOffset, paint)
                                            }
    
                                            paint.style = Paint.Style.FILL
                                            paint.color = item.col
                                            drawText(item.txt, 0f, verticalOffset, paint)
    
                                            if (item.id == currentSelectedId) {
                                                val width = paint.measureText(item.txt)
                                                val height = fontMetrics.descent - fontMetrics.ascent
                                                val padding = 20f / scale
    
                                                val rect = android.graphics.RectF(-width/2 - padding, -height/2 - padding, width/2 + padding, height/2 + padding)
                                                paint.style = Paint.Style.STROKE
                                                paint.strokeWidth = 4f / scale
                                                paint.color = android.graphics.Color.WHITE
                                                drawRect(rect, paint)
                                            }
    
                                            restore()
                                        }
    
                                        if (editMode == EditMode.CROP) {
                                            val rect = cropRect ?: Rect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
    
                                            val overlayPath = android.graphics.Path().apply {
                                                addRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), android.graphics.Path.Direction.CW)
                                                addRect(rect.left, rect.top, rect.right, rect.bottom, android.graphics.Path.Direction.CCW)
                                            }
                                            paint.style = Paint.Style.FILL
                                            paint.color = android.graphics.Color.parseColor("#99000000")
                                            drawPath(overlayPath, paint)
    
                                            paint.style = Paint.Style.STROKE
                                            paint.color = android.graphics.Color.WHITE
                                            paint.strokeWidth = 4f / scale
                                            drawRect(rect.left, rect.top, rect.right, rect.bottom, paint)
    
                                            paint.style = Paint.Style.FILL
                                            val cornerRadiusParam = 20f / scale
                                            drawCircle(rect.left, rect.top, cornerRadiusParam, paint)
                                            drawCircle(rect.right, rect.top, cornerRadiusParam, paint)
                                            drawCircle(rect.left, rect.bottom, cornerRadiusParam, paint)
                                            drawCircle(rect.right, rect.bottom, cornerRadiusParam, paint)
                                        }
                                        restore()
                                    }
                                }
                            }
                        }
                    }
                }
    
                // --- НИЖНЯЯ ПАНЕЛЬ С ИНСТРУМЕНТАМИ ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .pointerInput(Unit) { detectTapGestures { } }
                ) {
                    androidx.compose.animation.AnimatedVisibility(visible = editMode != EditMode.NONE && !isVideo) {
                        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFF242629).copy(alpha=0.9f)).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(20.dp)).padding(12.dp)) {
                            when (editMode) {
                                EditMode.DRAW -> DrawToolbar(
                                    currentColor = currentDrawColor, currentWidth = currentStrokeWidth,
                                    onColorSelect = { currentDrawColor = it }, onWidthChange = { currentStrokeWidth = it },
                                    onDone = { editMode = EditMode.NONE }
                                )
                                EditMode.TEXT -> TextToolbar(
                                    item = editableTexts.find { it.id == selectedTextId },
                                    hasSelectedText = selectedTextId != null,
                                    onDelete = {
                                        editableTexts.removeIf { it.id == selectedTextId }
                                        selectedTextId = null; editMode = EditMode.NONE
                                    },
                                    onDone = { selectedTextId = null; editMode = EditMode.NONE }
                                )
                                EditMode.CROP -> CropToolbar(
                                    onDone = {
                                        cropRect?.let { rect ->
                                            originalBitmap?.let { bmp ->
                                                // ✨ Сохраняем целое состояние до обрезки в историю
                                                actionHistory.add(HistoryAction.Crop(
                                                    oldBitmap = bmp,
                                                    oldDraws = drawHistory.toList(),
                                                    oldTexts = editableTexts.map { it.copy() }
                                                ))
    
                                                val newBitmap = Bitmap.createBitmap(bmp, rect.left.toInt(), rect.top.toInt(), rect.width.toInt(), rect.height.toInt())
                                                originalBitmap = newBitmap
    
                                                drawHistory.forEach { action -> action.path.offset(-rect.left, -rect.top) }
                                                editableTexts.forEach { text -> text.bmpX -= rect.left; text.bmpY -= rect.top }
                                            }
                                        }
                                        editMode = EditMode.NONE
                                        cropRect = null
                                    },
                                    onCancel = {
                                        editMode = EditMode.NONE
                                        cropRect = null
                                    }
                                )
                                else -> {}
                            }
                        }
                    }
    
                    androidx.compose.animation.AnimatedVisibility(visible = editMode == EditMode.NONE) {
                        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFF242629).copy(alpha=0.9f)).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(24.dp)).padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = captionText, onValueChange = { captionText = it },
                                    placeholder = { Text("Добавить подпись...", color = Color(0XFF72757e)) },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF7f5af0).copy(alpha = 0.7f), unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                        focusedContainerColor = Color.White.copy(alpha = 0.05f), unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                        cursorColor = Color(0xFF7f5af0),
                                    ),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (isProcessing) {
                                    CircularProgressIndicator(color = Color(0xFF7f5af0), modifier = Modifier.size(24.dp))
                                } else {
                                    IconButton(
                                        onClick = {
                                            isProcessing = true
                                            scope.launch {
                                                if (isVideo) {
                                                    onSend(uri, captionText)

                                                } else {
                                                    originalBitmap?.let { bitmap ->
                                                        val finalUri = exportEditedImage(context, bitmap, drawHistory, editableTexts)
                                                        onSend(finalUri, captionText)
                                                    }
                                                }
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
    
                    androidx.compose.animation.AnimatedVisibility(visible = editMode == EditMode.NONE && !isVideo) {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            EditorModeButton(iconRes = com.composables.icons.lucide.R.drawable.lucide_ic_crop, isSelected = false) { editMode = EditMode.CROP; selectedTextId = null }
                            Spacer(modifier = Modifier.width(32.dp))
                            EditorModeButton(iconRes = com.composables.icons.lucide.R.drawable.lucide_ic_pencil, isSelected = false) { editMode = EditMode.DRAW; selectedTextId = null }
                            Spacer(modifier = Modifier.width(32.dp))
                            EditorModeButton(iconRes = com.composables.icons.lucide.R.drawable.lucide_ic_type, isSelected = false) {
                                editMode = EditMode.TEXT
                                if (editableTexts.isEmpty()) {
                                    originalBitmap?.let { bmp ->
                                        val newText = EditableText(initialBmpX = bmp.width / 2f, initialBmpY = bmp.height / 2f)
                                        editableTexts.add(newText)
                                        actionHistory.add(HistoryAction.AddText(newText.id)) // В историю
                                        selectedTextId = newText.id
                                        textValue = TextFieldValue(newText.text)
                                        textColor = newText.color
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    
        if (showDiscardDialog) {
            Dialog(onDismissRequest = { showDiscardDialog = false }) {
                Box(modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(Color(0xFF242629)).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(24.dp)).padding(20.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Хотите отменить изменение?", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            TextButton(onClick = { showDiscardDialog = false }) { Text("Отмена", color = Color(0xFF94A3B8)) }
                            Button(onClick = {
                                showDiscardDialog = false
                                isClosing = true
                                scope.launch {
                                    if (initialBounds != null) {
                                        launch { backgroundAlpha.animateTo(0f, TweenSpec(300)) }
                                        launch { flyScale.animateTo(targetScale, TweenSpec(300)) }
                                        launch { flyOffsetX.animateTo(targetTranslateX, TweenSpec(300)) }
                                        launch { flyOffsetY.animateTo(targetTranslateY - dismissTranslationY.value, TweenSpec(300)) }
                                        launch { cornerRadius.animateTo(targetRadius, TweenSpec(300)) }
                                        kotlinx.coroutines.delay(300)
                                        onClose()
                                    } else {
                                        launch { backgroundAlpha.animateTo(0f, TweenSpec(200)) }
                                        launch { dismissTranslationY.animateTo(if (dismissTranslationY.value > 0) screenHeight else -screenHeight, TweenSpec(200)) }
                                        kotlinx.coroutines.delay(200)
                                        onClose()
                                    }
                                }
                            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) { Text("Сбросить", color = Color.White) }
                        }
                    }
                }
            }
        }
    }
    @Composable
    fun EditorModeButton(iconRes: Int, isSelected: Boolean, onClick: () -> Unit) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isSelected) Color(0xFF38BDF8).copy(0.2f) else Color.Transparent)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = if (isSelected) Color(0xFF38BDF8) else Color.White.copy(0.6f)
            )
        }
    }
    @Composable
    fun CropToolbar(onDone: () -> Unit, onCancel: () -> Unit) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCancel, modifier = Modifier.clip(CircleShape).background(Color(0xFFEF4444))) {
                Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_x), contentDescription = "Отмена", tint = Color.White)
            }
            Text("Обрезка", color = Color.White, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDone, modifier = Modifier.clip(CircleShape).background(Color(0xFF10B981))) {
                Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_check), contentDescription = "Применить", tint = Color.White)
            }
        }
    }
    @Composable
    fun DrawToolbar(currentColor: Int, currentWidth: Float, onColorSelect: (Int) -> Unit, onWidthChange: (Float) -> Unit, onDone: () -> Unit) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ColorPickerRow(selectedColor = currentColor, onColorSelect = onColorSelect)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onDone, modifier = Modifier.clip(CircleShape).background(Color(0xFF10B981))) {
                    Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_check), contentDescription = "Готово", tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Slider(value = currentWidth, onValueChange = onWidthChange, valueRange = 5f..50f, colors = SliderDefaults.colors(activeTrackColor = Color(currentColor), thumbColor = Color.White))
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TextToolbar(
        item: EditableText?,
        hasSelectedText: Boolean,
        onDelete: () -> Unit,
        onDone: () -> Unit
    ) {
        var editingStroke by remember { mutableStateOf(false) }
        var fontMenuExpanded by remember { mutableStateOf(false) }
    
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = item?.text ?: "",
                    onValueChange = { item?.text = it },
                    modifier = Modifier.weight(1f),
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    placeholder = { Text("Текст...", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF38BDF8))
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (hasSelectedText) {
                    IconButton(onClick = onDelete, modifier = Modifier.clip(CircleShape).background(Color(0xFFEF4444))) {
                        Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_trash_2), contentDescription = "Удалить", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                IconButton(onClick = onDone, modifier = Modifier.clip(CircleShape).background(Color(0xFF10B981))) {
                    Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_check), contentDescription = "Готово", tint = Color.White)
                }
            }
    
            Spacer(modifier = Modifier.height(8.dp))
    
            if (item != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Box {
                        TextButton(
                            onClick = { fontMenuExpanded = true },
                            modifier = Modifier.border(1.dp, Color.White.copy(0.2f), RoundedCornerShape(8.dp))
                        ) {
                            Text(EditorFonts[item.fontIndex].first, color = Color.White)
                        }
                        DropdownMenu(
                            expanded = fontMenuExpanded,
                            onDismissRequest = { fontMenuExpanded = false },
                            modifier = Modifier.background(Color(0xFF242629))
                        ) {
                            EditorFonts.forEachIndexed { index, fontPair ->
                                DropdownMenuItem(
                                    text = { Text(fontPair.first, color = Color.White) },
                                    onClick = { item.fontIndex = index; fontMenuExpanded = false }
                                )
                            }
                        }
                    }
    
                    Row(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White.copy(0.1f))) {
                        TextButton(onClick = { editingStroke = false }, modifier = Modifier.background(if (!editingStroke) Color(0xFF38BDF8).copy(0.3f) else Color.Transparent)) {
                            Text("Текст", color = Color.White)
                        }
                        TextButton(onClick = { editingStroke = true; item.hasStroke = true }, modifier = Modifier.background(if (editingStroke) Color(0xFF38BDF8).copy(0.3f) else Color.Transparent)) {
                            Text("Обводка", color = Color.White)
                        }
                        if (item.hasStroke) {
                            IconButton(onClick = { item.hasStroke = false; editingStroke = false }) {
                                Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_x), contentDescription = "Убрать обводку", tint = Color(0xFFEF4444))
                            }
                        }
                    }
                }
    
                Spacer(modifier = Modifier.height(12.dp))
    
                ColorPickerRow(
                    selectedColor = if (editingStroke) item.strokeColor else item.color,
                    onColorSelect = { color ->
                        if (editingStroke) item.strokeColor = color else item.color = color
                    }
                )
            }
        }
    }
    
    @Composable
    fun ColorPickerRow(selectedColor: Int, onColorSelect: (Int) -> Unit) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EditorColors.forEach { color ->
                Box(modifier = Modifier.size(30.dp).clip(CircleShape).background(Color(color)).border(if (color == selectedColor) 3.dp else 1.dp, if (color == selectedColor) Color(0xFF38BDF8) else Color.White.copy(0.2f), CircleShape).clickable { onColorSelect(color) })
            }
        }
    }
    // Вспомогательная функция для красивой даты
    fun formatGalleryDate(dateAddedSeconds: Long): String {
        // Android хранит дату создания в секундах, поэтому умножаем на 1000 для миллисекунд
        val date = java.util.Date(dateAddedSeconds * 1000L)
        val formatter = java.text.SimpleDateFormat("d MMMM", java.util.Locale("ru"))
        return formatter.format(date)
    }
    
    enum class FilterType { ALL, IMAGE, VIDEO, AUDIO }
    
    @Composable
    fun InlineMediaGallery(
        onMediaSelected: (Uri, Rect?) -> Unit,
        onOpenFileManager: () -> Unit
    ) {
        val context = LocalContext.current
        var allMediaItems by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
        var currentFilter by remember { mutableStateOf(FilterType.ALL) }
    
        var cameraUri by remember { mutableStateOf<Uri?>(null) }
        val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && cameraUri != null) {
                onMediaSelected(cameraUri!!, null)
            }
        }
    
        LaunchedEffect(Unit) {
            kotlinx.coroutines.Dispatchers.IO.invoke {
                allMediaItems = getRecentMedia(context)
            }
        }
    
        val filteredItems = remember(allMediaItems, currentFilter) {
            when (currentFilter) {
                FilterType.ALL -> allMediaItems.filter { it.type != MediaType.AUDIO }
                FilterType.IMAGE -> allMediaItems.filter { it.type == MediaType.IMAGE }
                FilterType.VIDEO -> allMediaItems.filter { it.type == MediaType.VIDEO }
                FilterType.AUDIO -> allMediaItems.filter { it.type == MediaType.AUDIO }
            }
        }
    
        // ✨ Группируем файлы по датам
        val groupedItems = remember(filteredItems) {
            filteredItems.groupBy { formatGalleryDate(it.dateAdded) }
        }
    
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(top = 8.dp)
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (filteredItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (allMediaItems.isEmpty()) CircularProgressIndicator(color = Color(0xFF38BDF8))
                        else Text("Ничего не найдено", color = Color.Gray)
                    }
                } else {
                    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // ✨ Проходимся по каждой дате
                        groupedItems.forEach { (dateStr, itemsInDate) ->
    
                            // ✨ 1. Рисуем заголовок с датой (он занимает всю ширину - maxLineSpan)
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                Text(
                                    text = dateStr,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 8.dp)
                                )
                            }
    
                            // ✨ 2. Рисуем сами файлы для этой даты
                            items(itemsInDate.size) { index ->
                                val item = itemsInDate[index]
                                var bounds by remember { mutableStateOf<Rect?>(null) }
    
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .onGloballyPositioned { bounds = it.boundsInWindow() }
                                        .clickable { onMediaSelected(item.uri, bounds) }
                                        .background(Color(0xFF242629))
                                ) {
                                    if (item.type == MediaType.AUDIO) {
                                        Column(
                                            modifier = Modifier.fillMaxSize().padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_music), contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(32.dp))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(item.name ?: "Аудио", color = Color.White, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                                        }
                                    } else {
                                        val imageRequest = ImageRequest.Builder(context)
                                            .data(item.uri)
                                            .crossfade(true)
                                            .apply {
                                                if (item.type == MediaType.VIDEO) {
                                                    decoderFactory(coil.decode.VideoFrameDecoder.Factory())
                                                    videoFrameMicros(1000000)
                                                }
                                            }
                                            .build()
    
                                        AsyncImage(
                                            model = imageRequest,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
    
                                        if (item.type == MediaType.VIDEO) {
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
    
                FloatingActionButton(
                    onClick = {
                        val contentValues = android.content.ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, "camera_${System.currentTimeMillis()}.jpg")
                            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        }
                        cameraUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                        cameraUri?.let { cameraLauncher.launch(it) }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = Color(0xFF7f5af0),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_camera), contentDescription = "Сделать фото")
                }
            }
    
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterButton(
                    iconId = com.composables.icons.lucide.R.drawable.lucide_ic_layout_grid,
                    isSelected = currentFilter == FilterType.ALL,
                    onClick = { currentFilter = FilterType.ALL }
                )
                FilterButton(
                    iconId = com.composables.icons.lucide.R.drawable.lucide_ic_image,
                    isSelected = currentFilter == FilterType.IMAGE,
                    onClick = { currentFilter = FilterType.IMAGE }
                )
                FilterButton(
                    iconId = com.composables.icons.lucide.R.drawable.lucide_ic_video,
                    isSelected = currentFilter == FilterType.VIDEO,
                    onClick = { currentFilter = FilterType.VIDEO }
                )
                FilterButton(
                    iconId = com.composables.icons.lucide.R.drawable.lucide_ic_music,
                    isSelected = currentFilter == FilterType.AUDIO,
                    onClick = { currentFilter = FilterType.AUDIO }
                )
                FilterButton(
                    iconId = com.composables.icons.lucide.R.drawable.lucide_ic_folder,
                    isSelected = false,
                    onClick = onOpenFileManager
                )
            }
        }
    }
    
    @Composable
    fun FilterButton(iconId: Int, isSelected: Boolean, onClick: () -> Unit) {
        val bgColor = if (isSelected) Color(0xFF38BDF8).copy(alpha = 0.2f) else Color.Transparent
        val iconColor = if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.5f)
    
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(bgColor)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
    @Composable
    fun ImageViewerScreen(
        messages: List<Message>,
        initialMessageId: Int,
        initialBounds: Rect?,
        onClose: () -> Unit,
        // ✨ НОВЫЕ КОЛЛБЭКИ ДЛЯ МЕНЮ
        onReply: (Message) -> Unit,
        onForward: (Message) -> Unit,
        onShowInChat: (Int) -> Unit
    ) {
        val context = LocalContext.current
        val mediaMessages = remember(messages) {
            messages.filter { it.image_view != null || it.video_master != null || it.image_master?.endsWith(".mp4", ignoreCase = true) == true }
        }
    
        val initialPage = remember { mediaMessages.indexOfFirst { it.id == initialMessageId }.coerceAtLeast(0) }
        val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { mediaMessages.size })
        var menuExpanded by remember { mutableStateOf(false) }
    
        val scope = rememberCoroutineScope()
        val backgroundAlpha = remember { Animatable(0f) }
        var isClosing by remember { mutableStateOf(false) }
    
        BackHandler(enabled = !isClosing) { isClosing = true }
    
        LaunchedEffect(Unit) { backgroundAlpha.animateTo(0.95f, TweenSpec(300)) }
        Dialog(
            onDismissRequest = onClose,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = backgroundAlpha.value }.background(GlassBackground2))
    
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                val msg = mediaMessages[page]
                val isActive = pagerState.currentPage == page
                val mediaUrl = msg.video_master ?: msg.image_view ?: msg.image_master ?: ""
    
                DismissibleMedia(
                    model = mediaUrl, isActivePage = isActive, initialBounds = if (msg.id == initialMessageId) initialBounds else null,
                    bgAlpha = backgroundAlpha, isClosing = isClosing, onClose = onClose
                )
            }
    
            // --- ВЕРХНЯЯ ПАНЕЛЬ С МЕНЮ ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp).statusBarsPadding().align(Alignment.TopCenter).graphicsLayer { alpha = if (backgroundAlpha.value > 0.7f) backgroundAlpha.value else 0f },
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { isClosing = true }) { Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = Color.White) }
                Text("${pagerState.currentPage + 1} из ${mediaMessages.size}", color = Color.White, style = MaterialTheme.typography.titleMedium)
    
                Box {
                    IconButton(onClick = { menuExpanded = true }) { Icon(Icons.Default.MoreVert, contentDescription = "Меню", tint = Color.White) }
                    val currentMsg = mediaMessages.getOrNull(pagerState.currentPage)
    
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }, modifier = Modifier.background(Color(0xFF1E293B))) {
                        // Скачать оригинал
                        DropdownMenuItem(text = { Text("Скачать оригинал", color = Color.White) }, onClick = {
                            menuExpanded = false
                            val urlToSave = currentMsg?.video_master ?: currentMsg?.image_master ?: currentMsg?.image_view
                            urlToSave?.let { downloadFile(context, it, "original_media_${currentMsg?.id}") }
                        })
                        // Скачать 1280px (только для фото, если есть image_view)
                        if (currentMsg?.video_master == null && currentMsg?.image_view != null) {
                            DropdownMenuItem(text = { Text("Скачать 1280px", color = Color.White) }, onClick = {
                                menuExpanded = false
                                downloadFile(context, currentMsg.image_view, "1280_media_${currentMsg.id}.jpg")
                            })
                        }
                        // Переслать
                        DropdownMenuItem(text = { Text("Переслать", color = Color.White) }, onClick = {
                            menuExpanded = false
                            currentMsg?.let { onForward(it) }
                            isClosing = true
                        })
                        // Ответить
                        DropdownMenuItem(text = { Text("Ответить", color = Color.White) }, onClick = {
                            menuExpanded = false
                            currentMsg?.let { onReply(it) }
                            isClosing = true
                        })
                        // Показать в чате
                        DropdownMenuItem(text = { Text("Показать в чате", color = Color.White) }, onClick = {
                            menuExpanded = false
                            currentMsg?.let { onShowInChat(it.id) }
                            isClosing = true
                        })
                    }
                }
            }
        }
        }
    }
    
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    @Composable
    fun DismissibleMedia(
        model: Any,
        isActivePage: Boolean,
        initialBounds: Rect?,
        bgAlpha: Animatable<Float, androidx.compose.animation.core.AnimationVector1D>,
        isClosing: Boolean,
        onClose: () -> Unit
    ) {
        val coroutineScope = rememberCoroutineScope()
        val configuration = LocalConfiguration.current
        val density = LocalDensity.current
        val context = LocalContext.current
    
        val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
        val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }
        val dismissThreshold = screenHeight * 0.15f
    
        val mediaUrl = model.toString()
        val isVideo = mediaUrl.endsWith(".mp4", ignoreCase = true) ||
                mediaUrl.endsWith(".webm", ignoreCase = true) ||
                mediaUrl.endsWith(".mov", ignoreCase = true)
    
        // ✨ Инициализация плеера (только если это видео)
        val exoPlayer = remember(mediaUrl) {
            if (isVideo) {
                androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
                    setMediaItem(androidx.media3.common.MediaItem.fromUri(mediaUrl))
                    prepare()
                }
            } else null
        }
    
        var isPlaying by remember { mutableStateOf(false) }
        var progress by remember { mutableFloatStateOf(0f) }
        var duration by remember { mutableLongStateOf(0L) }
        var isControlsVisible by remember { mutableStateOf(true) }
        var isDraggingSlider by remember { mutableStateOf(false) }
    
        // Управление паузой при перелистывании страниц Pager'а
        LaunchedEffect(isActivePage) {
            if (!isActivePage) exoPlayer?.pause()
        }
    
        // Обновление прогресса ползунка
        LaunchedEffect(exoPlayer) {
            while (isActive && exoPlayer != null) {
                if (!isDraggingSlider) {
                    duration = exoPlayer.duration.coerceAtLeast(1)
                    progress = (exoPlayer.currentPosition.toFloat() / duration).coerceIn(0f, 1f)
                }
                isPlaying = exoPlayer.isPlaying
                kotlinx.coroutines.delay(100)
            }
        }
    
        DisposableEffect(Unit) {
            onDispose { exoPlayer?.release() }
        }
    
        var scale by remember { mutableFloatStateOf(1f) }
        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }
        val dismissTranslationY = remember { Animatable(0f) }
    
        // Математика размеров (твоя логика)
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
        LaunchedEffect(isClosing) {
            if (isClosing) {
                isControlsVisible = false // Прячем плеер/кнопки, чтобы не мешали лететь
                if (initialBounds != null) {
                    // Если мы знаем, откуда прилетела картинка — летим обратно
                    launch { bgAlpha.animateTo(0f, TweenSpec(300)) }
                    launch { flyScale.animateTo(targetScale, TweenSpec(300)) }
                    launch { flyOffsetX.animateTo(targetTranslateX, TweenSpec(300)) }
                    launch { flyOffsetY.animateTo(targetTranslateY - dismissTranslationY.value, TweenSpec(300)) }
                    launch { cornerRadius.animateTo(targetRadius, TweenSpec(300)) }
                    kotlinx.coroutines.delay(300)
                    onClose()
                } else {
                    // Если это не первая картинка (например, перелистнули на следующую и закрыли)
                    launch { bgAlpha.animateTo(0f, TweenSpec(200)) }
                    launch { dismissTranslationY.animateTo(screenHeight, TweenSpec(200)) }
                    kotlinx.coroutines.delay(200)
                    onClose()
                }
            }
        }
        LaunchedEffect(Unit) {
            if (initialBounds != null) {
                launch { flyScale.animateTo(1f, TweenSpec(300)) }
                launch { flyOffsetX.animateTo(0f, TweenSpec(300)) }
                launch { flyOffsetY.animateTo(0f, TweenSpec(300)) }
                launch { cornerRadius.animateTo(0f, TweenSpec(300)) }
            }
        }
    
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val downTime = System.currentTimeMillis()
    
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
                                            bgAlpha.snapTo((1f - (abs(dismissTranslationY.value) / screenHeight * 1.5f)).coerceIn(0f, 1f))
                                        }
                                        event.changes.forEach { it.consume() }
                                    }
                                }
                            } while (event.changes.any { it.pressed })
    
                            // Обработка отпускания пальца
                            val upTime = System.currentTimeMillis()
                            if (scale == 1f) {
                                // ✨ Распознаем обычный клик по экрану для показа/скрытия интерфейса
                                if (abs(dismissTranslationY.value) < 10f && (upTime - downTime) < 250) {
                                    isControlsVisible = !isControlsVisible
                                }
    
                                coroutineScope.launch {
                                    if (abs(dismissTranslationY.value) > dismissThreshold) {
                                        if (initialBounds != null) {
                                            launch { bgAlpha.animateTo(0f, TweenSpec(300)) }
                                            launch { flyScale.animateTo(targetScale, TweenSpec(300)) }
                                            launch { flyOffsetX.animateTo(targetTranslateX, TweenSpec(300)) }
                                            launch { flyOffsetY.animateTo(targetTranslateY - dismissTranslationY.value, TweenSpec(300)) }
                                            launch { cornerRadius.animateTo(targetRadius, TweenSpec(300)) }
                                            kotlinx.coroutines.delay(300)
                                            onClose()
                                        } else {
                                            launch { bgAlpha.animateTo(0f, TweenSpec(200)) }
                                            launch { dismissTranslationY.animateTo(if (dismissTranslationY.value > 0) screenHeight else -screenHeight, TweenSpec(200)) }
                                            kotlinx.coroutines.delay(200)
                                            onClose()
                                        }
                                    } else {
                                        launch { dismissTranslationY.animateTo(0f, TweenSpec(300)) }
                                        // ✨ ИСПРАВЛЕНИЕ 2: Возвращаем к 0.95f вместо 1f
                                        launch { bgAlpha.animateTo(0.95f, TweenSpec(300)) }
                                    }
                                }
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // ✨ СЛОЙ 1: Медиа-контент (Зумируется и двигается)
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
                        clip = true
                        shape = RoundedCornerShape(cornerRadius.value.dp)
                    }
            ) {
                if (isVideo && exoPlayer != null) {
                    // Плеер без стандартных элементов управления (чтобы не перехватывали свайпы)
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { ctx ->
                            androidx.media3.ui.PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = false
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(mediaUrl)
                            .apply {
                                decoderFactory(
                                    if (Build.VERSION.SDK_INT >= 28) coil.decode.ImageDecoderDecoder.Factory()
                                    else coil.decode.GifDecoder.Factory()
                                )
                            }.build(),
                        contentDescription = "Full Media",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
    
            // ✨ СЛОЙ 2: Панель управления видео (НЕ зумируется, висит поверх экрана)
            if (isVideo && exoPlayer != null) {
                androidx.compose.animation.AnimatedVisibility(
                    // Прячем контролы, если начался зум
                    visible = isControlsVisible && scale == 1f,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically { it / 2 },
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically { it / 2 },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(GlassBackground)
                            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Кнопка Play/Pause
                        IconButton(onClick = { if (isPlaying) exoPlayer.pause() else exoPlayer.play() }) {
                            Icon(
                                painter = if (isPlaying) painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_pause)
                                else painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_play),
                                contentDescription = "Play/Pause",
                                tint = Color.White
                            )
                        }
    
                        // Ползунок перемотки
                        Slider(
                            value = progress,
                            onValueChange = {
                                isDraggingSlider = true
                                progress = it
                            },
                            onValueChangeFinished = {
                                isDraggingSlider = false
                                exoPlayer.seekTo((progress * duration).toLong())
                            },
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(
                                activeTrackColor = Color(0xFF38BDF8),
                                inactiveTrackColor = Color.White.copy(alpha = 0.2f),
                                thumbColor = Color.White
                            )
                        )
    
                        // Время
                        Text(
                            text = "${formatTime((progress * duration).toInt())} / ${formatTime(duration.toInt())}",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
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
