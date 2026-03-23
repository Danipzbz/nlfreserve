package com.dpbprog.nlfreserve

class JSPlatform: Platform {
    override val name: String = "Web Browser (JavaScript)"
}

actual fun getPlatform(): Platform = JSPlatform()

actual fun initFirebase() {
    // Aquí irá la lógica de Firebase Web más adelante
}