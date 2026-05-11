package com.example.mitiendapabloescobar.api

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.util.concurrent.CopyOnWriteArrayList

class MockInterceptor : Interceptor {
    
    companion object {
        // Usamos una lista thread-safe para evitar problemas de sincronización en el simulador
        private val cartList = CopyOnWriteArrayList<String>()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        val method = request.method

        val responseString = when {
            url.contains("login") && method == "POST" -> {
                """{"token": "jwt-pablo-escobar-music-2024", "username": "Pablo Escobar"}"""
            }

            url.contains("products") && method == "GET" -> {
                """
                [
                    {"id": 1, "name": "Gibson Les Paul Custom", "description": "Acabado Ebony.", "price": 4899.0, "image": "https://images.gibson.com/Products/Electric-Guitars/2023/Custom/Les-Paul-Custom/LPC-EBGH1_Ebony_FRONT.png", "category": "Guitarras"},
                    {"id": 2, "name": "Fender Stratocaster", "description": "Sonido vintage.", "price": 3200.0, "image": "https://www.fmicassets.com/Damroot/ZoomJpg/10001/0119010800_alt_1.jpg", "category": "Guitarras"},
                    {"id": 4, "name": "Roland V-Drums", "description": "Batería electrónica.", "price": 2850.0, "image": "https://static.roland.com/products/td-27kv2/images/td-27kv2_top_main.jpg", "category": "Baterías"}
                ]
                """.trimIndent()
            }

            url.contains("cart/add") && method == "POST" -> {
                cartList.add("{\"id\": 1, \"name\": \"Gibson Les Paul\", \"price\": 4899.0, \"image\": \"https://images.gibson.com/Products/Electric-Guitars/2023/Custom/Les-Paul-Custom/LPC-EBGH1_Ebony_FRONT.png\", \"category\": \"Guitarras\"}")
                "{}"
            }

            url.contains("cart/remove") && method == "DELETE" -> {
                if (cartList.isNotEmpty()) cartList.removeAt(0)
                "{}"
            }

            url.contains("cart") && method == "GET" -> {
                if (cartList.isEmpty()) "[]" else "[${cartList.joinToString(",")}]"
            }

            else -> "{}"
        }

        Thread.sleep(300)

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseString.toResponseBody("application/json".toMediaTypeOrNull()))
            .addHeader("Content-Type", "application/json")
            .build()
    }
}
