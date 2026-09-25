package com.example.cerradura.iot

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Cifrado AES-128 para los comandos (ISO 27400).
 * Protege la información sensible en tránsito por la red WiFi.
 */
object Cifrado {

    // Clave de 16 caracteres (AES-128)
    private const val CLAVE = "CerraIoT2026Key!"

    /**
     * Cifra un texto plano y lo devuelve en Base64.
     */
    fun cifrar(textoPlano: String): String {
        return try {
            val key = SecretKeySpec(CLAVE.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val bytesCifrados = cipher.doFinal(textoPlano.toByteArray())
            Base64.encodeToString(bytesCifrados, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            textoPlano
        }
    }

    /**
     * Descifra un texto cifrado en Base64.
     */
    fun descifrar(textoCifrado: String): String {
        return try {
            val key = SecretKeySpec(CLAVE.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, key)
            val bytesDescifrados = cipher.doFinal(Base64.decode(textoCifrado, Base64.NO_WRAP))
            String(bytesDescifrados)
        } catch (e: Exception) {
            e.printStackTrace()
            textoCifrado
        }
    }
}