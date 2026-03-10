package cat.copernic.aguamap1.aplication.fountain.comments

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.comment.Comment
import cat.copernic.aguamap1.domain.model.fountain.Fountain
import cat.copernic.aguamap1.domain.repository.auth.AuthRepository
import cat.copernic.aguamap1.domain.repository.fountain.FountainRepository
import cat.copernic.aguamap1.domain.usecase.comment.AddCommentUseCase
import cat.copernic.aguamap1.domain.usecase.comment.CensorCommentUseCase
import cat.copernic.aguamap1.domain.usecase.comment.DeleteCommentUseCase
import cat.copernic.aguamap1.domain.usecase.comment.UpdateCommentUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.GetFountainsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de orquestar la lógica de interacción social (comentarios y valoraciones).
 * Gestiona el ciclo de vida de la observación en tiempo real, la moderación de contenido
 * y las operaciones CRUD sobre los comentarios de las fuentes.
 */
@HiltViewModel
class FountainCommentsViewModel @Inject constructor(
    private val getFountainsUseCase: GetFountainsUseCase,
    private val authRepository: AuthRepository,
    private val repository: FountainRepository,
    private val addCommentUseCase: AddCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val censorCommentUseCase: CensorCommentUseCase,
    private val updateCommentUseCase: UpdateCommentUseCase,
    application: Application
) : AndroidViewModel(application) {

    /**
     * Accede a recursos de cadenas de texto del sistema para internacionalización de errores.
     */
    private fun getString(resId: Int): String = getApplication<Application>().getString(resId)

    // --- ESTADO DE LA INTERFAZ ---
    var comments by mutableStateOf<List<Comment>>(emptyList()); private set
    var reportSuccess by mutableStateOf(false); private set
    var errorMessage by mutableStateOf<String?>(null); private set

    private var commentsJob: Job? = null

    /**
     * Inicia la observación reactiva de comentarios para una fuente específica.
     * Cancela suscripciones previas para evitar fugas de memoria y ordena por fecha descendente.
     */
    fun observeComments(fountainId: String) {
        commentsJob?.cancel()
        commentsJob = viewModelScope.launch {
            getFountainsUseCase.fetchComments(fountainId).collect { result ->
                result.onSuccess { list ->
                    comments = list.sortedByDescending { it.timestamp }
                }.onFailure {
                    errorMessage = getString(R.string.error_sync_comments)
                }
            }
        }
    }

    /**
     * Registra un nuevo comentario asociándolo a la identidad del usuario actual.
     */
    fun addComment(fountain: Fountain, rating: Int, text: String) {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserUid() ?: return@launch
            val uName = authRepository.getCurrentUserName() ?: getString(R.string.default_user_name)

            val newComment = Comment(
                userId = uid,
                userName = uName,
                rating = rating,
                comment = text,
                timestamp = System.currentTimeMillis()
            )

            // Pasamos fountain.id en lugar del objeto completo para la transacción
            addCommentUseCase(fountain.id, newComment).onFailure {
                errorMessage = getString(R.string.error_add_comment)
            }
        }
    }

    // Dentro de FountainCommentsViewModel
    var isOperating by mutableStateOf(false); private set

    fun editComment(fountain: Fountain, oldComment: Comment, newRating: Int, newText: String) {
        viewModelScope.launch {
            isOperating = true
            updateCommentUseCase(
                fountainId = fountain.id,
                oldComment = oldComment,
                newRating = newRating,
                newText = newText
            ).onSuccess {
                isOperating = false
                // Aquí podrías cerrar un diálogo o mostrar un Toast
            }.onFailure {
                isOperating = false
                errorMessage = getString(R.string.error_edit_generic)
            }
        }
    }

    /**
     * Elimina de forma permanente un comentario de la base de datos.
     */
    fun deleteComment(fountain: Fountain, comment: Comment) {
        viewModelScope.launch {
            // Solo necesitamos los IDs para que el Repo haga su magia atómica
            deleteCommentUseCase(fountain.id, comment.id).onFailure {
                errorMessage = getString(R.string.error_delete_generic)
            }
        }
    }

    /**
     * Envía una notificación de reporte por contenido inapropiado a los administradores.
     */
    fun onReportComment(fountainId: String, commentId: String) {
        if (fountainId.isEmpty() || commentId.isEmpty()) return
        viewModelScope.launch {
            repository.reportComment(fountainId, commentId, "Reporte de usuario")
                .onSuccess { reportSuccess = true }
                .onFailure { errorMessage = getString(R.string.error_report_comment) }
        }
    }

    /**
     * Aplica restricciones de visibilidad (censura) sobre un comentario, actualizando el estado local.
     */
    fun censorComment(fountainId: String, commentId: String) {
        viewModelScope.launch {
            censorCommentUseCase(fountainId, commentId)
                .onSuccess {
                    comments = comments.map {
                        if (it.id == commentId) it.copy(censored = true) else it
                    }
                }
                .onFailure { errorMessage = getString(R.string.error_censor_comment) }
        }
    }

    /**
     * Finaliza la observación de datos y limpia la lista de comentarios.
     */
    fun stopObserving() {
        commentsJob?.cancel(); comments = emptyList()
    }

    /**
     * Reinicia el estado de confirmación de reporte.
     */
    fun clearReportSuccess() {
        reportSuccess = false
    }

    /**
     * Elimina mensajes de error pendientes en la UI.
     */
    fun clearError() {
        errorMessage = null
    }
}