package com.main.geohogar.geohogarapp.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.main.geohogar.geohogarapp.data.remote.dto.ContactRequestDto
import com.main.geohogar.geohogarapp.data.repository.PropertyRepository
import com.main.geohogar.geohogarapp.domain.model.Property
import com.main.geohogar.geohogarapp.utils.Resource
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {

    private val repository = PropertyRepository()

    private val _property = MutableLiveData<Property>()
    val property: LiveData<Property> = _property

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _contactResult = MutableLiveData<Boolean>()
    val contactResult: LiveData<Boolean> = _contactResult

    fun loadPropertyDetail(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = repository.getPropertyDetail(id)) {
                is Resource.Success -> {
                    result.data?.let {
                        _property.value = it
                    }
                }
                is Resource.Error -> {
                    result.message?.let {
                        _error.value = it
                    }
                }
                is Resource.Loading -> {
                    // No acción necesaria
                }
            }

            _isLoading.value = false
        }
    }

    fun sendContactRequest(
        nombre: String,
        correo: String,
        telefono: String?,
        mensaje: String?,
        propertyId: Int
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            val request = ContactRequestDto(
                nombre = nombre,
                correo = correo,
                telefono = telefono,
                mensaje = mensaje,
                idPropiedad = propertyId
            )

            when (repository.sendContactRequest(request)) {
                is Resource.Success -> {
                    _contactResult.value = true
                }
                is Resource.Error -> {
                    _contactResult.value = false
                }
                is Resource.Loading -> {
                    // No acción necesaria
                }
            }

            _isLoading.value = false
        }
    }
}