package com.main.geohogar.geohogarapp.data.repository

import android.util.Log
import com.main.geohogar.geohogarapp.data.remote.api.RetrofitClient
import com.main.geohogar.geohogarapp.data.remote.dto.ContactRequestDto
import com.main.geohogar.geohogarapp.data.remote.dto.PropertyDto
import com.main.geohogar.geohogarapp.data.remote.dto.toDomain
import com.main.geohogar.geohogarapp.domain.model.Filter
import com.main.geohogar.geohogarapp.domain.model.Property
import com.main.geohogar.geohogarapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PropertyRepository {

    private val api = RetrofitClient.instance

    // Cache para mapear nombres a IDs
    private var cachedPropertiesDto: List<PropertyDto>? = null

    suspend fun getAllProperties(): Resource<List<Property>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAllProperties()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.data != null) {
                    cachedPropertiesDto = apiResponse.data // Guardar DTOs completos
                    val properties = apiResponse.data.map { it.toDomain() }
                    Resource.Success(properties)
                } else {
                    Resource.Error("No se encontraron propiedades")
                }
            } else {
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("PropertyRepository", "Error getAllProperties", e)
            Resource.Error("Error de conexión: ${e.localizedMessage}")
        }
    }

    suspend fun searchPropertiesWithFilters(filter: Filter): Resource<List<Property>> = withContext(Dispatchers.IO) {
        try {
            // Asegurarse de tener el cache
            if (cachedPropertiesDto == null) {
                getAllProperties()
            }

            // Mapear nombres a IDs
            val idZona = getZonaIdFromName(filter.selectedNeighborhood)
            val idTipoInmueble = getTipoInmuebleIdFromName(filter.selectedPropertyType)
            val idEstadoPropiedad = getEstadoPropiedadId(filter.isRent, filter.isSale)

            Log.d("PropertyRepository", """
                🔍 Enviando filtros:
                - ID_zona: $idZona (${filter.selectedNeighborhood})
                - ID_tipoinmueble: $idTipoInmueble (${filter.selectedPropertyType})
                - ID_estadopropiedad: $idEstadoPropiedad
                - Precio: ${filter.priceMin} - ${filter.priceMax}
            """.trimIndent())

            val response = api.getPropertiesWithFilters(
                precioDesde = filter.priceMin.toDouble(),
                precioHasta = filter.priceMax.toDouble(),
                idZona = getZonaIdFromName(filter.selectedNeighborhood),
                idTipoInmueble = getTipoInmuebleIdFromName(filter.selectedPropertyType),
                idAmbiente = filter.ambienteId,
                idEstadoPropiedad = getEstadoPropiedadId(filter.isRent, filter.isSale),
                garage = filter.garage,
                balcon = filter.balcon,
                patio = filter.patio,
                aceptaMascota = filter.aceptaMascota
            )



            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.data != null) {
                    val properties = apiResponse.data.map { it.toDomain() }
                    Log.d("PropertyRepository", "✅ Filtrado exitoso: ${properties.size} propiedades")
                    Resource.Success(properties)
                } else {
                    Resource.Error("No se encontraron propiedades con esos filtros")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("PropertyRepository", "Error ${response.code()}: $errorBody")
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("PropertyRepository", "Error searchPropertiesWithFilters", e)
            Resource.Error("Error de conexión: ${e.localizedMessage}")
        }
    }

    /**
     * Mapear nombre de zona a ID desde el cache
     */
    private fun getZonaIdFromName(zonaName: String?): Int? {
        if (zonaName == null) return null

        val zonaId = cachedPropertiesDto
            ?.firstOrNull { it.zona.nombre == zonaName }
            ?.zona?.id

        Log.d("PropertyRepository", "Mapeo zona: '$zonaName' → ID: $zonaId")
        return zonaId
    }

    /**
     * Mapear nombre de tipo inmueble a ID desde el cache
     */
    private fun getTipoInmuebleIdFromName(tipoName: String?): Int? {
        if (tipoName == null) return null

        val tipoId = cachedPropertiesDto
            ?.firstOrNull { it.tipoInmueble.nombre == tipoName }
            ?.tipoInmueble?.id

        Log.d("PropertyRepository", "Mapeo tipo: '$tipoName' → ID: $tipoId")
        return tipoId
    }

    /**
     * Mapear operación a ID de estado propiedad
     */
    private fun getEstadoPropiedadId(isRent: Boolean, isSale: Boolean): Int? {
        return when {
            isRent && !isSale -> {
                // Buscar ID de "Para Alquiler"
                val id = cachedPropertiesDto
                    ?.firstOrNull { it.estadoPropiedad.nombre.contains("Alquiler", ignoreCase = true) }
                    ?.estadoPropiedad?.id
                Log.d("PropertyRepository", "Mapeo operación: Alquiler → ID: $id")
                id
            }
            isSale && !isRent -> {
                // Buscar ID de "Para Venta"
                val id = cachedPropertiesDto
                    ?.firstOrNull { it.estadoPropiedad.nombre.contains("Venta", ignoreCase = true) }
                    ?.estadoPropiedad?.id
                Log.d("PropertyRepository", "Mapeo operación: Venta → ID: $id")
                id
            }
            else -> {
                Log.d("PropertyRepository", "Mapeo operación: Todos → ID: null")
                null // Ambos o ninguno
            }
        }
    }

    suspend fun getPropertyDetail(id: Int): Resource<Property> = withContext(Dispatchers.IO) {
        try {
            Log.d("PropertyRepository", "🔍 Obteniendo detalle de propiedad ID: $id")
            val response = api.getPropertyDetail(id)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.data != null) {
                    val property = apiResponse.data.toDomain()
                    Log.d("PropertyRepository", "✅ Detalle obtenido: ${property.direccion}")
                    Resource.Success(property)
                } else {
                    Resource.Error("Propiedad no encontrada")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("PropertyRepository", "Error ${response.code()}: $errorBody")
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("PropertyRepository", "Error getPropertyDetail", e)
            Resource.Error("Error de conexión: ${e.localizedMessage}")
        }
    }

    suspend fun getZonas(): Resource<List<String>> = withContext(Dispatchers.IO) {
        try {
            val allProperties = getAllProperties()
            if (allProperties is Resource.Success) {
                val zonas = allProperties.data
                    ?.map { it.zona }
                    ?.distinct()
                    ?.sorted()
                    ?: emptyList()

                if (zonas.isNotEmpty()) {
                    Resource.Success(zonas)
                } else {
                    Resource.Success(getDefaultZonas())
                }
            } else {
                Resource.Success(getDefaultZonas())
            }
        } catch (e: Exception) {
            Resource.Success(getDefaultZonas())
        }
    }

    suspend fun getTiposInmueble(): Resource<List<String>> = withContext(Dispatchers.IO) {
        try {
            val allProperties = getAllProperties()
            if (allProperties is Resource.Success) {
                val tipos = allProperties.data
                    ?.map { it.tipo }
                    ?.distinct()
                    ?.sorted()
                    ?: emptyList()

                if (tipos.isNotEmpty()) {
                    Resource.Success(tipos)
                } else {
                    Resource.Success(getDefaultTiposInmueble())
                }
            } else {
                Resource.Success(getDefaultTiposInmueble())
            }
        } catch (e: Exception) {
            Resource.Success(getDefaultTiposInmueble())
        }
    }

    suspend fun sendContactRequest(request: ContactRequestDto): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.sendContactRequest(request)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Error al enviar: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Error de conexión: ${e.localizedMessage}")
        }
    }

    private fun getDefaultZonas(): List<String> {
        return listOf("Centro", "Villa Sarita", "Palomar", "San Miguel", "Itaembé Miní", "Villa Urquiza")
    }

    private fun getDefaultTiposInmueble(): List<String> {
        return listOf("Casa", "Departamento", "Duplex", "Local Comercial", "Terreno")
    }
}
