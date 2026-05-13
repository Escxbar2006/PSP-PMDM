package com.example.mitiendapabloescobar.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mitiendapabloescobar.api.RetrofitClient
import com.example.mitiendapabloescobar.api.TokenManager
import com.example.mitiendapabloescobar.model.LoginRequest
import com.example.mitiendapabloescobar.model.LoginResponse
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _loginResponse = MutableLiveData<Result<LoginResponse>>()
    val loginResponse: LiveData<Result<LoginResponse>> = _loginResponse

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val tokenManager = TokenManager(application)

    fun login(user: String, pass: String) {
        if (user.isBlank() || pass.isBlank()) {
            _loginResponse.value = Result.failure(Exception("Introduce usuario y contraseña"))
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.getInstance(getApplication()).login(LoginRequest(user, pass))
                
                if (response.isSuccessful && response.body() != null) {
                    val loginData = response.body()!!
                    tokenManager.saveToken(loginData.token)
                    _loginResponse.value = Result.success(loginData)
                } else {
                    val errorDetail = when(response.code()) {
                        401 -> "Credenciales incorrectas"
                        404 -> "Servicio de login no encontrado"
                        else -> "Error del servidor (${response.code()})"
                    }
                    _loginResponse.value = Result.failure(Exception(errorDetail))
                }
            } catch (e: Exception) {
                _loginResponse.value = Result.failure(Exception("Error de red: ${e.localizedMessage}"))
            } finally {
                _isLoading.value = false
            }
        }
    }
}
