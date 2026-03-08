package cat.copernic.aguamap1.aplication.profile.moderation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.comment.Comment
import cat.copernic.aguamap1.domain.model.fountain.Fountain
import cat.copernic.aguamap1.domain.model.comment.ReportedComment
import cat.copernic.aguamap1.domain.repository.fountain.FountainRepository
import cat.copernic.aguamap1.domain.usecase.comment.CensorCommentUseCase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * ViewModel que gestiona la lógica de moderación de comentarios.
 * Interactúa con Firebase Firestore para recuperar reportes, enriquecer los datos
 * con los comentarios originales y ejecutar acciones de limpieza o censura.
 */
@HiltViewModel
class ModerationViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val fountainRepository: FountainRepository,
    private val censorCommentUseCase: CensorCommentUseCase
) : ViewModel() {

    private val _reportedComments = MutableStateFlow<List<ReportedComment>>(emptyList())
    val reportedComments: StateFlow<List<ReportedComment>> = _reportedComments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Manejo de recursos de strings para localización (R.string...)
    private val _errorResId = MutableStateFlow<Int?>(null)
    val errorResId: StateFlow<Int?> = _errorResId.asStateFlow()

    private val _successResId = MutableStateFlow<Int?>(null)
    val successResId: StateFlow<Int?> = _successResId.asStateFlow()

    // --- LÓGICA DE UBICACIÓN PARA NAVEGACIÓN ---
    private var _userLat: Double? = null
    private var _userLng: Double? = null

    /**
     * Establece la ubicación actual del usuario para que esté disponible
     * al navegar hacia el detalle de una fuente.
     */
    fun setLocation(lat: Double?, lng: Double?) {
        _userLat = lat
        _userLng = lng
    }

    init {
        loadReportedComments()
    }

    /**
     * Recupera la lista de reportes de la colección "reportsComments" y
     * realiza un "join" manual con la subcolección de comentarios de cada fuente.
     */
    fun loadReportedComments() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Obtener los documentos de reporte (Metadatos del reporte)
                val snapshot = db.collection("reportsComments")
                    .whereEqualTo("type", "COMMENT_REPORT")
                    .get()
                    .await()

                val items = snapshot.documents.mapNotNull { doc ->
                    val fountainId = doc.getString("fountainId") ?: return@mapNotNull null
                    val commentId = doc.getString("commentId") ?: return@mapNotNull null

                    ReportedComment(
                        reportId = doc.id,
                        fountainId = fountainId,
                        commentId = commentId,
                        reason = doc.getString("reason") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                }

                // 2. Enriquecer con los datos del comentario original (Contenido del mensaje)
                val enrichedItems = items.map { item ->
                    try {
                        val commentDoc = db.collection("fountains")
                            .document(item.fountainId)
                            .collection("comments")
                            .document(item.commentId)
                            .get()
                            .await()

                        val comment =
                            commentDoc.toObject(Comment::class.java)?.copy(id = commentDoc.id)
                        item.copy(comment = comment)
                    } catch (e: Exception) {
                        item // Si falla la carga del comentario, devolvemos el reporte sin info extra
                    }
                }

                _reportedComments.value = enrichedItems.sortedByDescending { it.timestamp }

            } catch (e: Exception) {
                _errorResId.value = R.string.error_loading_reports
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Elimina físicamente el comentario de la fuente y borra el reporte.
     */
    fun deleteComment(item: ReportedComment) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                fountainRepository.deleteComment(item.fountainId, item.commentId)
                deleteReportFromDb(item.reportId)
                _successResId.value = R.string.success_comment_deleted
                loadReportedComments()
            } catch (e: Exception) {
                _errorResId.value = R.string.error_deleting_comment
                _isLoading.value = false
            }
        }
    }

    /**
     * Ejecuta el UseCase para ocultar el contenido del comentario (censura) sin borrarlo.
     */
    fun censorComment(item: ReportedComment) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                censorCommentUseCase(item.fountainId, item.commentId)
                deleteReportFromDb(item.reportId)
                _successResId.value = R.string.success_comment_censored
                loadReportedComments()
            } catch (e: Exception) {
                _errorResId.value = R.string.error_censoring_comment
                _isLoading.value = false
            }
        }
    }

    /**
     * Desestima el reporte, marcando el comentario como seguro y borrando el ticket de reporte.
     */
    fun dismissReport(item: ReportedComment) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.collection("fountains")
                    .document(item.fountainId)
                    .collection("comments")
                    .document(item.commentId)
                    .update("isReported", false)
                    .await()

                deleteReportFromDb(item.reportId)
                _successResId.value = R.string.success_report_dismissed
                loadReportedComments()
            } catch (e: Exception) {
                _errorResId.value = R.string.error_dismissing_report
                _isLoading.value = false
            }
        }
    }

    /**
     * Función auxiliar para limpiar la cola de reportes tras una acción.
     */
    private suspend fun deleteReportFromDb(reportId: String) {
        db.collection("reportsComments")
            .document(reportId)
            .delete()
            .await()
    }

    /**
     * Obtiene una fuente por su ID para permitir la navegación al detalle global.
     * Se usa cuando el administrador pulsa en "Ver fuente" desde un comentario reportado.
     */
    fun getFountainById(fountainId: String, onResult: (Fountain?) -> Unit) {
        viewModelScope.launch {
            try {
                val document = db.collection("fountains")
                    .document(fountainId)
                    .get()
                    .await()

                val fountain = document.toObject(Fountain::class.java)?.copy(id = document.id)
                onResult(fountain)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    /**
     * Limpia los IDs de recursos para que el Snackbar no se vuelva a mostrar tras una recomposición.
     */
    fun clearMessages() {
        _errorResId.value = null
        _successResId.value = null
    }
}