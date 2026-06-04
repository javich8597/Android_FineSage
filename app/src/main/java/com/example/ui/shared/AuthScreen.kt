package com.example.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import com.example.ui.finance.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: FinanceViewModel,
    labels: Map<String, String>,
    isDarkMode: Boolean
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(false) }

    val authError by viewModel.authError.collectAsState()
    val isSpanish = labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isSignUpMode) (if (isSpanish) "Crear cuenta" else "Create Account") else (if (isSpanish) "Iniciar Sesión" else "Log In"),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (authError != null) {
                    Text(
                        text = authError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it.trim() },
                    label = { Text(if (isSpanish) "Correo electrónico" else "Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (isSpanish) "Contraseña" else "Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (isSignUpMode) {
                            viewModel.signUp(email, password)
                        } else {
                            viewModel.login(email, password)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(if (isSignUpMode) (if (isSpanish) "Unirse" else "Sign up") else (if (isSpanish) "Entrar" else "Login"))
                }

                TextButton(onClick = { isSignUpMode = !isSignUpMode }) {
                    Text(
                        text = if (isSignUpMode) (if (isSpanish) "¿Ya tienes cuenta? Entrar" else "Already have an account? Log in") else (if (isSpanish) "¿No tienes cuenta? Crear una" else "Don't have an account? Sign up"),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
