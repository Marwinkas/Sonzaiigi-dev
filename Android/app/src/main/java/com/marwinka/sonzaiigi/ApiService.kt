package com.marwinka.sonzaiigi

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

import android.content.Context
import android.content.SharedPreferences
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

object AuthManager {
    private const val PREFS_NAME = "sonzaiigi_prefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER_ID = "user_id"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var token: String?
        get() = prefs?.getString(KEY_TOKEN, null)
        set(value) { prefs?.edit()?.putString(KEY_TOKEN, value)?.apply() }

    var userId: Int
        get() = prefs?.getInt(KEY_USER_ID, 0) ?: 0
        set(value) { prefs?.edit()?.putInt(KEY_USER_ID, value)?.apply() }
    fun clearAuth() {
        prefs?.edit()?.apply {
            remove(KEY_TOKEN)
            remove(KEY_USER_ID)
        }?.apply()
    }
    fun clear() {
        prefs?.edit()?.clear()?.apply()
    }
}

interface ApiService {
    @Multipart
    @POST("groups/create")
    suspend fun createGroup(
        @Part("Name") name: RequestBody,
        @Part("Description") desc: RequestBody,
        @Part avatar: MultipartBody.Part? = null
    ): retrofit2.Response<Unit>
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
    // Было users/{id}/mute, а для групп лучше сделать так:
    @POST("groups/{id}/toggle-mute")
    suspend fun toggleGroupMute(@Path("id") chatId: Int): MuteResponse
    // ✨ НОВЫЙ МЕТОД ДЛЯ ПОЛУЧЕНИЯ ЧАТОВ
    @GET("chats")
    suspend fun getChats(): ChatsResponse
    // Было просто {id}, а сервер ждет groups/{id}
    @GET("groups/{id}")
    suspend fun getGroupInfo(@Path("id") chatId: Int): GroupProfileResponse
    @GET("messages/{chatId}")
    suspend fun getMessages(
        @retrofit2.http.Path("chatId") chatId: Int,
        @retrofit2.http.Query("offset") offset: Int = 0
    ): MessagesResponse

    @POST("users/{id}/mute")
    suspend fun toggleMute(@Path("id") userId: Int): MuteResponse

    // ✨ Выход из группы (используем ID чата)
    // Используем Response<Unit>, так как бэкенд возвращает Ok() без тела
    @POST("groups/{id}/leave")
    suspend fun leaveGroup(@Path("id") chatId: Int): Response<Unit>

    @POST("messages/forward")
    suspend fun forwardMessage(@Body request: ForwardDto): okhttp3.ResponseBody
    // В твоем React коде отправка идет через FormData.
    // Если на сервере можно просто отправить JSON, то оставляем так:
    // ✨ ИСПРАВЛЕНО: Теперь отправляем как форму!
    @retrofit2.http.PATCH("messages/{id}")
    suspend fun editMessage(
        @retrofit2.http.Path("id") id: Int,
        @retrofit2.http.Body dto: UpdateMsgDto
    ): Message
    @Multipart
    @POST("messages/{conversationId}/upload-chunk")
    suspend fun uploadChunk(
        @Path("conversationId") conversationId: Int,
        @Part chunk: MultipartBody.Part,
        @Part("chunkIndex") chunkIndex: RequestBody,
        @Part("uploadId") uploadId: RequestBody
    )

    // ✨ Удаление группы
    @retrofit2.http.DELETE("groups/{id}")
    suspend fun deleteGroup(@Path("id") id: Int): retrofit2.Response<Unit>

    // ✨ Обновленный метод сохранения настроек (добавили IsPrivate)
    @Multipart
    @retrofit2.http.PATCH("groups/{id}")
    suspend fun updateGroup(
        @Path("id") id: Int,
        @Part("Name") name: RequestBody? = null,
        @Part("Description") description: RequestBody? = null,
        @Part("InviteToken") inviteToken: RequestBody? = null,
        @Part("SlowMode") slowMode: RequestBody? = null,
        @Part("SysMsgJoin") sysMsgJoin: RequestBody? = null,
        @Part("SysMsgLeave") sysMsgLeave: RequestBody? = null,
        @Part("IsPrivate") isPrivate: RequestBody? = null, // <--- НОВОЕ ПОЛЕ
        @Part avatar: MultipartBody.Part? = null
    ): retrofit2.Response<Unit>
    // ✨ Кик или бан участника (actionType = "kick" или "ban")
    @POST("groups/{id}/members/{userId}/{actionType}")
    suspend fun manageMember(
        @Path("id") id: Int,
        @Path("userId") userId: Int,
        @Path("actionType") actionType: String
    ): retrofit2.Response<Unit>

    // ✨ Обновление ролей и персональных прав
    @retrofit2.http.PATCH("groups/{id}/members/{userId}")
    suspend fun updateMemberRoles(
        @Path("id") id: Int,
        @Path("userId") userId: Int,
        @Body dto: UpdateMemberDto
    ): retrofit2.Response<Unit>

    // ✨ Создание новой роли
    @Multipart
    @POST("groups/{id}/roles")
    suspend fun createRole(
        @Path("id") id: Int,
        @Part("Name") name: RequestBody,
        @Part("Color") color: RequestBody,
        @Part("IsMentionable") isMentionable: RequestBody,
        @Part("CanEditRoleDesign") canEditRoleDesign: RequestBody,
        @Part("PermissionsJson") permissionsJson: RequestBody,
        @Part("GrantablePermissionsJson") grantablePermissionsJson: RequestBody,
        @Part icon: MultipartBody.Part?
    ): retrofit2.Response<Unit>

    @GET("users/{id}")
    suspend fun getUserProfile(@Path("id") id: Int): UserProfileDataResponse

    @GET("users/{id}/relations")
    suspend fun getUserRelations(@Path("id") id: Int): RelationsResponse

    @POST("users/{id}/follow")
    suspend fun toggleFollow(@Path("id") id: Int): FollowResponse
    @POST("users/{id}/block")
    suspend fun toggleBlock(@Path("id") userId: Int): BlockResponse

    // ✨ Редактирование профиля (отправка файлов и текста)
    @Multipart
    @POST("profile")
    suspend fun updateProfile(
        @Part("Name") name: RequestBody?,
        @Part("Username") username: RequestBody?,
        @Part("Bio") bio: RequestBody?,
        @Part avatar: MultipartBody.Part?
    ): retrofit2.Response<UserProfileDataResponse> // Обернул в Response для обработки ошибок
    // Метод мута юзера (не путать с мутом группы!)
    // ✨ Удаление личного чата (переписки)
    @retrofit2.http.DELETE("conversations/{id}")
    suspend fun deleteConversation(@Path("id") id: Int): retrofit2.Response<Unit>
    @POST("users/{id}/mute")
    suspend fun toggleUserMute(@Path("id") id: Int): MuteResponse
    // ✨ Вступление в группу по токену
    @POST("groups/join/{token}")
    suspend fun joinGroup(@Path("token") token: String): retrofit2.Response<Unit>
    @POST("messages/start/{userId}")
    suspend fun startConversation(@Path("userId") userId: Int): StartChatResponse

    @retrofit2.http.PATCH("groups/{id}/roles/reorder")
    suspend fun reorderRoles(
        @retrofit2.http.Path("id") id: Int,
        @retrofit2.http.Body dto: ReorderRolesDto
    ): retrofit2.Response<Unit>
    // ✨ Редактирование существующей роли
    // ✨ Удаление роли
    @retrofit2.http.DELETE("groups/{id}/roles/{roleId}")
    suspend fun deleteRole(
        @Path("id") id: Int,
        @Path("roleId") roleId: Int
    ): retrofit2.Response<Unit>
    @Multipart
    @retrofit2.http.PATCH("groups/{id}/roles/{roleId}")
    suspend fun updateRole(
        @Path("id") id: Int,
        @Path("roleId") roleId: Int,
        @Part("Name") name: RequestBody,
        @Part("Color") color: RequestBody,
        @Part("IsMentionable") isMentionable: RequestBody,
        @Part("CanEditRoleDesign") canEditRoleDesign: RequestBody,
        @Part("PermissionsJson") permissionsJson: RequestBody,
        @Part("GrantablePermissionsJson") grantablePermissionsJson: RequestBody,
        @Part icon: MultipartBody.Part?
    ): retrofit2.Response<Unit>
    // ✨ Сброс ссылки
    @POST("groups/{id}/reset-link")
    suspend fun resetGroupLink(@Path("id") id: Int): ResetLinkResponse
    @POST("users/fcm-token")
    suspend fun updateFcmToken(@Body request: FcmTokenDto): Response<Unit>
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): LoginResponse
    // Класс данных (можно в конец файла ApiService)
    data class FcmTokenDto(val token: String)
    // ✨ Разбан пользователя
    @POST("groups/{id}/bans/{userId}/unban")
    suspend fun unbanUser(
        @Path("id") id: Int,
        @Path("userId") userId: Int
    ): retrofit2.Response<Unit>

    @Multipart
    @POST("messages/{conversationId}")
    suspend fun sendMessage(
        @Path("conversationId") conversationId: Int,
        @Part("text") text: RequestBody? = null,
        @Part("parent_id") parentId: RequestBody? = null,
        // ✨ Теперь это списки!
        @Part uploadedFileIds: List<MultipartBody.Part>? = null,
        @Part originalFileNames: List<MultipartBody.Part>? = null,
        @Part contentTypes: List<MultipartBody.Part>? = null
    ): Message
    @GET("app/check")
    suspend fun checkUpdate(): AppUpdateResponse
    @GET("messages/{conversationId}/context/{id}")
    suspend fun loadContext(
        @Path("conversationId") conversationId: Int,
        @Path("id") messageId: Int
    ): ContextMessagesResponse
    @GET("messages/{conversationId}/more-down")
    suspend fun loadMoreDown(
        @Path("conversationId") conversationId: Int,
        @Query("after_id") afterId: Int
    ): MoreDownResponse
    @retrofit2.http.POST("messages/{id}/react")
    suspend fun react(
        @retrofit2.http.Path("id") messageId: Int,
        @retrofit2.http.Body dto: ReactDto
    ): retrofit2.Response<Unit> // Можно Unit, так как обновление придет по WebSocket

    // Модель для ответа (вниз)
    data class MoreDownResponse(
        val messages: List<Message>,
        val has_more_down: Boolean
    )
    @Multipart
    @POST("app/upload")
    suspend fun uploadUpdate(
        @Part("version") version: RequestBody,
        @Part apk: MultipartBody.Part
    ): retrofit2.Response<Unit>

    @retrofit2.http.DELETE("messages/{id}")
    suspend fun deleteMessage(
        @retrofit2.http.Path("id") id: Int,
        @retrofit2.http.Query("forEveryone") forEveryone: Boolean
    )
    @POST("conversations/{id}/read/{maxMessageId}")
    suspend fun markAsRead(
        @Path("id") chatId: Int,
        @Path("maxMessageId") maxMessageId: Int
    ): retrofit2.Response<Unit>
    companion object {
        private const val BASE_URL = "https://sonzaiigi.com/api/"

        fun create(): ApiService {
            // Логгер, чтобы видеть запросы
            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // Перехватчик, который приклеивает токен
            val authInterceptor = Interceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()

                // Если токен есть, добавляем его в заголовок
                AuthManager.token?.let {
                    requestBuilder.addHeader("Authorization", "Bearer $it")
                }

                chain.proceed(requestBuilder.build())
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .addInterceptor(authInterceptor) // Добавили перехватчик
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}