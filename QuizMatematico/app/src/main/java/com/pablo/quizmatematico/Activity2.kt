package com.pablo.quizmatematico

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class Activity2 : AppCompatActivity() {

    private var randomNumber: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_2)

        val btnGenerate = findViewById<Button>(R.id.btnGenerate2)
        val tvNumber = findViewById<TextView>(R.id.tvNumber2)
        val cb2 = findViewById<CheckBox>(R.id.cb2)
        val cb3 = findViewById<CheckBox>(R.id.cb3)
        val cb5 = findViewById<CheckBox>(R.id.cb5)
        val cb10 = findViewById<CheckBox>(R.id.cb10)
        val cbNone = findViewById<CheckBox>(R.id.cbNone)
        val btnCheck = findViewById<Button>(R.id.btnCheck2)
        val tvResult = findViewById<TextView>(R.id.tvResult2)
        val ivResult = findViewById<ImageView>(R.id.ivResult)
        val btnBack = findViewById<Button>(R.id.btnBack)

        btnGenerate.setOnClickListener {
            randomNumber = Random.nextInt(1000, 2001)
            tvNumber.text = randomNumber.toString()
            
            // Clear selections
            cb2.isChecked = false
            cb3.isChecked = false
            cb5.isChecked = false
            cb10.isChecked = false
            cbNone.isChecked = false
            
            tvResult.text = ""
            ivResult.visibility = View.GONE
        }

        btnCheck.setOnClickListener {
            if (randomNumber == 0) return@setOnClickListener

            val checked2 = cb2.isChecked
            val checked3 = cb3.isChecked
            val checked5 = cb5.isChecked
            val checked10 = cb10.isChecked
            val checkedNone = cbNone.isChecked

            if (!checked2 && !checked3 && !checked5 && !checked10 && !checkedNone) {
                tvResult.text = "Debe escoger al menos una de las opciones"
                tvResult.setTextColor(android.graphics.Color.BLUE)
                ivResult.visibility = View.GONE
                return@setOnClickListener
            }

            val isDivBy2 = randomNumber % 2 == 0
            val isDivBy3 = randomNumber % 3 == 0
            val isDivBy5 = randomNumber % 5 == 0
            val isDivBy10 = randomNumber % 10 == 0
            val isDivByNone = !isDivBy2 && !isDivBy3 && !isDivBy5 && !isDivBy10

            val isCorrect = if (checkedNone) {
                // If None is checked, it must be the only one checked and it must be true
                isDivByNone && !checked2 && !checked3 && !checked5 && !checked10
            } else {
                // Otherwise, all checked must be correct and all correct must be checked
                (checked2 == isDivBy2) && (checked3 == isDivBy3) && (checked5 == isDivBy5) && (checked10 == isDivBy10)
            }

            if (isCorrect) {
                tvResult.text = "Correcto"
                tvResult.setTextColor(android.graphics.Color.GREEN)
                ivResult.setImageResource(R.drawable.ic_ok)
            } else {
                tvResult.text = "Error"
                tvResult.setTextColor(android.graphics.Color.RED)
                ivResult.setImageResource(R.drawable.ic_ko)
            }
            ivResult.visibility = View.VISIBLE
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
