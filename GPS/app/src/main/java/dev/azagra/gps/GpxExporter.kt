package dev.azagra.gps

import android.content.Context
import android.location.Location
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object GpxExporter {

    // Para esta demo guardamos en almacenamiento privado externo (sin pedir permiso adicional)
    fun exportToGpx(context: Context) {
        // TODO: en producción, deberías pasar las ubicaciones reales guardadas del tracking
        val dummyLocations = listOf(
            Location("gps").apply {
                latitude = 40.4168
                longitude = -3.7038
                time = System.currentTimeMillis()
            },
            Location("gps").apply {
                latitude = 40.4178
                longitude = -3.7048
                time = System.currentTimeMillis() + 60000
            }
        )

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        val gpxHeader = """
            <?xml version="1.0" encoding="UTF-8"?>
            <gpx version="1.1" creator="GPS - Jose Luis Azagra Lazcano" xmlns="http://www.topografix.com/GPX/1/1">
            <trk><name>Tracking GPS</name><trkseg>
        """.trimIndent()

        val gpxFooter = """
            </trkseg></trk>
            </gpx>
        """.trimIndent()

        val gpxPoints = dummyLocations.joinToString("\n") { loc ->
            "    <trkpt lat=\"${loc.latitude}\" lon=\"${loc.longitude}\"><time>${sdf.format(Date(loc.time))}</time></trkpt>"
        }

        val gpxContent = gpxHeader + "\n" + gpxPoints + "\n" + gpxFooter

        try {
            val file = File(context.getExternalFilesDir(null), "tracking_${System.currentTimeMillis()}.gpx")
            FileOutputStream(file).use { it.write(gpxContent.toByteArray()) }
            Toast.makeText(context, "GPX exportado a: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error exportando GPX: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
