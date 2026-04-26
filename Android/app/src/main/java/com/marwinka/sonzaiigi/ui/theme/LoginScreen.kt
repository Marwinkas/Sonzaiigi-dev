package com.marwinka.sonzaiigi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun LoginScreen(onNavigateToChats: () -> Unit, onNavigateToRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var processing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val apiService = remember { ApiService.create() }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0B1120)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("С возвращением", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 24.dp))

                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = Color(0xFFEF4444), modifier = Modifier.padding(bottom = 16.dp))
                }

                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email", color = Color(0xFF64748B)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color.DarkGray)
                )

                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Пароль", color = Color(0xFF64748B)) },
                    singleLine = true, visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color.DarkGray)
                )

                Button(
                    onClick = {
                        processing = true
                        errorMessage = null
                        coroutineScope.launch {
                            try {
                                val response = apiService.login(LoginRequest(email, password))
                                AuthManager.token = response.token
                                AuthManager.userId = response.user.id

                                // ✨ FCM логика теперь здесь, внутри корутины onClick
                                com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val fcmToken = task.result
                                        coroutineScope.launch {
                                            try { apiService.updateFcmToken(
                                                ApiService.FcmTokenDto(
                                                    fcmToken
                                                )
                                            ) } catch (e: Exception) { e.printStackTrace() }
                                        }
                                    }
                                }
                                onNavigateToChats()
                            } catch (e: HttpException) {
                                errorMessage = "Неверный email или пароль"
                            } catch (e: Exception) {
                                errorMessage = "Ошибка сети"
                            } finally {
                                processing = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                    enabled = !processing
                ) {
                    Text(if (processing) "Входим..." else "Войти", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Нет аккаунта? Зарегистрироваться",
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.align(Alignment.CenterHorizontally).clickable { onNavigateToRegister() }
                )
            }
        }
    }
}

@Composable
fun RegisterScreen(onNavigateToLogin: () -> Unit, onNavigateToChats: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var processing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val apiService = remember { ApiService.create() }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0B1120)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Регистрация", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 24.dp))

                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = Color(0xFFEF4444), modifier = Modifier.padding(bottom = 16.dp))
                }

                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Ваше имя", color = Color(0xFF64748B)) }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color.DarkGray)
                )

                OutlinedTextField(
                    value = username, onValueChange = { username = it },
                    label = { Text("Никнейм (@)", color = Color(0xFF64748B)) }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color.DarkGray)
                )

                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email", color = Color(0xFF64748B)) }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color.DarkGray)
                )

                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Пароль", color = Color(0xFF64748B)) }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color.DarkGray)
                )

                Button(
                    onClick = {
                        if (name.isBlank() || username.isBlank() || email.isBlank() || password.isBlank()) {
                            errorMessage = "Заполните все поля"
                            return@Button
                        }
                        processing = true
                        errorMessage = null
                        coroutineScope.launch {
                            try {
                                val response = apiService.register(RegisterRequest(name, username, email, password))
                                AuthManager.token = response.token
                                AuthManager.userId = response.user.id
                                onNavigateToChats()
                            } catch (e: HttpException) {
                                // ✨ Бэкенд должен вернуть статус 400 или 409, если занято!
                                errorMessage = if (e.code() == 400 || e.code() == 409) {
                                    "Email или никнейм уже заняты"
                                } else {
                                    "Ошибка регистрации"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Ошибка сети"
                            } finally {
                                processing = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                    enabled = !processing
                ) {
                    Text(if (processing) "Регистрация..." else "Создать аккаунт", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Уже есть аккаунт? Войти",
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.align(Alignment.CenterHorizontally).clickable { onNavigateToLogin() }
                )
            }
        }
    }
}