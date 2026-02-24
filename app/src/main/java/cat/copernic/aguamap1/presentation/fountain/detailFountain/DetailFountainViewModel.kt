package cat.copernic.aguamap1.presentation.fountain.addFountain.detailFountain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.Report
import cat.copernic.aguamap1.domain.model.StateFountain
import cat.copernic.aguamap1.domain.model.UserRole
import cat.copernic.aguamap1.domain.repository.AuthRepository
import cat.copernic.aguamap1.domain.usecase.auth.GetUserRoleUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.DeleteFountainUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.ProcessFountainVoteUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.UpdateFountainUseCase
import cat.copernic.aguamap1.domain.usecase.report.SendReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DetailFountainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getUserRoleUseCase: GetUserRoleUseCase,
    private val deleteFountainUseCase: DeleteFountainUseCase,
    private val updateFountainUseCase: UpdateFountainUseCase,
    private val processFountainVoteUseCase: ProcessFountainVoteUseCase,
    private val sendReportUseCase: SendReportUseCase
) : ViewModel() {

    var isAdmin by mutableStateOf(false)
        private set

    var currentUserId by mutableStateOf<String?>(null)
        private set

    var selectedFountain by mutableStateOf<Fountain?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserUid()
            currentUserId = uid
            if (uid != null) {
                val role = getUserRoleUseCase(uid)
                isAdmin = (role == UserRole.ADMIN)
            }
        }
    }

    fun selectFountain(fountain: Fountain) {
        selectedFountain = fountain
    }

    fun clearSelection() {
        selectedFountain = null
        errorMessage = null
    }

    fun deleteFountain(onDeleted: () -> Unit) {
        val fountainId = selectedFountain?.id ?: return
        viewModelScope.launch {
            deleteFountainUseCase(fountainId)
                .onSuccess {
                    clearSelection()
                    onDeleted()
                }
                .onFailure { errorMessage = "Error al eliminar: ${it.message}" }
        }
    }

    fun confirmFountain(onSuccess: () -> Unit) {
        val userId = currentUserId ?: return
        val fountain = selectedFountain ?: return

        if (fountain.positiveVotes >= 3 || fountain.votedByPositive.contains(userId)) {
            errorMessage = "No puedes votar más en esta fuente"
            return
        }

        viewModelScope.launch {
            processFountainVoteUseCase.addPositiveVote(fountain, userId)
                .onSuccess {
                    val updatedVotes = fountain.positiveVotes + 1
                    val updatedVoters = fountain.votedByPositive + userId
                    val newStatus =
                        if (updatedVotes >= 3) StateFountain.ACCEPTED else fountain.status

                    selectedFountain = fountain.copy(
                        positiveVotes = updatedVotes,
                        votedByPositive = updatedVoters,
                        status = newStatus
                    )
                    onSuccess()
                }
                .onFailure { errorMessage = it.message ?: "Error al votar" }
        }
    }

    fun reportNonExistent(onSuccess: () -> Unit) {
        val userId = currentUserId ?: return
        val fountain = selectedFountain ?: return

        if (fountain.votedByNegative.contains(userId)) {
            errorMessage = "Ya has reportado que esta fuente no existe"
            return
        }

        viewModelScope.launch {
            processFountainVoteUseCase.addNegativeVote(fountain, userId)
                .onSuccess {
                    val updatedVotes = fountain.negativeVotes + 1
                    val updatedVotersNegative = fountain.votedByNegative + userId

                    if (updatedVotes >= 3) {
                        clearSelection()
                    } else {
                        selectedFountain = fountain.copy(
                            negativeVotes = updatedVotes,
                            votedByNegative = updatedVotersNegative
                        )
                    }
                    onSuccess()
                }
                .onFailure { errorMessage = it.message ?: "Error al reportar" }
        }
    }

    // --- NUEVA FUNCIÓN: CONFIRMAR QUE SÍ EXISTE (RESTAR VOTO NEGATIVO) ---
    fun confirmExistence(onSuccess: () -> Unit) {
        val userId = currentUserId ?: return
        val fountain = selectedFountain ?: return

        viewModelScope.launch {
            processFountainVoteUseCase.confirmExistence(fountain, userId)
                .onSuccess {
                    val updatedVotes = (fountain.negativeVotes - 1).coerceAtLeast(0)
                    val updatedVotersNegative = fountain.votedByNegative.filter { it != userId }

                    selectedFountain = fountain.copy(
                        negativeVotes = updatedVotes,
                        votedByNegative = updatedVotersNegative
                    )
                    onSuccess()
                }
                .onFailure { errorMessage = it.message ?: "Error al confirmar existencia" }
        }
    }

    fun toggleOperationalStatus(onSuccess: () -> Unit) {
        val fountain = selectedFountain ?: return
        viewModelScope.launch {
            val newStatus = !fountain.operational
            updateFountainUseCase(fountain.id, mapOf("operational" to newStatus))
                .onSuccess {
                    selectedFountain = fountain.copy(operational = newStatus)
                    onSuccess()
                }
                .onFailure { errorMessage = "Error al cambiar estado" }
        }
    }

    fun reportOtherIssue(description: String, onSuccess: () -> Unit) {
        val userId = currentUserId ?: return
        val fountain = selectedFountain ?: return

        viewModelScope.launch {
            val report = Report(
                fountainId = fountain.id,
                fountainName = fountain.name,
                userId = userId,
                description = description
            )

            sendReportUseCase(report)
                .onSuccess { onSuccess() }
                .onFailure { errorMessage = "Error al enviar el reporte: ${it.message}" }
        }
    }

    // --- FORMATEADORES ---
    fun getFormattedDate(date: Date): String =
        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(date)

    fun getFormattedCoordinates(lat: Double, lng: Double): String =
        String.format(Locale.US, "%.4f; %.4f", lat, lng)

    fun getDistanceText(distance: Double?): String {
        return distance?.let {
            if (it < 1000) "${it.toInt()}m"
            else String.format(Locale.US, "%.1fkm", it / 1000.0)
        } ?: "---"
    }
}