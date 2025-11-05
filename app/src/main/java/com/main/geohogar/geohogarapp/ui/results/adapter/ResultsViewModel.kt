package com.main.geohogar.geohogarapp.ui.results.adapter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.main.geohogar.geohogarapp.data.repository.PropertyRepository
import com.main.geohogar.geohogarapp.domain.model.Filter
import com.main.geohogar.geohogarapp.domain.model.Property
import com.main.geohogar.geohogarapp.utils.Resource
import kotlinx.coroutines.launch

class ResultsViewModel : ViewModel() {

    private val repository = PropertyRepository()

    private val _properties = MutableLiveData<List<Property>>()
    val properties: LiveData<List<Property>> = _properties

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun searchProperties(filter: Filter) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = repository.searchPropertiesWithFilters(filter)) {
                is Resource.Success -> {
                    _properties.value = result.data ?: emptyList()
                }
                is Resource.Error -> {
                    _error.value = result.message ?: "Ocurrió un error inesperado."
                    _properties.value = emptyList()
                }
                is Resource.Loading -> {}
            }

            _isLoading.value = false
        }
    }

    fun getAllProperties() {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = repository.getAllProperties()) {
                is Resource.Success -> {
                    _properties.value = result.data ?: emptyList()
                }
                is Resource.Error -> {
                    _error.value = result.message ?: "Ocurrió un error inesperado."
                    _properties.value = emptyList()
                }
                is Resource.Loading -> {}
            }

            _isLoading.value = false
        }
    }
}
