package com.example.fitnessmonitor.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class StepTrackerManager(
    context: Context,
    private val onStepCountChanged: (Int) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastAccel: Float = 0f
    private var lastAccelCurrent: Float = SensorManager.GRAVITY_EARTH
    private var lastAccelK: Float = 0f
    private var stepCount = 0
    private var lastStepTime = 0L

    val isSensorAvailable: Boolean
        get() = accelerometer != null

    fun startListening() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    fun addManualStep(count: Int = 1) {
        stepCount += count
        onStepCountChanged(stepCount)
    }

    fun setInitialSteps(steps: Int) {
        stepCount = steps
        onStepCountChanged(stepCount)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            lastAccel = lastAccelCurrent
            lastAccelCurrent = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta = lastAccelCurrent - lastAccel
            lastAccelK = lastAccelK * 0.9f + delta * 0.1f // low pass filter

            // Peak detection for step
            if (lastAccelK > 2.2f) { // Threshold for step movement
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastStepTime > 300) { // debounce 300ms
                    stepCount++
                    lastStepTime = currentTime
                    onStepCountChanged(stepCount)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }
}
