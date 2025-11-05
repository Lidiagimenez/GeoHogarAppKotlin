package com.main.geohogar.geohogarapp.ui.detail

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.geohogar.app.databinding.ActivityDetailBinding
import com.main.geohogar.geohogarapp.domain.model.Property
import com.main.geohogar.geohogarapp.ui.results.adapter.ImagePagerAdapter
import com.main.geohogar.geohogarapp.utils.Constants

import com.main.geohogar.geohogarapp.utils.gone
import com.main.geohogar.geohogarapp.utils.showToast
import com.main.geohogar.geohogarapp.utils.visible
import java.text.NumberFormat
import java.util.Locale

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val viewModel: DetailViewModel by viewModels()

    private var currentPropertyId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        loadPropertyData()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSend.setOnClickListener {
            sendContactRequest()
        }
    }

    private fun setupObservers() {
        viewModel.property.observe(this) { property ->
            property?.let { displayProperty(it) }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.loadingOverlay.visible()
            } else {
                binding.loadingOverlay.gone()
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                showToast(it)
                finish()
            }
        }

        viewModel.contactResult.observe(this) { success ->
            if (success) {
                showToast("Consulta enviada exitosamente")
                clearForm()
            } else {
                showToast("Error al enviar la consulta")
            }
        }
    }

    private fun loadPropertyData() {
        currentPropertyId = intent.getIntExtra(Constants.EXTRA_PROPERTY_ID, -1)

        if (currentPropertyId == -1) {
            showToast("Error: Propiedad no encontrada")
            finish()
            return
        }

        viewModel.loadPropertyDetail(currentPropertyId)
    }

    private fun displayProperty(property: Property) {
        binding.apply {
            // Tipo de propiedad
            tvPropertyType.text = property.tipo

            // Dirección
            tvAddress.text = "📍 ${property.direccion}"

            // Precio
            val formatter = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
            formatter.maximumFractionDigits = 0
            tvPrice.text = formatter.format(property.precio)

            // Detalles
            tvRooms.text = property.habitaciones.toString()
            tvBathrooms.text = property.banos.toString()
            tvGarage.text = if (property.garage) "✓" else "✗"
            tvPets.text = if (property.aceptaMascotas) "🐕" else "✗"

            // Características
            val characteristics = buildString {
                if (property.balcon) append("• Balcón\n")
                if (property.patio) append("• Patio\n")
                if (property.garage) append("• Garage\n")
                if (property.aceptaMascotas) append("• Acepta mascotas: ${property.tipoMascota}\n")
            }
            tvCharacteristics.text = characteristics.trim()

            // Descripción
            tvDescription.text = property.requisitos

            // Requisitos
            tvRequirements.text = property.contratos

            // Agente
            tvAgentName.text = "${property.agenteNombre} - ${property.agenteMatricula}"

            // Configurar ViewPager de imágenes
            setupImagePager(property.imagenes)
        }
    }

    private fun setupImagePager(images: List<String>) {
        if (images.isEmpty()) {
            binding.tvImageIndicator.text = "0 / 0"
            return
        }

        val adapter = ImagePagerAdapter(images)
        binding.vpImages.adapter = adapter

        // Indicador de página
        binding.tvImageIndicator.text = "1 / ${images.size}"

        binding.vpImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tvImageIndicator.text = "${position + 1} / ${images.size}"
            }
        })
    }

    private fun sendContactRequest() {
        val nombre = binding.etName.text.toString().trim()
        val correo = binding.etEmail.text.toString().trim()
        val telefono = binding.etPhone.text.toString().trim()
        val mensaje = binding.etMessage.text.toString().trim()

        // Validaciones
        if (nombre.isEmpty()) {
            showToast("Por favor ingresa tu nombre")
            return
        }

        if (correo.isEmpty()) {
            showToast("Por favor ingresa tu correo")
            return
        }

        if (!isValidEmail(correo)) {
            showToast("Por favor ingresa un correo válido")
            return
        }

        if (mensaje.isEmpty()) {
            showToast("Por favor ingresa un mensaje")
            return
        }

        // Enviar solicitud
        viewModel.sendContactRequest(
            nombre = nombre,
            correo = correo,
            telefono = telefono,
            mensaje = mensaje,
            propertyId = currentPropertyId
        )
    }

    private fun clearForm() {
        binding.etName.text?.clear()
        binding.etEmail.text?.clear()
        binding.etPhone.text?.clear()
        binding.etMessage.text?.clear()
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

}