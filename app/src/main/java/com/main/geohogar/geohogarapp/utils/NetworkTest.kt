package com.main.geohogar.geohogarapp.utils


import android.util.Log
import com.main.geohogar.geohogarapp.data.remote.api.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object NetworkTest {

    private const val TAG = "NetworkTest"

    /**
     * Probar conexión con la API
     */
    fun testConnection(onResult: (Boolean, String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Probando conexión a: ${Constants.BASE_URL}")

                val response = RetrofitClient.instance.getAllProperties()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        val count = apiResponse?.data?.size ?: 0
                        Log.d(TAG, "✅ Conexión exitosa! Propiedades encontradas: $count")
                        onResult(true, "Conexión exitosa! $count propiedades encontradas")
                    } else {
                        Log.e(TAG, "❌ Error: ${response.code()} - ${response.message()}")
                        onResult(false, "Error ${response.code()}: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error de conexión: ${e.localizedMessage}", e)
                withContext(Dispatchers.Main) {
                    onResult(false, "Error de conexión: ${e.localizedMessage ?: "Desconocido"}")
                }
            }
        }
    }
}