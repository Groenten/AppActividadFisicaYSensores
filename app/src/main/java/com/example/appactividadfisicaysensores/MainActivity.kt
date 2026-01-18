package com.example.appactividadfisicaysensores
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appactividadfisicaysensores.R
import kotlin.toString

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val spTipo = findViewById<Spinner>(R.id.spTipoActividad)
        val etMinutosActividad = findViewById<EditText>(R.id.etMinutosActividad)
        val btnAnadirActividad = findViewById<Button>(R.id.btnAnadirActividad)


        //spinner para enseñar actividaades
        val adapterTipos = ArrayAdapter.createFromResource(
            this,
            R.array.tipos_actividad,
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        spTipo.adapter = adapterTipos


        // Boton
        btnAnadirActividad.setOnClickListener {
            val tipo = spTipo.selectedItem?.toString()?.lowercase() ?:"caminar"
            val minutosText = etMinutosActividad.text.toString().trim()
            val minutos = minutosText.toIntOrNull()


            if (minutos == null || minutos <= 0) {
                etMinutosActividad.error = "Introduce minutos válidos"
                return@setOnClickListener
            }

            //muestra lo guardado
            Toast.makeText(this, "Guardado: $tipo - $minutos min", Toast.LENGTH_SHORT).show()
            val iconRes: Int = when (tipo) {
                "caminar" -> R.drawable.ic_caminar
                "correr" -> R.drawable.ic_correr
                "yoga" -> R.drawable.ic_yoga
                "gimnasio" -> R.drawable.ic_gym
                "bicicleta" -> R.drawable.ic_bicicleta
                "natación" -> R.drawable.ic_natacion
                else -> R.drawable.ic_default
            }
            // ir a la pantalla 2 pasando datos:
            val intent = Intent(this, ProcesadoActividadActivity::class.java).apply {
                putExtra("TIPO", tipo)
                putExtra("MINUTOS", minutos)
                putExtra("ICON_RES", iconRes)

            }
            startActivity(intent)

        }

    }







}

