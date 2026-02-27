package cat.copernic.aguamap1.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.Report
import cat.copernic.aguamap1.domain.repository.AuthRepository
import cat.copernic.aguamap1.domain.repository.ReportRepository
import cat.copernic.aguamap1.domain.usecase.fountain.GetFountainByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FountainReportsViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val authRepository: AuthRepository,
    private val getFountainByIdUseCase: GetFountainByIdUseCase
) : ViewModel() {

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    // Cache uid → nombre para no repetir llamadas a Firestore
    private val usernameCache = mutableMapOf<String, String>()

    // Mapa uid → nombre resuelto que la UI consume
    private val _userNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val userNames: StateFlow<Map<String, String>> = _userNames.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadReports()
    }

    fun loadReports() {
        viewModelScope.launch {
            _isLoading.value = true
            reportRepository.getPendingReports()
                .onSuccess { list ->
                    _reports.value = list
                    resolveUserNames(list.map { it.userId }.distinct())
                }
                .onFailure { _errorMessage.value = "Error al cargar los reportes" }
            _isLoading.value = false
        }
    }

    private suspend fun resolveUserNames(uids: List<String>) {
        val resolved = mutableMapOf<String, String>()
        for (uid in uids) {
            val name = usernameCache[uid] ?: run {
                // getUserNameById usa el campo "nom" de la colección users
                authRepository.getUserNameById(uid)
                    .getOrDefault("Usuario desconocido")
                    .also { usernameCache[uid] = it }
            }
            resolved[uid] = name
        }
        _userNames.value = _userNames.value + resolved
    }

    fun resolveReport(reportId: String) {
        viewModelScope.launch {
            reportRepository.resolveReport(reportId)
                .onSuccess {
                    _reports.value = _reports.value.filter { it.id != reportId }
                    _successMessage.value = "Reporte marcado como resuelto"
                }
                .onFailure { _errorMessage.value = "Error al resolver el reporte" }
        }
    }

    fun getFountainById(fountainId: String, onResult: (Fountain?) -> Unit) {
        viewModelScope.launch {
            val result = getFountainByIdUseCase(fountainId)
            onResult(result.getOrNull())
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}