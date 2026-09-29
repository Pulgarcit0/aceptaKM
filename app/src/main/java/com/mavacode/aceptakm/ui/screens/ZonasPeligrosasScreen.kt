package com.mavacode.aceptakm.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mavacode.aceptakm.domain.model.ZonaPeligrosa
import com.mavacode.aceptakm.ui.theme.AceptaTheme
import com.mavacode.aceptakm.ui.viewmodel.ZonasViewModel

private val ColorDarkRed = Color(0xFFB72025)
private val ColorLightPink = Color(0xFFFDECEB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZonasPeligrosasScreen(viewModel: ZonasViewModel = viewModel()) {
    var textoInput by remember { mutableStateOf("") }
    val zonas by viewModel.listaZonas.collectAsState()
    val cs = MaterialTheme.colorScheme
    val app = AceptaTheme.colors

    val municipios = zonas.filter { it.palabraClave.startsWith("Municipio/Viaje:") }
    val colonias = zonas.filter { it.palabraClave.startsWith("Colonia:") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = "Zonas Peligrosas",
                tint = ColorDarkRed,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Zonas Peligrosas",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = cs.onBackground
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Agregar nueva zona",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = app.textSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = textoInput,
            onValueChange = { textoInput = it },
            placeholder = { Text("Ej. Ecatepec, Tepito, Iztapalapa", color = app.textSecondary) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Buscar", tint = app.textSecondary)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = app.card,
                unfocusedContainerColor = app.card,
                focusedBorderColor = cs.primary,
                unfocusedBorderColor = app.outline,
                focusedTextColor = app.textPrimary,
                unfocusedTextColor = app.textPrimary,
                cursorColor = cs.primary
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (textoInput.isNotBlank()) {
                    viewModel.agregarZona("Colonia: ${textoInput.trim()}")
                    textoInput = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorDarkRed)
        ) {
            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Bloquear Colonia", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (textoInput.isNotBlank()) {
                    viewModel.agregarZona("Municipio/Viaje: ${textoInput.trim()}")
                    textoInput = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorLightPink)
        ) {
            Icon(Icons.Outlined.Map, contentDescription = null, tint = ColorDarkRed, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Bloquear Municipio", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ColorDarkRed)
        }

        Spacer(modifier = Modifier.height(40.dp))

        ZonasListCard(
            titulo = "Municipios Bloqueados",
            icono = Icons.Outlined.Map,
            items = municipios,
            onDelete = { viewModel.eliminarZona(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        ZonasListCard(
            titulo = "Colonias Bloqueadas",
            icono = Icons.Outlined.LocationOn,
            items = colonias,
            onDelete = { viewModel.eliminarZona(it) }
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun ZonasListCard(
    titulo: String,
    icono: ImageVector,
    items: List<ZonaPeligrosa>,
    onDelete: (ZonaPeligrosa) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val app = AceptaTheme.colors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = app.card),
        border = BorderStroke(1.dp, app.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = icono, contentDescription = titulo, tint = cs.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = titulo,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = app.textPrimary,
                        lineHeight = 24.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(cs.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items.size.toString(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = app.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = app.divider, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            if (items.isEmpty()) {
                Text(
                    text = "Aún no has agregado zonas.",
                    color = app.textSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            } else {
                items.forEach { zona ->
                    val textoLimpio = zona.palabraClave
                        .replace("Municipio/Viaje: ", "")
                        .replace("Colonia: ", "")
                        .trim()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .background(app.cardAlt, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Block,
                                contentDescription = "Bloqueado",
                                tint = ColorDarkRed,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = textoLimpio,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = app.textPrimary
                            )
                        }
                        IconButton(
                            onClick = { onDelete(zona) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Eliminar",
                                tint = app.textSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}