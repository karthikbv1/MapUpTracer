package com.karthik.mapuptracer.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.room.Room
import com.google.android.gms.location.*
import com.karthik.mapuptracer.MainActivity
import com.karthik.mapuptracer.R
import com.karthik.mapuptracer.data.AppDatabase
import com.karthik.mapuptracer.data.LocationEntity
import com.karthik.mapuptracer.data.SessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_TRACKING_STATUS = "ACTION_TRACKING_STATUS"
        const val EXTRA_IS_TRACKING = "EXTRA_IS_TRACKING"

        const val CHANNEL_ID = "tracking_channel"
        const val NOTIFICATION_ID = 101
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var db: AppDatabase
    private lateinit var prefs: SharedPreferences

    private var currentSessionId: Long = 0L
    private var isTracking = false

    override fun onCreate() {
        super.onCreate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "mapup_db"
        ).build()

        prefs = getSharedPreferences("tracker_prefs", MODE_PRIVATE)

        createNotificationChannel()
        setupLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                if (!isTracking) {
                    startForeground(NOTIFICATION_ID, createNotification())
                    startTracking()
                    Toast.makeText(this, "Tracking Started", Toast.LENGTH_SHORT).show()
                }
            }

            ACTION_STOP -> {
                stopTracking()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                Toast.makeText(this, "Tracking Stopped", Toast.LENGTH_SHORT).show()
            }

            else -> {
                if (!isTracking) {
                    startForeground(NOTIFICATION_ID, createNotification())
                    startTracking()
                }
            }
        }

        return START_STICKY
    }

    private fun startTracking() {
        isTracking = true
        prefs.edit().putBoolean("is_tracking", true).apply()
        sendTrackingStatus(true)

        currentSessionId = System.currentTimeMillis()

        CoroutineScope(Dispatchers.IO).launch {
            db.locationDao().insertSession(
                SessionEntity(
                    sessionId = currentSessionId,
                    startTime = System.currentTimeMillis(),
                    endTime = null
                )
            )
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        )
            .setMinUpdateIntervalMillis(3000L)
            .build()

        if (
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            mainLooper
        )
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    CoroutineScope(Dispatchers.IO).launch {
                        db.locationDao().insertLocation(
                            LocationEntity(
                                sessionId = currentSessionId,
                                latitude = location.latitude,
                                longitude = location.longitude,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
        }
    }

    private fun stopTracking() {
        if (!isTracking) return

        fusedLocationClient.removeLocationUpdates(locationCallback)

        CoroutineScope(Dispatchers.IO).launch {
            db.locationDao().updateSessionEndTime(
                currentSessionId,
                System.currentTimeMillis()
            )
        }

        isTracking = false
        prefs.edit().putBoolean("is_tracking", false).apply()
        sendTrackingStatus(false)
    }

    private fun sendTrackingStatus(status: Boolean) {
        val intent = Intent(ACTION_TRACKING_STATUS).apply {
            putExtra(EXTRA_IS_TRACKING, status)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTracking()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, LocationService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MapUpTracer")
            .setContentText("Tracking your location in background")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Stop Tracking", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Shows tracking status"

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}