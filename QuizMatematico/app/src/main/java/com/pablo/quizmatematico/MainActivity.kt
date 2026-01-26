package com.pablo.quizmatematico

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private var currentYear: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mainLayout = findViewById<ConstraintLayout>(R.id.main)
        val swBackground = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.swBackground)
        val btnGenerate = findViewById<Button>(R.id.btnGenerate)
        val tvNumber = findViewById<TextView>(R.id.tvNumber)
        val rgOptions = findViewById<RadioGroup>(R.id.rgOptions)
        val btnCheck = findViewById<Button>(R.id.btnCheck)
        val tvResult = findViewById<TextView>(R.id.tvResult)
        val btnNextActivity = findViewById<Button>(R.id.btnNextActivity)

        swBackground.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                mainLayout.setBackgroundColor(Color.YELLOW)
            } else {
                mainLayout.setBackgroundColor(Color.WHITE)
            }
        }

        btnGenerate.setOnClickListener {
            currentYear = Random.nextInt(1900, 2501)
            tvNumber.text = currentYear.toString()
            rgOptions.clearCheck()
            tvResult.text = ""
        }

        btnCheck.setOnClickListener {
            val selectedId = rgOptions.checkedRadioButtonId
            if (selectedId == -1) {
                tvResult.text = "Debe escoger una de las opciones"
                tvResult.setTextColor(Color.BLUE)
            } else {
                val isLeap = (currentYear % 4 == 0 && currentYear % 100 != 0) || (currentYear % 400 == 0)
                val userAnswerIsSi = selectedId == R.id.rbSi

                if (userAnswerIsSi == isLeap) {
                    tvResult.text = "Correcto"
                    tvResult.setTextColor(Color.GREEN)
                } else {
                    tvResult.text = "Error"
                    tvResult.setTextColor(Color.RED)
                }
            }
        }

        btnNextActivity.setOnClickListener {
            val intent = Intent(this, Activity2::class.java)
            startActivity(intent)
        }
    }
}
