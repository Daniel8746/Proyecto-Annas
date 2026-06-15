package com.annas.ui.view

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.annas.data.sensor.ShakeDetector
import com.annas.ui.features.chatbot.ChatbotOverlay
import com.annas.ui.navigation.AnnasNavHost
import com.annas.ui.theme.AnnasTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.collections.immutable.persistentListOf
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(), SensorEventListener {
    @Inject
    lateinit var shakeDetector: ShakeDetector

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        enableEdgeToEdge()

        setContent {
            AnnasTheme {
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { _ -> }

                LaunchedEffect(Unit) {
                    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        persistentListOf(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        persistentListOf()
                    }
                    val toRequest = permissions.filter {
                        ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            it
                        ) != PackageManager.PERMISSION_GRANTED
                    }
                    if (toRequest.isNotEmpty()) {
                        permissionLauncher.launch(toRequest.toTypedArray())
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    ChatbotOverlay {
                        AnnasNavHost(shakeDetector = shakeDetector)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        shakeDetector.resetShakeGesture()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            shakeDetector.processSensorData(event.values)
        }
    }
}
