package cat.copernic.aguamap1.presentation.home.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MapViewModel : ViewModel() {
    var latitude by mutableDoubleStateOf(41.5632)
    var longitude by mutableDoubleStateOf(2.0089)
    var zoomLevel by mutableDoubleStateOf(15.0)
    var isFirstLocationUpdate by mutableStateOf(true)

    fun onMapMoved(lat: Double, lng: Double, zoom: Double) {
        latitude = lat
        longitude = lng
        zoomLevel = zoom
    }

    fun onFirstLocationFound(lat: Double, lng: Double) {
        if (isFirstLocationUpdate) {
            latitude = lat
            longitude = lng
            zoomLevel = 17.0
            isFirstLocationUpdate = false
        }
    }
}