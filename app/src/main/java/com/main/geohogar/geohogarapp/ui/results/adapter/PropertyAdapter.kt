package com.main.geohogar.geohogarapp.ui.results.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.geohogar.app.databinding.ItemPropertyBinding
import com.main.geohogar.geohogarapp.domain.model.Property
import com.main.geohogar.geohogarapp.utils.ImageLoader
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.NumberFormat
import java.util.Locale

class PropertyAdapter(
    private val onPropertyClick: (Property) -> Unit,
    private val onMapClick: (Property) -> Unit
) : ListAdapter<Property, PropertyAdapter.PropertyViewHolder>(PropertyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val binding = ItemPropertyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PropertyViewHolder(binding, onPropertyClick, onMapClick)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PropertyViewHolder(
        private val binding: ItemPropertyBinding,
        private val onPropertyClick: (Property) -> Unit,
        private val onMapClick: (Property) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(property: Property) {
            binding.apply {
                // Tipo de propiedad
                tvPropertyType.text = property.tipo

                // Dirección
                tvPropertyAddress.text = property.direccion

                // Precio
                val formatter = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
                formatter.maximumFractionDigits = 0
                tvPropertyPrice.text = formatter.format(property.precio)

                // Habitaciones y baños
                tvRooms.text = "🛏️ ${property.habitaciones}"
                tvBathrooms.text = "🚿 ${property.banos}"

                // Imagen principal
                val firstImage = property.imagenes.firstOrNull()
                ImageLoader.loadImage(ivPropertyImage, firstImage)

                // Click en detalle
                btnViewDetail.setOnClickListener {
                    onPropertyClick(property)
                }

                // Click en toda la card
                root.setOnClickListener {
                    onPropertyClick(property)
                }

                // Mapa embebido
                mapPreviewContainer.removeAllViews()

                val context = mapPreviewContainer.context.applicationContext
                val prefs = context.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE)
                Configuration.getInstance().load(context, prefs)

                val mapView = MapView(mapPreviewContainer.context)
                mapView.setTileSource(TileSourceFactory.MAPNIK)
                mapView.setMultiTouchControls(false)
                mapView.setUseDataConnection(true)
                mapView.setLayerType(MapView.LAYER_TYPE_SOFTWARE, null)

                val geoPoint = GeoPoint(property.ubicacionLat, property.ubicacionLng)
                mapView.controller.setZoom(15.0)
                mapView.controller.setCenter(geoPoint)

                val marker = Marker(mapView)
                marker.position = geoPoint
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                val goldMarker = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#A6893C"))
                    setSize(48, 48)
                }
                marker.icon = goldMarker

                mapView.overlays.add(marker)
                mapPreviewContainer.addView(mapView)

                // ✅ El mapa ahora funciona como botón
                mapView.setOnClickListener {
                    onMapClick(property)
                }
            }
        }
    }

    class PropertyDiffCallback : DiffUtil.ItemCallback<Property>() {
        override fun areItemsTheSame(oldItem: Property, newItem: Property): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Property, newItem: Property): Boolean {
            return oldItem == newItem
        }
    }
}