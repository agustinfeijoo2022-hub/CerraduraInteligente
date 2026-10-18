package com.example.cerradura.iot

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Gestor del PIN de acceso por usuario.
 * Guarda el PIN en Firestore asociado al UID del usuario.
 */
object PinDB {

    private const val TAG = "PinDB"
    private const val COLECCION = "usuarios_pin"

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun guardarPin(pin: String, onResultado: (Boolean) -> Unit) {
        val usuario = auth.currentUser
        if (usuario == null) {
            Log.e(TAG, "No hay usuario autenticado")
            onResultado(false)
            return
        }

        if (pin.length != 4 || !pin.all { it.isDigit() }) {
            Log.e(TAG, "El PIN debe tener exactamente 4 dígitos")
            onResultado(false)
            return
        }

        val datos = hashMapOf(
            "uid" to usuario.uid,
            "email" to (usuario.email ?: "sin_email"),
            "pinHash" to hashPin(pin),
            "creado" to System.currentTimeMillis()
        )

        db.collection(COLECCION)
            .document(usuario.uid)
            .set(datos)
            .addOnSuccessListener {
                Log.d(TAG, "✅ PIN guardado para ${usuario.email}")
                onResultado(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al guardar PIN: ${e.message}")
                onResultado(false)
            }
    }

    fun verificarPin(pin: String, onResultado: (Boolean) -> Unit) {
        val usuario = auth.currentUser
        if (usuario == null) {
            Log.e(TAG, "No hay usuario autenticado")
            onResultado(false)
            return
        }

        if (pin.length != 4 || !pin.all { it.isDigit() }) {
            onResultado(false)
            return
        }

        db.collection(COLECCION)
            .document(usuario.uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val pinGuardado = doc.getString("pinHash") ?: ""
                    val coincide = pinGuardado == hashPin(pin)
                    Log.d(TAG, "Verificación: ${if (coincide) "OK ✅" else "FALLO ❌"}")
                    onResultado(coincide)
                } else {
                    Log.w(TAG, "El usuario no tiene PIN registrado")
                    onResultado(false)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al verificar PIN: ${e.message}")
                onResultado(false)
            }
    }

    fun tienePinRegistrado(onResultado: (Boolean) -> Unit) {
        val usuario = auth.currentUser
        if (usuario == null) {
            onResultado(false)
            return
        }

        db.collection(COLECCION)
            .document(usuario.uid)
            .get()
            .addOnSuccessListener { doc ->
                onResultado(doc.exists())
            }
            .addOnFailureListener {
                onResultado(false)
            }
    }

    fun eliminarPin(onResultado: (Boolean) -> Unit) {
        val usuario = auth.currentUser
        if (usuario == null) {
            onResultado(false)
            return
        }

        db.collection(COLECCION)
            .document(usuario.uid)
            .delete()
            .addOnSuccessListener { onResultado(true) }
            .addOnFailureListener { onResultado(false) }
    }

    private fun hashPin(pin: String): String {
        val bytes = pin.toByteArray(Charsets.UTF_8)
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}