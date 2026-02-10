package cat.copernic.aguamap1.presentation.home

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(auth: FirebaseAuth) {
    Text("Hola casa")
}