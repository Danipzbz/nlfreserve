package com.dpbprog.nlfreserve

import androidx.compose.runtime.*

@Composable
fun App() {
    initFirebase()

    var logged by remember { mutableStateOf(false) }
    var userDni by remember { mutableStateOf("") }
    var userEsAdmin by remember { mutableStateOf(false) }

    if (!logged) {
        LoginScreen { dniRecibido, adminRecibido ->
            userDni = dniRecibido
            userEsAdmin = adminRecibido // Este valor viene de viewModel.esAdmin
            logged = true
        }
    }
    else {
        MainScreen(
            dniLogueado = userDni,
            esAdmin = userEsAdmin, // Se lo pasamos a la MainScreen
            onLogout = {
                logged = false
                userDni = ""
                userEsAdmin = false
            }
        )
    }
}