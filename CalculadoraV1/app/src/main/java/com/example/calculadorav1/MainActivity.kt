package com.example.calculadorav1

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etOpcion1 = findViewById<EditText>(R.id.etOpcion1)
        val etOpcion2 = findViewById<EditText>(R.id.etOpcion2)
        val tvResultado = findViewById<TextView>(R.id.tvResultado)

        val btnSumar = findViewById<Button>(R.id.btnSumar)
        val btnRestar = findViewById<Button>(R.id.btnRestar)
        val btnMultiplicar = findViewById<Button>(R.id.btnMultiplicar)
        val btnDividir = findViewById<Button>(R.id.btnDividir)

        btnSumar.setOnClickListener {
            val op1 = etOpcion1.text.toString().toDoubleOrNull()
            val op2 = etOpcion2.text.toString().toDoubleOrNull()

            if (op1 != null && op2 != null) {
                val resultado = op1 + op2
                tvResultado.text = "Resultado: $resultado"
            } else {
                tvResultado.text = "Resultado: Ingrese números válidos"
            }
        }

        btnRestar.setOnClickListener {
            val op1 = etOpcion1.text.toString().toDoubleOrNull()
            val op2 = etOpcion2.text.toString().toDoubleOrNull()

            if (op1 != null && op2 != null) {
                val resultado = op1 - op2
                tvResultado.text = "Resultado: $resultado"
            } else {
                tvResultado.text = "Resultado: Ingrese números válidos"
            }
        }

        btnMultiplicar.setOnClickListener {
            val op1 = etOpcion1.text.toString().toDoubleOrNull()
            val op2 = etOpcion2.text.toString().toDoubleOrNull()

            if (op1 != null && op2 != null) {
                val resultado = op1 * op2
                tvResultado.text = "Resultado: $resultado"
            } else {
                tvResultado.text = "Resultado: Ingrese números válidos"
            }
        }

        btnDividir.setOnClickListener {
            val op1 = etOpcion1.text.toString().toDoubleOrNull()
            val op2 = etOpcion2.text.toString().toDoubleOrNull()

            if (op1 != null && op2 != null) {
                if (op2 != 0.0) {
                    val resultado = op1 / op2
                    tvResultado.text = "Resultado: $resultado"
                } else {
                    tvResultado.text = "Resultado: No se puede dividir por cero"
                }
            } else {
                tvResultado.text = "Resultado: Ingrese números válidos"
            }
        }
    }
}