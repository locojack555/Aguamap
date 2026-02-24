package cat.copernic.aguamap1.data.cloudinary

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class CloudinaryService @Inject constructor() {

    private val uploadPreset = "aguamap_preset" // Lo crearás en la web

    /**
     * Sube una imagen a Cloudinary usando corrutinas
     */
    suspend fun uploadImage(uri: Uri): Result<String> = suspendCancellableCoroutine { continuation ->
        val requestId = MediaManager.get().upload(uri)
            .unsigned(uploadPreset)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    // Upload started
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    // Progress
                }

                override fun onSuccess(requestId: String, resultData: MutableMap<*, *>?) {
                    val imageUrl = resultData?.get("secure_url") as? String
                    if (imageUrl != null) {
                        continuation.resume(Result.success(imageUrl))
                    } else {
                        continuation.resumeWithException(Exception("No se pudo obtener la URL de la imagen"))
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    continuation.resumeWithException(Exception(error.description))
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    // Reintentar
                }
            })
            .dispatch()

        continuation.invokeOnCancellation {
            MediaManager.get().cancelRequest(requestId)
        }
    }

    /**
     * Flow para seguir el progreso de la subida
     */
    fun uploadImageWithProgress(uri: Uri): Flow<UploadProgress> = callbackFlow {
        val requestId = MediaManager.get().upload(uri)
            .unsigned(uploadPreset)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    trySend(UploadProgress.Started)
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    val progress = (bytes * 100f / totalBytes).toInt()
                    trySend(UploadProgress.InProgress(progress))
                }

                override fun onSuccess(requestId: String, resultData: MutableMap<*, *>?) {
                    val imageUrl = resultData?.get("secure_url") as? String
                    if (imageUrl != null) {
                        trySend(UploadProgress.Success(imageUrl))
                    } else {
                        trySend(UploadProgress.Error("No se pudo obtener la URL"))
                    }
                    close()
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    trySend(UploadProgress.Error(error.description))
                    close()
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    trySend(UploadProgress.Reschedule)
                }
            })
            .dispatch()

        awaitClose {
            MediaManager.get().cancelRequest(requestId)
        }
    }
}

sealed class UploadProgress {
    object Started : UploadProgress()
    data class InProgress(val percentage: Int) : UploadProgress()
    data class Success(val imageUrl: String) : UploadProgress()
    data class Error(val message: String) : UploadProgress()
    object Reschedule : UploadProgress()
}