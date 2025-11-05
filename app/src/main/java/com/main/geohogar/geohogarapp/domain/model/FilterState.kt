package com.main.geohogar.geohogarapp.domain.model

data class Filter(
    val selectedNeighborhood: String? = null,
    val selectedPropertyType: String? = null,
    val priceMin: Float = 0f,
    val priceMax: Float = 10000000f,
    val isRent: Boolean = false,
    val isSale: Boolean = false,
    val garage: Boolean? = null,  // ✅ Cambio: Boolean? en lugar de Boolean
    val balcon: Boolean? = null,  // ✅ Cambio: Boolean? en lugar de Boolean
    val patio: Boolean? = null,   // ✅ Cambio: Boolean? en lugar de Boolean
    val aceptaMascota: Boolean? = null,  // ✅ Cambio: Boolean? en lugar de Boolean
    val ambienteId: Int? = null,
    val estadoPropiedadId: Int? = null
)