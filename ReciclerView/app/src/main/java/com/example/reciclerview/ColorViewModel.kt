package com.example.reciclerview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ColorViewModel : ViewModel() {
    private val _colors = MutableLiveData<List<Color>>()
    val colors: LiveData<List<Color>> get() = _colors

    init {
        loadColors()
    }

    private fun loadColors() {
        _colors.value = listOf(
            Color("Verde", "#ff4caf50"),
            Color("Amarillo", "#FFEB3B"),
            Color("Azul", "#ff2196f3"),
            Color("Indigo", "#ff3f51b5"),
            Color("Rojo", "#fff44336"),
            Color("Naranja", "#ff6f00"),
            Color("Gris", "#757575"),
            Color("Violeta", "#ff673ab7")
        )
    }
}
