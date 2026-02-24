package cat.copernic.aguamap1.presentation.fountain.comments

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.repository.AuthRepository
import cat.copernic.aguamap1.domain.repository.FountainRepository
import cat.copernic.aguamap1.domain.usecase.comments.AddCommentUseCase
import cat.copernic.aguamap1.domain.usecase.comments.CensorCommentUseCase
import cat.copernic.aguamap1.domain.usecase.comments.DeleteCommentUseCase
import cat.copernic.aguamap1.domain.usecase.comments.UpdateCommentUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.GetFountainsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FountainCommentsViewModel @Inject constructor(
    private val getFountainsUseCase: GetFountainsUseCase,
    private val authRepository: AuthRepository,
    private val repository: FountainRepository,
    private val addCommentUseCase: AddCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val censorCommentUseCase: CensorCommentUseCase,
    private val updateCommentUseCase: UpdateCommentUseCase
) : ViewModel() {

    var comments by mutableStateOf<List<Comment>>(emptyList())
        private set

    // ErrorMessage ahora podría ser un ID de recurso o un objeto de estado para evitar strings hardcoded
    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var commentsJob: Job? = null

    /**
     * Observa los comentarios de una fuente específica en tiempo real.
     */
    fun observeComments(fountainId: String) {
        commentsJob?.cancel()
        commentsJob = viewModelScope.launch {
            getFountainsUseCase.fetchComments(fountainId).collect { result ->
                result.onSuccess { list ->
                    comments = list
                }.onFailure {
                    // Aquí lo ideal es usar una Sealed Class de errores o un ID de StringResource
                    errorMessage = "error_loading_comments"
                }
            }
        }
    }

    /**
     * Detiene la observación y limpia la lista de comentarios.
     */
    fun stopObserving() {
        commentsJob?.cancel()
        comments = emptyList()
    }

    // --- ACCIONES DE COMENTARIOS ---

    /**
     * Añade un nuevo comentario a la fuente.
     */
    fun addComment(fountain: Fountain, rating: Int, text: String) {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserUid() ?: return@launch
            val uName = authRepository.getCurrentUserName() ?: ""

            val newComment = Comment(
                userId = uid,
                userName = uName,
                rating = rating,
                comment = text,
                timestamp = System.currentTimeMillis()
            )

            addCommentUseCase(fountain, newComment).onFailure {
                errorMessage = "error_adding_comment"
            }
        }
    }

    /**
     * Actualiza un comentario existente (Rating y Texto).
     */
    fun editComment(fountain: Fountain, oldComment: Comment, newRating: Int, newText: String) {
        viewModelScope.launch {
            updateCommentUseCase(fountain, oldComment, newRating, newText).onFailure {
                errorMessage = "error_editing_comment"
            }
        }
    }

    /**
     * Elimina permanentemente un comentario.
     */
    fun deleteComment(fountain: Fountain, comment: Comment) {
        viewModelScope.launch {
            deleteCommentUseCase(fountain, comment).onFailure {
                errorMessage = "error_deleting_comment"
            }
        }
    }

    /**
     * Reporta un comentario al administrador.
     * Implementa la lógica de reporte similar a la de las fuentes.
     */
    fun onReportComment(fountainId: String, commentId: String) {
        viewModelScope.launch {
            // El tercer parámetro (reason) se envía vacío por defecto o podrías pasar un string desde un diálogo
            repository.reportComment(fountainId, commentId, "")
                .onFailure { errorMessage = "error_reporting_comment" }
        }
    }

    /**
     * Censura un comentario (Acción exclusiva de Admin).
     */
    fun censorComment(fountainId: String, commentId: String) {
        viewModelScope.launch {
            censorCommentUseCase(fountainId, commentId).onFailure {
                errorMessage = "error_censoring_comment"
            }
        }
    }

    /**
     * Limpia el mensaje de error después de ser mostrado.
     */
    fun clearError() {
        errorMessage = null
    }
}