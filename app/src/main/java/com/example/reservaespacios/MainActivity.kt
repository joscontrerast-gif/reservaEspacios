package com.example.reservaespacios

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.example.reservaespacios.ui.reservation.ReservationFormRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Reemplaza MaterialTheme por el tema que te generó Android Studio si quieres.
            MaterialTheme {
                ReservationFormRoute()
            }
        }
    }
}