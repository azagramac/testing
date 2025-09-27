package dev.azagra.gps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

class MainActivity : AppCompatActivity() {

    private lateinit var compassView: CompassView
    private lateinit var snrGraph: SnrGraphView
    private lateinit var tvCoordinates: MaterialTextView
    private lateinit var tvAltitude: MaterialTextView
    private lateinit var tvSatellitesCount: MaterialTextView
    private lateinit var tvUtcTime: MaterialTextView
    private lateinit var btnShareLocation: MaterialButton

    private lateinit var locationManager: LocationManager

    private val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startGpsTracking()
        else Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar vistas
        compassView = findViewById(R.id.compassView)
        snrGraph = findViewById(R.id.snrGraph)
        tvCoordinates = findViewById(R.id.tvCoordinates)
        tvAltitude = findViewById(R.id.tvAltitude)
        tvSatellitesCount = findViewById(R.id.tvSatellitesCount)
        tvUtcTime = findViewById(R.id.tvUtcTime)
        btnShareLocation = findViewById(R.id.btnShareLocation)

        btnShareLocation.setOnClickListener { shareLocation() }

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        // Pedir permisos y empezar tracking automáticamente
        checkPermissionsAndStart()
    }

    private fun checkPermissionsAndStart() {
        when {
            ContextCompat.checkSelfPermission(this, locationPermission) == PackageManager.PERMISSION_GRANTED ->
                startGpsTracking()
            else -> requestPermissionLauncher.launch(locationPermission)
        }
    }

    private fun startGpsTracking() {
        // Registrar callback GNSS para satélites y SNR
        locationManager.registerGnssStatusCallback(object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                val snrValues = mutableListOf<Float>()
                for (i in 0 until status.satelliteCount) {
                    snrValues.add(status.getCn0DbHz(i))
                }
                val satellites = status.satelliteCount
                // Aquí podrías obtener bearing aproximado (si quieres brújula orientada al norte)
                val bearing = 0f
                runOnUiThread {
                    snrGraph.updateData(snrValues)
                    compassView.updateCompass(bearing, satellites)
                    tvSatellitesCount.text = getString(R.string.satellites) + satellites
                }
            }
        })

        // Registrar listener de ubicación para coords, altitud y UTC
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000L,
            0f
        ) { location: Location ->
            updateLocationData(location)
        }
    }

    private fun updateLocationData(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        val altitude = location.altitude
        val utcTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(location.time)

        runOnUiThread {
            tvCoordinates.text =
                getString(R.string.coordinates) + "%.6f, %.6f".format(latitude, longitude)
            tvAltitude.text = getString(R.string.altitude) + "%.1f m".format(altitude)
            tvUtcTime.text = getString(R.string.utc_time) + utcTime
        }
    }

    private fun shareLocation() {
        val coords = tvCoordinates.text.toString()
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, coords)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_location)))
    }
}
