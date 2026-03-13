package cat.copernic.aguamap1.aplication

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun Luis (onLuisClick: () -> Unit){
    Button(onClick = { onLuisClick() }) {
        Text("Luis")
    }

}