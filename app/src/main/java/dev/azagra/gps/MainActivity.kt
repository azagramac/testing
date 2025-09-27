package dev.azagra.gps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var btnStartTracking: MaterialButton
    private lateinit var btnStopTracking: MaterialButton
    private lateinit var btnShowMap: MaterialButton
    private lateinit var btnExportGpx: MaterialButton

    private val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startTracking()
        } else {
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStartTracking = findViewById(R.id.btnStartTracking)
        btnStopTracking = findViewById(R.id.btnStopTracking)
        btnShowMap = findViewById(R.id.btnShowMap)
        btnExportGpx = findViewById(R.id.btnExportGpx)

        btnStartTracking.setOnClickListener {
            checkPermissionsAndStart()
        }

        btnStopTracking.setOnClickListener {
            stopTracking()
        }

        btnShowMap.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        btnExportGpx.setOnClickListener {
            exportGpx()
        }

        updateUi(false)
    }

    private fun checkPermissionsAndStart() {
        when {
            ContextCompat.checkSelfPermission(this, locationPermission) == PackageManager.PERMISSION_GRANTED -> {
                startTracking()
            }
            else -> {
                requestPermissionLauncher.launch(locationPermission)
            }
        }
    }

    private fun startTracking() {
        val intent = Intent(this, TrackingService::class.java)
        ContextCompat.startForegroundService(this, intent)
        updateUi(true)
    }

    private fun stopTracking() {
        val intent = Intent(this, TrackingService::class.java)
        stopService(intent)
        updateUi(false)
    }

    private fun updateUi(tracking: Boolean) {
        btnStartTracking.isEnabled = !tracking
        btnStopTracking.isEnabled = tracking
    }

    private fun exportGpx() {
        GpxExporter.exportToGpx(this)
    }
}
