package com.example.cerradura.iot

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gestor del historial de accesos en Firebase Firestore.
 * Guarda cada acción realizada en la app con datos del usuario.
 *
 * Colección: "historial_accesos"
 */
object HistorialDB {

    private const val TAG = "HistorialDB"
    private const val COLECCION = "historial_accesos"

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    /**
     * Registra una acción en el historial.
     *
     * @param accion     Qué se hizo: "LOGIN", "LOCK", "UNLOCK", "STATUS", "LOGOUT", "MODO_SELECCIONADO"
     * @param rol        "CLIENTE", "SERVIDOR" o "DESCONOCIDO"
     * @param resultado  "OK" o "ERROR"
     * @param detalle    Información adicional (IP, mensaje, etc.)
     */
    fun registrarAccion(
        accion: String,
        rol: String = "DESCONOCIDO",
        resultado: String = "OK",
        detalle: String = ""
    ) {
        val usuario = auth.currentUser
        if (usuario == null) {
            Log.w(TAG, "No hay usuario autenticado, no se registra la acción")
            return
        }

        val fecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val registro = hashMapOf(
            "uid" to usuario.uid,
            "email" to (usuario.email ?: "sin_email"),
            "accion" to accion,
            "rol" to rol,
            "resultado" to resultado,
            "detalle" to detalle,
            "fecha" to fecha,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection(COLECCION)
            .add(registro)
            .addOnSuccessListener { doc ->
                Log.d(TAG, "✅ Acción registrada: ${doc.id} - $accion por ${usuario.email}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al registrar acción: ${e.message}")
            }
    }

    /**
     * Obtiene el historial de accesos del usuario actual.
     * Los devuelve ordenados por timestamp descendente (más reciente primero).
     */
    fun obtenerHistorialUsuario(
        limite: Long = 50,
        onResultado: (List<Map<String, Any>>) -> Unit,
        onError: (String) -> Unit
    ) {
        val usuario = auth.currentUser
        if (usuario == null) {
            onError("No hay usuario autenticado")
            return
        }

        db.collection(COLECCION)
            .whereEqualTo("uid", usuario.uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limite)
            .get()
            .addOnSuccessListener { documentos ->
                val lista = documentos.map { it.data ?: emptyMap() }
                onResultado(lista)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error desconocido")
            }
    }

    /**
     * Obtiene TODO el historial (para el servidor/admin).
     * Útil para que el servidor vea quién accedió desde el cliente.
     */
    fun obtenerTodoElHistorial(
        limite: Long = 100,
        onResultado: (List<Map<String, Any>>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection(COLECCION)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limite)
            .get()
            .addOnSuccessListener { documentos ->
                val lista = documentos.map { it.data ?: emptyMap() }
                onResultado(lista)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error desconocido")
            }
    }
}