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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.presentation.home.map.OSMMapContent
import cat.copernic.aguamap1.ui.theme.AguaMapGradient
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Rojo
import coil.compose.AsyncImage
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.util.Locale
import kotlin.math.max

@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel(),
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
    val hasLost by viewModel.hasLost.collectAsState()

    LaunchedEffect(gameState) {
        when (gameState) {
            GameViewModel.GameState.Playing -> {}
            GameViewModel.GameState.Finished,
            GameViewModel.GameState.DailyLimitReached,
            GameViewModel.GameState.Error -> {}
            else -> {}
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.onBackToHomePressed()
        }
    }

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
                        hasLost = hasLost,
                        onBackToHome = {
                            viewModel.onBackToHomePressed()
                            onBackToHome()
                        }
                    )
                }
            }

            GameViewModel.GameState.DailyLimitReached -> ErrorScreen(
                message = error ?: stringResource(R.string.game_error_daily_limit),
                onRetry = null,
                onBack = {
                    viewModel.onBackToHomePressed()
                    onBackToHome()
                }
            )

            else -> ErrorScreen(
                message = error ?: stringResource(R.string.game_error_unexpected),
                onRetry = { viewModel.retryGame() },
                onBack = {
                    viewModel.onBackToHomePressed()
                    onBackToHome()
                }
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Blanco)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.16f)
                .background(AguaMapGradient)
                .padding(18.dp)
        ) {
            Column(Modifier.align(Alignment.CenterStart)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.map_24px),
                        contentDescription = null,
                        tint = Blanco,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.game_instructions_title_app), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Blanco)
                }
                Text(stringResource(R.string.game_instructions_subtitle), color = Blanco.copy(alpha = 0.9f), fontSize = 18.sp)
            }
        }

        Column(
            modifier = Modifier
                .weight(0.65f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0A4D68),
                                    Color(0xFF75C9C8)
                                )
                            )
                        )
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_trophy),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = Blanco
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(stringResource(R.string.game_instructions_play_learn_title), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2D3142))

            Text(
                stringResource(R.string.game_instructions_play_learn_description),
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
                    Text(stringResource(R.string.game_instructions_rule_one), fontSize = 14.sp, color = Color(0xFF4A4A4A))
                    Text(stringResource(R.string.game_instructions_rule_two), fontSize = 14.sp, color = Color(0xFF4A4A4A))
                    Text(stringResource(R.string.game_instructions_rule_three), fontSize = 14.sp, color = Color(0xFF4A4A4A))
                }
            }
        }

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            )
        ) {
            Text(
                stringResource(R.string.game_instructions_start_button),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Blanco,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0A4D68),
                                Color(0xFF75C9C8)
                            )
                        ),
                        shape = RoundedCornerShape(5.dp)
                    )
                    .wrapContentSize(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.weight(0.05f))
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

    Box(Modifier.fillMaxSize()) {

        GameMapView(
            fountain = fountain,
            isFinished = false,
            onMarkerPlaced = { lat, lng ->
                hasPlacedPin = true
                onGuess(lat, lng)
            }
        )

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
                Text(stringResource(R.string.game_instructions_title_app), color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Icon(painterResource(R.drawable.timer_24px), null, tint = Color.Black, modifier = Modifier.size(24.dp))
            }
        }

        if (hasPlacedPin) {
            Button(
                onClick = onFinish,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
                    .fillMaxWidth(0.8f)
                    .height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34A853))
            ) {
                Text(stringResource(R.string.game_play_confirm_button), color = Blanco, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                    stringResource(R.string.game_play_hint_text),
                    color = Blanco,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
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
                            val drawable = ContextCompat.getDrawable(context, R.drawable.icon_pin)
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

        if (isFinished && userGuessPos != null) {
            LaunchedEffect(Unit) {
                mapViewRef?.let { map ->
                    map.overlays.clear()

                    val sizeInPx = (35 * context.resources.displayMetrics.density).toInt()
                    val realPoint = GeoPoint(fountain.latitude, fountain.longitude)
                    val guessPoint = userGuessPos

                    val realDrawable = ContextCompat.getDrawable(context, R.drawable.icon_pin)?.mutate()
                    realDrawable?.setTint(android.graphics.Color.parseColor("#FF4444"))
                    val realBitmap = realDrawable?.toBitmap()?.scale(sizeInPx, sizeInPx, false)

                    val realMarker = Marker(map).apply {
                        position = realPoint
                        title = "Ubicación Real"
                        icon = BitmapDrawable(context.resources, realBitmap)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    map.overlays.add(realMarker)

                    val guessDrawable = ContextCompat.getDrawable(context, R.drawable.icon_pin)?.mutate()
                    guessDrawable?.setTint(android.graphics.Color.parseColor("#34A853"))
                    val guessBitmap = guessDrawable?.toBitmap()?.scale(sizeInPx, sizeInPx, false)

                    val guessMarker = Marker(map).apply {
                        position = guessPoint
                        title = "Tu apuesta"
                        icon = BitmapDrawable(context.resources, guessBitmap)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    map.overlays.add(guessMarker)

                    val line = Polyline().apply {
                        outlinePaint.color = android.graphics.Color.BLUE
                        outlinePaint.strokeWidth = 5f
                        setPoints(listOf(realPoint, guessPoint))
                    }
                    map.overlays.add(line)

                    val midPoint = GeoPoint(
                        (realPoint.latitude + guessPoint.latitude) / 2,
                        (realPoint.longitude + guessPoint.longitude) / 2
                    )
                    val distanceMarker = Marker(map).apply {
                        position = midPoint
                        icon = createDistanceTag(context, "${distance.toInt()}m")
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        setInfoWindow(null)
                    }
                    map.overlays.add(distanceMarker)

                    try {
                        val minLat = minOf(realPoint.latitude, guessPoint.latitude)
                        val maxLat = maxOf(realPoint.latitude, guessPoint.latitude)
                        val minLon = minOf(realPoint.longitude, guessPoint.longitude)
                        val maxLon = maxOf(realPoint.longitude, guessPoint.longitude)

                        val centerLat = (minLat + maxLat) / 2
                        val centerLon = (minLon + maxLon) / 2

                        val latSpan = maxLat - minLat
                        val lonSpan = maxLon - minLon

                        val zoomLevel = when {
                            max(latSpan, lonSpan) < 0.005 -> 17.0
                            max(latSpan, lonSpan) < 0.01 -> 16.0
                            max(latSpan, lonSpan) < 0.02 -> 15.0
                            max(latSpan, lonSpan) < 0.05 -> 14.0
                            max(latSpan, lonSpan) < 0.1 -> 13.0
                            else -> 12.0
                        }

                        map.controller.setZoom(zoomLevel)
                        map.controller.setCenter(GeoPoint(centerLat, centerLon))

                    } catch (e: Exception) {
                        map.controller.setZoom(15.0)
                        map.controller.setCenter(GeoPoint(
                            (realPoint.latitude + guessPoint.latitude) / 2,
                            (realPoint.longitude + guessPoint.longitude) / 2
                        ))
                    }

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
    hasLost: Boolean,
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current

    Column(Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(0.45f)) {
            if (hasLost) {
                Box(modifier = Modifier.fillMaxSize()) {
                    OSMMapContent(viewModel = null, isHome = false) { map ->
                        map.controller.setZoom(17.0)
                        map.controller.setCenter(GeoPoint(fountain.latitude, fountain.longitude))

                        val sizeInPx = (35 * context.resources.displayMetrics.density).toInt()

                        val realMarker = Marker(map).apply {
                            position = GeoPoint(fountain.latitude, fountain.longitude)
                            title = "Ubicación Real"
                            val bitmap = ContextCompat.getDrawable(context, R.drawable.icon_pin)
                                ?.toBitmap()?.scale(sizeInPx, sizeInPx, false)
                            icon = BitmapDrawable(context.resources, bitmap)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        map.overlays.add(realMarker)
                        map.invalidate()
                    }
                }
            } else {
                GameMapView(
                    fountain = fountain,
                    isFinished = true,
                    userGuessPos = userGuessPos,
                    onMarkerPlaced = { _, _ -> },
                    distance = distance
                )
            }
        }

        Card(
            modifier = Modifier.weight(0.55f).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Blanco),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (hasLost) {
                        Icon(
                            painter = painterResource(R.drawable.error_24px),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Rojo
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.game_result_lost_title),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = Rojo
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.game_result_lost_message),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = Color(0xFFFFE0E0),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                stringResource(R.string.game_result_lost_points),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Rojo
                            )
                        }
                    } else {
                        Text(
                            stringResource(R.string.game_result_won_title),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.game_result_your_score),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Text(
                            "$score ${stringResource(R.string.game_result_points)}",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF007BFF)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                stringResource(R.string.game_result_distance, distance),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1976D2)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.game_result_daily_message),
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
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
            Text(stringResource(R.string.game_loading_message), fontWeight = FontWeight.Bold, color = Color.Gray)
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
        onRetry?.let {
            Button(onClick = it, Modifier.fillMaxWidth().height(50.dp)) {
                Text(stringResource(R.string.game_error_retry_button))
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}