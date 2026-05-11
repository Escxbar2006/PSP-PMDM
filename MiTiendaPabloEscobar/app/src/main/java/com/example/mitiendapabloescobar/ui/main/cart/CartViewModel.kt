package com.example.mitiendapabloescobar.ui.main.cart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mitiendapabloescobar.api.RetrofitClient
import com.example.mitiendapabloescobar.model.Product
import kotlinx.coroutines.launch

class CartViewModel(application: Application) : AndroidViewModel(application) {

    private val _cartProducts = MutableLiveData<List<Product>>()
    val cartProducts: LiveData<List<Product>> = _cartProducts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _deleteResult = MutableLiveData<Result<Unit>>()
    val deleteResult: LiveData<Result<Unit>> = _deleteResult

    fun getCart() {
        viewModelScope.launch {
            _isLoading.value = true
            fetchCartInternal()
            _isLoading.value = false
        }
    }

    private suspend fun fetchCartInternal() {
        try {
            val response = RetrofitClient.getInstance(getApplication()).getCart()
            if (response.isSuccessful && response.body() != null) {
                _cartProducts.value = response.body()!!
                _error.value = null
            } else {
                _error.value = "Error al cargar carrito: ${response.code()}"
            }
        } catch (e: Exception) {
            _error.value = "Error de conexión: ${e.message}"
        }
    }

    fun removeFromCart(productId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.getInstance(getApplication()).removeFromCart(productId)
                if (response.isSuccessful) {
                    _deleteResult.value = Result.success(Unit)
                    // Actualizamos la lista inmediatamente después del borrado
                    fetchCartInternal()
                } else {
                    _deleteResult.value = Result.failure(Exception("Error al borrar: ${response.code()}"))
                }
            } catch (e: Exception) {
                _deleteResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
