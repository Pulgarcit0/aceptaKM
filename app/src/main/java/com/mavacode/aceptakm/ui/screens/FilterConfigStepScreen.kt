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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mavacode.aceptakm.ui.theme.AceptaTheme

@Composable
fun FilterConfigStepScreen(
    stepNumber: Int,
    title: String,
    description: String,
    placeholder: String,
    prefixText: String = "",
    suffixText: String = "",
    onNextClick: (String) -> Unit
) {
    var inputValue by remember { mutableStateOf("") }
    val cs = MaterialTheme.colorScheme
    val app = AceptaTheme.colors
    val progress = stepNumber / 4f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.TwoWheeler,
                    contentDescription = "Logo",
                    tint = cs.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AceptaKm",
                    color = cs.primary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Paso $stepNumber de 4",
                color = app.textSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = cs.primary,
            trackColor = app.outline,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = title,
            color = cs.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = description,
            color = app.textSecondary,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = inputValue,
            onValueChange = { inputValue = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = app.textSecondary, fontSize = 18.sp) },
            leadingIcon = if (prefixText.isNotEmpty()) {
                {
                    Text(
                        text = prefixText,
                        color = cs.primary,
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
                        color = app.textSecondary,
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
                focusedBorderColor = cs.primary,
                unfocusedBorderColor = app.outline,
                focusedContainerColor = app.card,
                unfocusedContainerColor = app.card,
                focusedTextColor = app.textPrimary,
                unfocusedTextColor = app.textPrimary,
                cursorColor = cs.primary
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onNextClick(inputValue) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = cs.primary)
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