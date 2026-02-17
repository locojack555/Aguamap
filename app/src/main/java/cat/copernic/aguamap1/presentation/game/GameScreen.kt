package cat.copernic.aguamap1.presentation.game

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.lifecycle.viewmodel.compose.viewModel
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.presentation.home.map.OSMMapContent
import cat.copernic.aguamap1.ui.theme.AguaMapGradient
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Rojo
import coil.compose.AsyncImage
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.util.Locale

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(),
    onBackToHome: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val currentFountain by viewModel.currentFountain.collectAsState()
    val remainingTime by viewModel.remainingTime.collectAsState()
    val distance by viewModel.distance.collectAsState()
    val score by viewModel.score.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val userGuessPos by viewModel.userGuessPos.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkCanPlay(41.3851, 2.1734)
    }

    Box(modifier = Modifier.fillMaxSize().background(Blanco)) {
        when (gameState) {
            GameViewModel.GameState.Initial -> LoadingPartida()

            GameViewModel.GameState.Instructions -> {
                GameInstructionsScreen(onStart = { viewModel.onStartGameClicked() })
            }

            GameViewModel.GameState.Playing -> {
                currentFountain?.let {
                    GamePlayScreen(
                        fountain = it,
                        remainingTime = remainingTime,
                        onGuess = { lat, lng -> viewModel.setUserGuess(lat, lng) },
                        onFinish = { viewModel.finishGame() }
                    )
                }
            }

            GameViewModel.GameState.Finished -> {
                currentFountain?.let {
                    GameResultScreen(
                        fountain = it,
                        score = score,
                        distance = distance,
                        userGuessPos = userGuessPos?.let { pos -> GeoPoint(pos.first, pos.second) },
                        onBackToHome = onBackToHome
                    )
                }
            }

            GameViewModel.GameState.DailyLimitReached -> ErrorScreen(
                message = error ?: "Ya has jugado hoy. Vuelve mañana.",
                onRetry = null,
                onBack = onBackToHome
            )

            else -> ErrorScreen(
                message = error ?: "Ocurrió un error inesperado",
                onRetry = { viewModel.retryGame() },
                onBack = onBackToHome
            )
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), Alignment.Center) {
                CircularProgressIndicator(color = Blanco)
            }
        }
    }
}

@Composable
fun GameInstructionsScreen(onStart: () -> Unit) {
    // Eliminamos el scrollState y el verticalScroll para forzar adaptabilidad
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Blanco)
    ) {
        // 1. CABECERA (Se mantiene fija arriba)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.25f) // Ocupa el 25% de la pantalla
                .background(AguaMapGradient)
                .padding(24.dp)
        ) {
            Column(Modifier.align(Alignment.BottomStart)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.map_24px),
                        contentDescription = null,
                        tint = Blanco,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("AguaQuest", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Blanco)
                }
                Text("¿Dónde está esta fuente?", color = Blanco.copy(alpha = 0.9f), fontSize = 18.sp)
            }
        }

        // 2. CONTENIDO CENTRAL (Flexible)
        Column(
            modifier = Modifier
                .weight(0.65f) // Ocupa el 65% y gestiona el espacio interno
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Centra el contenido verticalmente
        ) {
            Surface(
                modifier = Modifier.size(100.dp), // Reducido un poco para ganar espacio
                shape = CircleShape,
                color = Color(0xFFE130AD)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_trophy),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = Blanco
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("¡Juega y aprende!", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2D3142))

            Text(
                "Adivina dónde se encuentra la fuente en el mapa y gana puntos",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp, vertical = 8.dp),
                color = Color.Gray,
                fontSize = 15.sp
            )

            Spacer(Modifier.height(16.dp))

            Surface(
                modifier = Modifier.padding(horizontal = 32.dp).widthIn(max = 280.dp),
                color = Color(0xFFF1F8FF),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("• 1 fuente por día", fontSize = 14.sp, color = Color(0xFF4A4A4A))
                    Text("• Más cerca = Más puntos", fontSize = 14.sp, color = Color(0xFF4A4A4A))
                    Text("• Máximo 1000 puntos", fontSize = 14.sp, color = Color(0xFF4A4A4A))
                }
            }
        }

        // 3. BOTÓN INFERIOR (Fijo abajo)
        Box(
            modifier = Modifier
                .weight(0.10f) // Ocupa el 10% restante
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE130AD))
            ) {
                Text("Empezar Juego", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Blanco)
            }
        }
    }
}
@Composable
fun GamePlayScreen(
    fountain: Fountain,
    remainingTime: Int,
    onGuess: (Double, Double) -> Unit,
    onFinish: () -> Unit
) {
    var hasPlacedPin by remember { mutableStateOf(false) }
    var isImageExpanded by remember { mutableStateOf(false) }

    // El Box permite que los elementos floten unos sobre otros
    Box(Modifier.fillMaxSize()) {

        // 1. MAPA AL FONDO
        GameMapView(
            fountain = fountain,
            isFinished = false,
            onMarkerPlaced = { lat, lng ->
                hasPlacedPin = true
                onGuess(lat, lng)
            }
        )

        // 2. HEADER SUPERIOR
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            color = Color.White.copy(alpha = 0.9f),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 4.dp
        ) {
            Row(
                Modifier.padding(12.dp),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                Text(
                    text = "${remainingTime}s",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (remainingTime < 10) Rojo else Color.Black
                )
                Text("AGUAQUEST", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Icon(painterResource(R.drawable.timer_24px), null, tint = Color.Black, modifier = Modifier.size(24.dp))
            }
        }

        // 3. FOTO FLOTANTE
       /* Card(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 110.dp, end = 16.dp)
                .size(if (isImageExpanded) 280.dp else 120.dp)
                .clickable { isImageExpanded = !isImageExpanded },
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            AsyncImage(
                model = if (fountain.imageUrl.isEmpty()) R.drawable.placeholder_fountain else fountain.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }*/

        // 4. BOTÓN O AYUDA (Abajo)
        if (hasPlacedPin) {
            Button(
                onClick = onFinish,
                modifier = Modifier
                    .align(Alignment.BottomCenter) // YA NO SALE EN ROJO
                    .padding(bottom = 40.dp)
                    .fillMaxWidth(0.8f)
                    .height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34A853))
            ) {
                Text("¡CONFIRMAR UBICACIÓN!", color = Blanco, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        } else {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp),
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    "Toca el mapa para marcar",
                    color = Blanco,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    fontSize = 14.sp
                )
            }
        }
    } // Cierre correcto del Box principal
}

@Composable
fun GameMapView(
    fountain: Fountain,
    isFinished: Boolean,
    userGuessPos: GeoPoint? = null,
    onMarkerPlaced: (Double, Double) -> Unit,
    distance: Double = 0.0
) {
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    val context = LocalContext.current


    Box(Modifier.fillMaxSize()) {
        OSMMapContent(viewModel = null, isHome = false) { map ->
            mapViewRef = map
            if (!isFinished) {
                map.controller.setZoom(17.0)
                map.controller.setCenter(GeoPoint(41.3851, 2.1734))

                val eventsReceiver = object : org.osmdroid.events.MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        if (isFinished) return false
                        map.overlays.removeAll { it is Marker && it.title == "Tu apuesta" }

                        val sizeInPx = (35 * context.resources.displayMetrics.density).toInt()
                        val userMarker = Marker(map).apply {
                            position = p
                            title = "Tu apuesta"

                            // 1. Obtenemos el drawable
                            val drawable = ContextCompat.getDrawable(context, R.drawable.icon_pin)


                            // 3. Convertimos a Bitmap escalado
                            val bitmap = drawable?.toBitmap()?.scale(sizeInPx, sizeInPx, false)

                            icon = BitmapDrawable(context.resources, bitmap)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }

                        map.overlays.add(userMarker)
                        onMarkerPlaced(p.latitude, p.longitude)
                        map.invalidate()
                        return true
                    }
                    override fun longPressHelper(p: GeoPoint): Boolean = false
                }
                val mapEventsOverlay = org.osmdroid.views.overlay.MapEventsOverlay(eventsReceiver)
                map.overlays.add(0, mapEventsOverlay)
            }
        }

        // Bloque isFinished (cuando se muestran ambos puntos al final)
        if (isFinished) {
            LaunchedEffect(Unit) {
                mapViewRef?.let { map ->
                    val sizeInPx = (35 * context.resources.displayMetrics.density).toInt()
                    val realPoint = GeoPoint(fountain.latitude, fountain.longitude)
                    val guessPoint = userGuessPos ?: GeoPoint(0.0, 0.0)

                    // MARCADOR REAL (Verde/Default)
                    val realMarker = Marker(map).apply {
                        position = realPoint
                        title = "Ubicación Real"
                        val bitmap = ContextCompat.getDrawable(context, R.drawable.icon_pin)
                            ?.toBitmap()?.scale(sizeInPx, sizeInPx, false)
                        icon = BitmapDrawable(context.resources, bitmap)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    map.overlays.add(realMarker)

                    // MARCADOR USUARIO (También Lila aquí para consistencia)
                    val guessMarker = Marker(map).apply {
                        position = guessPoint
                        title = "Tu apuesta"
                        val drawable = ContextCompat.getDrawable(context, R.drawable.icon_pin)
                        drawable?.setTint(android.graphics.Color.parseColor("#34A853"))
                        val bitmap = drawable?.toBitmap()?.scale(sizeInPx, sizeInPx, false)
                        icon = BitmapDrawable(context.resources, bitmap)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    map.overlays.add(guessMarker)

                    // Línea de distancia
                    val line = Polyline().apply {
                        outlinePaint.color = android.graphics.Color.BLUE
                        outlinePaint.strokeWidth = 5f
                        setPoints(listOf(realPoint, guessPoint))
                    }
                    map.overlays.add(line)

                    // Etiqueta de distancia
                    val midPoint = GeoPoint((realPoint.latitude + guessPoint.latitude) / 2, (realPoint.longitude + guessPoint.longitude) / 2)
                    val distanceMarker = Marker(map).apply {
                        position = midPoint
                        icon = createDistanceTag(context, "${distance.toInt()}m")
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        setInfoWindow(null)
                    }
                    map.overlays.add(distanceMarker)

                    val box = BoundingBox.fromGeoPoints(listOf(realPoint, guessPoint))
                    map.zoomToBoundingBox(box, true, 120)
                    map.invalidate()
                }
            }
        }
    }
}

private fun createDistanceTag(context: android.content.Context, text: String): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val paintText = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.BLACK
        textSize = 14 * density
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    val bounds = android.graphics.Rect()
    paintText.getTextBounds(text, 0, text.length, bounds)
    val width = bounds.width() + (24 * density).toInt()
    val height = bounds.height() + (12 * density).toInt()
    val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paintRect = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.parseColor("#E3F2FD") }
    val paintStroke = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#2196F3")
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 2 * density
    }
    val rectF = android.graphics.RectF(0f, 0f, width.toFloat(), height.toFloat())
    canvas.drawRoundRect(rectF, 8 * density, 8 * density, paintRect)
    canvas.drawRoundRect(rectF, 8 * density, 8 * density, paintStroke)
    canvas.drawText(text, width / 2f, (height / 2f) - bounds.exactCenterY(), paintText)
    return BitmapDrawable(context.resources, bitmap)
}

@Composable
fun GameResultScreen(
    fountain: Fountain,
    score: Int,
    distance: Double,
    userGuessPos: GeoPoint?,
    onBackToHome: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(0.45f)) {
            GameMapView(fountain = fountain, isFinished = true, userGuessPos = userGuessPos, onMarkerPlaced = { _, _ -> }, distance = distance)
        }
        Card(
            modifier = Modifier.weight(0.55f).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Blanco),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PARTIDA COMPLETADA", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("TU PUNTUACIÓN", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("$score Puntos", fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color(0xFF007BFF))
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(color = Color(0xFFE3F2FD), shape = RoundedCornerShape(8.dp)) {
                        Text("Distancia al objetivo: ${String.format(Locale.getDefault(), "%.0f", distance)}m", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1976D2))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Dirígete a Ranking si desea ver su puntuación", fontWeight = FontWeight.Bold, color = Color.Gray, textAlign = TextAlign.Center, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun LoadingPartida() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color(0xFF007BFF))
            Spacer(Modifier.height(16.dp))
            Text("PREPARANDO PARTIDA...", fontWeight = FontWeight.Bold, color = Color.Gray)
        }
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: (() -> Unit)?, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(32.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Icon(painterResource(R.drawable.error_24px), null, Modifier.size(64.dp), tint = Rojo)
        Spacer(Modifier.height(16.dp))
        Text(message, textAlign = TextAlign.Center, fontSize = 18.sp)
        Spacer(Modifier.height(24.dp))
        onRetry?.let { Button(onClick = it, Modifier.fillMaxWidth().height(50.dp)) { Text("REINTENTAR") } }
        Spacer(Modifier.height(8.dp))
    }
}