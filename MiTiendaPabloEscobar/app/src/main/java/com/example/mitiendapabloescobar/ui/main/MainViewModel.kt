package com.example.mitiendapabloescobar.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _token = MutableLiveData<String>()
    val token: LiveData<String> = _token

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> = _username

    fun setUserData(token: String, username: String) {
        _token.value = token
        _username.value = username
    }
}
