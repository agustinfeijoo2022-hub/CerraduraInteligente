package com.example.cerradura.iot

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

object WifiServer {

    private const val TAG = "WifiServer"
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
            Log.d(TAG, "Servidor iniciado en puerto $PUERTO")

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
                Log.d(TAG, "Mensaje cifrado recibido de $ipCliente")

                val mensajeDescifrado = try {
                    Cifrado.descifrar(mensajeCifrado)
                } catch (e: SecurityException) {
                    Log.e(TAG, "Mensaje rechazado de $ipCliente: ${e.message}")
                    val respuestaError = "ERROR:DESCIFRADO_FALLIDO"
                    val respuestaErrorCifrada = try {
                        Cifrado.cifrar(respuestaError)
                    } catch (ex: SecurityException) {
                        null
                    }
                    if (respuestaErrorCifrada != null) {
                        salida.println(respuestaErrorCifrada)
                    }
                    continue
                }

                val partes = Seguridad.parsearMensaje(mensajeDescifrado)

                val respuesta: String = if (partes == null) {
                    "ERROR:FORMATO_INVALIDO"
                } else {
                    val (token, comando) = partes

                    if (!Seguridad.tokenValido(token)) {
                        "ERROR:TOKEN_INVALIDO"
                    } else if (!Seguridad.comandoValido(comando)) {
                        "ERROR:COMANDO_DESCONOCIDO"
                    } else {
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

                if (!comando_es_status_o_repetido(respuesta)) {
                    onComandoRecibido?.invoke(
                        "$ipCliente → ${if (partes != null) partes.second else "?"} → $respuesta"
                    )
                }

                val respuestaCifrada = try {
                    Cifrado.cifrar(respuesta)
                } catch (e: SecurityException) {
                    Log.e(TAG, "No se pudo cifrar la respuesta: ${e.message}")
                    continue
                }
                salida.println(respuestaCifrada)
            }
            cliente.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun comando_es_status_o_repetido(respuesta: String): Boolean {
        if (respuesta.startsWith("STATUS:") || respuesta.startsWith("OK:AUTENTICADO")) {
            return true
        }
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