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
    private val repository: FountainRepository, // AÑADIDO: Necesario para reportar
    private val addCommentUseCase: AddCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val censorCommentUseCase: CensorCommentUseCase,
    private val updateCommentUseCase: UpdateCommentUseCase
) : ViewModel() {

    var comments by mutableStateOf<List<Comment>>(emptyList())
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var commentsJob: Job? = null

    fun observeComments(fountainId: String) {
        commentsJob?.cancel()
        commentsJob = viewModelScope.launch {
            getFountainsUseCase.fetchComments(fountainId).collect { result ->
                result.onSuccess { list ->
                    comments = list
                }.onFailure {
                    errorMessage = "Error al cargar comentarios"
                }
            }
        }
    }

    fun stopObserving() {
        commentsJob?.cancel()
        comments = emptyList()
    }

    // --- ACCIONES ---

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

            addCommentUseCase(fountain, newComment).onFailure { error ->
                errorMessage = "Error al añadir comentario"
            }
        }
    }

    fun editComment(fountain: Fountain, oldComment: Comment, newRating: Int, newText: String) {
        viewModelScope.launch {
            updateCommentUseCase(fountain, oldComment, newRating, newText).onFailure { error ->
                errorMessage = "Error al editar"
            }
        }
    }

    fun deleteComment(fountain: Fountain, comment: Comment) {
        viewModelScope.launch {
            deleteCommentUseCase(fountain, comment).onFailure { error ->
                errorMessage = "Error al borrar"
            }
        }
    }

    fun onReportComment(fountainId: String, commentId: String) {
        viewModelScope.launch {
            repository.reportComment(fountainId, commentId, "")
                .onFailure { errorMessage = "Error al enviar el reporte" }
        }
    }

    fun censorComment(fountainId: String, commentId: String) {
        viewModelScope.launch {
            censorCommentUseCase(fountainId, commentId).onFailure {
                errorMessage = "Error al censurar"
            }
        }
    }
}