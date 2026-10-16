package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.GameState
import com.example.ui.theme.CoinGold
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.StarGold
import com.example.ui.theme.SurfaceDark
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Representa una partícula física en el sistema de animación de victoria.
 */
data class VictoryParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    var vRot: Float,
    var size: Float,
    val color: Color,
    val isStar: Boolean,
    var alpha: Float = 1.0f
)

@Composable
fun VictoryCelebrationOverlay(
    state: GameState,
    starsEarned: Int,
    rewardCoins: Int,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onHome: () -> Unit,
    onPlayStarSound: (Int) -> Unit
) {
    // Fases de la coreografía de victoria
    var animStage by remember { mutableIntStateOf(0) }
    var displayedScore by remember { mutableIntStateOf(0) }
    var displayedCoins by remember { mutableIntStateOf(0) }

    // Partículas de confeti y fuegos artificiales
    val particles = remember { mutableStateListOf<VictoryParticle>() }

    // Rotación continua del aura de rayos dorados detrás del trofeo
    val infiniteTransition = rememberInfiniteTransition(label = "sunburst")
    val sunburstRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunburstAngle"
    )

    // Pulsación suave del botón principal
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseButton"
    )

    // Iniciar simulación de partículas y secuencia coreografiada
    LaunchedEffect(Unit) {
        val rand = Random.Default
        // Generar explosión inicial de confeti y chispas
        for (i in 0 until 90) {
            val angle = rand.nextFloat() * 2 * Math.PI.toFloat()
            val speed = rand.nextFloat() * 16f + 6f
            val isConfetti = rand.nextBoolean()
            val color = listOf(
                GoldAccent, Color(0xFFF59E0B), EmeraldPrimary, Color(0xFF38BDF8),
                Color(0xFFEC4899), Color(0xFFA855F7), Color(0xFFF97316), Color.White
            ).random()

            particles.add(
                VictoryParticle(
                    x = 540f + (rand.nextFloat() * 80f - 40f),
                    y = 700f + (rand.nextFloat() * 60f - 30f),
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 6f, // impulso hacia arriba
                    rotation = rand.nextFloat() * 360f,
                    vRot = rand.nextFloat() * 12f - 6f,
                    size = if (isConfetti) rand.nextFloat() * 14f + 8f else rand.nextFloat() * 8f + 4f,
                    color = color,
                    isStar = !isConfetti
                )
            )
        }

        // Fases secuenciales
        animStage = 1 // 1: Trofeo y Título aparecen
        delay(400)

        // Estrellas secuenciales
        animStage = 2 // Estrella 1
        onPlayStarSound(0)
        delay(380)

        if (starsEarned >= 2) {
            animStage = 3 // Estrella 2
            onPlayStarSound(1)
            delay(380)
        }

        if (starsEarned >= 3) {
            animStage = 4 // Estrella 3
            onPlayStarSound(2)
            delay(400)
        } else {
            animStage = 4
        }

        // Tally de conteo de puntos y monedas
        animStage = 5
        val targetScore = state.score
        val targetCoins = rewardCoins
        val steps = 15
        for (step in 1..steps) {
            displayedScore = (targetScore * step) / steps
            displayedCoins = (targetCoins * step) / steps
            delay(30)
        }

        // Fila de botones final
        animStage = 6
    }

    // Bucle de física de partículas
    LaunchedEffect(Unit) {
        var lastTime = withFrameMillis { it }
        while (true) {
            val currentTime = withFrameMillis { it }
            val dt = ((currentTime - lastTime) / 1000f).coerceIn(0.01f, 0.05f)
            lastTime = currentTime

            for (p in particles) {
                p.x += p.vx * 60f * dt
                p.y += p.vy * 60f * dt
                p.vy += 18f * 60f * dt * dt // gravedad
                p.vx *= 0.985f // resistencia del aire
                p.rotation += p.vRot
                if (p.y > 1800f) {
                    p.alpha = (p.alpha - 0.05f).coerceAtLeast(0f)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .testTag("win_dialog"),
        contentAlignment = Alignment.Center
    ) {
        // Lienzo de Rayos de Sol Dorados y Partículas Físicas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.38f)
            val rayRadius = size.width * 0.95f

            // Rayos dorados rotando
            rotate(degrees = sunburstRotation, pivot = center) {
                val raysCount = 16
                val angleStep = 360f / raysCount
                for (i in 0 until raysCount) {
                    val angle1 = Math.toRadians((i * angleStep).toDouble())
                    val angle2 = Math.toRadians((i * angleStep + angleStep * 0.5f).toDouble())

                    val p1 = Offset(
                        center.x + (cos(angle1) * rayRadius).toFloat(),
                        center.y + (sin(angle1) * rayRadius).toFloat()
                    )
                    val p2 = Offset(
                        center.x + (cos(angle2) * rayRadius).toFloat(),
                        center.y + (sin(angle2) * rayRadius).toFloat()
                    )

                    val path = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(p1.x, p1.y)
                        lineTo(p2.x, p2.y)
                        close()
                    }
                    drawPath(
                        path = path,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GoldAccent.copy(alpha = 0.22f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = rayRadius
                        )
                    )
                }
            }

            // Dibujar partículas de confeti y estrellas
            for (p in particles) {
                if (p.alpha <= 0f) continue
                rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
                    if (p.isStar) {
                        drawCircle(
                            color = p.color.copy(alpha = p.alpha),
                            radius = p.size,
                            center = Offset(p.x, p.y)
                        )
                    } else {
                        drawRect(
                            color = p.color.copy(alpha = p.alpha),
                            topLeft = Offset(p.x - p.size / 2, p.y - p.size / 4),
                            size = Size(p.size, p.size * 0.6f)
                        )
                    }
                }
            }
        }

        // Tarjeta Central de Victoria
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.96f)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(24.dp, RoundedCornerShape(28.dp))
                .border(2.5.dp, GoldAccent, RoundedCornerShape(28.dp))
                .padding(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Paso 1: Trofeo con animación elástica de impacto
                val trophyScale by animateFloatAsState(
                    targetValue = if (animStage >= 1) 1.0f else 0.1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "trophyScale"
                )

                Box(
                    modifier = Modifier
                        .scale(trophyScale)
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(GoldAccent.copy(alpha = 0.4f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🏆",
                        fontSize = 68.sp,
                        modifier = Modifier.offset(y = (-2).dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "¡TABLERO LIMPIO!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldAccent,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "${state.destination.flag} ${state.destination.country} • Nivel ${state.levelNumber}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Paso 2, 3, 4: Revelación escalonada e impactante de estrellas ⭐⭐⭐
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    for (i in 0..2) {
                        val isStarUnlocked = i < starsEarned
                        val isRevealed = when (i) {
                            0 -> animStage >= 2
                            1 -> animStage >= 3
                            else -> animStage >= 4
                        }

                        val starScale by animateFloatAsState(
                            targetValue = if (isRevealed && isStarUnlocked) 1.0f else if (isRevealed) 0.85f else 0.2f,
                            animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
                            label = "starScale$i"
                        )

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .scale(starScale),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isStarUnlocked && isRevealed) {
                                Text(
                                    text = "⭐",
                                    fontSize = if (i == 1) 48.sp else 38.sp
                                )
                            } else {
                                Text(
                                    text = "▪️",
                                    fontSize = 28.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Paso 5: Caja de Estadísticas con Tally animado de puntos, monedas y bonus de tiempo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Puntos", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(
                                    text = "$displayedScore",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Movimientos", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(
                                    text = "${state.movesCount}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Recompensa", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(
                                    text = "+$displayedCoins 🪙",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CoinGold
                                )
                            }
                        }

                        // Fila de Bonus por Temporizador de nivel
                        val timeMin = state.timeRemainingSec / 60
                        val timeSec = state.timeRemainingSec % 60
                        val timeFormatted = String.format(java.util.Locale.getDefault(), "%02d:%02d", timeMin, timeSec)

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B).copy(alpha = 0.7f))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⏱️ Tiempo sobrante: $timeFormatted",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                            Text(
                                text = "+${state.timeRemainingSec * 5} bonus",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldAccent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Paso 6: Botones de Acción con pulsación en Siguiente Nivel
                AnimatedVisibility(
                    visible = animStage >= 5,
                    enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = onNextLevel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .scale(pulseScale)
                                .shadow(12.dp, RoundedCornerShape(16.dp))
                                .testTag("btn_next_level"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Siguiente Nivel ➡️",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onReplay,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF64748B))
                            ) {
                                Text("🔄 Repetir", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onHome,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF64748B))
                            ) {
                                Text("🏠 Menú", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
