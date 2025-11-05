package com.main.geohogar.geohogarapp.domain.model

data class Property(
    val id: Int,
    val tipo: String,
    val precio: Int,
    val direccion: String,
    val habitaciones: Int,
    val banos: Int,
    val pisos: Int,
    val imagenes: List<String>,
    val aceptaMascotas: Boolean,
    val ubicacionLat: Double,
    val ubicacionLng: Double,
    val requisitos: String,
    val contratos: String,
    // Campos adicionales de tu API
    val zona: String,
    val tipoOperacion: String, // "Para Venta" o "Para Alquiler"
    val garage: Boolean,
    val balcon: Boolean,
    val patio: Boolean,
    val tipoMascota: String,
    val agenteNombre: String,
    val agenteMatricula: String
)