package com.main.geohogar.geohogarapp.domain.model

data class Filter(
    val selectedNeighborhood: String? = null,
    val selectedPropertyType: String? = null,
    val priceMin: Int = 0,
    val priceMax: Int = 10000000,
    val isRent: Boolean = false,
    val isSale: Boolean = false,
    val garage: Boolean? = null,
    val balcon: Boolean? = null,
    val patio: Boolean? = null,
    val aceptaMascota: Boolean? = null,
    val ambienteId: Int? = null,
    val estadoPropiedadId: Int? = null
)
