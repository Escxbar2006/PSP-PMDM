package com.example.mitiendapabloescobar.api

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.util.concurrent.CopyOnWriteArrayList

class MockInterceptor : Interceptor {
    
    companion object {
        private val cartList = CopyOnWriteArrayList<String>()
        
        // Base de datos de productos simulada
        private val allProducts = """
            [
                {"id": 1, "name": "Gibson Les Paul Custom", "description": "El estándar de oro. Acabado Ebony con herrajes dorados.", "price": 4899.0, "image": "https://images.gibson.com/Products/Electric-Guitars/2023/Custom/Les-Paul-Custom/LPC-EBGH1_Ebony_FRONT.png", "category": "Guitarras"},
                {"id": 2, "name": "Fender Stratocaster '60", "description": "Sonido vintage californiano con cuerpo de aliso.", "price": 3200.0, "image": "https://www.fmicassets.com/Damroot/ZoomJpg/10001/0119010800_alt_1.jpg", "category": "Guitarras"},
                {"id": 3, "name": "Ibanez RG Prestige", "description": "Velocidad pura para shredders. Mástil Super Wizard.", "price": 1999.0, "image": "https://www.ibanez.com/common/product_artist_file/file/p_region_RG5120M_FCN_00_01.png", "category": "Guitarras"},
                
                {"id": 4, "name": "Roland V-Drums TD-27KV2", "description": "Batería electrónica de alta gama con sonido prismático.", "price": 2850.0, "image": "https://static.roland.com/products/td-27kv2/images/td-27kv2_top_main.jpg", "category": "Baterías"},
                {"id": 5, "name": "Pearl Export EXX725", "description": "La batería más vendida del mundo. Sonido y durabilidad.", "price": 899.0, "image": "https://m.media-amazon.com/images/I/71Y8M8f-jmL._AC_SL1500_.jpg", "category": "Baterías"},
                {"id": 6, "name": "Yamaha Stage Custom", "description": "Cuerpo 100% de abedul. Sonido brillante y potente.", "price": 1150.0, "image": "https://es.yamaha.com/es/products/contents/musical_instruments/drums/dr_drums/stage_custom_birch/images/stage_custom_birch_01.jpg", "category": "Baterías"},
                
                {"id": 7, "name": "Yamaha P-145 B", "description": "Piano digital compacto con teclado GHC de 88 teclas.", "price": 449.0, "image": "https://es.yamaha.com/es/products/contents/musical_instruments/pianos/p-series/p-145/images/p-145_01.jpg", "category": "Pianos"},
                {"id": 8, "name": "Kawai CA701", "description": "Piano digital híbrido con acción de madera real.", "price": 3450.0, "image": "https://www.kawai-global.com/data/products/digital/ca701/ca701_r_1.jpg", "category": "Pianos"},
                {"id": 9, "name": "Roland FP-30X", "description": "El piano portátil perfecto para profesionales y estudiantes.", "price": 699.0, "image": "https://static.roland.com/products/fp-30x/images/fp-30x_top_main.jpg", "category": "Pianos"}
            ]
        """.trimIndent()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url
        val method = request.method

        val responseString = when {
            url.toString().contains("login") && method == "POST" -> {
                """{"token": "jwt-pablo-escobar-music-2024", "username": "Pablo Escobar"}"""
            }

            url.toString().contains("products") && method == "GET" -> {
                val categoryQuery = url.queryParameter("category")
                if (categoryQuery != null && categoryQuery.isNotBlank()) {
                    // Simulación rudimentaria de filtrado en el servidor
                    // En un caso real, el servidor SQL haría esto. Aquí devolvemos todo por simplicidad
                    // pero indicamos que el sistema de filtrado está listo en la API.
                    allProducts 
                } else {
                    allProducts
                }
            }

            url.toString().contains("cart/add") && method == "POST" -> {
                cartList.add("{\"id\": 1, \"name\": \"Gibson Les Paul\", \"price\": 4899.0, \"image\": \"https://images.gibson.com/Products/Electric-Guitars/2023/Custom/Les-Paul-Custom/LPC-EBGH1_Ebony_FRONT.png\", \"category\": \"Guitarras\"}")
                "{}"
            }

            url.toString().contains("cart/remove") && method == "DELETE" -> {
                if (cartList.isNotEmpty()) cartList.removeAt(0)
                "{}"
            }

            url.toString().contains("cart") && method == "GET" -> {
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
