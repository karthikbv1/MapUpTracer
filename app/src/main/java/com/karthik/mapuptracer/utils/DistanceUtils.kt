package com.karthik.mapuptracer.utils

import android.location.Location
import com.karthik.mapuptracer.data.LocationEntity

object DistanceUtils {

    fun calculateTotalDistance(locations: List<LocationEntity>): Float {
        if (locations.size < 2) return 0f

        var totalDistance = 0f

        for (i in 0 until locations.size - 1) {
            val start = Location("start").apply {
                latitude = locations[i].latitude
                longitude = locations[i].longitude
            }

            val end = Location("end").apply {
                latitude = locations[i + 1].latitude
                longitude = locations[i + 1].longitude
            }

            totalDistance += start.distanceTo(end)
        }

        return totalDistance / 1000f // convert meters to KM
    }
}