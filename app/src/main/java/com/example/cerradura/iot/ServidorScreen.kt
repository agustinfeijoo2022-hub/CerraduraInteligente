package com.example.cerradura.iot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import kotlinx.coroutines.launch

@Composable
fun ServidorScreen(onBack: () -> Unit) {

    val scope = rememberCoroutineScope()

    var estado by remember { mutableStateOf(ArduinoSimulator.estado) }
    var mensajes = remember { mutableStateListOf<String>() }
    var servidorActivo by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        WifiServer.onComandoRecibido = { texto ->
            mensajes.add(0, "▶ $texto")
            if (mensajes.size > 30) mensajes.removeAt(mensajes.size - 1)
        }
        WifiServer.onClienteConectado = { ip ->
            mensajes.add(0, "✅ Cliente conectado: $ip")
        }

        scope.launch {
            servidorActivo = WifiServer.iniciar()
        }
    }

    LaunchedEffect(Unit) {
        var ultimoEstado = ""
        while (true) {
            val nuevoEstado = ArduinoSimulator.estado
            if (nuevoEstado != ultimoEstado) {
                estado = nuevoEstado
                ultimoEstado = nuevoEstado
            }
            delay(500)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            WifiServer.detener()
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Modo Servidor",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "🔒 Modo seguro (ISO 27400)",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (servidorActivo) "🟢 Servidor activo en puerto 8080" else "🔴 Servidor detenido",
                color = Color.White,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ===== ESTADO ACTUAL =====
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
                        modifier = Modifier.size(80.dp),
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
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = estado,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (estado == "BLOQUEADO")
                            Color(0xFFE53935) else Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== LOG DE COMANDOS =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "📋 Registro de comandos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F3D9E)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (mensajes.isEmpty()) {
                        Text(
                            text = "Esperando conexiones...",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    } else {
                        mensajes.forEach { msg ->
                            Text(
                                text = msg,
                                fontSize = 13.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    WifiServer.detener()
                    onBack()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text(
                    "Detener servidor",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}