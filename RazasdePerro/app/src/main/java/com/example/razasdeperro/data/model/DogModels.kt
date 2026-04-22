package com.example.razasdeperro.data.model

import com.google.gson.annotations.SerializedName

data class BreedsResponse(
    @SerializedName("message") val message: Map<String, List<String>>,
    @SerializedName("status") val status: String
)

data class DogImagesResponse(
    @SerializedName("message") val message: List<String>,
    @SerializedName("status") val status: String
)

data class DogBreed(
    val name: String,
    val subBreeds: List<String> = emptyList()
)
