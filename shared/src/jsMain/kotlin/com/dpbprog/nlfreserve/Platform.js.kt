package com.dpbprog.nlfreserve

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.FirebaseOptions

class JSPlatform : Platform {
    override val name: String = "Web Browser (JavaScript)"
}

actual fun getPlatform(): Platform = JSPlatform()

actual fun initFirebase() {
    Firebase.initialize(
        options = FirebaseOptions(
            apiKey = "AIzaSyBb-WfBFGxUeba0qB66LykG-jAlWbQ8PdE",
            authDomain = "com-dpbprog-nlfreserve.firebaseapp.com",
            projectId = "com-dpbprog-nlfreserve",
            storageBucket = "com-dpbprog-nlfreserve.firebasestorage.app",
            applicationId = "1:753732719768:web:e1a6ab768ce4062aea2ced" // Este es el appId
        )
    )
}