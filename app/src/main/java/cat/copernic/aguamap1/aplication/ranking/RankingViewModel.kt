package cat.copernic.aguamap1.aplication.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.ranking.RankingPeriod
import cat.copernic.aguamap1.domain.model.ranking.RankingState
import cat.copernic.aguamap1.domain.usecase.ranking.GetRankingByPeriodUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsable de la lógica de negocio de la pantalla de Ranking.
 * Actúa como puente entre la UI y el caso de uso de obtención de clasificaciones,
 * transformando los IDs de recursos de la vista en tipos de dominio.
 */
@HiltViewModel
class RankingViewModel @Inject constructor(
    private val getRankingByPeriodUseCase: GetRankingByPeriodUseCase
) : ViewModel() {

    // Estado reactivo que encapsula la lista de jugadores y el estado de carga
    private val _state = MutableStateFlow(RankingState())
    val state: StateFlow<RankingState> = _state

    /* objeto rankinguser pasarlo
    var selectedPlayer by mutableStateOf<UserRanking?>(null)
    private set

    fun selectPlayer(player: UserRanking) {
        selectedPlayer = player
    }
     */
    /**
     * Carga el ranking filtrado por el periodo seleccionado.
     * * @param periodResId El ID del recurso de string (R.string) que identifica el periodo (Día, Mes, Año).
     */
    fun loadRanking(periodResId: Int) {
        // Activamos el indicador de carga antes de iniciar la petición
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // Mapeamos el ID del recurso de la UI al Enum de dominio RankingPeriod
                val period = when (periodResId) {
                    R.string.ranking_day -> RankingPeriod.DAY
                    R.string.ranking_month -> RankingPeriod.MONTH
                    R.string.ranking_year -> RankingPeriod.YEAR
                    else -> RankingPeriod.DAY
                }

                // Ejecutamos el caso de uso para obtener los datos de Firestore/Repositorio
                val ranking = getRankingByPeriodUseCase(period)

                // Actualizamos el estado con la nueva lista de jugadores y desactivamos el loading
                _state.update {
                    RankingState(players = ranking, isLoading = false)
                }
            } catch (e: Exception) {
                // En caso de error, aseguramos que la UI deje de mostrar el progreso
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}