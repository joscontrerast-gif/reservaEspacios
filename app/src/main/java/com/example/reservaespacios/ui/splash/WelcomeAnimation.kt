package com.example.reservaespacios.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reservaespacios.R
import kotlinx.coroutines.delay

@Composable
fun WelcomeAnimation(
    onFinished: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val progress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "progress"
    )

    val reveal by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(600, delayMillis = 60, easing = LinearOutSlowInEasing),
        label = "reveal"
    )

    LaunchedEffect(Unit) {
        delay(100)
        expanded = true
        delay(1300)
        onFinished()
    }

    val blue = colorResource(R.color.therion_blue)
    val teal = colorResource(R.color.therion_teal)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.splash_background)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = blue)) { append("THERION") }
                withStyle(SpanStyle(color = teal)) { append("LABS") }
            },
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier
                .offset(x = 40.dp)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    val feather = size.width * 0.4f
                    val front = size.width - (size.width + feather) * reveal
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            startX = front,
                            endX = front + feather
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
        )

        Image(
            painter = painterResource(id = R.drawable.logo_splash),
            contentDescription = "Logo de THERIONLABS",
            modifier = Modifier
                .size(288.dp)
                .graphicsLayer {
                    translationX = -106.dp.toPx() * progress
                    val s = 1f - 0.35f * progress
                    scaleX = s
                    scaleY = s
                }
        )
    }
}