package com.main.geohogar.geohogarapp.data.remote.dto

import com.main.geohogar.geohogarapp.domain.model.Property
import com.google.gson.annotations.SerializedName

data class PropertyDto(
    @SerializedName("ID_propiedades") val idPropiedades: Int,
    @SerializedName("direccion") val direccion: String,
    @SerializedName("precio") val precio: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("latitud") val latitud: String,
    @SerializedName("longitud") val longitud: String,
    @SerializedName("estado") val estado: Boolean,
    @SerializedName("ID_zona") val idZona: Int,
    @SerializedName("ID_tipoinmueble") val idTipoInmueble: Int,
    @SerializedName("ID_estadopropiedad") val idEstadoPropiedad: Int,
    @SerializedName("ID_ambiente") val idAmbiente: Int,
    @SerializedName("garage") val garage: Boolean,
    @SerializedName("balcon") val balcon: Boolean,
    @SerializedName("patio") val patio: Boolean,
    @SerializedName("acepta_mascota") val aceptaMascota: Boolean,
    @SerializedName("ID_Mascota") val idMascota: Int? = null,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("agenteinmobiliario") val agenteInmobiliario: AgenteInmobiliarioDto? = null,
    @SerializedName("zona") val zona: ZonaDto,
    @SerializedName("tipoinmueble") val tipoInmueble: TipoInmuebleDto,
    @SerializedName("estadopropiedad") val estadoPropiedad: EstadoPropiedadDto,
    @SerializedName("ambientes") val ambientes: AmbienteDto,  // ✅ CORREGIDO: Singular
    @SerializedName("Mascota") val mascota: MascotaDto? = null,
    @SerializedName("imagenes") val imagenes: List<ImagenDto>? = null

)

data class AgenteInmobiliarioDto(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("matricula") val matricula: String
)

// ✅ ZonaDto - CORRECTO
data class ZonaDto(
    @SerializedName("ID_zona") val id: Int,
    @SerializedName("zona") val nombre: String
)

// ✅ TipoInmuebleDto - CORRECTO
data class TipoInmuebleDto(
    @SerializedName("ID_tipoinmueble") val id: Int,
    @SerializedName("inmueble") val nombre: String
)

// ✅ AmbienteDto - CORREGIDO: Singular
data class AmbienteDto(
    @SerializedName("ID_ambiente") val id: Int,
    @SerializedName("ambientes") val nombre: String
)

// ✅ EstadoPropiedadDto - CORRECTO
data class EstadoPropiedadDto(
    @SerializedName("ID_estadopropiedad") val id: Int,
    @SerializedName("estado_propiedad") val nombre: String
)

data class MascotaDto(
    @SerializedName("ID_Mascota") val idMascota: Int,
    @SerializedName("Mascota") val mascota: String
)

data class ImagenDto(
    @SerializedName("ID_imagen") val idImagen: Int,
    @SerializedName("URL") val url: String,
    @SerializedName("estado") val estado: Boolean
)

// ✅ Mapper CORREGIDO
fun PropertyDto.toDomain() = Property(
    id = idPropiedades,
    tipo = tipoInmueble.nombre,  // ✅ Usar .nombre en lugar de .inmueble
    precio = precio.toDoubleOrNull()?.toInt() ?: 0,
    direccion = direccion,
    habitaciones = extractHabitacionesFromAmbientes(ambientes.nombre),  // ✅ Usar .nombre
    banos = 1,
    pisos = 1,
    imagenes = imagenes?.filter { it.estado }?.map { it.url } ?: emptyList(),

    aceptaMascotas = aceptaMascota,
    ubicacionLat = latitud.toDoubleOrNull() ?: 0.0,
    ubicacionLng = longitud.toDoubleOrNull() ?: 0.0,
    requisitos = descripcion,
    contratos = estadoPropiedad.nombre,  // ✅ Usar .nombre
    zona = zona.nombre,  // ✅ Usar .nombre
    tipoOperacion = estadoPropiedad.nombre,  // ✅ Usar .nombre
    garage = garage,
    balcon = balcon,
    patio = patio,
    tipoMascota = mascota?.mascota ?: "No acepta mascotas",
    agenteNombre = agenteInmobiliario?.nombre ?: "Sin agente",
    agenteMatricula = agenteInmobiliario?.matricula ?: "Sin matrícula"

)

private fun extractHabitacionesFromAmbientes(ambientes: String): Int {
    return ambientes.filter { it.isDigit() }.firstOrNull()?.toString()?.toIntOrNull() ?: 1
}