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

// Clase temporal para mapear al usuario (ajusta los campos si tienes más)
data class UsuarioDatos(
    val dni: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val password: String = "",
    val admin: Boolean = false,
    val activo: Boolean = true,
    val limiteReservas: String = "0",
    val fechaNacimiento: String = "",
    val patologiasEnfermedades: String = "",
    val lesiones: String = "",
    val infoAdicional: String = ""
)

class AdminUsuariosViewModel : ViewModel() {
    private val firestore = Firebase.firestore

    // Lista completa de la base de datos
    private var todosLosUsuarios = emptyList<UsuarioDatos>()

    // Lista filtrada que ve la pantalla
    var usuariosFiltrados by mutableStateOf<List<UsuarioDatos>>(emptyList())
        private set

    init {
        cargarUsuarios()
    }

    private fun cargarUsuarios() {
        viewModelScope.launch {
            firestore.collection("usuarios").snapshots().collect { snapshot ->
                todosLosUsuarios = snapshot.documents.map { doc ->
                    UsuarioDatos(
                        // Usamos strings vacíos por defecto para evitar nulos
                        dni = doc.get<String?>("dni") ?: "",
                        nombre = doc.get<String?>("nombre") ?: "",
                        apellidos = doc.get<String?>("apellidos") ?: "",
                        password = doc.get<String?>("password") ?: "",
                        admin = doc.get<Boolean?>("admin") ?: false,
                        activo = doc.get<Boolean?>("activo") ?: true,

                        // CORRECCIÓN AQUÍ: Leemos como String directamente o Long si es número
                        limiteReservas = try {
                            doc.get<String?>("limiteReservas") ?: "0"
                        } catch (e: Exception) {
                            doc.get<Long?>("limiteReservas")?.toString() ?: "0"
                        },

                        fechaNacimiento = doc.get<String?>("fechaNacimiento") ?: "",
                        patologiasEnfermedades = doc.get<String?>("patologiasEnfermedades") ?: "",
                        lesiones = doc.get<String?>("lesiones") ?: "",
                        infoAdicional = doc.get<String?>("infoAdicional") ?: ""
                    )
                }
                usuariosFiltrados = todosLosUsuarios
            }
        }
    }

    fun filtrar(query: String) {
        usuariosFiltrados = if (query.isEmpty()) {
            todosLosUsuarios
        } else {
            todosLosUsuarios.filter {
                it.nombre.contains(query, ignoreCase = true) ||
                        it.dni.contains(query, ignoreCase = true)
            }
        }
    }

    fun actualizarUsuario(dniOriginal: String, datos: Map<String, Any>, onExito: () -> Unit) {
        viewModelScope.launch {
            try {
                val query = firestore.collection("usuarios")
                    .where("dni", equalTo = dniOriginal)
                    .get()

                if (query.documents.isNotEmpty()) {
                    query.documents[0].reference.update(datos)
                    onExito()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun crearUsuario(datos: Map<String, Any>, onExito: () -> Unit) {
        viewModelScope.launch {
            try {
                val dni = datos["dni"].toString()
                // Creamos el documento usando el DNI como ID único
                firestore.collection("usuarios").document(dni).set(datos)
                onExito()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}