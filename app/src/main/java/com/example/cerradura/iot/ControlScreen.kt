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
import java.util.Locale

@Composable
fun ControlScreen(onBack: () -> Unit) {

    val scope = rememberCoroutineScope()

    var estadoCerradura by remember { mutableStateOf("BLOQUEADO") }
    var modoAuto by remember { mutableStateOf(ArduinoSimulator.modoAuto) }
    var tiempoDesbloqueo by remember { mutableStateOf(ArduinoSimulator.tiempoDesbloqueo) }
    var gestoSeleccionado by remember { mutableStateOf(ArduinoSimulator.gestoSeleccionado) }
    var mensaje by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Sincroniza el estado con el servidor cada 2 segundos
    LaunchedEffect(Unit) {
        while (true) {
            val resp = WifiClient.enviarComando("STATUS")
            if (resp.startsWith("STATUS:")) {
                val estadoReal = resp.removePrefix("STATUS:").trim()
                if (estadoReal == "BLOQUEADO" || estadoReal == "DESBLOQUEADO") {
                    estadoCerradura = estadoReal
                }
            }
            delay(2000)
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
                text = "Panel de Control",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Controla tu cerradura de forma remota",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ===== ESTADO Y BOTONES =====
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
                    Text(
                        text = "Estado actual",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = if (estadoCerradura == "BLOQUEADO")
                            Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (estadoCerradura == "BLOQUEADO")
                                    Icons.Filled.Lock else Icons.Filled.LockOpen,
                                contentDescription = null,
                                tint = if (estadoCerradura == "BLOQUEADO")
                                    Color(0xFFE53935) else Color(0xFF4CAF50),
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = estadoCerradura,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (estadoCerradura == "BLOQUEADO")
                            Color(0xFFE53935) else Color(0xFF4CAF50)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ===== BOTÓN PRINCIPAL CON WIFI =====
                    Button(
                        onClick = {
                            if (isLoading) return@Button
                            isLoading = true
                            val comando = if (estadoCerradura == "BLOQUEADO") "UNLOCK" else "LOCK"

                            scope.launch {
                                // 1. Enviar el comando
                                val respuesta = WifiClient.enviarComando(comando)
                                mensaje = "Comando: $respuesta"

                                // 2. Consultar el estado real al servidor
                                val estadoRespuesta = WifiClient.enviarComando("STATUS")
                                if (estadoRespuesta.startsWith("STATUS:")) {
                                    val estadoReal = estadoRespuesta.removePrefix("STATUS:").trim()
                                    if (estadoReal == "BLOQUEADO" || estadoReal == "DESBLOQUEADO") {
                                        estadoCerradura = estadoReal
                                    }
                                } else if (respuesta.startsWith("OK:")) {
                                    // Fallback
                                    estadoCerradura = if (comando == "UNLOCK") "DESBLOQUEADO" else "BLOQUEADO"
                                }

                                isLoading = false

                                // 3. Auto-bloqueo
                                if (comando == "UNLOCK" && modoAuto && respuesta.startsWith("OK:")) {
                                    val tiempo = tiempoDesbloqueo
                                    val tiempoTexto = if (tiempo % 1f == 0f) {
                                        "${tiempo.toInt()}"
                                    } else {
                                        String.format(Locale.US, "%.1f", tiempo)
                                    }
                                    mensaje = "Desbloqueado por $tiempoTexto segundos..."
                                    delay((tiempo * 1000).toLong())

                                    val respLock = WifiClient.enviarComando("LOCK")
                                    mensaje = "🔒 Cerrado automáticamente ($respLock)"
                                    estadoCerradura = "BLOQUEADO"
                                }
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (estadoCerradura == "BLOQUEADO")
                                Color(0xFF4CAF50) else Color(0xFFE53935)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = if (estadoCerradura == "BLOQUEADO")
                                    "🔓 Desbloquear" else "🔒 Bloquear",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== MODO AUTOMÁTICO =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Modo Automático",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3F3D9E)
                        )
                        Text(
                            text = if (modoAuto) "Activado" else "Desactivado",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = modoAuto,
                        onCheckedChange = {
                            modoAuto = it
                            ArduinoSimulator.modoAuto = it
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== TIEMPO DE DESBLOQUEO =====
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
                        text = "Tiempo de Desbloqueo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F3D9E)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val tiempoTexto = if (tiempoDesbloqueo % 1f == 0f) {
                        "${tiempoDesbloqueo.toInt()} segundos"
                    } else {
                        String.format(Locale.US, "%.1f segundos", tiempoDesbloqueo)
                    }

                    Text(
                        text = tiempoTexto,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )

                    Slider(
                        value = tiempoDesbloqueo,
                        onValueChange = {
                            tiempoDesbloqueo = it
                            ArduinoSimulator.tiempoDesbloqueo = it
                        },
                        valueRange = 1f..10f
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== SELECCIÓN DE GESTO =====
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
                        text = "Gesto de Desbloqueo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F3D9E)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(
                        "Derecha + Arriba",
                        "Izquierda + Arriba",
                        "Derecha + Abajo",
                        "Izquierda + Abajo"
                    ).forEach { gesto ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = gestoSeleccionado == gesto,
                                onClick = {
                                    gestoSeleccionado = gesto
                                    ArduinoSimulator.gestoSeleccionado = gesto
                                }
                            )
                            Text(text = gesto, color = Color.Black)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (mensaje.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = mensaje,
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFF3F3D9E),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text(
                    "Volver al Monitoreo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}