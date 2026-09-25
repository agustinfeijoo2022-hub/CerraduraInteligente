package com.example.cerradura.iot

import kotlinx.coroutines.delay

object ArduinoSimulator {

    // Cambiar a false cuando tengas el Arduino real
    var usarSimulador = true

    // IP del Arduino real (para después)
    var ipArduino = "192.168.1.100"
    var puertoArduino = 8080

    // Estado de la cerradura
    var estado: String = "BLOQUEADO"

    // Modo automático
    var modoAuto: Boolean = false

    // Tiempo de desbloqueo (segundos)
    var tiempoDesbloqueo: Float = 3f

    // Gesto seleccionado
    var gestoSeleccionado: String = "Derecha + Arriba"

    // Simula enviar un comando (SIN auto-bloqueo)
    suspend fun enviarComando(comando: String): String {
        delay(300)

        return when (comando.uppercase()) {
            "UNLOCK" -> {
                estado = "DESBLOQUEADO"
                "OK:DESBLOQUEADO"
            }
            "LOCK" -> {
                estado = "BLOQUEADO"
                "OK:BLOQUEADO"
            }
            "STATUS" -> "STATUS:$estado"
            else -> "ERROR:COMANDO_DESCONOCIDO"
        }
    }

    // Bloquear manualmente
    fun bloquear() {
        estado = "BLOQUEADO"
    }
}