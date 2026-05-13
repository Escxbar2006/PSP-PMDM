package com.example.mitiendapabloescobar.ui.main.products

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mitiendapabloescobar.api.RetrofitClient
import com.example.mitiendapabloescobar.model.Product
import kotlinx.coroutines.launch

class ProductsViewModel(application: Application) : AndroidViewModel(application) {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentPage = 1
    private var isLastPage = false

    fun getProducts(category: String? = null, isRefresh: Boolean = false) {
        if (isRefresh) {
            currentPage = 1
            isLastPage = false
        }
        
        if (isLastPage && !isRefresh) return

        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                // El Token se añade automáticamente gracias al Interceptor en RetrofitClient
                val response = RetrofitClient.getInstance(getApplication()).getProducts(category, currentPage)
                
                if (response.isSuccessful && response.body() != null) {
                    val newProducts = response.body()!!
                    if (newProducts.isEmpty()) {
                        isLastPage = true
                    } else {
                        val currentList = if (isRefresh) mutableListOf() else _products.value?.toMutableList() ?: mutableListOf()
                        currentList.addAll(newProducts)
                        _products.value = currentList
                        currentPage++
                    }
                    _error.value = null
                } else {
                    _error.value = "Error al cargar productos: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error de red: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
