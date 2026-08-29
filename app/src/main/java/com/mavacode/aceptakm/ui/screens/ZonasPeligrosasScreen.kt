package com.mavacode.aceptakm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.mavacode.aceptakm.ui.viewmodel.ZonasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZonasPeligrosasScreen(viewModel: ZonasViewModel = viewModel()) {
    var textoInput by remember { mutableStateOf("") }
    val zonas by viewModel.listaZonas.collectAsState()

    // Filtramos la lista principal en dos listas distintas basándonos en tu lógica de guardado
    val municipios = zonas.filter { it.palabraClave.startsWith("Municipio/Viaje:") }
    val colonias = zonas.filter { it.palabraClave.startsWith("Colonia:") }

    // Colores extraídos directamente del diseño
    val colorDarkRed = Color(0xFFB72025)
    val colorLightPink = Color(0xFFFDECEB)
    val colorBadgeBg = Color(0xFFE2E8F0)
    val colorBadgeText = Color(0xFF0F172A)
    val colorDarkTitle = Color(0xFF111827)
    val colorBorder = Color(0xFFE5E7EB)
    val colorItemBg = Color(0xFFF9FAFB)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // HEADER
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = "Zonas Peligrosas",
                tint = colorDarkRed,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Zonas Peligrosas",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colorDarkTitle
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ÁREA DE INPUT
        Text(
            text = "Agregar nueva zona",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = textoInput,
            onValueChange = { textoInput = it },
            placeholder = { Text("Ej. Ecatepec, Tepito, Iztapalapa", color = Color.Gray) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.Gray)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = colorBorder,
                unfocusedBorderColor = colorBorder
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // BOTONES VERTICALES
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
            colors = ButtonDefaults.buttonColors(containerColor = colorDarkRed)
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
            colors = ButtonDefaults.buttonColors(containerColor = colorLightPink)
        ) {
            Icon(Icons.Outlined.Map, contentDescription = null, tint = colorDarkRed, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Bloquear Municipio", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorDarkRed)
        }

        Spacer(modifier = Modifier.height(40.dp))

        // LISTA DE MUNICIPIOS
        ZonasListCard(
            titulo = "Municipios Bloqueados",
            icono = Icons.Outlined.Map,
            iconTint = Color(0xFF0052CC),
            items = municipios,
            onDelete = { viewModel.eliminarZona(it) },
            badgeBg = colorBadgeBg,
            badgeText = colorBadgeText,
            itemBg = colorItemBg,
            borderColor = colorBorder
        )

        Spacer(modifier = Modifier.height(24.dp))

        // LISTA DE COLONIAS
        ZonasListCard(
            titulo = "Colonias Bloqueadas",
            icono = Icons.Outlined.LocationOn,
            iconTint = Color(0xFF0052CC),
            items = colonias,
            onDelete = { viewModel.eliminarZona(it) },
            badgeBg = colorBadgeBg,
            badgeText = colorBadgeText,
            itemBg = colorItemBg,
            borderColor = colorBorder
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun ZonasListCard(
    titulo: String,
    icono: ImageVector,
    iconTint: Color,
    items: List<ZonaPeligrosa>,
    onDelete: (ZonaPeligrosa) -> Unit,
    badgeBg: Color,
    badgeText: Color,
    itemBg: Color,
    borderColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Encabezado con título y contador
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icono, contentDescription = titulo, tint = iconTint, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    // Hacemos que el título pueda usar dos líneas si es necesario
                    Text(
                        text = titulo,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        lineHeight = 24.sp,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items.size.toString(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = borderColor.copy(alpha = 0.5f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Iterador de elementos
            if (items.isEmpty()) {
                Text(
                    text = "Aún no has agregado zonas.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            } else {
                items.forEach { zona ->
                    // Removemos el prefijo lógico para que la UI se vea limpia como en tu diseño
                    val textoLimpio = zona.palabraClave
                        .replace("Municipio/Viaje: ", "")
                        .replace("Colonia: ", "")
                        .trim()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .background(itemBg, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Block,
                                contentDescription = "Bloqueado",
                                tint = Color(0xFFB72025),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = textoLimpio,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1F2937)
                            )
                        }
                        IconButton(
                            onClick = { onDelete(zona) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Eliminar",
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}