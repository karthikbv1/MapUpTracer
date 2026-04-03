package com.karthik.mapuptracer.utils

import android.content.Context
import com.karthik.mapuptracer.data.LocationEntity
import java.io.File
import java.io.FileWriter

object ExportUtils {

    fun exportToCSV(context: Context, sessionId: Long, locations: List<LocationEntity>): String {
        val fileName = "session_$sessionId.csv"
        val file = File(context.getExternalFilesDir(null), fileName)

        val writer = FileWriter(file)

        // Header
        writer.append("Latitude,Longitude,Timestamp\n")

        // Data
        for (loc in locations) {
            writer.append("${loc.latitude},${loc.longitude},${loc.timestamp}\n")
        }

        writer.flush()
        writer.close()

        return file.absolutePath
    }
}