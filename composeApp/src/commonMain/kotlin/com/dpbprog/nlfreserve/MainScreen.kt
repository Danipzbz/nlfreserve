package com.dpbprog.nlfreserve

// HEMOS BORRADO: import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
// HEMOS BORRADO: @SuppressLint(...)
@Composable
fun MainScreen(dniLogueado: String, esAdmin: Boolean, onLogout: () -> Unit) {
    var screen by remember {
        mutableStateOf(if (esAdmin) Screen.AdminReservas else Screen.Reservar)
    }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "NLF Reserve",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212)
                ),
                actions = {
                    TextButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Cerrar sesión",
                            color = VerdeApp,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF1E1E1E)) {
                if (esAdmin) {
                    NavigationBarItem(
                        selected = screen == Screen.AdminReservas,
                        onClick = { screen = Screen.AdminReservas },
                        label = { Text("Control Reservas") },
                        icon = { Icon(Icons.Default.DateRange, null) }
                    )
                    NavigationBarItem(
                        selected = screen == Screen.AdminUsuarios,
                        onClick = { screen = Screen.AdminUsuarios },
                        label = { Text("Usuarios") },
                        icon = { Icon(Icons.Default.Person, null) }
                    )
                } else {
                    NavigationBarItem(
                        selected = screen == Screen.Reservar,
                        onClick = { screen = Screen.Reservar },
                        label = { Text("Reservar") },
                        icon = { Icon(Icons.Default.AddCircle, null) }
                    )
                    NavigationBarItem(
                        selected = screen == Screen.MisReservas,
                        onClick = { screen = Screen.MisReservas },
                        label = { Text("Mis Reservas") },
                        icon = { Icon(Icons.Default.List, null) }
                    )
                }
            }
        }
    ) { paddingValues ->
        // Usamos los paddingValues aquí para que no haya avisos de Scaffold
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            FondoCurvoVerde()
            when (screen) {
                Screen.Reservar -> ReservarScreen(dniLogueado)
                Screen.MisReservas -> MisReservasScreen(dniUsuario = dniLogueado)
                Screen.AdminReservas -> AdminReservasScreen()
                Screen.AdminUsuarios -> AdminUsuariosScreen()
                else -> { /* Opción de seguridad */ }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color(0xFF1E1E1E),
            tonalElevation = 8.dp,
            title = {
                Text(
                    text = "Cerrar sesión",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que quieres salir de tu cuenta?",
                    color = Color.LightGray
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text(
                        text = "Cerrar sesión",
                        color = Color(0xFFFF8A80),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        text = "Volver",
                        color = VerdeApp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        )
    }
}

@Composable
fun FondoCurvoVerde() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, height * 0.7f)
            quadraticBezierTo(
                x1 = width * 0.5f, y1 = height * 0.9f,
                x2 = width, y2 = height * 0.6f
            )
        }

        drawPath(
            path = path,
            color = Color(0xFF4CAF50),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 80f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            ),
            alpha = 0.3f
        )
    }
}