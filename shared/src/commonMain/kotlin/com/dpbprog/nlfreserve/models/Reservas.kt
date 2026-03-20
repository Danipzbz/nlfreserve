package com.dpbprog.nlfreserve.models

import kotlinx.serialization.Serializable

@Serializable
data class Reservas(
    val id: String = "",
    val fecha: String = "",
    val hora: String = "",
    val plazas: Int = 0
)