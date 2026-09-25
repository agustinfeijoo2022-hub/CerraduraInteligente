package com.example.cerradura.iot

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

object WifiClient {

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
            println("Enviando: $mensajeSeguro")

            // ===== 2. CIFRAR =====
            val mensajeCifrado = Cifrado.cifrar(mensajeSeguro)
            salida.println(mensajeCifrado)

            // ===== 3. RECIBIR RESPUESTA CIFRADA =====
            val respuestaCifrada = entrada.readLine() ?: "ERROR:SIN_RESPUESTA"

            // ===== 4. DESCIFRAR RESPUESTA =====
            val respuesta = Cifrado.descifrar(respuestaCifrada)

            conectado = true
            respuesta
        } catch (e: Exception) {
            conectado = false
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