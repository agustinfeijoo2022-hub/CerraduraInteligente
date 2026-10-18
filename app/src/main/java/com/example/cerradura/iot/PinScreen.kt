package com.example.cerradura.iot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PinScreen(
    onPinCorrecto: () -> Unit
) {
    var tienePin by remember { mutableStateOf<Boolean?>(null) }
    var pin by remember { mutableStateOf("") }
    var pinConfirmar by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var intentos by remember { mutableStateOf(0) }
    var verificando by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        PinDB.tienePinRegistrado { existe ->
            tienePin = existe
        }
    }

    if (tienePin == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF6C63FF), Color(0xFF3F3D9E))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    val esPrimeraVez = tienePin == false

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
            Surface(
                modifier = Modifier.size(90.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "PIN",
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (esPrimeraVez) "Crear PIN de acceso" else "Acceso protegido",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (esPrimeraVez)
                    "Ingresa un PIN de 4 dígitos para proteger tu cuenta"
                else
                    "Ingresa tu PIN de 4 dígitos",
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { nuevo ->
                            if (nuevo.length <= 4 && nuevo.all { it.isDigit() }) {
                                pin = nuevo
                                error = ""
                            }
                        },
                        label = { Text(if (esPrimeraVez) "Nuevo PIN" else "PIN") },
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, contentDescription = null)
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (esPrimeraVez) {
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = pinConfirmar,
                            onValueChange = { nuevo ->
                                if (nuevo.length <= 4 && nuevo.all { it.isDigit() }) {
                                    pinConfirmar = nuevo
                                    error = ""
                                }
                            },
                            label = { Text("Confirmar PIN") },
                            leadingIcon = {
                                Icon(Icons.Filled.Lock, contentDescription = null)
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (error.isNotEmpty()) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = {
                            if (pin.length != 4) {
                                error = "El PIN debe tener 4 dígitos"
                                return@Button
                            }

                            if (esPrimeraVez) {
                                if (pin != pinConfirmar) {
                                    error = "Los PIN no coinciden"
                                    return@Button
                                }
                                verificando = true
                                PinDB.guardarPin(pin) { ok ->
                                    verificando = false
                                    if (ok) {
                                        onPinCorrecto()
                                    } else {
                                        error = "Error al guardar el PIN"
                                    }
                                }
                            } else {
                                verificando = true
                                PinDB.verificarPin(pin) { correcto ->
                                    verificando = false
                                    if (correcto) {
                                        onPinCorrecto()
                                    } else {
                                        intentos++
                                        if (intentos >= 3) {
                                            error = "Demasiados intentos. Reinicia la app."
                                        } else {
                                            error = "PIN incorrecto. Intentos: ${3 - intentos}"
                                            pin = ""
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !verificando && pin.length == 4,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (verificando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = if (esPrimeraVez) "Crear PIN" else "Verificar PIN",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}