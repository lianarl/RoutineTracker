package si.uni_lj.fri.pbd.routinetracker.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

// https://medium.com/@charles-raj/comprehensive-guide-to-implementing-sensors-in-android-studio-with-kotlin-b167b5397ce4
// https://blog.stackademic.com/master-android-sensors-with-kotlin-guide-best-practices-examples-e5f6af44b769

class LightSensorReader(context: Context) {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    var light: Float? = null

    suspend fun readOnce(): Float {

        // reset value, so we dont get old value
        light = null

        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {

                // get light value (in lux)
                light = event.values[0]

                // unregister the sensor (unsubscribe)
                sensorManager.unregisterListener(this)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        // register the sensor (subscribe)
        sensorManager.registerListener(sensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)

        // wait until we get value back -> i guess since i always get the info from sensor, this is okay
        while (light == null) {
            kotlinx.coroutines.delay(500)
        }
        return light!!
    }
}
