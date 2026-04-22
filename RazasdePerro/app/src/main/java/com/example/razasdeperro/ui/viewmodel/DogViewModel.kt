package com.example.razasdeperro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.razasdeperro.data.model.DogBreed
import com.example.razasdeperro.data.repository.DogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DogViewModel : ViewModel() {
    private val repository = DogRepository()

    private val _breeds = MutableStateFlow<List<DogBreed>>(emptyList())
    val breeds: StateFlow<List<DogBreed>> = _breeds

    private val _images = MutableStateFlow<List<String>>(emptyList())
    val images: StateFlow<List<String>> = _images

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchBreeds()
    }

    fun fetchBreeds() {
        viewModelScope.launch {
            _isLoading.value = true
            _breeds.value = repository.getBreeds()
            _isLoading.value = false
        }
    }

    fun fetchBreedImages(breedName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _images.value = repository.getBreedImages(breedName)
            _isLoading.value = false
        }
    }
}
