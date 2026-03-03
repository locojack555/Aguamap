package cat.copernic.aguamap1.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// --- 1. COLORES DE MARCA (BRAND) ---
val Blue10 = Color(0xFF007BFF)      // Azul Principal
val Blue20 = Color(0xFF00C3FF)      // Azul Secundario / Brillo
val AzulOscuro = Color(0xFF0A4D68)   // Azul Profundo (Fondos oscuros)
val AzulTurquesa = Color(0xFF75C9C8) // Acento Agua
val AzulClaro = Color(0xFFF1F8FF)    // Fondo muy suave (secciones/listas)
val AzulGrisaceo = Color(0xFF2D3142) // Elegante para textos o UI oscura
val Celeste = Color(0xFFE3F2FD)      // Fondo suave para comentarios propios

// --- 2. COLORES SEMÁNTICOS (ESTADOS) ---
val Rojo = Color(0xFFEF4444)        // Danger / Errores
val Verde = Color(0xFF34A853)       // Success / Operativo
val VerdeClaro = Color(0xFFD4EDDA)  // Fondos de éxito / validación
val Naranja = Color(0xFFFFA500)      // Warning / Estrellas / Reportes
val InfoBlue = Color(0xFF3B82F6)    // Información

// --- 3. NEUTROS Y ESCALA DE GRISES ---
val Blanco = Color(0xFFFFFFFF)
val BlancoClaro = Color(0xFFF8F9FA) // Blanco suave para fondos limpios
val Negro = Color(0xFF1A1A1A)       // Negro elegante

val GrisOscuro = Color(0xFF4A4A4A)  // Texto secundario
val Gris = Color(0xFF808080)
val GrisClaro = Color(0xFFD3D3D3)   // Bordes y separadores
val BgGray50 = Color(0xFFF9FAFB)    // Fondo de pantallas

// --- 4. VARIANTES DE TRANSPARENCIA (ALPHAS) ---
val BlancoTranslucido = Color(0xB3FFFFFF) // 70%
val BlancoCasiOpaco = Color(0xE6FFFFFF)   // 90%
val NegroSuave = Color(0xB3000000)        // 70%
val NegroMuySuave = Color(0x66000000)     // 40%
val NegroMinimal = Color(0x1A000000)      // 10%



// --- 5. GRADIENTES ---
val AguaMapGradient = Brush.horizontalGradient(
    colors = listOf(Blue10, Blue20)
)

val AzulGradient = Brush.verticalGradient(
    colors = listOf(AzulOscuro, AzulTurquesa)
)

// --- 6. COLORES DE COMPATIBILIDAD / ANDROID DEFAULT ---
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)     // <--- AÑADIDO
val PurpleGrey40 = Color(0xFF625B71)
val Pink40 = Color(0xFF7D5260)       // <--- AÑADIDO