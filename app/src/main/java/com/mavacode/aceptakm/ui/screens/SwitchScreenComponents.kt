package com.mavacode.aceptakm.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.outlined.Anchor
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mavacode.aceptakm.ui.theme.*

@Composable
fun CardBloqueo(
    titulo: String,
    mensaje: String,
    textoBoton: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = titulo,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = mensaje,
                fontSize = 14.sp,
                color = OnSurfaceVariantColor,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text(textoBoton, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Card de plataforma individual o maestra.
 *
 * @param switchEnabled false = switch bloqueado (ej. cuando "Todas" está activa)
 * @param isAnclada true = es el default de Home
 * @param mostrarAncla false en la card maestra "Todas las plataformas"
 * @param onAnclaClick toque en el pin
 */
@Composable
fun PlataformaCardHtml(
    nombre: String,
    icono: ImageVector,
    colorTema: Color,
    isActivo: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    switchEnabled: Boolean = true,
    isAnclada: Boolean = false,
    mostrarAncla: Boolean = true,
    onAnclaClick: (() -> Unit)? = null
) {
    val opacidad = when {
        !switchEnabled && isActivo -> 0.85f
        isActivo -> 1f
        else -> 0.7f
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (switchEnabled || isActivo) 1f else 0.9f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            width = if (isAnclada) 2.dp else 1.dp,
            color = if (isAnclada) PrimaryColor.copy(alpha = 0.5f)
            else OutlineVariantColor.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActivo) 4.dp else 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono de la app
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colorTema.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = nombre,
                        tint = colorTema,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Ancla (default para Home)
                    if (mostrarAncla && onAnclaClick != null) {
                        IconButton(
                            onClick = onAnclaClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (isAnclada) Icons.Default.Anchor
                                else Icons.Outlined.Anchor,
                                contentDescription = if (isAnclada) "Default en Home"
                                else "Fijar como default en Home",
                                tint = if (isAnclada) PrimaryColor
                                else Color(0xFF9CA3AF),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Switch(
                        checked = isActivo,
                        onCheckedChange = onCheckedChange,
                        enabled = switchEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryColor,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFD3E4FE),
                            uncheckedBorderColor = Color.Transparent,
                            disabledCheckedTrackColor = PrimaryColor.copy(alpha = 0.5f),
                            disabledCheckedThumbColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = nombre,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnSurfaceColor,
                modifier = Modifier.alpha(opacidad)
            )

            val subtitulo = when {
                isAnclada && isActivo -> "Activo · Default en Home"
                isAnclada -> "Inactivo · Default en Home"
                isActivo && !switchEnabled -> "Activo (Todas las plataformas)"
                isActivo -> "Activo - Recibiendo viajes"
                else -> "Inactivo"
            }

            Text(
                text = subtitulo,
                fontSize = 14.sp,
                color = if (isAnclada) PrimaryColor else OnSurfaceVariantColor,
                modifier = Modifier.alpha(opacidad)
            )
        }
    }
}

@Composable
fun BannerProximamenteHtml() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryColor.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(32.dp)) {
            Surface(
                color = PrimaryColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(50)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Funciones Próximamente",
                        fontSize = 12.sp,
                        color = PrimaryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Integración Automática de Tarifas",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnSurfaceColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Estamos trabajando duro para ofrecerte una vista unificada de las tarifas en tiempo real de todas tus plataformas activas. ¡Mantente atento!",
                fontSize = 16.sp,
                color = OnSurfaceVariantColor,
                lineHeight = 24.sp
            )
        }
    }
}