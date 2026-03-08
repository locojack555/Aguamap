package cat.copernic.aguamap1.aplication.fountain.detailFountain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.fountain.Fountain
import cat.copernic.aguamap1.domain.model.fountain.Report
import cat.copernic.aguamap1.domain.model.fountain.StateFountain
import cat.copernic.aguamap1.domain.model.user.UserRole
import cat.copernic.aguamap1.domain.repository.auth.AuthRepository
import cat.copernic.aguamap1.domain.repository.status.RealtimeStatusRepository
import cat.copernic.aguamap1.domain.usecase.auth.GetUserRoleUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.DeleteFountainUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.ProcessFountainVoteUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.UpdateFountainUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.SendReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel que gestiona la lógica de negocio para el detalle de una fuente.
 * Implementa un sistema de votos (positivos/negativos), gestión de reportes
 * y sincronización de estado operativo en tiempo real.
 */
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

    // --- ESTADOS REACTIVOS ---
    var isAdmin by mutableStateOf(false); private set
    var currentUserId by mutableStateOf<String?>(null); private set
    var creatorName by mutableStateOf<String?>(null); private set
    var selectedFountain by mutableStateOf<Fountain?>(null); private set
    var errorMessage by mutableStateOf<String?>(null); private set
    var isActionLoading by mutableStateOf(false); private set // Evita múltiples clics en acciones críticas

    // Flujo interno para rastrear qué fuente se está observando en tiempo real
    private val _fountainIdFlow = MutableStateFlow<String?>(null)

    /**
     * Estado operativo en tiempo real. Utiliza [flatMapLatest] para que, cada vez que cambie
     * el ID de la fuente, se cancele la suscripción anterior y se inicie una nueva
     * hacia el nodo de estado en tiempo real (Realtime Database).
     */
    val isOperationalRealtime: StateFlow<Boolean> = _fountainIdFlow
        .flatMapLatest { id ->
            if (id != null) realtimeStatusRepository.getFountainStatus(id)
            else flowOf(true)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    init {
        loadUserData()
    }

    /**
     * Carga el UID del usuario actual y verifica si tiene rol de administrador.
     */
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

    /**
     * Selecciona una fuente para mostrar su detalle y calcula la distancia
     * si se proporcionan las coordenadas del usuario.
     */
    fun selectFountain(fountain: Fountain, userLat: Double? = null, userLng: Double? = null) {
        selectedFountain = fountain
        _fountainIdFlow.value = fountain.id

        // Si recibimos coordenadas, calculamos la distancia inmediatamente
        if (userLat != null && userLng != null) {
            val distance = calculateDistance(
                userLat, userLng,
                fountain.latitude, fountain.longitude
            )
            // Suponiendo que tienes un estado para la distancia, lo actualizamos aquí
            // p.ej: _fountainDistance.value = distance
        }

        fetchCreatorName(fountain.createdBy)
    }

    /**
     * Función auxiliar para calcular la distancia en metros entre dos puntos (Haversine).
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    private fun fetchCreatorName(uid: String) {
        viewModelScope.launch {
            authRepository.getUserNameById(uid)
                .onSuccess { creatorName = it }
                .onFailure { creatorName = getString(R.string.default_user_name) }
        }
    }

    // --- LÓGICA DE ESTADO OPERACIONAL ---
    /**
     * Cambia el estado de la fuente (Disponible / Averiada).
     * Actualiza tanto el repositorio en tiempo real como el documento principal en Firestore.
     */
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

    // --- SISTEMA DE VOTOS Y VALIDACIÓN ---

    /**
     * Voto positivo: Si una fuente llega a 3 votos, cambia su estado a ACCEPTED.
     */
    fun confirmFountain(onSuccess: () -> Unit) {
        val userId = currentUserId ?: return
        val fountain = selectedFountain ?: return
        if (fountain.votedByPositive.contains(userId) || isActionLoading) return

        isActionLoading = true
        val previousFountain = selectedFountain

        val updatedVotes = fountain.positiveVotes + 1
        val updatedVoters = fountain.votedByPositive + userId
        val newStatus = if (updatedVotes >= 3) StateFountain.ACCEPTED else fountain.status

        // Optimistic Update: Actualizamos la UI antes de recibir confirmación del servidor
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
                        selectedFountain = previousFountain // Rollback en caso de error
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

    /**
     * Voto negativo: Incrementa el contador de reportes por "no existe".
     */
    fun reportNonExistent(onSuccess: () -> Unit) {
        val userId = currentUserId ?: return
        val fountain = selectedFountain ?: return
        if (fountain.votedByNegative.contains(userId) || isActionLoading) return

        isActionLoading = true
        val previousFountain = selectedFountain

        val updatedVotes = fountain.negativeVotes + 1
        val updatedVoters = fountain.votedByNegative + userId

        // Optimistic update
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

    /**
     * Revierte un voto negativo: Útil si un usuario reportó por error que no existía.
     */
    fun confirmExistence(onSuccess: () -> Unit) {
        val userId = currentUserId ?: return
        val fountain = selectedFountain ?: return
        if (!fountain.votedByNegative.contains(userId) || isActionLoading) return

        isActionLoading = true
        val previousFountain = selectedFountain

        val updatedVotes = (fountain.negativeVotes - 1).coerceAtLeast(0)
        val updatedVotersNegative = fountain.votedByNegative.filter { it != userId }

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

    // --- GESTIÓN DE REPORTES ---
    fun reportOtherIssue(description: String, onSuccess: () -> Unit) {
        val userId = currentUserId ?: return
        val fountain = selectedFountain ?: return
        viewModelScope.launch {
            val report = Report(
                id = "", // Se generará en el repositorio
                fountainId = fountain.id,
                fountainName = fountain.name,
                userId = userId,
                description = description,
                timestamp = System.currentTimeMillis(),
                resolved = false
            )
            sendReportUseCase(report)
                .onSuccess { onSuccess() }
                .onFailure { errorMessage = getString(R.string.error_report_comment) }
        }
    }

    /**
     * Borra la fuente definitivamente.
     */
    fun deleteFountain(onDeleted: () -> Unit) {
        val fountainId = selectedFountain?.id ?: return
        viewModelScope.launch {
            deleteFountainUseCase(fountainId)
                .onSuccess {
                    clearSelection()
                    onDeleted()
                }
                .onFailure {
                    errorMessage = "${getString(R.string.error_delete_generic)}: ${it.message}"
                }
        }
    }

    // --- MÉTODOS DE UTILIDAD (HELPERS) ---

    /**
     * Determina si el usuario actual tiene permisos para modificar la fuente.
     * Solo Admins o el creador (si la fuente sigue pendiente).
     */
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

    /**
     * Formatea la distancia para mostrar metros o kilómetros según el valor.
     */
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

    fun clearError() {
        errorMessage = null
    }
}