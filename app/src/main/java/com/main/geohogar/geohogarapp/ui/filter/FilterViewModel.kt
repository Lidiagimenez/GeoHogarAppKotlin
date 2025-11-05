package com.main.geohogar.geohogarapp.ui.filter

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.main.geohogar.geohogarapp.data.repository.PropertyRepository
import com.main.geohogar.geohogarapp.domain.model.Property
import com.main.geohogar.geohogarapp.domain.model.Filter
import com.main.geohogar.geohogarapp.utils.Resource
import kotlinx.coroutines.launch

class FilterViewModel : ViewModel() {

    private val repository = PropertyRepository()

    private val _filter = MutableLiveData<Filter>()
    val filter: LiveData<Filter> = _filter

    private val _neighborhoods = MutableLiveData<List<String>>()
    val neighborhoods: LiveData<List<String>> = _neighborhoods

    private val _propertyTypes = MutableLiveData<List<String>>()
    val propertyTypes: LiveData<List<String>> = _propertyTypes

    private val _allProperties = MutableLiveData<List<Property>>()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _priceRange = MutableLiveData<PriceRangeData>()
    val priceRange: LiveData<PriceRangeData> = _priceRange

    private val _searchResult = MutableLiveData<SearchResult>()
    val searchResult: LiveData<SearchResult> = _searchResult

    init {
        _filter.value = Filter()
        Log.d("FilterViewModel", "Iniciando carga de datos...")
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            loadAllProperties()
            loadNeighborhoods()
            loadPropertyTypes()
            _isLoading.value = false
        }
    }

    private suspend fun loadAllProperties() {
        Log.d("FilterViewModel", "Cargando todas las propiedades...")
        when (val result = repository.getAllProperties()) {
            is Resource.Success -> {
                _allProperties.value = result.data ?: emptyList()
                Log.d("FilterViewModel", "✅ Propiedades cargadas: ${result.data?.size}")
                calculatePriceRange()
            }
            is Resource.Error -> {
                Log.e("FilterViewModel", "❌ Error cargando propiedades: ${result.message}")
                result.message.also { _error.value = it }
                _priceRange.value = PriceRangeData(0f, 1000000f, 10000f)
            }
            is Resource.Loading -> {}
        }
    }

    private fun calculatePriceRange() {
        val properties = _allProperties.value
        if (properties.isNullOrEmpty()) {
            _priceRange.value = PriceRangeData(0f, 1000000f, 10000f)
            Log.d("FilterViewModel", "⚠️ Sin propiedades, usando valores por defecto")
            return
        }

        val prices = properties.map { it.precio.toFloat() }
        val minPrice = prices.minOrNull() ?: 0f
        val maxPrice = prices.maxOrNull() ?: 1000000f

        val roundedMin = (minPrice / 5000f).toInt() * 5000f
        val roundedMax = ((maxPrice / 5000f).toInt() + 1) * 5000f

        val range = roundedMax - roundedMin
        val calculatedStepSize = calculateValidStepSize(range)

        Log.d("FilterViewModel", "✅ Rango: $roundedMin - $roundedMax (step: $calculatedStepSize)")

        _priceRange.value = PriceRangeData(roundedMin, roundedMax, calculatedStepSize)
    }

    private fun calculateValidStepSize(range: Float): Float {
        val possibleSteps = listOf(
            50000f, 25000f, 20000f, 10000f, 5000f,
            2500f, 1000f, 500f, 100f, 10f, 1f
        )
        return possibleSteps.firstOrNull { range % it == 0f } ?: 1f
    }

    private suspend fun loadNeighborhoods() {
        Log.d("FilterViewModel", "Cargando barrios...")
        when (val result = repository.getZonas()) {
            is Resource.Success -> {
                val zones = result.data ?: emptyList()
                if (zones.isEmpty()) {
                    val propertiesZones = _allProperties.value?.map { it.zona }?.distinct() ?: emptyList()
                    _neighborhoods.value = propertiesZones
                    Log.d("FilterViewModel", "✅ Barrios de propiedades: $propertiesZones")
                } else {
                    _neighborhoods.value = zones
                    Log.d("FilterViewModel", "✅ Barrios API: $zones")
                }
            }
            is Resource.Error -> {
                val propertiesZones = _allProperties.value?.map { it.zona }?.distinct() ?: getDefaultNeighborhoods()
                _neighborhoods.value = propertiesZones
                Log.d("FilterViewModel", "⚠️ Barrios fallback: $propertiesZones")
            }
            is Resource.Loading -> {}
        }
    }

    private suspend fun loadPropertyTypes() {
        Log.d("FilterViewModel", "Cargando tipos de propiedad...")
        when (val result = repository.getTiposInmueble()) {
            is Resource.Success -> {
                val types = result.data ?: emptyList()
                if (types.isEmpty()) {
                    val propertiesTypes = _allProperties.value?.map { it.tipo }?.distinct() ?: emptyList()
                    _propertyTypes.value = propertiesTypes
                    Log.d("FilterViewModel", "✅ Tipos de propiedades: $propertiesTypes")
                } else {
                    _propertyTypes.value = types
                    Log.d("FilterViewModel", "✅ Tipos API: $types")
                }
            }
            is Resource.Error -> {
                val propertiesTypes = _allProperties.value?.map { it.tipo }?.distinct() ?: getDefaultPropertyTypes()
                _propertyTypes.value = propertiesTypes
                Log.d("FilterViewModel", "⚠️ Tipos fallback: $propertiesTypes")
            }
            is Resource.Loading -> {}
        }
    }

    fun setNeighborhood(neighborhood: String?) {
        _filter.value = _filter.value?.copy(selectedNeighborhood = neighborhood)
        Log.d("FilterViewModel", "🏘️ Barrio seleccionado: ${neighborhood ?: "Ninguno"}")
    }

    fun setPropertyType(type: String?) {
        _filter.value = _filter.value?.copy(selectedPropertyType = type)
        Log.d("FilterViewModel", "🏠 Tipo seleccionado: ${type ?: "Ninguno"}")
    }

    fun setPriceRange(min: Float, max: Float) {
        _filter.value = _filter.value?.copy(priceMin = min.toInt(), priceMax = max.toInt())
        Log.d("FilterViewModel", "💰 Precio: ${min.toInt()} - ${max.toInt()}")
    }

    fun setRentOperation(isRent: Boolean) {
        _filter.value = _filter.value?.copy(isRent = isRent)
        Log.d("FilterViewModel", "🔑 Alquiler: $isRent")
    }

    fun setSaleOperation(isSale: Boolean) {
        _filter.value = _filter.value?.copy(isSale = isSale)
        Log.d("FilterViewModel", "💵 Venta: $isSale")
    }

    fun searchProperties() {
        val currentFilter = _filter.value ?: return

        Log.d("FilterViewModel", """
    🔍 BÚSQUEDA INICIADA:
    - Barrio: ${currentFilter.selectedNeighborhood ?: "Todos"}
    - Tipo: ${currentFilter.selectedPropertyType ?: "Todos"}
    - Precio: ${currentFilter.priceMin} - ${currentFilter.priceMax}
    - Alquiler: ${currentFilter.isRent}
    - Venta: ${currentFilter.isSale}
    - Garage: ${currentFilter.garage ?: "No filtrar"}
    - Balcón: ${currentFilter.balcon ?: "No filtrar"}
    - Patio: ${currentFilter.patio ?: "No filtrar"}
    - Mascotas: ${currentFilter.aceptaMascota ?: "No filtrar"}
    - Ambiente ID: ${currentFilter.ambienteId ?: "No filtrar"}
    - Estado Propiedad ID: ${currentFilter.estadoPropiedadId ?: "No filtrar"}
""".trimIndent())


        viewModelScope.launch {
            _isLoading.value = true

            when (val result = repository.searchPropertiesWithFilters(currentFilter)) {
                is Resource.Success -> {
                    val properties = result.data ?: emptyList()
                    Log.d("FilterViewModel", "✅ Búsqueda completada: ${properties.size} propiedades")

                    if (properties.isEmpty()) {
                        findAlternatives(currentFilter)
                    } else {
                        _searchResult.value = SearchResult.Success(properties)
                    }
                }
                is Resource.Error -> {
                    Log.e("FilterViewModel", "❌ Error: ${result.message}")
                    _searchResult.value = SearchResult.Error(result.message ?: "Error al buscar")
                }
                is Resource.Loading -> {}
            }

            _isLoading.value = false
        }
    }

    private suspend fun findAlternatives(originalFilter: Filter) {
        val expandedPriceFilter = originalFilter.copy(
            priceMin = (originalFilter.priceMin * 0.8).toInt(),
            priceMax = (originalFilter.priceMax * 1.2).toInt()
        )

        Log.d("FilterViewModel", "🔄 Buscando alternativas...")

        when (val result = repository.searchPropertiesWithFilters(expandedPriceFilter)) {
            is Resource.Success -> {
                val alternatives = result.data ?: emptyList()
                if (alternatives.isNotEmpty()) {
                    Log.d("FilterViewModel", "✅ ${alternatives.size} alternativas")
                    _searchResult.value = SearchResult.NoResults(
                        message = "No se encontraron propiedades exactas",
                        alternatives = alternatives,
                        suggestion = "Encontramos ${alternatives.size} propiedades similares"
                    )
                } else {
                    _searchResult.value = SearchResult.NoResults(
                        message = "No hay propiedades con esos filtros",
                        alternatives = emptyList(),
                        suggestion = "Intenta cambiar los filtros"
                    )
                }
            }
            is Resource.Error -> {
                _searchResult.value = SearchResult.NoResults(
                    message = "No se encontraron propiedades",
                    alternatives = emptyList(),
                    suggestion = "Intenta cambiar los filtros"
                )
            }
            is Resource.Loading -> {}
        }
    }

    fun getNeighborhoods(): List<String> = _neighborhoods.value ?: getDefaultNeighborhoods()
    fun getPropertyTypes(): List<String> = _propertyTypes.value ?: getDefaultPropertyTypes()
    fun getMinPrice(): Float = _priceRange.value?.minPrice ?: 0f
    fun getMaxPrice(): Float = _priceRange.value?.maxPrice ?: 1000000f
    fun getStepSize(): Float = _priceRange.value?.stepSize ?: 5000f

    fun getFilterSummary(): String {
        val f = _filter.value ?: return "Sin filtros"
        return buildString {
            appendLine("Barrio: ${f.selectedNeighborhood ?: "Todos"}")
            appendLine("Tipo: ${f.selectedPropertyType ?: "Todos"}")
            appendLine("Precio: ${f.priceMin} - ${f.priceMax}")
            if (f.isRent) appendLine("Alquiler")
            if (f.isSale) appendLine("Venta")
            if (f.garage == true) appendLine("Con garage")
            if (f.balcon == true) appendLine("Con balcón")
            if (f.patio == true) appendLine("Con patio")
            if (f.aceptaMascota == true) appendLine("Acepta mascotas")
            if (f.ambienteId != null) appendLine("Ambiente ID: ${f.ambienteId}")
            if (f.estadoPropiedadId != null) appendLine("Estado Propiedad ID: ${f.estadoPropiedadId}")
        }
    }


    private fun getDefaultNeighborhoods(): List<String> =
        listOf("Centro", "Villa Sarita", "Palomar", "San Miguel", "Itaembé Miní", "Villa Urquiza")

    private fun getDefaultPropertyTypes(): List<String> =
        listOf("Casa", "Departamento", "Duplex", "Local Comercial")
}

data class PriceRangeData(
    val minPrice: Float,
    val maxPrice: Float,
    val stepSize: Float
)

sealed class SearchResult {
    data class Success(val properties: List<Property>) : SearchResult()
    data class NoResults(
        val message: String,
        val alternatives: List<Property>,
        val suggestion: String
    ) : SearchResult()
    data class Error(val message: String) : SearchResult()
}