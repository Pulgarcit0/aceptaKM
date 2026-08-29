package com.mavacode.aceptakm.core.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FiltroZonasPeligrosas(
    zonasBloqueadas: List<String>,
    onAgregarZona: (String) -> Unit,
    onEliminarZona: (String) -> Unit
) {
    var textoInput by remember { mutableStateOf("") }

    // Colores basados en tu diseño principal de AceptaKm
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color(0xFFF7F9FC), // WhiteBackground
        unfocusedContainerColor = Color(0xFFF7F9FC),
        focusedBorderColor = Color(0xFF0052CC),    // MainBlue
        unfocusedBorderColor = Color(0xFFE0E0E0),  // MainOutline
        focusedTextColor = Color.DarkGray,
        unfocusedTextColor = Color.DarkGray
    )

    // Función interna para validar y procesar el guardado
    val intentarAgregarZona = {
        // Limpiamos espacios basura y pasamos todo a minúsculas
        val palabraLimpia = textoInput.trim().lowercase()

        // Solo agregamos si no está vacío y si no existe ya en la lista
        if (palabraLimpia.isNotEmpty() && !zonasBloqueadas.contains(palabraLimpia)) {
            onAgregarZona(palabraLimpia)
            textoInput = "" // Limpiamos el campo después de guardar
        } else if (zonasBloqueadas.contains(palabraLimpia)) {
            // Opcional: Podrías lanzar un Toast aquí si el usuario pone una colonia repetida
            textoInput = ""
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Zonas Excluidas (Palabras Clave)",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Input principal
        OutlinedTextField(
            value = textoInput,
            onValueChange = { textoInput = it },
            placeholder = { Text("Ej. montoya, xoxocotlan, centro") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done // Muestra el botón de "Listo/Enter" en el teclado
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    intentarAgregarZona()
                }
            ),
            trailingIcon = {
                IconButton(
                    onClick = { intentarAgregarZona() }
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Agregar Zona",
                        tint = Color(0xFF0052CC) // MainBlue
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Contenedor dinámico (FlowRow) que acomoda los chips automáticamente
        if (zonasBloqueadas.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                zonasBloqueadas.forEach { zona ->
                    InputChip(
                        selected = false,
                        onClick = { onEliminarZona(zona) },
                        label = { Text(text = zona, color = Color(0xFF0052CC), fontWeight = FontWeight.Medium) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Eliminar $zona",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF0052CC)
                            )
                        },
                        colors = InputChipDefaults.inputChipColors(
                            containerColor = Color(0xFFE3F2FD), // Fondo azul claro tipo Tag
                            labelColor = Color(0xFF0052CC)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        } else {
            // Mensaje de estado vacío
            Text(
                text = "No hay zonas bloqueadas. Los viajes no se filtrarán por colonia.",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}