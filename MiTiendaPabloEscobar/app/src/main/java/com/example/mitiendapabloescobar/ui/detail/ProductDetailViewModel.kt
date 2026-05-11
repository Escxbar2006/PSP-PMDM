package com.example.mitiendapabloescobar.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mitiendapabloescobar.api.RetrofitClient
import com.example.mitiendapabloescobar.model.AddToCartRequest
import kotlinx.coroutines.launch

class ProductDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val _cartOperationResult = MutableLiveData<Result<Unit>>()
    val cartOperationResult: LiveData<Result<Unit>> = _cartOperationResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun addToCart(productId: Int, quantity: Int) {
        if (quantity <= 0) {
            _cartOperationResult.value = Result.failure(Exception("La cantidad debe ser mayor que 0"))
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                // El Token se añade automáticamente gracias al Interceptor
                val response = RetrofitClient.getInstance(getApplication()).addToCart(
                    AddToCartRequest(productId, quantity)
                )
                if (response.isSuccessful) {
                    _cartOperationResult.value = Result.success(Unit)
                } else {
                    _cartOperationResult.value = Result.failure(Exception("Error al añadir al carrito: ${response.code()}"))
                }
            } catch (e: Exception) {
                _cartOperationResult.value = Result.failure(Exception("Error de red: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }
}
