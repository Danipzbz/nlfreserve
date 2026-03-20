package com.dpbprog.nlfreserve

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Reservar : Screen("reservar")
    object MisReservas : Screen("mis_reservas")
    object AdminReservas : Screen("admin_reservas")
    object AdminUsuarios : Screen("admin_usuarios")
}