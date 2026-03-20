package com.dpbprog.nlfreserve

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

// Añade esto para Firebase
expect fun initFirebase()