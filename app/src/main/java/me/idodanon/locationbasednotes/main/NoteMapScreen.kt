package me.idodanon.locationbasednotes.main

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.osmdroid.views.MapView
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import androidx.compose.ui.viewinterop.AndroidView
import me.idodanon.locationbasednotes.data.Note
import android.location.Geocoder
import android.location.Address
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun NotesMapScreen(
    notes: List<Note>,
    onNoteClick: (String) -> Unit
) {
    val context = LocalContext.current

    AndroidView(factory = {
        Configuration.getInstance().load(context, context.getSharedPreferences("osm", 0))
        val map = MapView(context)
        map.setMultiTouchControls(true)
        map
    }, update = { map ->
        map.overlays.clear()

        notes.forEachIndexed { index, note ->
            getGeoPointFromLocation(context, note.location) { geoPoint ->
                geoPoint?.let {
                    val offsetLat = it.latitude + index * 0.001
                    val offsetLon = it.longitude + index * 0.001
                    val marker = Marker(map)
                    marker.position = GeoPoint(offsetLat, offsetLon)
                    map.controller.setCenter(GeoPoint(offsetLat, offsetLon))
                    map.controller.setZoom(14.0)
                    marker.title = note.title

                    marker.setOnMarkerClickListener { _, _ ->
                        onNoteClick(note.id)
                        true
                    }

                    map.overlays.add(marker)

                    if (index == 0) {
                        map.controller.setCenter(GeoPoint(offsetLat, offsetLon))
                        map.controller.setZoom(6.0)
                    }

                    map.invalidate()
                }
            }
        }
    })
}



@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun getGeoPointFromLocation(
    context: Context,
    locationName: String?,
    onResult: (GeoPoint?) -> Unit
) {
    if (locationName.isNullOrBlank()) {
        onResult(null)
        return
    }

    try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val mainHandler = Handler(Looper.getMainLooper())

        geocoder.getFromLocationName(
            locationName,
            1,
            object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    val geoPoint = addresses.firstOrNull()?.let {
                        GeoPoint(it.latitude, it.longitude)
                    }
                    mainHandler.post { onResult(geoPoint) }
                }

                override fun onError(errorMessage: String?) {
                    mainHandler.post { onResult(null) }
                }
            }
        )
    } catch (e: Exception) {
        e.printStackTrace()
        onResult(null)
    }
}


