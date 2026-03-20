package com.dpbprog.nlfreserve.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    var esAdmin by mutableStateOf(false)
        private set

    private val firestore = Firebase.firestore

    var loginError by mutableStateOf(false)
        private set

    // NUEVA VARIABLE: Para avisar si el usuario está baneado/desactivado
    var usuarioDesactivado by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun login(dni: String, password: String, onSuccess: () -> Unit) {
        if (dni.isBlank() || password.isBlank()) {
            loginError = true
            return
        }

        viewModelScope.launch {
            isLoading = true
            loginError = false
            usuarioDesactivado = false // Reiniciamos errores al intentar entrar

            try {
                // Buscamos al usuario por DNI y Password
                val query = firestore.collection("usuarios")
                    .where("dni", equalTo = dni)
                    .where("password", equalTo = password)
                    .get()

                if (query.documents.isNotEmpty()) {
                    val userDoc = query.documents[0]

                    // 1. COMPROBAMOS SI ESTÁ ACTIVO
                    val estaActivo = try {
                        userDoc.get<Boolean>("activo") ?: true
                    } catch (e: Exception) {
                        true // Si no existe el campo, por defecto dejamos entrar
                    }

                    if (!estaActivo) {
                        usuarioDesactivado = true
                        isLoading = false
                        return@launch // DETENEMOS EL LOGIN AQUÍ
                    }

                    // 2. COMPROBAMOS SI ES ADMIN
                    esAdmin = try {
                        userDoc.get<Boolean>("admin") ?: false
                    } catch (e: Exception) {
                        false
                    }

                    loginError = false
                    onSuccess()
                } else {
                    loginError = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                loginError = true
            } finally {
                isLoading = false
            }
        }
    }
}