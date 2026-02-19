package cat.copernic.aguamap1.presentation.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.domain.model.RankingPeriod
import cat.copernic.aguamap1.domain.model.UserRanking
import cat.copernic.aguamap1.domain.usecase.ranking.GetRankingByPeriodUseCase
import cat.copernic.aguamap1.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RankingViewModel @Inject constructor(
    private val getRankingByPeriodUseCase: GetRankingByPeriodUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RankingState())
    val state: StateFlow<RankingState> = _state

    fun loadRanking(periodResId: Int) {
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val period = when (periodResId) {
                    R.string.ranking_day -> RankingPeriod.DAY
                    R.string.ranking_month -> RankingPeriod.MONTH
                    R.string.ranking_year -> RankingPeriod.YEAR
                    else -> RankingPeriod.DAY
                }

                val ranking = getRankingByPeriodUseCase(period)
                _state.update {
                    RankingState(players = ranking, isLoading = false)
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}