package com.mavacode.aceptakm.ui.overlay

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mavacode.aceptakm.feature.asistente.NivelRentabilidad
import com.mavacode.aceptakm.feature.asistente.TripData
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

private data class OverlayPalette(
    val card: Color,
    val header: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
    val accent: Color
)

@Composable
private fun overlayPalette(): OverlayPalette {
    return if (isSystemInDarkTheme()) {
        OverlayPalette(
            card = Color(0xFF1C1F26),
            header = Color(0xFF252A33),
            textPrimary = Color(0xFFF3F4F6),
            textSecondary = Color(0xFF9CA3AF),
            divider = Color(0xFF3A404A),
            accent = Color(0xFF6EA8FF)
        )
    } else {
        OverlayPalette(
            card = Color(0xFFFFFFFF),
            header = Color(0xFFF8F9FA),
            textPrimary = Color(0xFF111827),
            textSecondary = Color(0xFF4B5563),
            divider = Color(0xFFE5E7EB),
            accent = Color(0xFF0052CC)
        )
    }
}

@Composable
fun FloatingOverlay(
    tripData: TripData?,
    capturaActiva: Boolean = false,
    onDrag: (Float, Float) -> Unit,
    onClose: () -> Unit,
    onReactivar: () -> Unit = {}
) {
    if (tripData != null) {
        AceptaKMCard(tripData = tripData, onClose = onClose)
    } else {
        Box(
            modifier = Modifier.pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
        ) {
            WaitingTripCard(
                capturaActiva = capturaActiva,
                onReactivar = onReactivar
            )
        }
    }
}

@Composable
fun WaitingTripCard(
    capturaActiva: Boolean,
    onReactivar: () -> Unit = {}
) {
    var estaAburrido by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(60_000L)
        estaAburrido = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "animacion_gusano")
    val progreso by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progreso_ciclo"
    )

    val vaHaciaLaDerecha = progreso <= 1f
    val desplazamientoX = if (vaHaciaLaDerecha) {
        (progreso * 16f) - 8f
    } else {
        8f - ((progreso - 1f) * 16f)
    }
    val desplazamientoY = (sin(progreso * PI * 6).toFloat()) * -3f
    val escalaX = if (vaHaciaLaDerecha) -1f else 1f
    val emojiFlotante = if (estaAburrido) "🐛" else "🍏"

    val modifierEmoji = if (estaAburrido) {
        Modifier
            .offset(x = desplazamientoX.dp, y = desplazamientoY.dp)
            .graphicsLayer(scaleX = escalaX)
    } else {
        Modifier
    }

    val textoEstado = if (capturaActiva) "Listo" else "Toca para leer"
    val colorFondo = if (capturaActiva) {
        Color(0xFF166534).copy(alpha = 0.92f)
    } else {
        Color(0xFF1A1A1A).copy(alpha = 0.88f)
    }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .wrapContentSize()
            .clickable { onReactivar() },
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emojiFlotante,
                fontSize = 16.sp,
                modifier = modifierEmoji
            )
            Text(
                text = textoEstado,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AceptaKMCard(tripData: TripData, onClose: () -> Unit) {
    val colors = overlayPalette()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val cardWidth = screenWidth * 0.88f

    val esPeligro = tripData.sugerencia.contains("PELIGRO", ignoreCase = true)
    val zonaIcon = if (esPeligro) Icons.Default.Warning else Icons.Default.Security
    val zonaColor = if (esPeligro) Color(0xFFEF4444) else Color(0xFF22C55E)
    val zonaText = if (esPeligro) "Zona Peligrosa" else "Zona Segura"

    val (badgeColor, actionColor, iconRes, textoRecomendacion) = when (tripData.nivelRentabilidad) {
        NivelRentabilidad.ALTA -> Quadruple(
            Color(0xFF16A34A), Color(0xFF22C55E), Icons.Default.CheckBox, "ACEPTAR"
        )
        NivelRentabilidad.MEDIA -> Quadruple(
            Color(0xFFD97706), Color(0xFFF59E0B), Icons.Default.Warning, "ACEPTAR CON CUIDADO"
        )
        NivelRentabilidad.BAJA, NivelRentabilidad.RECHAZAR -> Quadruple(
            Color(0xFFDC2626), Color(0xFFEF4444), Icons.Default.Cancel, "RECHAZAR"
        )
    }

    Card(
        modifier = Modifier
            .width(cardWidth)
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            SeasonalFallingLeaves(modifier = Modifier.matchParentSize())

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.header)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "AceptaKm",
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        modifier = Modifier
                            .background(zonaColor.copy(alpha = 0.16f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(zonaIcon, null, tint = zonaColor, modifier = Modifier.size(12.dp))
                        Text(
                            text = zonaText,
                            color = zonaColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Card(
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = badgeColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(10.dp))
                            Text(
                                text = tripData.nivelRentabilidad.name.replace("_", " "),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onClose() },
                        tint = colors.textSecondary
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GANANCIA NETA",
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "$${tripData.gananciaNeta}",
                        color = colors.textPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        thickness = 1.dp,
                        color = colors.divider
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            modifier = Modifier.weight(1f),
                            icon = Icons.AutoMirrored.Filled.Sort,
                            value = "$${tripData.pagoPorKm}/km",
                            label = "Rentabilidad",
                            iconColor = colors.accent,
                            textPrimary = colors.textPrimary,
                            textSecondary = colors.textSecondary
                        )
                        VerticalDivider(color = colors.divider)
                        MetricItem(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Timer,
                            value = "${tripData.tiempoTotal} min",
                            label = "Tiempo",
                            iconColor = colors.accent,
                            textPrimary = colors.textPrimary,
                            textSecondary = colors.textSecondary
                        )
                        VerticalDivider(color = colors.divider)
                        MetricItem(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.MyLocation,
                            value = "${tripData.distanciaTotal} km",
                            label = "Total",
                            iconColor = colors.accent,
                            textPrimary = colors.textPrimary,
                            textSecondary = colors.textSecondary
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        thickness = 1.dp,
                        color = colors.divider
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(iconRes, null, tint = actionColor, modifier = Modifier.size(18.dp))
                        Text(
                            text = textoRecomendacion,
                            color = actionColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(colors.divider)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.6f)
                            .background(colors.accent)
                    )
                    Box(modifier = Modifier.weight(0.4f))
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    iconColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
        Text(value, color = textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(label, color = textSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun VerticalDivider(color: Color) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(color)
    )
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)