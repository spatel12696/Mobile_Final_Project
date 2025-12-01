package com.Project.Project

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

class SensorActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var lastUpdate: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    private lateinit var dbHelper: EventDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor)

        // Initialize database and sensor
        dbHelper = EventDatabaseHelper(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Register accelerometer listener
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI
        )

        Toast.makeText(this, "Shake your device to undo the last saved event!", Toast.LENGTH_SHORT).show()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // Simple shake detector that triggers an undo dialog
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val currentTime = System.currentTimeMillis()
            // Detect every 150 ms
            if ((currentTime - lastUpdate) > 150) {
                val diffTime = currentTime - lastUpdate
                lastUpdate = currentTime

                // Calculate motion speed
                val speed = kotlin.math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000

                // 🔹 Lowered threshold for easier testing in emulator (600–800)
                if (speed > 700) {
                    showUndoDialog()
                }

                lastX = x
                lastY = y
                lastZ = z
            }
        }
    }

    // 🟡 Dialog to undo last saved event
    private fun showUndoDialog() {
        dbHelper.getSavedEvents(
            onResult = { savedEvents ->
                if (savedEvents.isNotEmpty()) {
                    val lastEvent = savedEvents.last()
                    AlertDialog.Builder(this)
                        .setTitle("Undo Last Saved Event?")
                        .setMessage("Do you want to remove '${lastEvent.name}' from saved events?")
                        .setPositiveButton("Yes") { _, _ ->
                            dbHelper.deleteSavedEventByName(lastEvent.name)
                            Toast.makeText(this, "Removed last saved event.", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("No", null)
                        .show()
                } else {
                    Toast.makeText(this, "No saved events to undo.", Toast.LENGTH_SHORT).show()
                }
            },
            onError = {
                Toast.makeText(this, "Couldn't load saved events.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI
        )
    }
}
