package dev.azagra.gps

import android.content.Intent
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.button.MaterialButton
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

        // Tamaño texto ya ajustado en XML, centrado en tarjeta

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        btnShareLocation.setOnClickListener { shareLocation() }

        startLocationUpdates()
        startGnssUpdates()
        startSensorUpdates()
    }

    private fun formatBoldLabel(label: String, value: String): SpannableString {
        val fullText = "$label\n$value"
        val spannable = SpannableString(fullText)
        spannable.setSpan(StyleSpan(Typeface.BOLD), 0, label.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }

    private fun updateLocationUI(location: Location) {
        tvCoordinates.text = formatBoldLabel(getString(R.string.coordinates), "%.6f, %.6f".format(location.latitude, location.longitude))
        tvAltitude.text = formatBoldLabel(getString(R.string.altitude), "%.1f m".format(location.altitude))
        tvUtcTime.text = formatBoldLabel(getString(R.string.utc_time), SimpleDateFormat("HH:mm:ss", Locale.US).format(Date()))
    }

    private fun startLocationUpdates() {
        try {
            val provider = LocationManager.GPS_PROVIDER
            locationManager.requestLocationUpdates(provider, 1000L, 0f) { location ->
                updateLocationUI(location)
            }
        } catch (ex: SecurityException) {
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
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
                    tvSatellitesCount.text = formatBoldLabel(getString(R.string.satellites), "$visibleSatellites")
                    compassView.updateSatellites(satellitesPositions)
                }
            }, null)
        } catch (ex: SecurityException) {
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    private fun startSensorUpdates() {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> accelerometerReading = event.values.clone()
            Sensor.TYPE_MAGNETIC_FIELD -> magnetometerReading = event.values.clone()
        }
        if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            val smoothAzimuth = lastAzimuth + (azimuth - lastAzimuth) * 0.1f
            compassView.updateOrientation(smoothAzimuth)
            lastAzimuth = smoothAzimuth
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        locationManager.removeUpdates { }
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
            startActivity(Intent.createChooser(intent, getString(R.string.share_location)))
        } else {
            Toast.makeText(this, getString(R.string.location_not_available), Toast.LENGTH_SHORT).show()
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
