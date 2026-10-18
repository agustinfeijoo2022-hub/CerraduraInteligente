package com.example.cerradura.iot

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

object WifiClient {

    private const val TAG = "WifiClient"
    private const val PUERTO = 8080
    private const val TIMEOUT_MS = 5000

    var ipServidor: String = ""
    var conectado: Boolean = false
        private set

    suspend fun enviarComando(comando: String): String = withContext(Dispatchers.IO) {
        var socket: Socket? = null
        return@withContext try {
            socket = Socket()
            socket.connect(InetSocketAddress(ipServidor, PUERTO), TIMEOUT_MS)

            val salida = PrintWriter(socket.getOutputStream(), true)
            val entrada = BufferedReader(InputStreamReader(socket.getInputStream()))

            // ===== 1. CONSTRUIR MENSAJE SEGURO (TOKEN:COMANDO) =====
            val mensajeSeguro = Seguridad.construirMensaje(comando.uppercase())
            Log.d(TAG, "Enviando: $mensajeSeguro")

            // ===== 2. CIFRAR (puede fallar → NO se envía nada) =====
            val mensajeCifrado = try {
                Cifrado.cifrar(mensajeSeguro)
            } catch (e: SecurityException) {
                Log.e(TAG, "No se pudo cifrar el mensaje: ${e.message}")
                conectado = false
                return@withContext "ERROR:CIFRADO_FALLIDO"
            }
            salida.println(mensajeCifrado)

            // ===== 3. RECIBIR RESPUESTA CIFRADA =====
            val respuestaCifrada = entrada.readLine() ?: "ERROR:SIN_RESPUESTA"

            // ===== 4. DESCIFRAR RESPUESTA (puede fallar → respuesta inválida) =====
            val respuesta = try {
                Cifrado.descifrar(respuestaCifrada)
            } catch (e: SecurityException) {
                Log.e(TAG, "Respuesta rechazada: ${e.message}")
                conectado = false
                return@withContext "ERROR:RESPUESTA_INVALIDA"
            }

            conectado = true
            respuesta
        } catch (e: Exception) {
            conectado = false
            Log.e(TAG, "Fallo de conexión: ${e.message}")
            "ERROR:${e.message ?: "FALLO_CONEXION"}"
        } finally {
            try {
                socket?.close()
            } catch (e: Exception) {
                // ignorar
            }
        }
    }

    suspend fun probarConexion(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val socket = Socket()
            socket.connect(InetSocketAddress(ipServidor, PUERTO), TIMEOUT_MS)
            socket.close()
            conectado = true
            true
        } catch (e: Exception) {
            conectado = false
            false
        }
    }
}