package com.dpbprog.nlfreserve

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dpbprog.nlfreserve.viewmodels.LoginViewModel

// Definimos los colores aquí para tenerlos a mano
val GrisFondo = Color(0xFF121212)
val VerdeApp = Color(0xFF4CAF50)

@Composable
fun LoginScreen(onLogin: (String, Boolean) -> Unit) {
    val viewModel = remember { LoginViewModel() }

    var dni by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GrisFondo), // Fondo gris oscuro
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.width(280.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TÍTULO NLF RESERVE EN VERDE
            Text(
                text = "NLF Reserve",
                color = VerdeApp,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // CAMPO DNI
            TextField(
                value = dni,
                onValueChange = { dni = it },
                label = { Text("DNI", color = VerdeApp) }, // Texto del label en verde
                isError = viewModel.loginError,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E),
                    focusedIndicatorColor = VerdeApp,
                    unfocusedIndicatorColor = VerdeApp.copy(alpha = 0.5f), // Línea inferior verde tenue
                    cursorColor = VerdeApp,
                    focusedLabelColor = VerdeApp,   // Color cuando pulsas el input
                    unfocusedLabelColor = VerdeApp  // Color cuando el input está vacío
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // CAMPO CONTRASEÑA
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = VerdeApp) }, // Texto del label en verde
                isError = viewModel.loginError,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E),
                    focusedIndicatorColor = VerdeApp,
                    unfocusedIndicatorColor = VerdeApp.copy(alpha = 0.5f),
                    cursorColor = VerdeApp,
                    focusedLabelColor = VerdeApp,
                    unfocusedLabelColor = VerdeApp
                )
            )

            if (viewModel.loginError) {
                Text(
                    text = "Usuario y/o contraseña incorrecta",
                    color = Color.Red, // El rojo destaca bien sobre el gris
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp).align(Alignment.Start)
                )
            }

            if (viewModel.usuarioDesactivado) {
                Text(
                    text = "Tu cuenta está desactivada.\nContacta con el administrador.",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp).align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // BOTÓN ENTRAR VERDE CON TEXTO NEGRO
            Button(
                onClick = {
                    viewModel.login(dni, password) {
                        // Usamos un valor seguro por si esAdmin viene vacío o da error
                        val rolSeguro = viewModel.esAdmin ?: false
                        onLogin(dni, rolSeguro)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeApp,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black, // Spinner negro para que se vea sobre el verde
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "ENTRAR",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}