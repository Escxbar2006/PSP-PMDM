package com.example.mitiendapabloescobar.api

import com.example.mitiendapabloescobar.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("products")
    suspend fun getProducts(
        @Query("category") category: String?,
        @Query("page") page: Int
    ): Response<List<Product>>

    @POST("cart/add")
    suspend fun addToCart(
        @Body request: AddToCartRequest
    ): Response<Unit>

    @GET("cart")
    suspend fun getCart(): Response<List<Product>>

    @DELETE("cart/remove/{id}")
    suspend fun removeFromCart(
        @Path("id") productId: Int
    ): Response<Unit>
}
