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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSearch.setOnClickListener { performSearch() }
        setupOperationCheckboxes()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnSearch.isEnabled = !isLoading
            binding.btnSearch.text = if (isLoading) "Cargando..." else getString(R.string.btn_search)
        }

        viewModel.neighborhoods.observe(this) { neighborhoods ->
            if (neighborhoods.isNotEmpty()) {
                Log.d("FilterActivity", "✅ Recibidos ${neighborhoods.size} barrios")
                setupNeighborhoods(neighborhoods)
            }
        }

        viewModel.propertyTypes.observe(this) { types ->
            if (types.isNotEmpty()) {
                Log.d("FilterActivity", "✅ Recibidos ${types.size} tipos")
                setupPropertyTypes(types)
            }
        }

        // ✅ CAMBIO: Observar PriceRangeData en lugar de Pair
        viewModel.priceRange.observe(this) { priceData ->
            Log.d("FilterActivity", "✅ Rango recibido: ${priceData.minPrice} - ${priceData.maxPrice} (step: ${priceData.stepSize})")
            setupPriceSlider(priceData)
        }

        viewModel.searchResult.observe(this) { result ->
            handleSearchResult(result)
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Log.e("FilterActivity", "❌ Error: $it")
                showToast(it)
            }
        }
    }

    private fun setupNeighborhoods(neighborhoods: List<String>) {
        binding.llNeighborhoods.removeAllViews()
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

    // ✅ MÉTODO ACTUALIZADO: Recibe PriceRangeData completo
    private fun setupPriceSlider(priceData: PriceRangeData) {
        try {
            with(binding.rsPrice) {
                // Configurar valores del slider
                valueFrom = priceData.minPrice
                valueTo = priceData.maxPrice
                stepSize = priceData.stepSize

                // Establecer valores iniciales
                setValues(priceData.minPrice, priceData.maxPrice)

                Log.d("FilterActivity", "Slider configurado: ${priceData.minPrice} - ${priceData.maxPrice} step:${priceData.stepSize}")
            }

            // Listener para cambios
            binding.rsPrice.addOnChangeListener { slider, _, _ ->
                val values = slider.values
                viewModel.setPriceRange(values[0], values[1])
                updatePriceDisplay(values[0].toInt(), values[1].toInt())
            }

            updatePriceDisplay(priceData.minPrice.toInt(), priceData.maxPrice.toInt())
        } catch (e: Exception) {
            Log.e("FilterActivity", "❌ Error configurando slider: ${e.message}")
            showToast("Error al configurar filtro de precios")
        }
    }

    private fun updatePriceDisplay(min: Int, max: Int) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
        formatter.maximumFractionDigits = 0

        val displayText = when {
            min == viewModel.getMinPrice().toInt() && max == viewModel.getMaxPrice().toInt() ->
                "Cualquier precio"
            min == viewModel.getMinPrice().toInt() ->
                "Hasta ${formatter.format(max)}"
            max == viewModel.getMaxPrice().toInt() ->
                "Desde ${formatter.format(min)}"
            else ->
                "${formatter.format(min)} - ${formatter.format(max)}"
        }

        binding.tvPriceRange.text = displayText
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
        val filter = viewModel.filter.value

        if (filter?.isRent == false && filter.isSale == false) {
            showToast("Selecciona al menos una operación (Alquiler o Venta)")
            return
        }

        // Mostrar resumen de filtros activos
        binding.tvActiveFilters.apply {
            text = viewModel.getFilterSummary()
            visibility = View.VISIBLE
        }

        viewModel.searchProperties()
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
        val filter = viewModel.filter.value
        val intent = Intent(this, ResultsActivity::class.java).apply {
            putExtra(Constants.EXTRA_NEIGHBORHOOD, filter?.selectedNeighborhood)
            putExtra(Constants.EXTRA_PROPERTY_TYPE, filter?.selectedPropertyType)
            putExtra(Constants.EXTRA_PRICE_MIN, filter?.priceMin ?: 0)
            putExtra(Constants.EXTRA_PRICE_MAX, filter?.priceMax ?: 10000000)
            putExtra(Constants.EXTRA_IS_RENT, filter?.isRent ?: false)
            putExtra(Constants.EXTRA_IS_SALE, filter?.isSale ?: false)
            putExtra(Constants.EXTRA_GARAGE, filter?.garage ?: false)
            putExtra(Constants.EXTRA_BALCON, filter?.balcon ?: false)
            putExtra(Constants.EXTRA_PATIO, filter?.patio ?: false)
            putExtra(Constants.EXTRA_MASCOTA, filter?.aceptaMascota ?: false)
            putExtra(Constants.EXTRA_AMBIENTE_ID, filter?.ambienteId ?: -1)
            putExtra(Constants.EXTRA_ESTADO_ID, filter?.estadoPropiedadId ?: -1)
        }
        startActivity(intent)
    }

}