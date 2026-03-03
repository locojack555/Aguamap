package cat.copernic.aguamap1.presentation.fountain.comments

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.repository.AuthRepository
import cat.copernic.aguamap1.domain.repository.FountainRepository
import cat.copernic.aguamap1.domain.usecase.comments.*
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
    private val updateCommentUseCase: UpdateCommentUseCase,
    application: Application
) : AndroidViewModel(application) {

    private fun getString(resId: Int): String = getApplication<Application>().getString(resId)

    var comments by mutableStateOf<List<Comment>>(emptyList()) ; private set
    var reportSuccess by mutableStateOf(false) ; private set
    var errorMessage by mutableStateOf<String?>(null) ; private set

    private var commentsJob: Job? = null

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

            addCommentUseCase(fountain, newComment).onFailure {
                errorMessage = getString(R.string.error_add_comment)
            }
        }
    }

    fun editComment(fountain: Fountain, oldComment: Comment, newRating: Int, newText: String) {
        viewModelScope.launch {
            updateCommentUseCase(fountain, oldComment, newRating, newText).onFailure {
                errorMessage = getString(R.string.error_edit_generic)
            }
        }
    }

    fun deleteComment(fountain: Fountain, comment: Comment) {
        viewModelScope.launch {
            deleteCommentUseCase(fountain, comment).onFailure {
                errorMessage = getString(R.string.error_delete_generic)
            }
        }
    }

    fun onReportComment(fountainId: String, commentId: String) {
        if (fountainId.isEmpty() || commentId.isEmpty()) return
        viewModelScope.launch {
            repository.reportComment(fountainId, commentId, "Reporte de usuario")
                .onSuccess { reportSuccess = true }
                .onFailure { errorMessage = getString(R.string.error_report_comment) }
        }
    }

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

    fun stopObserving() { commentsJob?.cancel(); comments = emptyList() }
    fun clearReportSuccess() { reportSuccess = false }
    fun clearError() { errorMessage = null }
}