package com.dpbprog.nlfreserve.models

import kotlinx.serialization.Serializable

@Serializable
data class Usuarios(
    val id: String = "",
    val dni: String = "",
    val password: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val limiteReservas: Int = 0
)