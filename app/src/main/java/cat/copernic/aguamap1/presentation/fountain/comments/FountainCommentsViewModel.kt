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
    var reportSuccess by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var commentsJob: Job? = null

    fun observeComments(fountainId: String) {
        commentsJob?.cancel()
        commentsJob = viewModelScope.launch {
            // fetchComments debe devolver un Flow directamente desde Firestore (.snapshots())
            getFountainsUseCase.fetchComments(fountainId).collect { result ->
                result.onSuccess { list ->
                    // Actualizamos la lista con lo que diga la NUBE
                    comments = list.sortedByDescending { it.timestamp }
                }.onFailure {
                    errorMessage = "Error al sincronizar comentarios"
                }
            }
        }
    }

    fun stopObserving() {
        commentsJob?.cancel()
        comments = emptyList()
    }

    fun addComment(fountain: Fountain, rating: Int, text: String) {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserUid() ?: return@launch
            val uName = authRepository.getCurrentUserName() ?: "Usuario"

            val newComment = Comment(
                userId = uid,
                userName = uName,
                rating = rating,
                comment = text,
                timestamp = System.currentTimeMillis()
            )

            addCommentUseCase(fountain, newComment).onFailure {
                errorMessage = "Error al añadir comentario"
            }
        }
    }

    fun editComment(fountain: Fountain, oldComment: Comment, newRating: Int, newText: String) {
        viewModelScope.launch {
            updateCommentUseCase(fountain, oldComment, newRating, newText).onFailure {
                errorMessage = "Error al editar"
            }
        }
    }

    fun deleteComment(fountain: Fountain, comment: Comment) {
        viewModelScope.launch {
            deleteCommentUseCase(fountain, comment).onFailure {
                errorMessage = "Error al eliminar"
            }
        }
    }

    fun onReportComment(fountainId: String, commentId: String) {
        if (fountainId.isEmpty() || commentId.isEmpty()) return
        viewModelScope.launch {
            repository.reportComment(fountainId, commentId, "Reporte de usuario")
                .onSuccess { reportSuccess = true }
                .onFailure { errorMessage = "Error al reportar" }
        }
    }

    fun clearReportSuccess() {
        reportSuccess = false
    }

    /**
     * CENSURAR COMENTARIO (ADMIN)
     * Asegúrate de que el UseCase acceda a:
     * fountains/{fountainId}/comments/{commentId} -> update("isCensored", true)
     */
    fun censorComment(fountainId: String, commentId: String) {
        viewModelScope.launch {
            censorCommentUseCase(fountainId, commentId)
                .onSuccess {
                    // Actualización local inmediata para feedback visual
                    comments = comments.map {
                        if (it.id == commentId) it.copy(censored = true) else it
                    }
                }
                .onFailure {
                    errorMessage = "Error al censurar: ${it.message}"
                }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}