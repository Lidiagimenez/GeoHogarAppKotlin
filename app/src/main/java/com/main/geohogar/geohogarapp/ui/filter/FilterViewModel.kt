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

data class PriceRangeData(
    val minPrice: Float,
    val maxPrice: Float,
    val stepSize: Float
)

sealed class SearchResult {
    data class Success(val properties: List<Property>) : SearchResult()
    data class NoResults(
        val message: String,
        val suggestion: String,
        val alternatives: List<Property> = emptyList()
    ) : SearchResult()
    data class Error(val message: String) : SearchResult()
}

class FilterViewModel : ViewModel() {

    private val repository = PropertyRepository()

    // LiveData para opciones de filtro
    private val _neighborhoods = MutableLiveData<List<String>>()
    val neighborhoods: LiveData<List<String>> = _neighborhoods

    private val _propertyTypes = MutableLiveData<List<String>>()
    val propertyTypes: LiveData<List<String>> = _propertyTypes

    private val _priceRange = MutableLiveData<PriceRangeData>()
    val priceRange: LiveData<PriceRangeData> = _priceRange

    // Estado del filtro
    private val _filter = MutableLiveData<Filter>()
    val filter: LiveData<Filter> = _filter

    // Estado de carga y resultados
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _searchResult = MutableLiveData<SearchResult>()
    val searchResult: LiveData<SearchResult> = _searchResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // ✅ NUEVO: LiveData para habilitar botón de búsqueda
    private val _isSearchEnabled = MutableLiveData<Boolean>(false)
    val isSearchEnabled: LiveData<Boolean> = _isSearchEnabled

    // ✅ Variables para rastrear filtros
    private var hasPriceFilter: Boolean = false

    init {
        // Inicializar filtro vacío
        _filter.value = Filter()
        loadFilterOptions()
    }

    private fun loadFilterOptions() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // ✅ Cargar todas las propiedades primero (para cache) y esperar a que termine
                val allPropertiesResult = repository.getAllProperties()

                if (allPropertiesResult is Resource.Success) {
                    // ✅ Cargar zonas usando el método del repositorio
                    when (val zonasResult = repository.getZonas()) {
                        is Resource.Success -> {
                            val zonas = zonasResult.data ?: emptyList()
                            _neighborhoods.value = zonas
                            Log.d("FilterViewModel", "✅ Cargados ${zonas.size} barrios: $zonas")
                        }

                        is Resource.Error -> {
                            Log.e("FilterViewModel", "❌ Error cargando zonas: ${zonasResult.message}")
                            _neighborhoods.value = emptyList()
                        }

                        else -> {}
                    }

                    // ✅ Cargar tipos de inmueble usando el método del repositorio
                    when (val tiposResult = repository.getTiposInmueble()) {
                        is Resource.Success -> {
                            val tipos = tiposResult.data ?: emptyList()
                            _propertyTypes.value = tipos
                            Log.d("FilterViewModel", "✅ Cargados ${tipos.size} tipos: $tipos")
                        }

                        is Resource.Error -> {
                            Log.e("FilterViewModel", "❌ Error cargando tipos: ${tiposResult.message}")
                            _propertyTypes.value = emptyList()
                        }

                        else -> {}
                    }

                    // ✅ Calcular rango de precios desde todas las propiedades
                    val properties = allPropertiesResult.data ?: emptyList()
                    val prices = properties.mapNotNull { it.precio.toDouble() }

                    if (prices.isNotEmpty()) {
                        var minPrice = prices.minOrNull()?.toFloat() ?: 0f
                        var maxPrice = prices.maxOrNull()?.toFloat() ?: 10000000f

                        // ✅ FIX: Handle case where min and max price are the same
                        if (minPrice == maxPrice) {
                            maxPrice += 10000f // Add a default amount to create a valid range
                        }

                        val stepSize = calculateStepSize(minPrice, maxPrice)

                        _priceRange.value = PriceRangeData(minPrice, maxPrice, stepSize)
                        Log.d(
                            "FilterViewModel",
                            "✅ Rango de precios: $minPrice - $maxPrice (step: $stepSize)"
                        )
                    } else {
                        setDefaultPriceRange()
                    }
                } else {
                    Log.e("FilterViewModel", "❌ Error cargando todas las propiedades: ${allPropertiesResult.message}")
                    _error.value = "Error al cargar los filtros iniciales"
                    _neighborhoods.value = emptyList()
                    _propertyTypes.value = emptyList()
                    setDefaultPriceRange()
                }

            } catch (e: Exception) {
                Log.e("FilterViewModel", "❌ Error cargando filtros: ${e.message}")
                _error.value = "Error al cargar los filtros"
                _neighborhoods.value = emptyList()
                _propertyTypes.value = emptyList()
                setDefaultPriceRange()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun setDefaultPriceRange() {
        _priceRange.value = PriceRangeData(0f, 10000000f, 100000f)
        Log.d("FilterViewModel", "⚠️ Usando rango de precios por defecto")
    }

    private fun calculateStepSize(min: Float, max: Float): Float {
        val range = max - min

        // Calcular un step aproximado basado en el rango
        val roughStep = when {
            range > 1000000 -> 100000f
            range > 500000 -> 50000f
            range > 100000 -> 10000f
            range > 50000 -> 5000f
            range > 10000 -> 1000f
            range > 1000 -> 100f
            else -> 10f
        }

        // ✅ Asegurar que sea divisor exacto del rango
        val stepsCount = (range / roughStep).toInt().coerceAtLeast(1)
        return range / stepsCount
    }

    // ✅ Métodos para actualizar selecciones
    fun setNeighborhood(neighborhood: String?) {
        val currentFilter = _filter.value ?: Filter()
        _filter.value = currentFilter.copy(selectedNeighborhood = neighborhood)
        updateSearchButtonState()
        Log.d("FilterViewModel", "📍 Barrio seleccionado: $neighborhood")
    }

    fun setPropertyType(type: String?) {
        val currentFilter = _filter.value ?: Filter()
        _filter.value = currentFilter.copy(selectedPropertyType = type)
        updateSearchButtonState()
        Log.d("FilterViewModel", "🏠 Tipo seleccionado: $type")
    }

    fun setPriceRange(min: Float, max: Float) {
        val currentFilter = _filter.value ?: Filter()
        _filter.value = currentFilter.copy(priceMin = min, priceMax = max)

        val priceData = _priceRange.value
        hasPriceFilter = priceData != null &&
                (min > priceData.minPrice || max < priceData.maxPrice)

        updateSearchButtonState()
        Log.d("FilterViewModel", "💰 Rango de precio: $min - $max (filtro activo: $hasPriceFilter)")
    }

    fun setRentOperation(isRent: Boolean) {
        val currentFilter = _filter.value ?: Filter()
        _filter.value = currentFilter.copy(isRent = isRent)
        updateSearchButtonState()
        Log.d("FilterViewModel", "🔑 Alquiler: $isRent")
    }

    fun setSaleOperation(isSale: Boolean) {
        val currentFilter = _filter.value ?: Filter()
        _filter.value = currentFilter.copy(isSale = isSale)
        updateSearchButtonState()
        Log.d("FilterViewModel", "💵 Venta: $isSale")
    }

    // ✅ Actualizar estado del botón de búsqueda
    private fun updateSearchButtonState() {
        val currentFilter = _filter.value

        val hasAtLeastOneFilter = currentFilter != null && (
                !currentFilter.selectedNeighborhood.isNullOrBlank() ||
                        !currentFilter.selectedPropertyType.isNullOrBlank() ||
                        hasPriceFilter ||
                        currentFilter.garage == true ||      // ✅ Cambio: == true
                        currentFilter.balcon == true ||      // ✅ Cambio: == true
                        currentFilter.patio == true ||       // ✅ Cambio: == true
                        currentFilter.aceptaMascota == true  // ✅ Cambio: == true
                )

        _isSearchEnabled.postValue(hasAtLeastOneFilter)
        Log.d(
            "FilterViewModel",
            "🔘 Botón búsqueda: ${if (hasAtLeastOneFilter) "HABILITADO ✅" else "DESHABILITADO ❌"}"
        )
    }

    fun searchProperties() {
        val currentFilter = _filter.value ?: return

        viewModelScope.launch {
            try {
                _isLoading.value = true

                // ✅ Si se seleccionó "Todos", convertir a null
                val filterToUse = currentFilter.copy(
                    selectedNeighborhood = if (currentFilter.selectedNeighborhood == "Todos") null else currentFilter.selectedNeighborhood,
                    selectedPropertyType = if (currentFilter.selectedPropertyType == "Todos") null else currentFilter.selectedPropertyType
                )

                Log.d(
                    "FilterViewModel", """
                    🔍 Iniciando búsqueda con filtros:
                    - Barrio: ${filterToUse.selectedNeighborhood ?: "Todos"}
                    - Tipo: ${filterToUse.selectedPropertyType ?: "Todos"}
                    - Precio: ${filterToUse.priceMin} - ${filterToUse.priceMax}
                    - Alquiler: ${filterToUse.isRent}
                    - Venta: ${filterToUse.isSale}
                """.trimIndent()
                )

                when (val result = repository.searchPropertiesWithFilters(filterToUse)) {
                    is Resource.Success -> {
                        val results = result.data ?: emptyList()
                        when {
                            results.isNotEmpty() -> {
                                _searchResult.value = SearchResult.Success(results)
                                Log.d(
                                    "FilterViewModel",
                                    "✅ ${results.size} propiedades encontradas"
                                )
                            }

                            else -> {
                                _searchResult.value = SearchResult.NoResults(
                                    message = "No se encontraron propiedades con estos filtros",
                                    suggestion = "Intenta modificar algunos criterios de búsqueda"
                                )
                                Log.d("FilterViewModel", "⚠️ Sin resultados")
                            }
                        }
                    }

                    is Resource.Error -> {
                        Log.e("FilterViewModel", "❌ Error en búsqueda: ${result.message}")
                        _searchResult.value =
                            SearchResult.Error(result.message ?: "Error al buscar propiedades")
                    }

                    is Resource.Loading -> {
                        Log.d("FilterViewModel", "⏳ Cargando...")
                    }
                }

            } catch (e: Exception) {
                Log.e("FilterViewModel", "❌ Excepción en búsqueda: ${e.message}", e)
                _searchResult.value =
                    SearchResult.Error("Error al buscar propiedades: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getFilterSummary(): String {
        val filter = _filter.value ?: return "Sin filtros aplicados"
        val parts = mutableListOf<String>()

        filter.selectedNeighborhood?.let {
            if (it != "Todos") parts.add("Barrio: $it")
            else parts.add("📍 Todos los barrios")
        }
        filter.selectedPropertyType?.let {
            if (it != "Todos") parts.add("Tipo: $it")
            else parts.add("🏠 Todos los tipos")
        }

        if (hasPriceFilter) {
            parts.add("💰 $${filter.priceMin.toInt()} - $${filter.priceMax.toInt()}")
        }

        if (filter.isRent) parts.add("🔑 Alquiler")
        if (filter.isSale) parts.add("💵 Venta")

        // ✅ CORRECCIÓN: Usar == true para Boolean?
        if (filter.garage == true) parts.add("🚗 Garage")
        if (filter.balcon == true) parts.add("🌿 Balcón")
        if (filter.patio == true) parts.add("🏡 Patio")
        if (filter.aceptaMascota == true) parts.add("🐾 Acepta mascotas")

        return if (parts.isEmpty()) "Sin filtros aplicados" else parts.joinToString(" • ")
    }
}
