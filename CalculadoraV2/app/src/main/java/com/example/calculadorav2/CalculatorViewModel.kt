package com.example.calculadorav2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CalculatorViewModel : ViewModel() {

    private val _display = MutableLiveData("0")
    val display: LiveData<String> = _display

    private val _history = MutableLiveData("")
    val history: LiveData<String> = _history

    private val _warning = MutableLiveData<String?>()
    val warning: LiveData<String?> = _warning

    private var firstOperand: Double? = null
    private var currentOperator: String? = null
    private var isNewOperation = true
    private var hasCalculated = false

    fun onDigitClicked(digit: String) {
        if (hasCalculated || isNewOperation) {
            _display.value = digit
            isNewOperation = false
            hasCalculated = false
        } else {
            if (_display.value == "0") {
                _display.value = digit
            } else {
                _display.value = (_display.value ?: "") + digit
            }
        }
    }

    fun onOperatorClicked(operator: String) {
        if (isNewOperation && !hasCalculated && firstOperand == null) {
            // Started with an operator or double operator
            _warning.value = "Debes elegir números"
            return
        }

        if (isNewOperation && currentOperator != null) {
             _warning.value = "Debes elegir números"
             return
        }

        val currentValue = _display.value?.toDoubleOrNull() ?: 0.0
        
        if (currentOperator != null && !isNewOperation) {
            // Auto calculate if there's already an operator? 
            // The requirements say "No se arrastran operaciones", but usually calculators allow 1+2+3.
            // However, it specifically says "Si una vez se ha hecho un cálculo el usuario toca un número, empezamos un nuevo cálculo."
            // Let's assume standard behavior for intermediate steps UNLESS "=" was pressed.
            calculate()
            firstOperand = _display.value?.toDoubleOrNull()
        } else {
            firstOperand = currentValue
        }

        currentOperator = operator
        _history.value = "${formatNumber(firstOperand ?: 0.0)} $operator"
        isNewOperation = true
        hasCalculated = false
    }

    fun onEqualsClicked() {
        if (currentOperator == null || isNewOperation) return

        calculate()
        val result = _display.value?.toDoubleOrNull() ?: 0.0
        _history.value = "${_history.value} ${_display.value} ="
        _display.value = formatNumber(result)
        
        currentOperator = null
        firstOperand = null
        hasCalculated = true
        isNewOperation = true
    }

    private fun calculate() {
        val secondOperand = _display.value?.toDoubleOrNull() ?: 0.0
        val result = when (currentOperator) {
            "+" -> (firstOperand ?: 0.0) + secondOperand
            "-" -> (firstOperand ?: 0.0) - secondOperand
            "*" -> (firstOperand ?: 0.0) * secondOperand
            "/" -> {
                if (secondOperand != 0.0) (firstOperand ?: 0.0) / secondOperand
                else Double.NaN
            }
            else -> secondOperand
        }
        _display.value = formatNumber(result)
    }

    fun onClearClicked() {
        _display.value = "0"
        _history.value = ""
        firstOperand = null
        currentOperator = null
        isNewOperation = true
        hasCalculated = false
    }

    fun resetWarning() {
        _warning.value = null
    }

    private fun formatNumber(number: Double): String {
        return if (number == number.toLong().toDouble()) {
            number.toLong().toString()
        } else {
            number.toString()
        }
    }
}
