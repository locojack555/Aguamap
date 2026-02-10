package cat.copernic.aguamap1.presentation.ranking

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RankingViewModel : ViewModel() {
    private val _state = MutableStateFlow(RankingState())
    val state: StateFlow<RankingState> = _state

    init {
        // Simular carga de datos
        _state.value = RankingState(
            players = listOf(
                UserRanking(1, "Administrador (Tú)", 125, 15, 25, true),
                UserRanking(2, "María García", 90, 12, 18),
                UserRanking(3, "Joan Martínez", 75, 10, 15),
                UserRanking(4, "Joaquin Perez", 60, 8, 12)
            )
        )
    }
}