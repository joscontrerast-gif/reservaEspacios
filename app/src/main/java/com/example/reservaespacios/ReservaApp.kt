package com.example.reservaespacios

import android.app.Application

class ReservaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }
}
