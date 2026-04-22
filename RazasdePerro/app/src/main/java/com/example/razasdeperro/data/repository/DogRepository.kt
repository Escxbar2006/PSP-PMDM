package com.example.razasdeperro.data.repository

import com.example.razasdeperro.data.model.DogBreed
import com.example.razasdeperro.data.remote.RetrofitInstance

class DogRepository {
    private val api = RetrofitInstance.api

    suspend fun getBreeds(): List<DogBreed> {
        return try {
            val response = api.getAllBreeds()
            response.message.map { (breedName, subBreeds) ->
                DogBreed(name = breedName, subBreeds = subBreeds)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getBreedImages(breedName: String): List<String> {
        return try {
            val response = api.getBreedImages(breedName)
            response.message
        } catch (e: Exception) {
            emptyList()
        }
    }
}
