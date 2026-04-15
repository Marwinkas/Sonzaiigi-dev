package com.marwinka.sonzaiigi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // ✨ Не забудь этот импорт!

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    viewModel: ChatViewModel = viewModel(), // ✨ Подключаем мозг!
    onChatClick: (Chat) -> Unit
) {
    // Берем данные из ViewModel
    val chats = viewModel.chats
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        TopAppBar(
            title = { Text("Сообщения", color = Color.White) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E293B)),
            actions = {
                IconButton(onClick = { viewModel.loadChats() }) { // Кнопка обновить для теста
                    Icon(Icons.Default.Search, contentDescription = "Поиск", tint = Color(0xFF94A3B8))
                }
            }
        )

        // Показываем загрузку, ошибку или список
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when {
                isLoading -> {
                    CircularProgressIndicator(color = Color(0xFF38BDF8))
                }
                errorMessage != null -> {
                    Text(text = errorMessage, color = Color(0xFFEF4444))
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(chats) { chat ->
                            ChatItemRow(chat = chat, onClick = { onChatClick(chat) })
                        }
                    }
                }
            }
        }
    }
}

// ... Функция ChatItemRow остается почти такой же, только исправь имена переменных
@Composable
fun ChatItemRow(chat: Chat, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (chat.is_group) Color(0xFFF59E0B) else Color(0xFF38BDF8)),
            contentAlignment = Alignment.Center
        ) {
            // Если имя пустое, ставим знак вопроса
            val initial = chat.name.takeIf { it.isNotEmpty() }?.take(1)?.uppercase() ?: "?"
            Text(text = initial, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = chat.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = chat.lastMessage ?: "Вложение", color = Color(0xFF64748B), fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(text = chat.time ?: "", color = Color(0xFF64748B), fontSize = 12.sp)
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