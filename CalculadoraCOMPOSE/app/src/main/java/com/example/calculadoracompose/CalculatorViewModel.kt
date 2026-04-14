package com.example.calculadoracompose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class CalculatorViewModel : ViewModel() {
    var number1 by mutableStateOf("")
    var number2 by mutableStateOf("")
    var result by mutableStateOf("0.0")

    fun onNumber1Change(newValue: String) {
        if (newValue.isEmpty() || newValue.matches(Regex("""^-?\d*\.?\d*$"""))) {
            number1 = newValue
        }
    }

    fun onNumber2Change(newValue: String) {
        if (newValue.isEmpty() || newValue.matches(Regex("""^-?\d*\.?\d*$"""))) {
            number2 = newValue
        }
    }

    fun calculate(operation: String) {
        val n1 = number1.toDoubleOrNull() ?: 0.0
        val n2 = number2.toDoubleOrNull() ?: 0.0

        val res = when (operation) {
            "+" -> n1 + n2
            "-" -> n1 - n2
            "*" -> n1 * n2
            "/" -> if (n2 != 0.0) n1 / n2 else Double.NaN
            else -> 0.0
        }
        result = res.toString()
    }
}
