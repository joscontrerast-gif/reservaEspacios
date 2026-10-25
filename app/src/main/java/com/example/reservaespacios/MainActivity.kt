package com.example.reservaespacios

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.reservaespacios.ui.AppRoot
import com.example.reservaespacios.ui.theme.ReservaEspaciosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        splash.setOnExitAnimationListener { it.remove() }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ReservaEspaciosTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot()
                }
            }
        }
    }
}