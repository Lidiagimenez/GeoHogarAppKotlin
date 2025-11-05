package com.main.geohogar.geohogarapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FilterRequestDto(
    @SerializedName("zona") val zona: String? = null,
    @SerializedName("tipo") val tipo: String? = null,
    @SerializedName("precioMin") val precioMin: Double? = null,
    @SerializedName("precioMax") val precioMax: Double? = null,
    @SerializedName("operacion") val operacion: String? = null,
    @SerializedName("ambientes") val ambientes: Int? = null,
    @SerializedName("garage") val garage: Boolean? = null,
    @SerializedName("balcon") val balcon: Boolean? = null,
    @SerializedName("patio") val patio: Boolean? = null,
    @SerializedName("acepta_mascota") val aceptaMascota: Boolean? = null
)