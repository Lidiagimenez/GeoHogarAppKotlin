package com.main.geohogar.geohogarapp.ui.welcome

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.geohogar.app.databinding.ActivityWelcomeBinding
import com.main.geohogar.geohogarapp.ui.filter.FilterActivity
import com.main.geohogar.geohogarapp.utils.NetworkTest
import com.main.geohogar.geohogarapp.utils.showToast

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()

        // Deshabilitar botón al inicio
        binding.btnStart.isEnabled = false

        // Probar conexión automáticamente
        testApiConnection()
    }

    private fun setupUI() {
        binding.btnStart.setOnClickListener {
            navigateToFilter()
        }

        // Long click para reintentar conexión manualmente
        binding.btnStart.setOnLongClickListener {
            testApiConnection()
            true
        }
    }

    private fun navigateToFilter() {
        val intent = Intent(this, FilterActivity::class.java)
        startActivity(intent)
    }

    private fun testApiConnection() {
        // Mostrar indicador de carga
        binding.progressLoading.visibility = View.VISIBLE
        binding.btnStart.isEnabled = false

        NetworkTest.testConnection { success, message ->
            runOnUiThread {
                // Ocultar indicador de carga
                binding.progressLoading.visibility = View.GONE

                Log.d("WelcomeActivity", "Test Result: $success - $message")

                if (success) {
                    showToast("Conexión exitosa")
                    binding.btnStart.isEnabled = true
                } else {
                    showToast("Error de conexión")
                    binding.btnStart.isEnabled = false

                    AlertDialog.Builder(this)
                        .setTitle("❌ Error de Conexión")
                        .setMessage(message)
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }
    }
}