package com.main.geohogar.geohogarapp.data.remote.api

import com.main.geohogar.geohogarapp.data.remote.dto.ApiResponse
import com.main.geohogar.geohogarapp.data.remote.dto.PropertyDto
import com.main.geohogar.geohogarapp.data.remote.dto.TipoInmuebleDto
import com.main.geohogar.geohogarapp.data.remote.dto.ZonaDto
import com.main.geohogar.geohogarapp.data.remote.dto.AmbienteDto
import com.main.geohogar.geohogarapp.data.remote.dto.EstadoPropiedadDto
import com.main.geohogar.geohogarapp.data.remote.dto.ContactRequestDto
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    /**
     * Obtener todas las propiedades
     * GET http://localhost:4000/api/propiedades
     */
    @GET("propiedades")
    suspend fun getAllProperties(): Response<ApiResponse<List<PropertyDto>>>

    /**
     * ✅ ACTUALIZADO: Buscar propiedades con filtros por IDs
     * GET http://localhost:4000/api/propiedades/filtrar
     */
    @GET("propiedades/filtrar")
    suspend fun getPropertiesWithFilters(
        @Query("estado") estado: Boolean? = null,
        @Query("precio_desde") precioDesde: Double? = null,
        @Query("precio_hasta") precioHasta: Double? = null,
        @Query("ID_zona") idZona: Int? = null,
        @Query("ID_tipoinmueble") idTipoInmueble: Int? = null,
        @Query("ID_ambiente") idAmbiente: Int? = null,
        @Query("ID_estadopropiedad") idEstadoPropiedad: Int? = null,
        @Query("garage") garage: Boolean? = null,
        @Query("balcon") balcon: Boolean? = null,
        @Query("patio") patio: Boolean? = null,
        @Query("acepta_mascota") aceptaMascota: Boolean? = null
    ): Response<ApiResponse<List<PropertyDto>>>

    /**
     * Obtener detalle de una propiedad
     * GET http://localhost:4000/api/propiedades/{id}
     */
    @GET("propiedades/{id}")
    suspend fun getPropertyDetail(
        @Path("id") id: Int
    ): Response<ApiResponse<PropertyDto>>

    /**
     * Obtener lista de zonas/barrios
     * GET http://localhost:4000/api/zona
     */
    @GET("zona")
    suspend fun getZonas(): Response<ApiResponse<List<ZonaDto>>>

    /**
     * Obtener tipos de inmueble
     * GET http://localhost:4000/api/tipoinmueble
     */
    @GET("tipoinmueble")
    suspend fun getTiposInmueble(): Response<ApiResponse<List<TipoInmuebleDto>>>

    /**
     * ✅ NUEVO: Obtener ambientes
     * GET http://localhost:4000/api/ambientes
     */
    @GET("ambientes")
    suspend fun getAmbientes(): Response<ApiResponse<List<AmbienteDto>>>

    /**
     * ✅ NUEVO: Obtener estados de propiedad
     * GET http://localhost:4000/api/estadopropiedad
     */
    @GET("estadopropiedad")
    suspend fun getEstadosPropiedades(): Response<ApiResponse<List<EstadoPropiedadDto>>>

    /**
     * Enviar consulta de contacto
     */
    @POST("consulta")
    suspend fun sendContactRequest(
        @Body request: ContactRequestDto
    ): Response<ApiResponse<Unit>>
}