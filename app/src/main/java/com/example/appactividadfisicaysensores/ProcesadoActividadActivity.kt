package com.example.appactividadfisicaysensores

import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.math.abs
import kotlin.math.sqrt


class ProcesadoActividadActivity : AppCompatActivity(), SensorEventListener, LocationListener {

    private var timer: CountDownTimer? = null
    private var remainingMs: Long = 0L


    //variables para el sensor:
    private lateinit var rootLayout: View
    private lateinit var tvMovimiento: TextView

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var lastAcceleration = 0f
    private var currentAcceleration = 0f
    private var shakeIntensity = 0f


    //Variables del gps:
    private lateinit var tvDistancia: TextView
    private lateinit var locationManager: LocationManager

    private val locationRequestCode = 2001

    private var lastLocation: Location? = null
    private var totalMeters = 0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_procesado_deporte)

        //var que cambia de color el fono
        rootLayout = findViewById(R.id.rootLayout)
        tvMovimiento = findViewById(R.id.tvMovimiento)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH

        // var para el gps:
        tvDistancia = findViewById(R.id.tvDistancia)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        checkLocationPermissionAndStart()


        val tvTime = findViewById<TextView>(R.id.tvTime)
        val btnFinalizar = findViewById<Button>(R.id.btnFinalizar)
        val tvActividad = findViewById<TextView>(R.id.tvActividad)
        val ivIcon = findViewById<ImageView>(R.id.ivIcon)

        val tipo = intent.getStringExtra("TIPO") ?: "Caminar"
        tvActividad.text = tipo

        val minutos = intent.getIntExtra("MINUTOS", 0)
        remainingMs = minutos * 60_000L

        val iconRes = intent.getIntExtra("ICON_RES", R.drawable.ic_default)
        ivIcon.setImageResource(iconRes)
        // Pinta el tiempo inicial
        tvTime.text = formatMs(remainingMs)

        // Inicia cuenta atrás
        timer = object : CountDownTimer(remainingMs, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMs = millisUntilFinished
                tvTime.text = formatMs(millisUntilFinished)
            }

            override fun onFinish() {
                remainingMs = 0L
                tvTime.text = "00:00"

                val mp =
                    MediaPlayer.create(this@ProcesadoActividadActivity, R.raw.soundaftertraining)
                mp.setOnCompletionListener { it.release() }
                mp.start()
                Toast.makeText(
                    this@ProcesadoActividadActivity,
                    "FIN DEL TIMER",
                    Toast.LENGTH_SHORT
                ).show()

                // Aquí puedes: parar sensores/GPS y mostrar "Sesión terminada"
            }
        }.start()

        btnFinalizar.setOnClickListener {
            timer?.cancel()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        locationManager.removeUpdates(this)
    }

    private fun formatMs(ms: Long): String {
        val totalSec = (ms / 1000).toInt()
        val min = totalSec / 60
        val sec = totalSec % 60
        return String.format("%02d:%02d", min, sec)
    }

    // sensor de acelerometro para cambiar luego el color

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val acceleration = sqrt(x * x + y * y + z * z)

        lastAcceleration = currentAcceleration
        currentAcceleration = acceleration

        val delta = currentAcceleration - lastAcceleration
        shakeIntensity = abs(delta)

        // Umbrales (ajústalos si quieres)
        val (texto, color) = when {
            shakeIntensity < 0.6f -> "Sin movimiento" to 0xFFB9F6CA.toInt() // verde suave
            shakeIntensity < 2.0f -> "Movimiento suave" to 0xFFFFF59D.toInt() // amarillo
            else -> "Movimiento intenso" to 0xFFFF8A80.toInt() // rojo
        }

        tvMovimiento.text = texto
        rootLayout.setBackgroundColor(color)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }


    // comporbar permisos para el gps:
    private fun checkLocationPermissionAndStart() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                locationRequestCode
            )
        } else {
            startLocationUpdates()
        }
    }


    //empieza localizacion
    @Suppress("MissingPermission")
    private fun startLocationUpdates() {
        Toast.makeText(this, "GPS activado", Toast.LENGTH_SHORT).show()

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            2000L,   // cada 2s
            1f,      // o cada 1 metro
            this
        )
    }

    override fun onLocationChanged(location: Location) {
        Toast.makeText(this, "LOC recibida", Toast.LENGTH_SHORT).show()

        if (lastLocation != null) {
            val meters = lastLocation!!.distanceTo(location)
            totalMeters += meters
        }

        lastLocation = location

        val km = totalMeters / 1000f
        tvDistancia.text = String.format("Distancia: %.3f km", km)
    }
}













