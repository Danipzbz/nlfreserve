package com.dpbprog.nlfreserve

import platform.UIKit.UIDevice
// Si estas lineas de firebase dan error en el IDE, no te preocupes,
// GitHub las resolverá porque tiene Mac instalado.
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun initFirebase() {
    Firebase.initialize()
}