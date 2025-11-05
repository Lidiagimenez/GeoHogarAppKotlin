package com.main.geohogar.geohogarapp.ui.results

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.geohogar.app.databinding.ActivityResultsBinding
import com.main.geohogar.geohogarapp.domain.model.Filter
import com.main.geohogar.geohogarapp.domain.model.Property
import com.main.geohogar.geohogarapp.ui.detail.DetailActivity
import com.main.geohogar.geohogarapp.ui.detail.MapDialogFragment
import com.main.geohogar.geohogarapp.ui.results.adapter.PropertyAdapter
import com.main.geohogar.geohogarapp.ui.results.adapter.ResultsViewModel
import com.main.geohogar.geohogarapp.utils.Constants
import com.main.geohogar.geohogarapp.utils.gone
import com.main.geohogar.geohogarapp.utils.showToast
import com.main.geohogar.geohogarapp.utils.visible

class ResultsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultsBinding
    private val viewModel: ResultsViewModel by viewModels()
    private lateinit var adapter: PropertyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerView()
        setupObservers()
        loadProperties()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
           // finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = PropertyAdapter(
            onPropertyClick = { property ->
                navigateToDetail(property)
            },
            onMapClick = { property ->
                showMapDialog(property)
            }
        ).apply {
            submitList(emptyList()) // ✅ Evita el warning inicial
        }

        binding.rvProperties.apply {
            layoutManager = LinearLayoutManager(this@ResultsActivity)
            adapter = this@ResultsActivity.adapter
        }
    }

    private fun setupObservers() {
        viewModel.properties.observe(this) { properties ->
            updateUI(properties)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressBar.visible()
                binding.rvProperties.gone()
                binding.llEmptyState.gone()
            } else {
                binding.progressBar.gone()
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let { showToast(it) }
        }
    }

    private fun loadProperties() {
        val filter = getFilterFromIntent()

        if (filter != null) {
            viewModel.searchProperties(filter)
        } else {
            viewModel.getAllProperties()
        }
    }

    private fun getFilterFromIntent(): Filter? {
        val neighborhood = intent.getStringExtra(Constants.EXTRA_NEIGHBORHOOD)
        val propertyType = intent.getStringExtra(Constants.EXTRA_PROPERTY_TYPE)
        val priceMin = intent.getIntExtra(Constants.EXTRA_PRICE_MIN, 0)
        val priceMax = intent.getIntExtra(Constants.EXTRA_PRICE_MAX, 10000000)
        val isRent = intent.getBooleanExtra(Constants.EXTRA_IS_RENT, false)
        val isSale = intent.getBooleanExtra(Constants.EXTRA_IS_SALE, false)

        if (neighborhood == null && propertyType == null && !isRent && !isSale) {
            return null
        }

        return Filter(
            selectedNeighborhood = neighborhood,
            selectedPropertyType = propertyType,
            priceMin = priceMin,
            priceMax = priceMax,
            isRent = isRent,
            isSale = isSale
        )
    }

    private fun updateUI(properties: List<Property>) {
        if (properties.isEmpty()) {
            binding.rvProperties.gone()
            binding.llEmptyState.visible()
            binding.tvResultsCount.text = "No se encontraron propiedades"
        } else {
            binding.rvProperties.visible()
            binding.llEmptyState.gone()
            binding.tvResultsCount.text = "Se encontraron ${properties.size} propiedades"
            adapter.submitList(properties)
        }
    }

    private fun showMapDialog(property: Property) {
        val mapDialog = MapDialogFragment.newInstance(
            property.ubicacionLat,
            property.ubicacionLng,
            property.direccion
        )
        mapDialog.show(supportFragmentManager, "MapDialog")
    }

    private fun navigateToDetail(property: Property) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(Constants.EXTRA_PROPERTY_ID, property.id)
        startActivity(intent)
    }
}