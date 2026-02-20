package cat.copernic.aguamap1.presentation.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.domain.model.UserRanking
import cat.copernic.aguamap1.domain.usecase.profile.GetCurrentUserHistoricStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserHistoricStatsUseCase: GetCurrentUserHistoricStatsUseCase
) : ViewModel() {

    var userStats by mutableStateOf<UserRanking?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        loadUserStats()
    }

    fun loadUserStats() {
        viewModelScope.launch {
            isLoading = true
            userStats = getCurrentUserHistoricStatsUseCase()
            isLoading = false
        }
    }
}