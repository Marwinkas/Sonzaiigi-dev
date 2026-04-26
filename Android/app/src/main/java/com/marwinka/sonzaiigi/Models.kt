package com.marwinka.sonzaiigi

import com.google.gson.annotations.SerializedName

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
data class Attachment(
    val type: String, // "image", "video", "audio", "file", "gif"
    val url: String,
    val thumb: String? = null,
    val name: String? = null,
    val size: String? = null,
    val width: Int?,
    val height: Int?,
    val duration: String? = null,
    val extra_info: String? = null
)
data class ReactionInfo(
    val emoji: String,
    val count: Int,
    val reacted_by_me: Boolean
)

data class ContextMessagesResponse(
    val messages: List<Message>,
    val has_more_up: Boolean,
    val has_more_down: Boolean
)
data class Chat(
    val id: Int,
    val name: String,
    // Используем аннотации, если имя в JSON отличается (например lastMessage или last_message)
    val lastMessage: String? = "Нет сообщений",
    val time: String? = "",
    val unread_count: Int = 0,
    val permissions: ChatPermissions? = null, // ✨ ДОБАВЛЯЕМ СЮДА
    val pings_count: Int = 0,
    val is_group: Boolean = false,
    val avatar: String? = null,
    val can_reply: Boolean = true, // ✨ Вот оно!
    val user: UserInfo? = null
)
// И создаем сам класс прав:
data class ChatPermissions(
    val sendMessages: Boolean = false,
    val attachFiles: Boolean = false,
    val addReactions: Boolean = false,
    val canForward: Boolean = false,
    val pinMessages: Boolean = false,
    val deleteOthersMessages: Boolean = false
)
data class AppUpdateResponse(
    val version: String,
    val url: String
)
data class SysMsgs(
    val join: Boolean,
    val leave: Boolean,
    val edit: Boolean
)

data class BannedUser(
    val id: Int,
    val name: String,
    val avatar: String?,
    val reason: String?,
    val date: String
)

data class AuditLogEntry(
    val id: Int,
    val user: String,
    val action: String,
    val time: String
)

data class ResetLinkResponse(
    val new_token: String
)
// Модель для отправки нового порядка ролей
data class ReorderRolesDto(
    @SerializedName("roleIds") val roleIds: List<Int>
)
// Обнови свой GroupProfileResponse:
data class GroupRole(
    val id: Int,
    val name: String,
    val color: String,
    val isMentionable: Boolean,
    val hierarchy: Int,
    val icon: String?,
    val permissions: Map<String, Boolean>? // ✨ Читаем права из БД
)

// Обновляем участника, чтобы видеть его роли и персональные права
data class GroupMemberProfile(
    val id: Int,
    val name: String,
    val nickname: String?,
    val avatar: String?,
    @SerializedName("isOwner") val isOwner: Boolean,
    val roleIds: List<Int>?,
    val individualOverrides: Map<String, Boolean>? // Персональные права (галочки/крестики)
)

// Обновляем ответ профиля группы
data class GroupProfileResponse(
    val id: Int,
    val name: String,
    val description: String?,
    val avatar: String?,
    @SerializedName("isMuted") val isMuted: Boolean,
    @SerializedName("isPrivate") val isPrivate: Boolean, // ✨ НОВОЕ ПОЛЕ
    @SerializedName("invite_token") val inviteToken: String?,
    val sysMsgs: SysMsgs?,
    val slowMode: Int?,
    val roles: List<GroupRole>?,
    val members: List<GroupMemberProfile>,
    val bannedUsers: List<BannedUser>?,
    val auditLog: List<AuditLogEntry>?
)
// Модели ответов
data class UserProfileDataResponse(val user: UserData)
data class UserData(val id: Int, val name: String?, val username: String?, val avatar: String?, val bio: String?, val email: String? = "")

data class RelationsResponse(
    val is_following: Boolean,
    val is_mutual: Boolean,
    val is_blocking: Boolean,
    val is_muted: Boolean
)

data class FollowResponse(
    val is_following: Boolean,
    val is_mutual: Boolean
)

data class BlockResponse(val is_blocked: Boolean)
data class StartChatResponse(val chatId: Int)
// DTO для отправки новых ролей и прав на сервер
data class UpdateMemberDto(
    val RoleIds: List<Int>,
    val OverridesJson: String
)

data class MuteResponse(
    val is_muted: Boolean
)
data class ReplyToInfo(
    val id: Int,
    val name: String?,
    val text: String?,
    // Добавляем новые поля для медиа:
    val image_thumb: String? = null,
    val video_master: String? = null,
    val audio_title: String? = null,
    val audio_stream: String? = null,
    val audio_master: String? = null
)
data class UpdateMsgDto(val text: String)
data class RegisterRequest(
    val name: String,
    val username: String,
    val email: String,
    val password: String
)
data class UserInfo(
    val id: Int,
    val name: String,
    val username: String
)
// 2. Обнови класс Message (добавь новые поля в конец)
data class Message(
    val id: Int,
    val text: String?,
    val attachments: List<Attachment>? = null,
    val senderId: Int,
    val senderName: String?,
    val senderAvatar: String?,
    val time: String,
    val image_thumb: String? = null,
    val image_view: String? = null,
    val image_master: String? = null,
    val video_master: String? = null,
    val document_size: String? = null,
    val document_url: String? = null,
    val document_name: String? = null,
    val senderColor: String? = null,
    val is_read: Boolean = false,
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
    val audio_duration: String? = null,
    val reactions: List<ReactionInfo>? = emptyList()


)
data class ReactDto(
    val emoji: String
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