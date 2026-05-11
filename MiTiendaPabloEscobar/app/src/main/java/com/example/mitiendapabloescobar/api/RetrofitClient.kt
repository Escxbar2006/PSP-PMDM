package com.example.mitiendapabloescobar.api

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://escobar-music-api.com/api/"
    private var apiService: ApiService? = null

    fun getInstance(context: Context): ApiService {
        if (apiService == null) {
            val tokenManager = TokenManager(context)
            
            val okHttpClient = OkHttpClient.Builder()
                // 1. Añadimos el MOCK para que la app funcione sin servidor real
                .addInterceptor(MockInterceptor())
                // 2. Añadimos el interceptor para el Bearer Token JWT
                .addInterceptor { chain ->
                    val original = chain.request()
                    val requestBuilder = original.newBuilder()
                    
                    tokenManager.getToken()?.let { token ->
                        requestBuilder.header("Authorization", "Bearer $token")
                    }
                    
                    chain.proceed(requestBuilder.build())
                }
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            apiService = retrofit.create(ApiService::class.java)
        }
        return apiService!!
    }
}
