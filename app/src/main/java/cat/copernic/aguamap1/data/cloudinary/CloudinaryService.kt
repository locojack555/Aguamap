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

@Singleton
class CloudinaryService @Inject constructor(
    @ApplicationContext private val context: Context // Inyectamos el contexto aquí
) {

    private val uploadPreset = "aguamap_preset"

    private val imagePreprocessingChain = ImagePreprocessChain()
        .addStep(Limit(1200, 1200))
        .saveWith(BitmapEncoder(BitmapEncoder.Format.JPEG, 80))

    suspend fun uploadImage(uri: Uri): Result<String> =
        suspendCancellableCoroutine { continuation ->
            val requestId = MediaManager.get().upload(uri)
                .unsigned(uploadPreset)
                .preprocess(imagePreprocessingChain)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String, resultData: MutableMap<*, *>?) {
                        val imageUrl = resultData?.get("secure_url") as? String
                        if (imageUrl != null) {
                            continuation.resume(Result.success(imageUrl))
                        } else {
                            continuation.resumeWithException(Exception("No se pudo obtener la URL"))
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        continuation.resumeWithException(Exception(error.description))
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                })
                .dispatch(context) // <--- PASAMOS EL CONTEXTO AQUÍ

            continuation.invokeOnCancellation {
                MediaManager.get().cancelRequest(requestId)
            }
        }

    fun uploadImageWithProgress(uri: Uri): Flow<UploadProgress> = callbackFlow {
        val requestId = MediaManager.get().upload(uri)
            .unsigned(uploadPreset)
            .preprocess(imagePreprocessingChain)
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
            .dispatch(context) // <--- Y AQUÍ TAMBIÉN

        awaitClose {
            MediaManager.get().cancelRequest(requestId)
        }
    }
}

// ... (El resto del código de UploadProgress se mantiene igual)

sealed class UploadProgress {
    object Started : UploadProgress()
    data class InProgress(val percentage: Int) : UploadProgress()
    data class Success(val imageUrl: String) : UploadProgress()
    data class Error(val message: String) : UploadProgress()
    object Reschedule : UploadProgress()
}