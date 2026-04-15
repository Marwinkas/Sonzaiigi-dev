package com.marwinka.sonzaiigi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    // Состояния, за которыми будет следить наш интерфейс
    var chats by mutableStateOf<List<Chat>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Создаем наш API клиент
    private val api = ApiService.create()

    init {
        // Как только ViewModel создается, сразу грузим чаты (Аналог useEffect с пустым массивом)
        loadChats()
    }

    fun loadChats() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Магия Retrofit: ждем ответ от сервера
                val response = api.getChats()
                chats = response.chats
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Не удалось загрузить чаты: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}