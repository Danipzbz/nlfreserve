package com.dpbprog.nlfreserve.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReservasViewModel : ViewModel() {
    private val firestore = Firebase.firestore

    private val _reservas = MutableStateFlow<List<Reservas>>(emptyList())
    val reservas: StateFlow<List<Reservas>> = _reservas

    private val _limiteUsuario = MutableStateFlow(0)
    val limiteUsuario: StateFlow<Int> = _limiteUsuario

    private val _fechasReservadasPorUsuario = MutableStateFlow<List<String>>(emptyList())
    val fechasReservadasPorUsuario: StateFlow<List<String>> = _fechasReservadasPorUsuario

    init {
        obtenerReservas()
    }

    private fun obtenerReservas() {
        viewModelScope.launch {
            firestore.collection("reservas").snapshots().collect { snapshot ->
                val lista = snapshot.documents.map { doc ->
                    doc.data<Reservas>().copy(id = doc.id)
                }
                _reservas.value = lista
            }
        }
    }

    fun cargarDatosValidacion(dniUsuario: String) {
        viewModelScope.launch {
            try {
                // 1. Obtener límite
                val userSnapshot = firestore.collection("usuarios")
                    .where("dni", equalTo = dniUsuario).get()
                if (userSnapshot.documents.isNotEmpty()) {
                    _limiteUsuario.value = userSnapshot.documents[0].get<Int>("limiteReservas") ?: 0
                }

                // 2. Obtener fechas de mis reservas actuales
                val resSnapshot = firestore.collection("mis_reservas")
                    .where("dni", equalTo = dniUsuario).get()

                val fechas = resSnapshot.documents.map { it.get<String>("fecha") ?: "" }
                _fechasReservadasPorUsuario.value = fechas
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
    fun reservarSesion(dniUsuario: String, reserva: Reservas, onResultado: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                // 1. Buscamos datos del usuario
                val userSnapshot = firestore.collection("usuarios")
                    .where("dni", equalTo = dniUsuario).get()

                val nombreUser = userSnapshot.documents.firstOrNull()?.get<String>("nombre") ?: "Desconocido"
                val apellidosUser = userSnapshot.documents.firstOrNull()?.get<String>("apellidos") ?: ""

                val reservaRef = firestore.collection("reservas").document(reserva.id)

                // 2. Transacción para restar la plaza
                firestore.runTransaction {
                    val snapshot = get(reservaRef)
                    val plazas: Long = snapshot.get("plazas")

                    if (plazas > 0) {
                        update(reservaRef, "plazas" to (plazas - 1))
                    } else {
                        throw Exception("No quedan plazas")
                    }
                }

                // 3. Añadimos la reserva (Esto genera el ID automático sin errores)
                firestore.collection("mis_reservas").add(mapOf(
                    "dni" to dniUsuario,
                    "nombre" to nombreUser,
                    "apellidos" to apellidosUser,
                    "fecha" to reserva.fecha,
                    "hora" to reserva.hora
                ))

                cargarDatosValidacion(dniUsuario)
                onResultado(true, "¡Reserva realizada con éxito!")
            } catch (e: Exception) {
                onResultado(false, e.message ?: "Error al reservar")
            }
        }
    }
}