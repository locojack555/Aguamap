package cat.copernic.aguamap1.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Colores principales de la app
val Blue10 = Color(0xFF007BFF)
val Blue20 = Color(0xFF00C3FF)
val AzulOscuro = Color(0xFF0A4D68)


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

// Gradientes
val AguaMapGradient = Brush.horizontalGradient(
    colors = listOf(Blue10, Blue20)
)

// Gradiente adicional usado en GameInstructionsScreen
val AzulGradient = Brush.verticalGradient(
    colors = listOf(AzulOscuro, AzulTurquesa)
)