package com.example.razasdeperro.data.remote

import com.example.razasdeperro.data.model.BreedsResponse
import com.example.razasdeperro.data.model.DogImagesResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface DogApiService {
    @GET("breeds/list/all")
    suspend fun getAllBreeds(): BreedsResponse

    @GET("breed/{breed}/images")
    suspend fun getBreedImages(@Path("breed") breed: String): DogImagesResponse
}
