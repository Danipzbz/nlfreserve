package com.dpbprog.nlfreserve

import platform.UIKit.UIDevice
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun initFirebase() {
    // En iOS, la inicialización de Firebase a veces requiere el archivo GoogleService-Info.plist,
    // pero para compilar, esta función debe existir así:
    Firebase.initialize()
}