package com.mavacode.aceptakm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mavacode.aceptakm.ui.theme.AceptaTheme
import kotlinx.coroutines.delay

@Composable
fun TariffConfigScreen(
    onNextClick: (String) -> Unit
) {
    var minimumTariff by remember { mutableStateOf("") }
    val cs = MaterialTheme.colorScheme
    val app = AceptaTheme.colors

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .imePadding()
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
                text = "Paso 1 de 4",
                color = app.textSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = { 0.25f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = cs.primary,
            trackColor = app.outline,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "¿Cuál es tu tarifa mínima?",
            color = cs.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Ingresa el monto mínimo por el que estás dispuesto a aceptar un viaje.",
            color = app.textSecondary,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = minimumTariff,
            onValueChange = { minimumTariff = it },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder = { Text("0.00", color = app.textSecondary, fontSize = 18.sp) },
            leadingIcon = {
                Text(
                    text = "$",
                    color = cs.primary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onNextClick(minimumTariff) },
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
                Text("Siguiente", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Siguiente",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        InfoFeatureCard(
            icon = Icons.Outlined.Security,
            iconTint = cs.primary,
            title = "Datos Seguros",
            description = "Tu información financiera se utiliza localmente para optimizar tus rutas y no se comparte con terceros."
        )

        Spacer(modifier = Modifier.height(16.dp))

        InfoFeatureCard(
            icon = Icons.Outlined.BarChart,
            iconTint = Color(0xFF4ADE80),
            title = "Cálculo Inteligente",
            description = "Calculamos el desgaste de tu moto y el costo de combustible actual para darte números reales."
        )

        Spacer(modifier = Modifier.height(16.dp))

        InfoFeatureCard(
            icon = Icons.Outlined.FlashOn,
            iconTint = app.textSecondary,
            title = "Sin Filtros",
            description = "Configura tus preferencias una vez y deja que AceptaKm filtre las mejores ofertas para ti."
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun InfoFeatureCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    val app = AceptaTheme.colors

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = app.cardAlt),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(app.iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                color = app.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                color = app.textSecondary,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }
    }
}