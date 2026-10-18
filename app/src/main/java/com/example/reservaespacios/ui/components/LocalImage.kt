package com.example.reservaespacios.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.example.reservaespacios.nativeres.PhotoStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Muestra una foto guardada en el almacenamiento interno (se decodifica fuera del hilo principal). */
@Composable
fun LocalImage(path: String, contentDescription: String?, modifier: Modifier = Modifier) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, key1 = path) {
        value = withContext(Dispatchers.IO) { PhotoStorage.loadBitmap(path)?.asImageBitmap() }
    }
    bitmap?.let {
        Image(bitmap = it, contentDescription = contentDescription, modifier = modifier, contentScale = ContentScale.Crop)
    }
}
