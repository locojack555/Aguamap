package cat.copernic.aguamap1.presentation.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cat.copernic.aguamap1.presentation.reusable.AguaMapLanguage
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(navigateToLogin: () -> Unit = {}) {
    Column {
        AguaMapLanguage()
        Button({
            FirebaseAuth.getInstance().signOut()
            navigateToLogin()
        }) {
            Text("Cerrar Sesión")
        }
    }
}