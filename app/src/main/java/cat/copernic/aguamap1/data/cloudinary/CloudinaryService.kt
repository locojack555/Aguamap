package cat.copernic.aguamap1.data.cloudinary

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.preprocess.BitmapEncoder
import com.cloudinary.android.preprocess.ImagePreprocessChain
import com.cloudinary.android.preprocess.Limit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Clase de servicio encargada de gestionar la subida de imágenes a la plataforma Cloudinary.
 * Está marcada como @Singleton para que exista una única instancia en toda la aplicación
 * y utiliza inyección de dependencias con Hilt.
 */
@Singleton
class CloudinaryService @Inject constructor(
    @ApplicationContext private val context: Context // Inyectamos el contexto aquí para las operaciones de MediaManager
) {

    // Identificador del preset de subida configurado en el panel de Cloudinary para subidas no firmadas
    private val uploadPreset = "aguamap_preset"

    /**
     * Define una cadena de procesamiento previo para las imágenes antes de ser enviadas.
     * Incluye limitación de dimensiones (max 1200px) y compresión en formato JPEG al 80%.
     */
    private val imagePreprocessingChain = ImagePreprocessChain()
        .addStep(Limit(1200, 1200))
        .saveWith(BitmapEncoder(BitmapEncoder.Format.JPEG, 80))

    /**
     * Sube una imagen de forma suspendida (Coroutine).
     * @param uri La ubicación local del archivo de imagen.
     * @return Un Result que contiene la URL segura de la imagen si tiene éxito, o una excepción si falla.
     */
    suspend fun uploadImage(uri: Uri): Result<String> =
        suspendCancellableCoroutine { continuation ->
            // Inicia la petición de subida mediante el MediaManager de Cloudinary
            val requestId = MediaManager.get().upload(uri)
                .unsigned(uploadPreset)
                .preprocess(imagePreprocessingChain)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                    // Se ejecuta cuando la imagen se ha subido correctamente
                    override fun onSuccess(requestId: String, resultData: MutableMap<*, *>?) {
                        val imageUrl = resultData?.get("secure_url") as? String
                        if (imageUrl != null) {
                            // Reanuda la corrutina devolviendo la URL de la imagen
                            continuation.resume(Result.success(imageUrl))
                        } else {
                            continuation.resumeWithException(Exception("No se pudo obtener la URL"))
                        }
                    }

                    // Se ejecuta si ocurre un error durante el proceso de subida
                    override fun onError(requestId: String, error: ErrorInfo) {
                        continuation.resumeWithException(Exception(error.description))
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                })
                .dispatch(context) // <--- PASAMOS EL CONTEXTO AQUÍ

            // Si la corrutina se cancela, se solicita al MediaManager cancelar la subida activa
            continuation.invokeOnCancellation {
                MediaManager.get().cancelRequest(requestId)
            }
        }

    /**
     * Sube una imagen y emite actualizaciones de estado y progreso mediante un Flow.
     * @param uri La ubicación local del archivo de imagen.
     * @return Un flujo (Flow) de estados del tipo UploadProgress.
     */
    fun uploadImageWithProgress(uri: Uri): Flow<UploadProgress> = callbackFlow {
        // Inicia la petición de subida configurando los callbacks para el flujo
        val requestId = MediaManager.get().upload(uri)
            .unsigned(uploadPreset)
            .preprocess(imagePreprocessingChain)
            .callback(object : UploadCallback {
                // Notifica que la subida ha comenzado
                override fun onStart(requestId: String) {
                    trySend(UploadProgress.Started)
                }

                // Calcula el porcentaje de subida y lo emite al flujo
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    val progress = (bytes * 100f / totalBytes).toInt()
                    trySend(UploadProgress.InProgress(progress))
                }

                // Emite el éxito con la URL obtenida y cierra el canal del flujo
                override fun onSuccess(requestId: String, resultData: MutableMap<*, *>?) {
                    val imageUrl = resultData?.get("secure_url") as? String
                    if (imageUrl != null) {
                        trySend(UploadProgress.Success(imageUrl))
                    } else {
                        trySend(UploadProgress.Error("No se pudo obtener la URL"))
                    }
                    close()
                }

                // Emite el error ocurrido y cierra el canal del flujo
                override fun onError(requestId: String, error: ErrorInfo) {
                    trySend(UploadProgress.Error(error.description))
                    close()
                }

                // Notifica si la subida ha sido reprogramada por el sistema
                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    trySend(UploadProgress.Reschedule)
                }
            })
            .dispatch(context) // <--- Y AQUÍ TAMBIÉN

        // Se asegura de cancelar la petición en Cloudinary si el Flow deja de ser recolectado
        awaitClose {
            MediaManager.get().cancelRequest(requestId)
        }
    }
}

/**
 * Clase sellada (Sealed Class) que representa los diferentes estados posibles
 * durante el proceso de subida de una imagen a Cloudinary.
 */
sealed class UploadProgress {
    // Estado inicial al comenzar la subida
    object Started : UploadProgress()

    // Estado que contiene el porcentaje actual de progreso
    data class InProgress(val percentage: Int) : UploadProgress()

    // Estado final exitoso que contiene la URL pública de la imagen
    data class Success(val imageUrl: String) : UploadProgress()

    // Estado de error que contiene el mensaje descriptivo del fallo
    data class Error(val message: String) : UploadProgress()

    // Estado que indica que la subida se ha pospuesto (ej. por pérdida de conexión)
    object Reschedule : UploadProgress()
}