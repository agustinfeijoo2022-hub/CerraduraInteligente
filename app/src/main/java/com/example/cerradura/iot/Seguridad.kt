package com.example.cerradura.iot

/**
 * Seguridad ISO 27400:
 * - Token de sesión
 * - Validación de comandos
 */
object Seguridad {

    // Token único de la app (en producción sería dinámico)
    const val TOKEN_VALIDO = "CERRA-2026-IOT-ABC123"

    // Comandos permitidos
    private val COMANDOS_VALIDOS = setOf(
        "UNLOCK", "LOCK", "STATUS", "INFO", "AUTH"
    )

    /**
     * Verifica que el token sea válido.
     */
    fun tokenValido(token: String): Boolean {
        return token == TOKEN_VALIDO
    }

    /**
     * Verifica que el comando esté en la lista de permitidos.
     */
    fun comandoValido(comando: String): Boolean {
        return comando.uppercase() in COMANDOS_VALIDOS
    }

    /**
     * Estructura de un mensaje seguro:
     * "TOKEN:COMANDO"
     * Ejemplo: "CERRA-2026-IOT-ABC123:UNLOCK"
     */
    fun construirMensaje(comando: String): String {
        return "$TOKEN_VALIDO:$comando"
    }

    /**
     * Separa el mensaje en token y comando.
     * Devuelve Pair(token, comando) o null si el formato es incorrecto.
     */
    fun parsearMensaje(mensaje: String): Pair<String, String>? {
        val partes = mensaje.split(":", limit = 2)
        if (partes.size != 2) return null
        return Pair(partes[0], partes[1])
    }
}