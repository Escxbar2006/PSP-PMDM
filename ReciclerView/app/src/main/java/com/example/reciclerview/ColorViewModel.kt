package com.example.reciclerview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.random.Random

class ColorViewModel : ViewModel() {
    private val _colors = MutableLiveData<List<Color>>()
    val colors: LiveData<List<Color>> get() = _colors

    init {
        loadColors()
    }

    private fun loadColors() {
        _colors.value = listOf(
            Color(1, "Verde", "#ff4caf50"),
            Color(2, "Amarillo", "#FFEB3B"),
            Color(3, "Azul", "#ff2196f3"),
            Color(4, "Indigo", "#ff3f51b5"),
            Color(5, "Rojo", "#fff44336"),
            Color(6, "Naranja", "#ff6f00"),
            Color(7, "Gris", "#757575"),
            Color(8, "Violeta", "#ff673ab7")
        )
    }

    fun addColor() {
        val currentList = _colors.value.orEmpty().toMutableList()
        val randomColorHex = String.format("#%06X", Random.nextInt(0xFFFFFF))
        val newColor = Color(
            id = System.currentTimeMillis(),
            name = "Nuevo Color",
            hexCode = randomColorHex
        )
        currentList.add(newColor)
        _colors.value = currentList
    }

    fun deleteLastColor() {
        val currentList = _colors.value.orEmpty().toMutableList()
        if (currentList.isNotEmpty()) {
            currentList.removeAt(currentList.size - 1)
            _colors.value = currentList
        }
    }

    fun toggleInversion(colorId: Long, inverted: Boolean) {
        val currentList = _colors.value.orEmpty()
        val newList = currentList.map { 
            if (it.id == colorId) it.copy(isInverted = inverted) else it 
        }
        _colors.value = newList
    }
}
