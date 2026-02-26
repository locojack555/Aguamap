package cat.copernic.aguamap1.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.domain.model.ReportedComment
import cat.copernic.aguamap1.domain.repository.FountainRepository
import cat.copernic.aguamap1.domain.usecase.comments.CensorCommentUseCase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

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

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadReportedComments()
    }

    fun loadReportedComments() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = db.collection("reports_comments")
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

                // Enrich each report with its actual comment data
                val enrichedItems = items.map { item ->
                    try {
                        val commentDoc = db.collection("fountains")
                            .document(item.fountainId)
                            .collection("comments")
                            .document(item.commentId)
                            .get()
                            .await()

                        val comment = commentDoc.toObject(Comment::class.java)?.copy(id = commentDoc.id)
                        item.copy(comment = comment)
                    } catch (e: Exception) {
                        item
                    }
                }

                _reportedComments.value = enrichedItems.sortedByDescending { it.timestamp }

            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar los reportes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Elimina el comentario y borra el reporte
    fun deleteComment(item: ReportedComment) {
        viewModelScope.launch {
            try {
                fountainRepository.deleteComment(item.fountainId, item.commentId)
                deleteReport(item.reportId)
                _successMessage.value = "Comentario eliminado"
                loadReportedComments()
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar: ${e.message}"
            }
        }
    }

    // Censura el comentario y borra el reporte
    fun censorComment(item: ReportedComment) {
        viewModelScope.launch {
            try {
                censorCommentUseCase(item.fountainId, item.commentId)
                deleteReport(item.reportId)
                _successMessage.value = "Comentario censurado"
                loadReportedComments()
            } catch (e: Exception) {
                _errorMessage.value = "Error al censurar: ${e.message}"
            }
        }
    }

    // Solo borra el reporte, el comentario queda intacto
    fun dismissReport(item: ReportedComment) {
        viewModelScope.launch {
            try {
                // Quitar el flag isReported del comentario
                db.collection("fountains")
                    .document(item.fountainId)
                    .collection("comments")
                    .document(item.commentId)
                    .update("isReported", false)
                    .await()

                deleteReport(item.reportId)
                _successMessage.value = "Reporte descartado"
                loadReportedComments()
            } catch (e: Exception) {
                _errorMessage.value = "Error al descartar: ${e.message}"
            }
        }
    }

    private suspend fun deleteReport(reportId: String) {
        db.collection("reports_comments")
            .document(reportId)
            .delete()
            .await()
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}