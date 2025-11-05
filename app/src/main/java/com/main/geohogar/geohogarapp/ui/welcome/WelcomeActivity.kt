package com.main.geohogar.geohogarapp.ui.welcome

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
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

        aplicarColoresTitulo() // ✅ Aplicar color dividido al título
        setupUI()

        binding.btnStart.isEnabled = false
        testApiConnection()
    }

    private fun aplicarColoresTitulo() {
        val texto = "GeoHogar"
        val spannable = SpannableString(texto)

        // Color dorado para "Geo"
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#A6893C")),
            0, 3,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Color negro para "Hogar"
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#000000")),
            3, 8,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvTitulo.text = spannable
    }

    private fun setupUI() {
        binding.btnStart.setOnClickListener {
            navigateToFilter()
        }

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
        binding.progressLoading.visibility = View.VISIBLE
        binding.btnStart.isEnabled = false

        NetworkTest.testConnection { success, message ->
            runOnUiThread {
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