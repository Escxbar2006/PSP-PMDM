package com.example.primercontacto

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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

        val etNum1 = findViewById<EditText>(R.id.et_num1)
        val etNum2 = findViewById<EditText>(R.id.et_num2)
        val tvResultado = findViewById<TextView>(R.id.tv_resultado)

        val btnSumar = findViewById<Button>(R.id.btn_sumar)
        val btnRestar = findViewById<Button>(R.id.btn_restar)
        val btnMultiplicar = findViewById<Button>(R.id.btn_multiplicar)
        val btnDividir = findViewById<Button>(R.id.btn_dividir)

        btnSumar.setOnClickListener {
            calcular(etNum1, etNum2, tvResultado, "+")
        }

        btnRestar.setOnClickListener {
            calcular(etNum1, etNum2, tvResultado, "-")
        }

        btnMultiplicar.setOnClickListener {
            calcular(etNum1, etNum2, tvResultado, "*")
        }

        btnDividir.setOnClickListener {
            calcular(etNum1, etNum2, tvResultado, "/")
        }
    }

    private fun calcular(et1: EditText, et2: EditText, tvRes: TextView, operacion: String) {
        val s1 = et1.text.toString()
        val s2 = et2.text.toString()

        if (s1.isEmpty() || s2.isEmpty()) {
            Toast.makeText(this, "Por favor, introduce ambos números", Toast.LENGTH_SHORT).show()
            return
        }

        val n1 = s1.toDouble()
        val n2 = s2.toDouble()
        var resultado = 0.0

        when (operacion) {
            "+" -> resultado = n1 + n2
            "-" -> resultado = n1 - n2
            "*" -> resultado = n1 * n2
            "/" -> {
                if (n2 == 0.0) {
                    Toast.makeText(this, "No se puede dividir por cero", Toast.LENGTH_SHORT).show()
                    return
                }
                resultado = n1 / n2
            }
        }

        tvRes.text = resultado.toString()
    }
}
