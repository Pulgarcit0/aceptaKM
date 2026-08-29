package com.mavacode.aceptakm.ui.overlay

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
import java.util.Calendar
import kotlin.random.Random

// Controla cada emoji (ahora incluye qué texto/emoji es)
private data class EmojiParticle(
    var text: String = "",
    var x: Float = Random.nextFloat(),
    var y: Float = Random.nextFloat() - 1f,
    var speedY: Float = Random.nextFloat() * 0.008f + 0.004f,
    var speedX: Float = Random.nextFloat() * 0.002f - 0.001f,
    var size: Float = Random.nextFloat() * 8f + 16f,
    var rotation: Float = Random.nextFloat() * 360f,
    var rotationSpeed: Float = Random.nextFloat() * 1.5f - 0.75f
)

@Composable
fun SeasonalFallingLeaves(modifier: Modifier = Modifier) {
    // 1. Detectar el mes actual (0 = Enero, 11 = Diciembre)
    val currentMonth = remember { Calendar.getInstance().get(Calendar.MONTH) }

    // 2. Catálogo de animaciones por festividades (¡sin banderas que estorben!)
    val emojiOptions = remember {
        when (currentMonth) {
            8 -> listOf("🪅", "🎊", "🌮") // Septiembre: Mes Patrio
            9 -> listOf("🎃", "👻", "🦇") // Octubre: Halloween / Pre-Muertos
            10 -> listOf("🎃", "🌼", "🍂") // Noviembre: Día de Muertos (Calaveras y Cempasúchil)
            11 -> listOf("🎄", "🎁", "❄️") // Diciembre: Navidad
            in 0..1 -> listOf("❄️", "⛄", "☕") // Enero/Febrero: Invierno
            in 2..4 -> listOf("🌸", "🦋", "🐝") // Marzo - Mayo: Primavera
            else -> listOf("🌿", "🍃", "👻") // Junio - Agosto: Verano
        }
    }

    // 3. ¡SOLO 3 FIGURAS! Le asignamos un emoji aleatorio a cada una al nacer
    val particles = remember {
        List(3) { EmojiParticle(text = emojiOptions.random()) }
    }

    var time by remember { mutableStateOf(0L) }

    // 4. Bucle de física
    LaunchedEffect(Unit) {
        var lastFrame = 0L
        while (isActive) {
            withFrameNanos { frameTimeNanos ->
                if (lastFrame == 0L) lastFrame = frameTimeNanos
                val delta = (frameTimeNanos - lastFrame) / 1_000_000f
                lastFrame = frameTimeNanos

                time = frameTimeNanos

                particles.forEach { p ->
                    // Movimiento
                    p.y += p.speedY * (delta / 16f)
                    p.x += p.speedX * (delta / 16f)
                    p.rotation += p.rotationSpeed * (delta / 16f)

                    // Magia: Si llega al fondo, reaparece arriba ¡Y CAMBIA DE EMOJI!
                    if (p.y > 1.2f) {
                        p.y = -0.2f
                        p.x = Random.nextFloat()
                        p.text = emojiOptions.random() // Escoge uno nuevo del mes
                    }
                }
            }
        }
    }

    // 5. Dibujar en la tarjeta
    BoxWithConstraints(modifier = modifier) {
        val widthDp = maxWidth
        val heightDp = maxHeight

        time.let {
            particles.forEach { p ->
                Text(
                    text = p.text,
                    fontSize = p.size.sp,
                    modifier = Modifier
                        .offset(
                            x = widthDp * p.x,
                            y = heightDp * p.y
                        )
                        .graphicsLayer {
                            rotationZ = p.rotation
                            alpha = 0.45f // Transparencia perfecta para que se vean los colores sin tapar la ruta
                        }
                )
            }
        }
    }
}