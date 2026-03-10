package cat.copernic.aguamap1.aplication.profile.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.fountain.Fountain
import cat.copernic.aguamap1.domain.model.fountain.Report
import cat.copernic.aguamap1.domain.repository.auth.AuthRepository
import cat.copernic.aguamap1.domain.repository.fountain.ReportRepository
import cat.copernic.aguamap1.domain.usecase.fountain.GetFountainByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de la gestión de reportes de fuentes de agua.
 * Implementa una estrategia de caché para nombres de usuario y coordina la
 * resolución de incidencias enviadas por la comunidad.
 */
@HiltViewModel
class FountainReportsViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val authRepository: AuthRepository,
    private val getFountainByIdUseCase: GetFountainByIdUseCase
) : ViewModel() {

    // Estado que contiene la lista de reportes pendientes
    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    // --- LÓGICA DE UBICACIÓN ---
    private var _userLat: Double? = null
    private var _userLng: Double? = null

    /**
     * Establece la ubicación actual del usuario para cálculos de distancia.
     */
    fun setLocation(lat: Double?, lng: Double?) {
        _userLat = lat
        _userLng = lng
        // Si quisieras recalcular distancias en la lista de reportes, lo harías aquí.
    }

    // Caché en memoria para evitar peticiones repetidas a Firebase Auth/Firestore por el mismo UID
    private val usernameCache = mutableMapOf<String, String>()

    // Mapeo de UID a Nombre para mostrar en la UI
    private val _userNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val userNames: StateFlow<Map<String, String>> = _userNames.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorResId = MutableStateFlow<Int?>(null)
    val errorResId: StateFlow<Int?> = _errorResId.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    init {
        loadReports()
    }

    /**
     * Carga los reportes con estado "pendiente" y dispara la resolución de nombres de usuario.
     */
    fun loadReports() {
        viewModelScope.launch {
            _isLoading.value = true
            reportRepository.getPendingReports()
                .onSuccess { list ->
                    _reports.value = list
                    // Resolvemos nombres solo para los IDs únicos presentes en la lista
                    resolveUserNames(list.map { it.userId }.distinct())
                }
                .onFailure {
                    _errorResId.value = R.string.error_loading_reports
                }
            _isLoading.value = false
        }
    }


    /**
     * Resuelve los nombres de los reporteros. Utiliza un caché local para optimizar
     * el consumo de red y cuotas de Firebase.
     */
    private suspend fun resolveUserNames(uids: List<String>) {
        val resolved = mutableMapOf<String, String>()
        for (uid in uids) {
            // Si el nombre ya está en el caché, lo usamos; si no, lo pedimos al repositorio
            val name = usernameCache[uid] ?: run {
                authRepository.getUserNameById(uid)
                    .getOrNull() ?: "Unknown User"
                    .also { usernameCache[uid] = it } // Guardamos en caché para futuras consultas
            }
            resolved[uid] = name
        }
        // Combinamos el mapa actual con los nuevos nombres resueltos
        _userNames.value = _userNames.value + resolved
    }

    /**
     * Marca un reporte como resuelto en la base de datos y lo elimina de la lista local.
     */
    fun resolveReport(reportId: String) {
        viewModelScope.launch {
            reportRepository.resolveReport(reportId)
                .onSuccess {
                    // Eliminación optimista en la UI para respuesta instantánea
                    _reports.value = _reports.value.filter { it.id != reportId }
                    _isSuccess.value = true
                }
                .onFailure {
                    _errorResId.value = R.string.error_resolving_report
                }
        }
    }

    /**
     * Obtiene los detalles de una fuente específica por su ID.
     */
    fun getFountainById(fountainId: String, onResult: (Fountain?) -> Unit) {
        viewModelScope.launch {
            val result = getFountainByIdUseCase(fountainId)
            onResult(result.getOrNull())
        }
    }

    fun clearError() {
        _errorResId.value = null
    }

    fun resetSuccess() {
        _isSuccess.value = false
    }
}