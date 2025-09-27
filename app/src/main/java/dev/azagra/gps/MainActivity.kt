package dev.azagra.gps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var compassView: CompassView
    private lateinit var snrGraph: SnrGraphView
    private lateinit var tvCoordinates: MaterialTextView
    private lateinit var tvAltitude: MaterialTextView
    private lateinit var tvSatellitesCount: MaterialTextView
    private lateinit var tvUtcTime: MaterialTextView
    private lateinit var btnShareLocation: MaterialButton

    private lateinit var locationManager: LocationManager
    private lateinit var sensorManager: SensorManager
    private var accelerometerReading = FloatArray(3)
    private var magnetometerReading = FloatArray(3)
    private var rotationMatrix = FloatArray(9)
    private var orientationAngles = FloatArray(3)
    private var lastAzimuth = 0f

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) startLocationUpdates()
        else Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
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
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        btnShareLocation.setOnClickListener { shareLocation() }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            startLocationUpdates()
        }

        startGnssUpdates()
        startSensorUpdates()
    }

    private fun formatBoldLabel(label: String, value: String) = "$label\n$value"

    private fun updateLocationUI(location: Location) {
        tvCoordinates.text = formatBoldLabel("Coordenadas", "%.6f, %.6f".format(location.latitude, location.longitude))
        tvAltitude.text = formatBoldLabel("Altitud", "%.1f m".format(location.altitude))
        tvUtcTime.text = formatBoldLabel("UTC", SimpleDateFormat("HH:mm:ss", Locale.US).format(Date()))
    }

    private fun startLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f) {
                updateLocationUI(it)
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
                        if (status.usedInFix(i)) visibleSatellites++
                        snrList.add(status.getCn0DbHz(i))
                        satellitesPositions.add(Pair(i * 360f / status.satelliteCount, status.getCn0DbHz(i)))
                    }
                    tvSatellitesCount.text = formatBoldLabel("Satélites", visibleSatellites.toString())
                    snrGraph.updateSnrData(snrList)
                    compassView.updateSatellites(satellitesPositions)
                }
            }, null)
        } catch (ex: SecurityException) {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startSensorUpdates() {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> accelerometerReading = event.values.clone()
            Sensor.TYPE_MAGNETIC_FIELD -> magnetometerReading = event.values.clone()
        }

        if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            compassView.updateOrientation(azimuth)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun shareLocation() {
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: return
        val uri = "geo:${location.latitude},${location.longitude}?q=${URLEncoder.encode("Mi ubicación", "utf-8")}"
        val intent = Intent(Intent.ACTION_VIEW).apply { data = android.net.Uri.parse(uri) }
        startActivity(intent)
    }
}
