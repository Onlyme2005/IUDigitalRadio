package com.iudigital.radio

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.ui.tooling.preview.Preview

private val Navy = Color(0xFF0B1020)
private val Purple = Color(0xFF7C5CFC)
private val Cyan = Color(0xFF35D6C7)
private val Panel = Color(0xFF151D35)

private data class RadioStation(
    val id: String,
    val name: String,
    val country: String,
    val language: String,
    val codec: String,
    val bitrate: Int,
    val streamUrl: String
)

private object RadioBrowserRepository {
    private const val ENDPOINT =
        "https://de1.api.radio-browser.info/json/stations/search" +
            "?language=spanish&limit=25&hidebroken=true&order=clickcount&reverse=true"

    val fallbackStations = listOf(
        RadioStation(
            id = "fallback-ser",
            name = "Cadena SER España",
            country = "España",
            language = "Español",
            codec = "MP3",
            bitrate = 128,
            streamUrl = "http://playerservices.streamtheworld.com/api/livestream-redirect/CADENASER.mp3"
        )
    )

    suspend fun loadStations(): List<RadioStation> = withContext(Dispatchers.IO) {
        val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 12_000
            readTimeout = 12_000
            setRequestProperty("User-Agent", "IUDigitalRadio/1.0 (Android student project)")
            setRequestProperty("Accept", "application/json")
        }
        try {
            if (connection.responseCode !in 200..299) error("HTTP ${connection.responseCode}")
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            parseStations(JSONArray(body))
        } finally {
            connection.disconnect()
        }
    }

    private fun parseStations(array: JSONArray): List<RadioStation> = buildList {
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val stream = item.optString("url_resolved").ifBlank { item.optString("url") }
            if (stream.isBlank()) continue
            add(
                RadioStation(
                    id = item.optString("stationuuid"),
                    name = item.optString("name").ifBlank { "Emisora sin nombre" },
                    country = item.optString("country").ifBlank { "Internacional" },
                    language = item.optString("language").ifBlank { "Español" },
                    codec = item.optString("codec").ifBlank { "Audio" },
                    bitrate = item.optInt("bitrate"),
                    streamUrl = stream
                )
            )
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { IUDigitalRadioApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IUDigitalRadioApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var stations by remember { mutableStateOf<List<RadioStation>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    var isMuted by rememberSaveable { mutableStateOf(false) }
    var selectedStationId by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val player = remember(context) { ExoPlayer.Builder(context).build() }
    val selectedStation = stations.firstOrNull { it.id == selectedStationId }

    fun vibrate() {
        val effect = VibrationEffect.createOneShot(42L, VibrationEffect.DEFAULT_AMPLITUDE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            (context.getSystemService(Vibrator::class.java))?.vibrate(effect)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap -> if (bitmap != null) profileBitmap = bitmap }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(null)
    }

    fun openCamera() {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) cameraLauncher.launch(null)
        else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    suspend fun refreshStations() {
        loading = true
        errorMessage = null
        runCatching { RadioBrowserRepository.loadStations() }
            .onSuccess { result ->
                stations = result
                if (selectedStationId.isBlank() && result.isNotEmpty()) {
                    selectedStationId = result.first().id
                }
            }
            .onFailure {
                stations = RadioBrowserRepository.fallbackStations
                selectedStationId = RadioBrowserRepository.fallbackStations.first().id
                errorMessage = "Catálogo temporal: se muestra una emisora de respaldo."
            }
        loading = false
    }

    LaunchedEffect(Unit) { refreshStations() }
    LaunchedEffect(selectedStation?.streamUrl) {
        selectedStation?.let { station ->
            player.setMediaItem(MediaItem.fromUri(station.streamUrl))
            player.prepare()
            player.volume = if (isMuted) 0f else 1f
            if (isPlaying) player.play() else player.pause()
        }
    }
    LaunchedEffect(isPlaying) { if (isPlaying) player.play() else player.pause() }
    LaunchedEffect(isMuted) { player.volume = if (isMuted) 0f else 1f }
    DisposableEffect(player) { onDispose { player.release() } }

    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme(
            primary = Purple,
            secondary = Cyan,
            background = Navy,
            surface = Panel
        )
    ) {
        Scaffold(
            containerColor = Navy,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("IU Digital Radio", fontWeight = FontWeight.Bold)
                            Text("Tu señal, siempre contigo", style = MaterialTheme.typography.labelSmall, color = Cyan)
                        }
                    },
                    navigationIcon = {
                        Icon(Icons.Default.Headphones, contentDescription = null, tint = Cyan)
                    },
                    actions = {
                        IconButton(onClick = { scope.launch { refreshStations() } }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Actualizar emisoras", tint = Cyan)
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileHeader(profileBitmap = profileBitmap, onCameraClick = ::openCamera)
                PlayerCard(
                    station = selectedStation,
                    isPlaying = isPlaying,
                    isMuted = isMuted,
                    onPlayPause = {
                        vibrate()
                        isPlaying = !isPlaying
                    },
                    onMute = {
                        vibrate()
                        isMuted = !isMuted
                    }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Emisoras destacadas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Datos en tiempo real · Radio Browser", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { /* La recarga se ejecuta desde el botón inferior */ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Cyan)
                    }
                }
                if (loading) {
                    Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Cyan)
                    }
                } else if (stations.isEmpty()) {
                    EmptyCatalog(errorMessage = errorMessage, onRetry = { scope.launch { refreshStations() } })
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(360.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(stations, key = { it.id }) { station ->
                            StationRow(
                                station = station,
                                selected = station.id == selectedStationId,
                                onClick = {
                                    selectedStationId = station.id
                                    isPlaying = true
                                    vibrate()
                                }
                            )
                        }
                    }
                }
                if (errorMessage != null && stations.isNotEmpty()) {
                    Text(errorMessage!!, color = Color(0xFFFFB4AB), style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "Fuente: Radio Browser · Las emisoras son servicios de terceros y su disponibilidad puede variar.",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}



@Composable
private fun ProfileHeader(profileBitmap: Bitmap?, onCameraClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape).background(Purple.copy(alpha = .25f)),
                contentAlignment = Alignment.Center
            ) {
                if (profileBitmap != null) {
                    Image(profileBitmap.asImageBitmap(), contentDescription = "Foto de perfil", modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = Cyan, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Mi perfil de escucha", fontWeight = FontWeight.Bold)
                Text("Personaliza tu experiencia", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = onCameraClick) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Foto")
            }
        }
    }
}

@Composable
private fun PlayerCard(
    station: RadioStation?,
    isPlaying: Boolean,
    isMuted: Boolean,
    onPlayPause: () -> Unit,
    onMute: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Purple),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("AHORA SONANDO", color = Color.White.copy(alpha = .75f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Cyan)
            }
            Text(station?.name ?: "Selecciona una emisora", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                station?.let { "${it.country} · ${it.codec}${if (it.bitrate > 0) " · ${it.bitrate} kbps" else ""}" } ?: "Catálogo cargando...",
                color = Color.White.copy(alpha = .78f)
            )
            Divider(color = Color.White.copy(alpha = .18f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMute, modifier = Modifier.size(52.dp)) {
                    Icon(if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp, contentDescription = "Silenciar", tint = Color.White)
                }
                IconButton(onClick = onPlayPause, modifier = Modifier.size(68.dp).background(Color.White, CircleShape)) {
                    Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = if (isPlaying) "Pausar" else "Reproducir", tint = Purple, modifier = Modifier.size(36.dp))
                }
                Spacer(Modifier.width(52.dp))
            }
            Text(if (isPlaying) "Reproduciendo en vivo" else "Listo para reproducir", color = Color.White.copy(alpha = .82f), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun StationRow(station: RadioStation, selected: Boolean, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (selected) Purple.copy(alpha = .28f) else Panel),
        border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, Cyan) else null,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(if (selected) Cyan.copy(alpha = .2f) else Color.White.copy(alpha = .08f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = if (selected) Cyan else Color.LightGray)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(station.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${station.country} · ${station.language}", color = Color.LightGray, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
            if (selected) Text("EN VIVO", color = Cyan, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptyCatalog(errorMessage: String?, onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(errorMessage ?: "No hay emisoras disponibles", color = Color.LightGray)
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onRetry) { Text("Reintentar") }
    }
}
