package com.mavacode.aceptakm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mavacode.aceptakm.ui.theme.*

@Composable
fun FilterConfigStepScreen(
    stepNumber: Int, // Pasas 2, 3 o 4
    title: String,
    description: String,
    placeholder: String,
    prefixText: String = "",
    suffixText: String = "",
    onNextClick: (String) -> Unit
) {
    var inputValue by remember { mutableStateOf("") }


    // Calculamos el progreso basado en el paso (Paso 2 = 50%, Paso 3 = 75%, Paso 4 = 100%)
    val progress = stepNumber / 4f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // --- CABECERA Y BARRA DE PROGRESO ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.TwoWheeler,
                    contentDescription = "Logo",
                    tint = primaryBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "aceptakm",
                    color = primaryBlue,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Paso $stepNumber de 4",
                color = TextGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Barra de progreso dinámica
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = primaryBlue,
            trackColor = Color(0xFFE5E7EB),
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- TÍTULO Y DESCRIPCIÓN DINÁMICOS ---
        Text(
            text = title,
            color = textDark,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = description,
            color = TextGray,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- CAMPO DE ENTRADA DE TEXTO ---
        OutlinedTextField(
            value = inputValue,
            onValueChange = { inputValue = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color(0xFFC2C6D6), fontSize = 18.sp) },
            leadingIcon = if (prefixText.isNotEmpty()) {
                {
                    Text(
                        text = prefixText,
                        color = primaryBlue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            } else null,
            trailingIcon = if (suffixText.isNotEmpty()) {
                {
                    Text(
                        text = suffixText,
                        color = TextGray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            } else null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryBlue,
                unfocusedBorderColor = Color(0xFFC2C6D6),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = textDark,
                unfocusedTextColor = textDark
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- BOTÓN SIGUIENTE / FINALIZAR ---
        Button(
            onClick = { onNextClick(inputValue) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (stepNumber == 4) "Finalizar Configuración" else "Siguiente",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Continuar",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}