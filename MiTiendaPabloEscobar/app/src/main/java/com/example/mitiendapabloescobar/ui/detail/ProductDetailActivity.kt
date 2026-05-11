package com.example.mitiendapabloescobar.ui.detail

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.mitiendapabloescobar.R
import com.example.mitiendapabloescobar.databinding.ActivityProductDetailBinding

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private val viewModel: ProductDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val productId = intent.getIntExtra("EXTRA_PRODUCT_ID", -1)
        val productName = intent.getStringExtra("EXTRA_PRODUCT_NAME") ?: ""
        val productPrice = intent.getDoubleExtra("EXTRA_PRODUCT_PRICE", 0.0)
        val productImage = intent.getStringExtra("EXTRA_PRODUCT_IMAGE") ?: ""

        setupView(productName, productPrice, productImage)
        setupObservers()
        setupListeners(productId)
    }

    private fun setupView(name: String, price: Double, image: String) {
        binding.tvNameDetail.text = name
        binding.tvPriceDetail.text = getString(R.string.product_price_format, price)
        Glide.with(this).load(image).into(binding.ivProductDetail)
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.pbDetail.visibility = if (isLoading) View.VISIBLE else View.GONE
            // Bloqueamos el botón mientras se realiza la operación
            binding.btnAddToCart.isEnabled = !isLoading
        }

        viewModel.cartOperationResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Producto añadido al carrito", Toast.LENGTH_SHORT).show()
                // No cerramos la actividad para permitir al usuario seguir navegando o añadir más
            }.onFailure { exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners(productId: Int) {
        binding.btnAddToCart.setOnClickListener {
            val quantity = binding.etQuantity.text.toString().toIntOrNull() ?: 0
            viewModel.addToCart(productId, quantity)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}
