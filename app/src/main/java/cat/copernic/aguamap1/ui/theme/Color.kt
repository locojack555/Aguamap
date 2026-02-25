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
// Colores principales de la app
val Blue10 = Color(0xFF007BFF)
val Blue20 = Color(0xFF00C3FF)
val AzulOscuro = Color(0xFF0A4D68)

val BgGray50 = Color(0xFFF9FAFB)
val TextGray800 = Color(0xFF1F2937)
val DangerRed = Color(0xFFEF4444)
val InfoBlue = Color(0xFF3B82F6)
val AzulTurquesa = Color(0xFF75C9C8)
val AzulGrisaceo = Color(0xFF2D3142)
val AzulClaro = Color(0xFFF1F8FF)
val GrisOscuro = Color(0xFF4A4A4A)

val Blanco = Color(0xFFFFFFFF)
val BlancoClaro = Color(0xFFFFFFFF)
val Negro = Color(0xFF000000)
val Rojo = Color(0xFFFF0000)
val Naranja = Color(0xFFFFA500)
val Verde = Color(0xFF4CAF50)

val VerdeClaro = Color(0xFF34A853)
val Gris = Color(0xFF808080)

// Variantes Semánticas
val BlancoTranslucido = Color(0xB3FFFFFF) // Blanco al 70%
val BlancoCasiOpaco = Color(0xE6FFFFFF)   // Blanco al 90%
val NegroSuave = Color(0xB3000000)        // Negro al 70%
val NegroMuySuave = Color(0x66000000)     // Negro al 40%
val NegroMinimal = Color(0x1A000000)      // Negro al 10%
val GrisClaro = Color(0xFFD3D3D3)         // Gris claro para bordes y fondos

// Gradiente (Ajustado a Horizontal como pediste)
// Gradientes
val AguaMapGradient = Brush.horizontalGradient(
    colors = listOf(Blue10, Blue20)
)

// Gradiente adicional usado en GameInstructionsScreen
val AzulGradient = Brush.verticalGradient(
    colors = listOf(AzulOscuro, AzulTurquesa)
)