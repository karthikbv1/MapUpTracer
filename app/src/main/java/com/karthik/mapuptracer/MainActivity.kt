package com.karthik.mapuptracer

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.room.Room
import com.karthik.mapuptracer.data.AppDatabase
import com.karthik.mapuptracer.data.SessionEntity
import com.karthik.mapuptracer.service.LocationService
import com.karthik.mapuptracer.ui.theme.MapUpTracerTheme
import com.karthik.mapuptracer.utils.DistanceUtils
import com.karthik.mapuptracer.utils.ExportUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class MainActivity : ComponentActivity() {

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestPermissionsIfNeeded()

        setContent {
            val themePrefs = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

            var isDark by remember {
                mutableStateOf(themePrefs.getBoolean("dark_mode", false))
            }

            MapUpTracerTheme(darkTheme = isDark) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TrackerScreen(
                        modifier = Modifier.padding(innerPadding),
                        onStartTracking = {
                            val intent = Intent(this, LocationService::class.java).apply {
                                action = LocationService.ACTION_START
                            }
                            ContextCompat.startForegroundService(this, intent)
                        },
                        onStopTracking = {
                            val intent = Intent(this, LocationService::class.java).apply {
                                action = LocationService.ACTION_STOP
                            }
                            ContextCompat.startForegroundService(this, intent)
                        },
                        isDark = isDark,
                        onToggleTheme = {
                            isDark = it
                            themePrefs.edit().putBoolean("dark_mode", it).apply()
                        }
                    )
                }
            }
        }
    }

    private fun requestPermissionsIfNeeded() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        locationPermissionLauncher.launch(permissions.toTypedArray())
    }
}

@Composable
fun TrackerScreen(
    modifier: Modifier = Modifier,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    isDark: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("tracker_prefs", Context.MODE_PRIVATE)

    var isTracking by remember {
        mutableStateOf(prefs.getBoolean("is_tracking", false))
    }

    var sessions by remember { mutableStateOf(listOf<SessionEntity>()) }
    val scope = rememberCoroutineScope()

    suspend fun loadSessions() {
        sessions = withContext(Dispatchers.IO) {
            val db = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "mapup_db"
            ).build()

            db.locationDao().getAllSessions()
        }
    }

    suspend fun openSessionRoute(sessionId: Long) {
        val locations = withContext(Dispatchers.IO) {
            val db = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "mapup_db"
            ).build()

            db.locationDao().getLocationsForSession(sessionId)
        }

        if (locations.size >= 2) {
            val first = locations.first()
            val last = locations.last()

            val uri = "https://www.google.com/maps/dir/?api=1" +
                    "&origin=${first.latitude},${first.longitude}" +
                    "&destination=${last.latitude},${last.longitude}" +
                    "&travelmode=walking"

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            context.startActivity(intent)

        } else if (locations.size == 1) {
            val point = locations.first()
            val uri = Uri.parse(
                "geo:${point.latitude},${point.longitude}?q=${point.latitude},${point.longitude}(Tracked Location)"
            )
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)

        } else {
            Toast.makeText(context, "No route points found for this session", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == LocationService.ACTION_TRACKING_STATUS) {
                    isTracking = intent.getBooleanExtra(LocationService.EXTRA_IS_TRACKING, false)
                }
            }
        }

        LocalBroadcastManager.getInstance(context).registerReceiver(
            receiver,
            IntentFilter(LocationService.ACTION_TRACKING_STATUS)
        )

        onDispose {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
        }
    }

    LaunchedEffect(Unit) {
        loadSessions()
        isTracking = prefs.getBoolean("is_tracking", false)
    }

    val gradient = Brush.linearGradient(
        colors = if (isDark) {
            listOf(Color(0xFF7F5AF0), Color(0xFF2CB67D))
        } else {
            listOf(Color(0xFF6C63FF), Color(0xFF4CAF50))
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(gradient)
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "MapUpTracer",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isTracking) "🟢 Tracking Active" else "⚪ Not Tracking",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isDark) "☀️ Light Mode" else "🌙 Dark Mode",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Switch(
                            checked = isDark,
                            onCheckedChange = { onToggleTheme(it) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                isTracking = true
                prefs.edit().putBoolean("is_tracking", true).apply()
                onStartTracking()
                Toast.makeText(context, "Tracking Started", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("▶ Start Tracking")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                isTracking = false
                prefs.edit().putBoolean("is_tracking", false).apply()
                onStopTracking()
                Toast.makeText(context, "Tracking Stopped", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Text("⏹ Stop Tracking")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                scope.launch {
                    isTracking = prefs.getBoolean("is_tracking", false)
                    loadSessions()
                    Toast.makeText(context, "Sessions refreshed", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("🔄 Refresh Sessions")
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Session History",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(14.dp))

        sessions.forEach { session ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    SessionItemContent(session)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                Toast.makeText(context, "Opening map...", Toast.LENGTH_SHORT).show()
                                scope.launch {
                                    openSessionRoute(session.sessionId)
                                }
                            }
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🗺 Open Route on Map",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                val db = Room.databaseBuilder(
                                    context,
                                    AppDatabase::class.java,
                                    "mapup_db"
                                ).build()

                                val locations = db.locationDao()
                                    .getLocationsForSession(session.sessionId)

                                val path = ExportUtils.exportToCSV(
                                    context,
                                    session.sessionId,
                                    locations
                                )

                                Toast.makeText(context, "CSV Saved:\n$path", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("⬇ Export CSV")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    LaunchedEffect(isTracking) {
        if (!isTracking) {
            delay(1000)
            loadSessions()
        }
    }
}

@Composable
fun SessionItemContent(session: SessionEntity) {
    val context = LocalContext.current
    var pointCount by remember { mutableStateOf(0) }
    var distanceKm by remember { mutableStateOf(0f) }

    LaunchedEffect(session.sessionId) {
        val db = withContext(Dispatchers.IO) {
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "mapup_db"
            ).build()
        }

        val locations = withContext(Dispatchers.IO) {
            db.locationDao().getLocationsForSession(session.sessionId)
        }

        pointCount = locations.size
        distanceKm = DistanceUtils.calculateTotalDistance(locations)
    }

    val durationMillis = (session.endTime ?: System.currentTimeMillis()) - session.startTime
    val durationSeconds = durationMillis / 1000
    val durationMinutes = durationSeconds / 60
    val remainingSeconds = durationSeconds % 60

    val mode = estimateTravelMode(distanceKm, durationSeconds)
    val aiInsight = generateAiInsight(distanceKm, durationMinutes, pointCount)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Session #${session.sessionId}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text("📍 Start: ${Date(session.startTime)}")
        Text("🏁 End: ${session.endTime?.let { Date(it) } ?: "Ongoing"}")
        Text("🛰️ Points Captured: $pointCount")
        Text("📏 Distance: %.2f km".format(distanceKm))
        Text("⏱️ Duration: ${durationMinutes} min ${remainingSeconds} sec")

        Text(
            text = "🧭 Travel Mode: $mode",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "🤖 AI Route Insight",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = aiInsight,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

fun estimateTravelMode(distanceKm: Float, durationSeconds: Long): String {
    if (durationSeconds == 0L) return "Unknown"

    if (distanceKm < 0.01f) return "🧍 Stationary"

    val hours = durationSeconds / 3600.0
    val speed = distanceKm / hours

    return when {
        speed < 4 -> "🚶 Walking"
        speed < 10 -> "🏃 Running"
        else -> "🚗 Vehicle"
    }
}

fun generateAiInsight(distanceKm: Float, durationMinutes: Long, pointCount: Int): String {
    val activityType = when {
        distanceKm < 0.5 -> "Minimal movement"
        distanceKm < 3 -> "Walking"
        distanceKm < 10 -> "Jogging / Local Travel"
        else -> "Long Travel"
    }

    val movementStyle = when {
        pointCount < 5 -> "very limited route data"
        pointCount < 20 -> "short and consistent movement"
        pointCount < 50 -> "moderate route activity"
        else -> "dense tracking with detailed route coverage"
    }

    return "This session covered %.2f km in %d min. Likely activity: %s. Movement looks %s."
        .format(distanceKm, durationMinutes, activityType, movementStyle)
}