package com.example.cerradura.iot

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

object WifiServer {

    private const val PUERTO = 8080
    private var serverSocket: ServerSocket? = null
    private var activo = false

    private var ipConectada: String? = null
    private var ultimoComando: String = ""
    private var ultimoComandoTime: Long = 0

    var onComandoRecibido: ((String) -> Unit)? = null
    var onClienteConectado: ((String) -> Unit)? = null

    suspend fun iniciar(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            serverSocket = ServerSocket(PUERTO)
            activo = true
            println("Servidor iniciado en puerto $PUERTO")

            while (activo) {
                val cliente = serverSocket?.accept() ?: break
                val ipCliente = cliente.inetAddress.hostAddress ?: "desconocido"

                if (ipCliente != ipConectada) {
                    ipConectada = ipCliente
                    onClienteConectado?.invoke(ipCliente)
                }

                Thread {
                    manejarCliente(cliente, ipCliente)
                }.start()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun manejarCliente(cliente: Socket, ipCliente: String) {
        try {
            val entrada = BufferedReader(InputStreamReader(cliente.getInputStream()))
            val salida = PrintWriter(cliente.getOutputStream(), true)

            var linea: String?
            while (entrada.readLine().also { linea = it } != null) {
                val mensajeCifrado = linea!!.trim()
                println("Mensaje cifrado recibido de $ipCliente: $mensajeCifrado")

                // ===== 1. DESCIFRAR =====
                val mensajeDescifrado = Cifrado.descifrar(mensajeCifrado)
                println("Mensaje descifrado: $mensajeDescifrado")

                // ===== 2. PARSEAR TOKEN + COMANDO =====
                val partes = Seguridad.parsearMensaje(mensajeDescifrado)

                val respuesta: String = if (partes == null) {
                    "ERROR:FORMATO_INVALIDO"
                } else {
                    val (token, comando) = partes

                    // ===== 3. VALIDAR TOKEN =====
                    if (!Seguridad.tokenValido(token)) {
                        "ERROR:TOKEN_INVALIDO"
                    }
                    // ===== 4. VALIDAR COMANDO =====
                    else if (!Seguridad.comandoValido(comando)) {
                        "ERROR:COMANDO_DESCONOCIDO"
                    }
                    // ===== 5. EJECUTAR COMANDO =====
                    else {
                        when (comando.uppercase()) {
                            "UNLOCK" -> {
                                ArduinoSimulator.estado = "DESBLOQUEADO"
                                "OK:DESBLOQUEADO"
                            }
                            "LOCK" -> {
                                ArduinoSimulator.estado = "BLOQUEADO"
                                "OK:BLOQUEADO"
                            }
                            "STATUS" -> "STATUS:${ArduinoSimulator.estado}"
                            "INFO" -> "INFO:cerradura-iot-v1.0"
                            "AUTH" -> "OK:AUTENTICADO"
                            else -> "ERROR:COMANDO_DESCONOCIDO"
                        }
                    }
                }

                // ===== 6. REGISTRAR EN EL LOG (sin repetir) =====
                if (!comando_es_status_o_repetido(respuesta)) {
                    onComandoRecibido?.invoke(
                        "$ipCliente → ${if (partes != null) partes.second else "?"} → $respuesta"
                    )
                }

                // ===== 7. ENVIAR RESPUESTA CIFRADA =====
                val respuestaCifrada = Cifrado.cifrar(respuesta)
                salida.println(respuestaCifrada)
            }
            cliente.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Devuelve true si la respuesta corresponde a un STATUS o a un comando repetido.
     */
    private fun comando_es_status_o_repetido(respuesta: String): Boolean {
        // No registrar STATUS
        if (respuesta.startsWith("STATUS:") || respuesta.startsWith("OK:AUTENTICADO")) {
            return true
        }
        // No registrar repetidos en menos de 1 segundo
        val ahora = System.currentTimeMillis()
        val esRepetido = (respuesta == ultimoComando) &&
                (ahora - ultimoComandoTime < 1000)
        return if (esRepetido) {
            true
        } else {
            ultimoComando = respuesta
            ultimoComandoTime = ahora
            false
        }
    }

    fun detener() {
        activo = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        serverSocket = null
        ipConectada = null
        ultimoComando = ""
        ultimoComandoTime = 0
    }

    fun estaActivo(): Boolean = activo
}