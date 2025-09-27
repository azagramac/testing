package dev.azagra.gps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.GnssStatus
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import kotlin.math.roundToInt
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var compassView: CompassView
    private lateinit var snrGraph: SnrGraphView
    private lateinit var tvCoordinates: MaterialTextView
    private lateinit var tvAltitude: MaterialTextView
    private lateinit var tvSatellitesCount: MaterialTextView
    private lateinit var tvUtcTime: MaterialTextView
    private lateinit var btnShareLocation: MaterialButton
    private lateinit var locationManager: LocationManager

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startLocationUpdates()
                startGnssUpdates()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        compassView = findViewById(R.id.compassView)
        snrGraph = findViewById(R.id.snrGraph)
        tvCoordinates = findViewById(R.id.tvCoordinates)
        tvAltitude = findViewById(R.id.tvAltitude)
        tvSatellitesCount = findViewById(R.id.tvSatellitesCount)
        tvUtcTime = findViewById(R.id.tvUtcTime)
        btnShareLocation = findViewById(R.id.btnShareLocation)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        btnShareLocation.setOnClickListener { shareLocation() }

        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                startLocationUpdates()
                startGnssUpdates()
            }
            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun startLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                0f
            ) { location ->
                updateLocationUI(location)
            }
        } catch (ex: SecurityException) {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startGnssUpdates() {
        try {
            locationManager.registerGnssStatusCallback(object : GnssStatus.Callback() {
                override fun onSatelliteStatusChanged(status: GnssStatus) {
                    val snrList = mutableListOf<Float>()
                    val satellitesPositions = mutableListOf<Pair<Float, Float>>()
                    var visibleSatellites = 0
                    for (i in 0 until status.satelliteCount) {
                        if (status.usedInFix(i)) {
                            val snr = status.getCn0DbHz(i)
                            snrList.add(snr)
                            satellitesPositions.add(Pair(status.getAzimuthDegrees(i), snr))
                            visibleSatellites++
                        }
                    }
                    snrGraph.updateSnrData(snrList)
                    tvSatellitesCount.text = formatBoldLabel("Satélites", "$visibleSatellites")
                    compassView.updateSatellites(satellitesPositions)
                }
            }, null)
        } catch (ex: SecurityException) {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLocationUI(location: Location) {
        tvCoordinates.text = formatBoldLabel(
            "Coordenadas",
            "%.6f, %.6f".format(location.latitude, location.longitude)
        )
        tvAltitude.text = formatBoldLabel("Altitud", "%.1f m".format(location.altitude))
        tvUtcTime.text = formatBoldLabel(
            "Hora UTC",
            SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        )
    }

    private fun formatBoldLabel(label: String, value: String): SpannableString {
        val fullText = "$label\n$value"
        val spannable = SpannableString(fullText)
        spannable.setSpan(StyleSpan(Typeface.BOLD), 0, label.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }

    private fun shareLocation() {
        val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (lastLocation != null) {
            val latDMS = decimalToDMS(lastLocation.latitude, true)
            val lonDMS = decimalToDMS(lastLocation.longitude, false)
            val url = "https://www.google.com/maps/place/${URLEncoder.encode("$latDMS $lonDMS", "UTF-8")}"

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, url)
            }
            startActivity(Intent.createChooser(intent, "Compartir ubicación"))
        } else {
            Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
        }
    }

    private fun decimalToDMS(coordinate: Double, isLatitude: Boolean): String {
        val absolute = Math.abs(coordinate)
        val degrees = absolute.toInt()
        val minutesFull = (absolute - degrees) * 60
        val minutes = minutesFull.toInt()
        val seconds = ((minutesFull - minutes) * 60).roundToInt()
        val direction = when {
            isLatitude && coordinate >= 0 -> "N"
            isLatitude && coordinate < 0 -> "S"
            !isLatitude && coordinate >= 0 -> "E"
            else -> "W"
        }
        return "%d°%02d'%02d\"%s".format(degrees, minutes, seconds, direction)
    }
}
