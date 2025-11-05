package com.main.geohogar.geohogarapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ContactRequestDto(
    @SerializedName("nombre_cliente") val nombre: String,
    @SerializedName("email") val correo: String,
    @SerializedName("Mensaje") val mensaje: String? = null,
    @SerializedName("telefono") val telefono: String? = null,
    @SerializedName("ID_propiedades") val idPropiedad: Int,
    @SerializedName("quiere_visitar") val quiereVisitar: Boolean = false,
    @SerializedName("quiere_mas_info") val quiereMasInfo: Boolean = false,
    @SerializedName("estado") val estado: Boolean = true,
    @SerializedName("ID_estadoconsulta") val idEstadoConsulta: Int = 1 // o el valor por defecto que uses
)

