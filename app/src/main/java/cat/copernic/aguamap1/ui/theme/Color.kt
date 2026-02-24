package cat.copernic.aguamap1.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Colores base
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Paleta Base AguaMap
val Blue10 = Color(0xFF007BFF)
val Blue20 = Color(0xFF00C3FF)
val Blanco = Color(0xFFFFFFFF)
val Negro = Color(0xFF000000)
val Rojo = Color(0xFFFF0000)
val Naranja = Color(0xFFFFA500)
val Verde = Color(0xFF4CAF50)
val Gris = Color(0xFF808080)

// Variantes Semánticas
val BlancoTranslucido = Color(0xB3FFFFFF) // Blanco al 70%
val BlancoCasiOpaco = Color(0xE6FFFFFF)   // Blanco al 90%
val NegroSuave = Color(0xB3000000)        // Negro al 70%
val NegroMuySuave = Color(0x66000000)     // Negro al 40%
val NegroMinimal = Color(0x1A000000)      // Negro al 10%
val GrisClaro = Color(0xFFD3D3D3)         // Gris claro para bordes y fondos

// Gradiente (Ajustado a Horizontal como pediste)
val AguaMapGradient = Brush.horizontalGradient(
    colors = listOf(Blue10, Blue20)
)