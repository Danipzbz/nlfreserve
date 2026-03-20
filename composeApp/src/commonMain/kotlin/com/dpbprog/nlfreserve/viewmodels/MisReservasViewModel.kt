package com.dpbprog.nlfreserve.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MisReservasViewModel : ViewModel() {
    private val db = Firebase.firestore
    var listaReservas by mutableStateOf<List<String>>(emptyList())
        private set

    private var reservasJob: kotlinx.coroutines.Job? = null

    fun cargarReservas(dniUsuario: String) {
        // Cancelamos cualquier escucha previa para no tener "fugas" de memoria
        reservasJob?.cancel()

        reservasJob = viewModelScope.launch {
            try {
                db.collection("mis_reservas")
                    .where("dni", equalTo = dniUsuario)
                    .orderBy("fecha", Direction.ASCENDING)
                    .snapshots()
                    .collect { snapshot ->
                        // Creamos la lista nueva
                        val nuevaLista = snapshot.documents.map { doc ->
                            val fecha: String = doc.get("fecha")
                            val hora: String = doc.get("hora")
                            "$fecha|$hora"
                        }
                        // ASIGNACIÓN DIRECTA: Esto fuerza a Compose a redibujar
                        listaReservas = nuevaLista
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun cancelarReserva(
        dniUsuario: String,
        fecha: String,
        hora: String,
        onResultado: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 1. Buscamos el ID
                val misReservasQuery = db.collection("mis_reservas")
                    .where("dni", equalTo = dniUsuario)
                    .where("fecha", equalTo = fecha)
                    .where("hora", equalTo = hora)
                    .get()

                if (misReservasQuery.documents.isEmpty()) {
                    onResultado(false, "No se encontró la reserva")
                    return@launch
                }
                val miReservaId = misReservasQuery.documents[0].id

                // 2. Buscamos la sesión
                val sesionQuery = db.collection("reservas")
                    .where("fecha", equalTo = fecha)
                    .where("hora", equalTo = hora)
                    .get()

                if (sesionQuery.documents.isNotEmpty()) {
                    val sesionRef = sesionQuery.documents[0].reference

                    db.runTransaction {
                        val snapshot = get(sesionRef)
                        val plazasActuales: Long = snapshot.get("plazas")
                        update(sesionRef, "plazas" to (plazasActuales + 1))
                        delete(db.collection("mis_reservas").document(miReservaId))
                    }

                    // --- LA SOLUCIÓN AQUÍ ---
                    // Eliminamos manualmente de la lista local PARA QUE SEA INSTANTÁNEO
                    // sin esperar a que Firebase responda al snapshot
                    listaReservas = listaReservas.filter { it != "$fecha|$hora" }

                    onResultado(true, "Reserva cancelada correctamente")
                }
            } catch (e: Exception) {
                onResultado(false, "Error: ${e.message}")
            }
        }
    }
}