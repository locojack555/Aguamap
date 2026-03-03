package cat.copernic.aguamap1.presentation.fountain.detailFountain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.*
import cat.copernic.aguamap1.domain.repository.AuthRepository
import cat.copernic.aguamap1.domain.repository.RealtimeStatusRepository
import cat.copernic.aguamap1.domain.usecase.auth.GetUserRoleUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.*
import cat.copernic.aguamap1.domain.usecase.report.SendReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DetailFountainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getUserRoleUseCase: GetUserRoleUseCase,
    private val deleteFountainUseCase: DeleteFountainUseCase,
    private val updateFountainUseCase: UpdateFountainUseCase,
    private val processFountainVoteUseCase: ProcessFountainVoteUseCase,
    private val sendReportUseCase: SendReportUseCase,
    private val realtimeStatusRepository: RealtimeStatusRepository,
    application: android.app.Application
) : androidx.lifecycle.AndroidViewModel(application) {

    private fun getString(resId: Int): String =
        getApplication<android.app.Application>().getString(resId)

    // --- ESTADOS ---
    var isAdmin by mutableStateOf(false); private set
    var currentUserId by mutableStateOf<String?>(null); private set
    var creatorName by mutableStateOf<String?>(null); private set
    var selectedFountain by mutableStateOf<Fountain?>(null); private set
    var errorMessage by mutableStateOf<String?>(null); private set
    var isActionLoading by mutableStateOf(false); private set

    private val _fountainIdFlow = MutableStateFlow<String?>(null)

    val isOperationalRealtime: StateFlow<Boolean> = _fountainIdFlow
        .flatMapLatest { id ->
            if (id != null) realtimeStatusRepository.getFountainStatus(id)
            else flowOf(true)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    init { loadUserData() }

    private fun loadUserData() {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserUid()
            currentUserId = uid
            uid?.let {
                val role = getUserRoleUseCase(it)
                isAdmin = (role == UserRole.ADMIN)
            }
        }
    }

    fun selectFountain(fountain: Fountain) {
        selectedFountain = fountain
        _fountainIdFlow.value = fountain.id
        fetchCreatorName(fountain.createdBy)
    }

    private fun fetchCreatorName(uid: String) {
        viewModelScope.launch {
            authRepository.getUserNameById(uid)
                .onSuccess { creatorName = it }
                .onFailure { creatorName = getString(R.string.default_user_name) }
        }
    }

    // --- ESTADO OPERACIONAL ---
    fun toggleOperationalStatus(onSuccess: () -> Unit) {
        val fountain = selectedFountain ?: return
        viewModelScope.launch {
            val newStatus = !isOperationalRealtime.value
            try {
                realtimeStatusRepository.updateFountainStatus(fountain.id, newStatus)
                updateFountainUseCase(fountain.id, mapOf("operational" to newStatus))
                selectedFountain = fountain.copy(operational = newStatus)
                onSuccess()
            } catch (e: Exception) {
                errorMessage = getString(R.string.error_edit_generic)
            }
        }
    }

    // --- VOTO POSITIVO ---
    fun confirmFountain(onSuccess: () -> Unit) {
        val userId = currentUserId ?: return
        val fountain = selectedFountain ?: return
        if (fountain.votedByPositive.contains(userId) || isActionLoading) return

        isActionLoading = true
        val previousFountain = selectedFountain

        val updatedVotes = fountain.positiveVotes + 1
        val updatedVoters = fountain.votedByPositive + userId
        val newStatus =
            if (updatedVotes >= 3) StateFountain.ACCEPTED else fountain.status

        selectedFountain = fountain.copy(
            positiveVotes = updatedVotes,
            votedByPositive = updatedVoters,
            status = newStatus
        )

        viewModelScope.launch {
            processFountainVoteUseCase.addPositiveVote(fountain, userId)
                .onSuccess {
                    updateFountainUseCase(
                        fountain.id,
                        mapOf(
                            "positiveVotes" to updatedVotes,
                            "votedByPositive" to updatedVoters,
                            "status" to newStatus.name
                        )
                    ).onSuccess {
                        isActionLoading = false
                        onSuccess()
                    }.onFailure {
                        selectedFountain = previousFountain
                        isActionLoading = false
                        errorMessage = getString(R.string.error_add_comment)
                    }
                }
                .onFailure {
                    selectedFountain = previousFountain
                    isActionLoading = false
                    errorMessage = getString(R.string.error_add_comment)
                }
        }
    }

    fun reportNonExistent(onSuccess: () -> Unit) {
        val userId = currentUserId ?: return
        val fountain = selectedFountain ?: return
        if (fountain.votedByNegative.contains(userId) || isActionLoading) return

        isActionLoading = true
        val previousFountain = selectedFountain

        val updatedVotes = fountain.negativeVotes + 1
        val updatedVoters = fountain.votedByNegative + userId

        // 🔥 Optimistic update inmediata
        selectedFountain = fountain.copy(
            negativeVotes = updatedVotes,
            votedByNegative = updatedVoters
        )

        viewModelScope.launch {
            processFountainVoteUseCase.addNegativeVote(fountain, userId)
                .onSuccess {
                    updateFountainUseCase(
                        fountain.id,
                        mapOf(
                            "negativeVotes" to updatedVotes,
                            "votedByNegative" to updatedVoters
                        )
                    )
                    isActionLoading = false
                    onSuccess()
                }
                .onFailure {
                    selectedFountain = previousFountain
                    isActionLoading = false
                    errorMessage = getString(R.string.error_report_comment)
                }
        }
    }

    fun confirmExistence(onSuccess: () -> Unit) {
        val userId = currentUserId ?: return
        val fountain = selectedFountain ?: return
        if (!fountain.votedByNegative.contains(userId) || isActionLoading) return

        isActionLoading = true
        val previousFountain = selectedFountain

        val updatedVotes = (fountain.negativeVotes - 1).coerceAtLeast(0)
        val updatedVotersNegative =
            fountain.votedByNegative.filter { it != userId }

        // 🔥 Optimistic update inmediata
        selectedFountain = fountain.copy(
            negativeVotes = updatedVotes,
            votedByNegative = updatedVotersNegative
        )

        viewModelScope.launch {
            processFountainVoteUseCase.confirmExistence(fountain, userId)
                .onSuccess {
                    updateFountainUseCase(
                        fountain.id,
                        mapOf(
                            "negativeVotes" to updatedVotes,
                            "votedByNegative" to updatedVotersNegative
                        )
                    )
                    isActionLoading = false
                    onSuccess()
                }
                .onFailure {
                    selectedFountain = previousFountain
                    isActionLoading = false
                    errorMessage = getString(R.string.error_edit_generic)
                }
        }
    }

    // --- OTROS REPORTES ---
    fun reportOtherIssue(description: String, onSuccess: () -> Unit) {
        val userId = currentUserId ?: return
        val fountain = selectedFountain ?: return
        viewModelScope.launch {
            val report = Report(fountain.id, fountain.name, userId, description)
            sendReportUseCase(report)
                .onSuccess { onSuccess() }
                .onFailure { errorMessage = getString(R.string.error_report_comment) }
        }
    }

    fun deleteFountain(onDeleted: () -> Unit) {
        val fountainId = selectedFountain?.id ?: return
        viewModelScope.launch {
            deleteFountainUseCase(fountainId)
                .onSuccess {
                    clearSelection()
                    onDeleted()
                }
                .onFailure {
                    errorMessage =
                        "${getString(R.string.error_delete_generic)}: ${it.message}"
                }
        }
    }

    // --- HELPERS ---
    fun canUserModify(fountain: Fountain?): Boolean {
        val f = fountain ?: return false
        val userId = currentUserId ?: return false
        if (isAdmin) return true
        return f.createdBy == userId && f.status == StateFountain.PENDING
    }

    fun getFormattedDate(date: Date): String =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)

    fun getFormattedCoordinates(lat: Double, lng: Double): String =
        String.format(Locale.US, "%.5f, %.5f", lat, lng)

    fun getDistanceText(distance: Double?): String {
        return distance?.let {
            if (it < 1000) "${it.toInt()} m"
            else String.format(Locale.US, "%.2f km", it / 1000.0)
        } ?: "---"
    }

    fun clearSelection() {
        selectedFountain = null
        _fountainIdFlow.value = null
    }

    fun clearError() { errorMessage = null }
}