package com.example.reciclerview

data class Color(
    val id: Long,
    val name: String, 
    val hexCode: String,
    var isInverted: Boolean = false
)
