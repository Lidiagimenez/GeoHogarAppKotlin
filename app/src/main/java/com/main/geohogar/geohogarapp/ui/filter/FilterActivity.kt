package com.main.geohogar.geohogarapp.ui.filter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.geohogar.app.R
import com.geohogar.app.databinding.ActivityFilterBinding
import com.main.geohogar.geohogarapp.ui.results.ResultsActivity
import com.main.geohogar.geohogarapp.utils.Constants
import com.main.geohogar.geohogarapp.utils.showToast
import java.text.NumberFormat
import java.util.Locale

class FilterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFilterBinding
    private val viewModel: FilterViewModel by viewModels()

    private var selectedNeighborhoodView: CardView? = null
    private var selectedPropertyTypeView: CardView? = null

    companion object {
        const val ALL_OPTION = "Todos"
        private const val TAG = "FilterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityFilterBinding.inflate(layoutInflater)
            setContentView(binding.root)

            Log.d(TAG, "✅ Activity creada correctamente")

            setupUI()
            setupObservers()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en onCreate", e)
            showToast("Error al inicializar la pantalla: ${e.message}")
            finish()
        }
    }

    private fun setupUI() {
        try {
            binding.btnBack.setOnClickListener { finish() }
            binding.btnSearch.setOnClickListener { performSearch() }
            setupOperationCheckboxes()

            // ✅ Inicialmente deshabilitado
            binding.btnSearch.isEnabled = false
            binding.btnSearch.alpha = 0.5f

            Log.d(TAG, "✅ UI configurada correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en setupUI", e)
            showToast("Error al configurar la interfaz")
        }
    }

    private fun setupObservers() {
        try {
            // Observar estado de carga
            viewModel.isLoading.observe(this) { isLoading ->
                try {
                    val isSearchEnabled = viewModel.isSearchEnabled.value ?: false
                    binding.btnSearch.isEnabled = !isLoading && isSearchEnabled
                    binding.btnSearch.text = if (isLoading) "Cargando..." else getString(R.string.btn_search)
                    Log.d(TAG, "Loading: $isLoading, SearchEnabled: $isSearchEnabled")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error actualizando estado de carga", e)
                }
            }

            // Observar barrios
            viewModel.neighborhoods.observe(this) { neighborhoods ->
                try {
                    if (neighborhoods.isNotEmpty()) {
                        Log.d(TAG, "✅ Recibidos ${neighborhoods.size} barrios")
                        setupNeighborhoods(neighborhoods)
                    } else {
                        Log.w(TAG, "⚠️ Lista de barrios vacía")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error configurando barrios", e)
                    showToast("Error al cargar barrios")
                }
            }

            // Observar tipos de propiedad
            viewModel.propertyTypes.observe(this) { types ->
                try {
                    if (types.isNotEmpty()) {
                        Log.d(TAG, "✅ Recibidos ${types.size} tipos")
                        setupPropertyTypes(types)
                    } else {
                        Log.w(TAG, "⚠️ Lista de tipos vacía")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error configurando tipos", e)
                    showToast("Error al cargar tipos de propiedad")
                }
            }

            // Observar rango de precios
            viewModel.priceRange.observe(this) { priceData ->
                try {
                    Log.d(TAG, "✅ Rango recibido: ${priceData.minPrice} - ${priceData.maxPrice} (step: ${priceData.stepSize})")
                    setupPriceSlider(priceData)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error configurando slider", e)
                    showToast("Error al configurar filtro de precios")
                }
            }

            // Observar estado del botón de búsqueda
            viewModel.isSearchEnabled.observe(this) { isEnabled ->
                try {
                    val isLoading = viewModel.isLoading.value ?: false
                    binding.btnSearch.isEnabled = isEnabled && !isLoading
                    binding.btnSearch.alpha = if (isEnabled) 1.0f else 0.5f
                    Log.d(TAG, "Botón búsqueda: ${if (isEnabled) "HABILITADO" else "DESHABILITADO"}")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error actualizando botón", e)
                }
            }

            // Observar resultados de búsqueda
            viewModel.searchResult.observe(this) { result ->
                try {
                    handleSearchResult(result)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error manejando resultados", e)
                    showToast("Error al procesar resultados")
                }
            }

            // Observar errores
            viewModel.error.observe(this) { error ->
                error?.let {
                    Log.e(TAG, "❌ Error del ViewModel: $it")
                    showToast(it)
                }
            }

            Log.d(TAG, "✅ Observers configurados correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en setupObservers", e)
            showToast("Error al configurar observadores")
        }
    }

    private fun setupNeighborhoods(neighborhoods: List<String>) {
        binding.llNeighborhoods.removeAllViews()

        // Agregar opción "Todos" primero
        val allOption = createOptionView(ALL_OPTION)
        allOption.setOnClickListener {
            selectNeighborhood(allOption, ALL_OPTION)
        }
        binding.llNeighborhoods.addView(allOption)

        // Agregar el resto de barrios
        neighborhoods.forEach { neighborhood ->
            val optionView = createOptionView(neighborhood)
            optionView.setOnClickListener {
                selectNeighborhood(optionView, neighborhood)
            }
            binding.llNeighborhoods.addView(optionView)
        }
    }

    private fun setupPropertyTypes(types: List<String>) {
        binding.llPropertyTypes.removeAllViews()

        // Agregar opción "Todos" primero
        val allOption = createOptionView(ALL_OPTION)
        allOption.setOnClickListener {
            selectPropertyType(allOption, ALL_OPTION)
        }
        binding.llPropertyTypes.addView(allOption)

        // Agregar el resto de tipos
        types.forEach { type ->
            val optionView = createOptionView(type)
            optionView.setOnClickListener {
                selectPropertyType(optionView, type)
            }
            binding.llPropertyTypes.addView(optionView)
        }
    }

    private fun createOptionView(text: String): CardView {
        val cardView = LayoutInflater.from(this)
            .inflate(R.layout.item_filter_option, binding.llNeighborhoods, false) as CardView
        val textView = cardView.findViewById<TextView>(R.id.tvOptionName)
        textView.text = text
        return cardView
    }

    private fun selectNeighborhood(cardView: CardView, neighborhood: String) {
        selectedNeighborhoodView?.let { unselectCard(it) }

        if (selectedNeighborhoodView == cardView) {
            selectedNeighborhoodView = null
            viewModel.setNeighborhood(null)
        } else {
            selectCard(cardView)
            selectedNeighborhoodView = cardView
            viewModel.setNeighborhood(neighborhood)
            Log.d(TAG, "✅ Seleccionado barrio: $neighborhood")
        }
    }

    private fun selectPropertyType(cardView: CardView, type: String) {
        selectedPropertyTypeView?.let { unselectCard(it) }

        if (selectedPropertyTypeView == cardView) {
            selectedPropertyTypeView = null
            viewModel.setPropertyType(null)
        } else {
            selectCard(cardView)
            selectedPropertyTypeView = cardView
            viewModel.setPropertyType(type)
            Log.d(TAG, "✅ Seleccionado tipo: $type")
        }
    }

    private fun selectCard(cardView: CardView) {
        val textView = cardView.findViewById<TextView>(R.id.tvOptionName)
        textView.background = ContextCompat.getDrawable(this, R.drawable.bg_card_selected)
        textView.setTextColor(ContextCompat.getColor(this, R.color.gold))
    }

    private fun unselectCard(cardView: CardView) {
        val textView = cardView.findViewById<TextView>(R.id.tvOptionName)
        textView.background = ContextCompat.getDrawable(this, R.drawable.bg_card_unselected)
        textView.setTextColor(ContextCompat.getColor(this, R.color.black))
    }

    private fun setupPriceSlider(priceData: PriceRangeData) {
        try {
            with(binding.rsPrice) {
                valueFrom = priceData.minPrice
                valueTo = priceData.maxPrice
                stepSize = priceData.stepSize
                setValues(priceData.minPrice, priceData.maxPrice)

                Log.d(TAG, "Slider configurado: ${priceData.minPrice} - ${priceData.maxPrice} step:${priceData.stepSize}")
            }

            binding.rsPrice.addOnChangeListener { slider, _, _ ->
                val values = slider.values
                viewModel.setPriceRange(values[0], values[1])
                updatePriceDisplay(values[0].toInt(), values[1].toInt())
            }

            updatePriceDisplay(priceData.minPrice.toInt(), priceData.maxPrice.toInt())
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error configurando slider: ${e.message}", e)
            showToast("Error al configurar filtro de precios")
        }
    }

    private fun updatePriceDisplay(min: Int, max: Int) {
        try {
            val priceData = viewModel.priceRange.value
            if (priceData == null) {
                binding.tvPriceRange.text = "Cualquier precio"
                return
            }

            val formatter = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
            formatter.maximumFractionDigits = 0

            val displayText = when {
                min == priceData.minPrice.toInt() && max == priceData.maxPrice.toInt() ->
                    "Cualquier precio"
                min == priceData.minPrice.toInt() ->
                    "Hasta ${formatter.format(max)}"
                max == priceData.maxPrice.toInt() ->
                    "Desde ${formatter.format(min)}"
                else ->
                    "${formatter.format(min)} - ${formatter.format(max)}"
            }

            binding.tvPriceRange.text = displayText
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error actualizando display de precio", e)
        }
    }


    private fun setupOperationCheckboxes() {
        binding.cbRent.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setRentOperation(isChecked)
        }
        binding.cbSale.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setSaleOperation(isChecked)
        }
    }

    private fun performSearch() {
        try {
            val filter = viewModel.filter.value

            //if (filter?.isRent == false && filter.isSale == false) {
            //    showToast("Selecciona al menos una operación (Alquiler o Venta)")
            //    return
            //}

            // Mostrar resumen de filtros activos
            binding.tvActiveFilters.apply {
                text = viewModel.getFilterSummary()
                visibility = View.VISIBLE
            }

            viewModel.searchProperties()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en performSearch", e)
            showToast("Error al realizar la búsqueda")
        }
    }

    private fun handleSearchResult(result: SearchResult) {
        when (result) {
            is SearchResult.Success -> {
                navigateToResults(result.properties)
            }
            is SearchResult.NoResults -> {
                showNoResultsDialog(result)
            }
            is SearchResult.Error -> {
                showToast(result.message)
            }
        }
    }

    private fun showNoResultsDialog(result: SearchResult.NoResults) {
        AlertDialog.Builder(this)
            .setTitle("Sin resultados")
            .setMessage("${result.message}\n\n${result.suggestion}")
            .setPositiveButton("Ver alternativas") { _, _ ->
                if (result.alternatives.isNotEmpty()) {
                    navigateToResults(result.alternatives)
                }
            }
            .setNegativeButton("Cambiar filtros", null)
            .show()
    }

    private fun navigateToResults(properties: List<com.main.geohogar.geohogarapp.domain.model.Property>) {
        try {
            val filter = viewModel.filter.value
            val intent = Intent(this, ResultsActivity::class.java).apply {
                val neighborhood = if (filter?.selectedNeighborhood == ALL_OPTION) null else filter?.selectedNeighborhood
                val propertyType = if (filter?.selectedPropertyType == ALL_OPTION) null else filter?.selectedPropertyType

                putExtra(Constants.EXTRA_NEIGHBORHOOD, neighborhood)
                putExtra(Constants.EXTRA_PROPERTY_TYPE, propertyType)
                putExtra(Constants.EXTRA_PRICE_MIN, filter?.priceMin ?: 0f)
                putExtra(Constants.EXTRA_PRICE_MAX, filter?.priceMax ?: 10000000f)
                putExtra(Constants.EXTRA_IS_RENT, filter?.isRent ?: false)
                putExtra(Constants.EXTRA_IS_SALE, filter?.isSale ?: false)
                putExtra(Constants.EXTRA_GARAGE, filter?.garage ?: false)
                putExtra(Constants.EXTRA_BALCON, filter?.balcon ?: false)
                putExtra(Constants.EXTRA_PATIO, filter?.patio ?: false)
                putExtra(Constants.EXTRA_MASCOTA, filter?.aceptaMascota ?: false)
                putExtra(Constants.EXTRA_AMBIENTE_ID, filter?.ambienteId ?: -1)
                putExtra(Constants.EXTRA_ESTADO_ID, filter?.estadoPropiedadId ?: -1)
            }

            Log.d(TAG, "✅ Navegando a ResultsActivity con ${properties.size} propiedades")
            startActivity(intent)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error navegando a resultados", e)
            showToast("Error al abrir resultados")
        }
    }
}