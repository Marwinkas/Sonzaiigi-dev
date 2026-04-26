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
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Path

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

    fun clear() {
        prefs?.edit()?.clear()?.apply()
    }
}

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // ✨ НОВЫЙ МЕТОД ДЛЯ ПОЛУЧЕНИЯ ЧАТОВ
    @GET("chats")
    suspend fun getChats(): ChatsResponse
    @GET("messages/{chatId}")
    suspend fun getMessages(
        @retrofit2.http.Path("chatId") chatId: Int,
        @retrofit2.http.Query("offset") offset: Int = 0
    ): MessagesResponse
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
    @Multipart
    @POST("messages/{conversationId}")
    suspend fun sendMessage(
        @Path("conversationId") conversationId: Int,
        @Part("text") text: RequestBody? = null,
        @Part("parent_id") parentId: RequestBody? = null,
        @Part("uploaded_file_id") uploadedFileId: RequestBody? = null,
        @Part("original_file_name") originalFileName: RequestBody? = null,
        @Part("content_type") contentType: RequestBody? = null
    ): Message
    @retrofit2.http.DELETE("messages/{id}")
    suspend fun deleteMessage(
        @retrofit2.http.Path("id") id: Int,
        @retrofit2.http.Query("forEveryone") forEveryone: Boolean
    )

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