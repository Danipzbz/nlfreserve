package com.dpbprog.nlfreserve.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dpbprog.nlfreserve.models.Reservas
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.launch

data class UsuarioApuntado(
    val idReserva: String,
    val nombre: String,
    val apellidos: String,
    val dni: String,
    val hora: String,
)

class AdminReservasViewModel : ViewModel() {
    private val db = Firebase.firestore

    var alumnosEnSesion by mutableStateOf<List<UsuarioApuntado>>(emptyList())
        private set

    var sesionesPorDia by mutableStateOf<Map<String, List<Reservas>>>(emptyMap())
        private set

    init {
        cargarTodasLasSesiones()
    }

    fun cargarAlumnos(fecha: String, hora: String) {
        viewModelScope.launch {
            try {
                db.collection("mis_reservas")
                    .where("fecha", equalTo = fecha)
                    .where("hora", equalTo = hora)
                    .snapshots().collect { snapshot ->
                        alumnosEnSesion = snapshot.documents.map { doc ->
                            UsuarioApuntado(
                                idReserva = doc.id,
                                nombre = doc.get("nombre") ?: "Desconocido",
                                apellidos = doc.get("apellidos") ?: "",
                                dni = doc.get("dni") ?: "",
                                hora = doc.get("hora") ?: ""
                            )
                        }
                    }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun eliminarReservaDeAlumno(alumno: UsuarioApuntado, fecha: String, onExito: () -> Unit) {
        viewModelScope.launch {
            try {
                // 1. Devolvemos la plaza en la tabla "reservas"
                val sesionQuery = db.collection("reservas")
                    .where("fecha", equalTo = fecha)
                    .where("hora", equalTo = alumno.hora)
                    .get()

                if (sesionQuery.documents.isNotEmpty()) {
                    val sesionRef = sesionQuery.documents[0].reference
                    db.runTransaction {
                        val snap = get(sesionRef)
                        val plazas: Long = snap.get("plazas")
                        update(sesionRef, "plazas" to (plazas + 1))
                        // 2. Borramos la reserva
                        delete(db.collection("mis_reservas").document(alumno.idReserva))
                    }

                    onExito()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun cargarTodasLasSesiones() {
        viewModelScope.launch {
            db.collection("reservas").snapshots().collect { snapshot ->
                val todas = snapshot.documents.map { it.data<Reservas>().copy(id = it.id) }

                sesionesPorDia = todas.groupBy { it.fecha }.toSortedMap()
            }
        }
    }
}