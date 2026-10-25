package com.example.reservaespacios.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reservaespacios.R

private val TealClaro = Color(0xFF3CC4CE)

@Composable
fun AuthLayout(
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(colorResource(R.color.therion_blue), colorResource(R.color.therion_teal))
                )
            )
    ) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
            Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.logo_blanco),
                    contentDescription = "Logo de THERIONLABS",
                    modifier = Modifier.height(48.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color.White)) { append("THERION") }
                            withStyle(SpanStyle(color = TealClaro)) { append("LABS") }
                        },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        letterSpacing = 1.5.sp
                    )
                }
            }
            Surface(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    content = content
                )
            }
        }
    }
}