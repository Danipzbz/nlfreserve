package com.dpbprog.nlfreserve

import platform.UIKit.UIDevice
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

// Implementación para iOS
actual fun initFirebase() {
    Firebase.initialize()
}