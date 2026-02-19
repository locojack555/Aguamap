package cat.copernic.aguamap1.presentation.music

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SoundManagerViewModel @Inject constructor(
    val soundManager: SoundManager
) : ViewModel()