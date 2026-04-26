package com.marwinka.sonzaiigi

// То, что отправляем на /auth/login
data class LoginRequest(
    val email: String,
    val password: String
)

// То, что получаем от сервера
data class LoginResponse(
    val token: String,
    val user: User
)

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val avatar: String?
)
data class ChatsResponse(
    val chats: List<Chat>
)

data class Chat(
    val id: Int,
    val name: String,
    // Используем аннотации, если имя в JSON отличается (например lastMessage или last_message)
    val lastMessage: String? = "Нет сообщений",
    val time: String? = "",
    val unread_count: Int = 0,
    val pings_count: Int = 0,
    val is_group: Boolean = false,
    val avatar: String? = null
)
data class ReplyToInfo(
    val id: Int, // ✨ Теперь мы знаем, куда прыгать!
    val name: String?,
    val text: String?
)
data class UpdateMsgDto(val text: String)

// 2. Обнови класс Message (добавь новые поля в конец)
data class Message(
    val id: Int,
    val text: String?,

    val senderId: Int,
    val senderName: String?,
    val senderAvatar: String?,
    val time: String,
    val image_thumb: String? = null,
    val image_view: String? = null,
    val image_master: String? = null,
    // ✨ НОВЫЕ ПОЛЯ:
    val is_edited: Boolean = false,
    val reply_to: ReplyToInfo? = null,
    val forwarded_from: String? = null,
    val gif_url: String?,
    val audio_stream: String? = null ,
    val audio_artist: String? = null,
    val audio_master: String? = null,
    val audio_title: String? = null,
    val audio_cover: String? = null,
    val audio_duration: String? = null


)
data class MessagesResponse(
    val messages: List<Message>,
    val has_more: Boolean,
    val can_reply: Boolean
)

// Ответ на отправку сообщения
data class SendMessageRequest(
    val text: String
)
data class ForwardDto(
    val message_id: Int,
    val conversation_ids: List<Int>,
    val include_author: Boolean
)