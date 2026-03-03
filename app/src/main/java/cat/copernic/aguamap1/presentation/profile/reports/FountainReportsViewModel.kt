package cat.copernic.aguamap1.presentation.profile.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.R
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

    private val usernameCache = mutableMapOf<String, String>()

    private val _userNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val userNames: StateFlow<Map<String, String>> = _userNames.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Cambiado para coincidir con la Screen
    private val _errorResId = MutableStateFlow<Int?>(null)
    val errorResId: StateFlow<Int?> = _errorResId.asStateFlow()

    // Cambiado a Boolean para manejar el estado de éxito simplemente
    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

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
                .onFailure {
                    _errorResId.value = R.string.error_loading_reports
                }
            _isLoading.value = false
        }
    }

    private suspend fun resolveUserNames(uids: List<String>) {
        val resolved = mutableMapOf<String, String>()
        for (uid in uids) {
            val name = usernameCache[uid] ?: run {
                authRepository.getUserNameById(uid)
                    .getOrNull() ?: "Unknown User"
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
                    _isSuccess.value = true // Activamos el éxito
                }
                .onFailure {
                    _errorResId.value = R.string.error_resolving_report
                }
        }
    }

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