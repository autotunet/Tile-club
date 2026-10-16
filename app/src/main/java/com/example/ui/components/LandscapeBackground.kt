package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.data.model.WorldDestination

@Composable
fun LandscapeBackground(
    destination: WorldDestination,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (destination.drawableResId != null) {
            // Real HD Generated Monument Landscape
            Image(
                painter = painterResource(id = destination.drawableResId),
                contentDescription = "${destination.country} - ${destination.monument}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Contrast & Vignette Overlay to make 3D tiles crystal clear
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.45f),
                                Color.Black.copy(alpha = 0.30f),
                                Color.Black.copy(alpha = 0.65f)
                            )
                        )
                    )
            )
        } else {
            // Procedural HD Atmospheric Monument Landscape
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Sky Gradient
                val skyColors = destination.skyGradient.map { Color(it) }
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = skyColors,
                        startY = 0f,
                        endY = h * 0.75f
                    )
                )

                // Sun / Moon / Celestial Orb
                val sunCenter = Offset(w * 0.75f, h * 0.22f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f),
                            Color(destination.ambientColor).copy(alpha = 0.6f),
                            Color.Transparent
                        ),
                        center = sunCenter,
                        radius = w * 0.25f
                    ),
                    radius = w * 0.25f,
                    center = sunCenter
                )

                // Distant Mountain / Monument silhouette (Layer 1)
                val path1 = Path().apply {
                    moveTo(0f, h * 0.62f)
                    lineTo(w * 0.25f, h * 0.48f)
                    lineTo(w * 0.5f, h * 0.55f)
                    lineTo(w * 0.78f, h * 0.42f)
                    lineTo(w, h * 0.58f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    path = path1,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(destination.ambientColor).copy(alpha = 0.35f),
                            Color(0xFF0F172A).copy(alpha = 0.85f)
                        ),
                        startY = h * 0.42f,
                        endY = h
                    )
                )

                // Middle Monument Ridge (Layer 2)
                val path2 = Path().apply {
                    moveTo(0f, h * 0.68f)
                    lineTo(w * 0.35f, h * 0.58f)
                    lineTo(w * 0.65f, h * 0.62f)
                    lineTo(w, h * 0.52f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    path = path2,
                    color = Color(0xFF0F172A).copy(alpha = 0.90f)
                )

                // Atmospheric Mist
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(destination.ambientColor).copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.75f)
                        ),
                        startY = h * 0.5f,
                        endY = h
                    )
                )
            }
        }
    }
}
