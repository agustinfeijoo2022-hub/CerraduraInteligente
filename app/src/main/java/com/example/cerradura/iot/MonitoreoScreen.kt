package com.example.cerradura.iot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun MonitoreoScreen(onGoToControl: () -> Unit) {

    var estado by remember { mutableStateOf(ArduinoSimulator.estado) }
    var conexion by remember { mutableStateOf("Simulador activo") }

    // Refresca cada 500ms para ver cambios en vivo
    LaunchedEffect(Unit) {
        while (true) {
            estado = ArduinoSimulator.estado
            conexion = if (ArduinoSimulator.usarSimulador) "Simulador activo" else "Conectado WiFi"
            delay(500)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF6C63FF), Color(0xFF3F3D9E))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Panel de Monitoreo",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = if (estado == "BLOQUEADO")
                            Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (estado == "BLOQUEADO")
                                    Icons.Filled.Lock else Icons.Filled.LockOpen,
                                contentDescription = null,
                                tint = if (estado == "BLOQUEADO")
                                    Color(0xFFE53935) else Color(0xFF4CAF50),
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Estado de la Cerradura",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )

                    Text(
                        text = estado,
                        fontSize = 28.sp,
                        color = if (estado == "BLOQUEADO")
                            Color(0xFFE53935) else Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Información del sistema",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF3F3D9E),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    InfoRow("Conexión", conexion)
                    InfoRow("Modo", if (ArduinoSimulator.modoAuto) "Automático" else "Manual")
                    InfoRow("Sensor MPU6050", "En espera")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onGoToControl,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF3F3D9E)
                )
            ) {
                Text(
                    "Ir al Panel de Control",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray)
        Text(text = value, fontWeight = FontWeight.Medium)
    }
}