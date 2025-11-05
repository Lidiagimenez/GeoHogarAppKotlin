package com.main.geohogar.geohogarapp.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.DialogFragment
import com.geohogar.app.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MapDialogFragment : DialogFragment() {

    private lateinit var mapView: MapView
    private lateinit var btnClose: ImageButton

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var propertyAddress: String = ""

    companion object {
        private const val ARG_LAT = "latitude"
        private const val ARG_LNG = "longitude"
        private const val ARG_ADDRESS = "address"

        fun newInstance(lat: Double, lng: Double, address: String): MapDialogFragment {
            return MapDialogFragment().apply {
                arguments = Bundle().apply {
                    putDouble(ARG_LAT, lat)
                    putDouble(ARG_LNG, lng)
                    putString(ARG_ADDRESS, address)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)

        arguments?.let {
            latitude = it.getDouble(ARG_LAT)
            longitude = it.getDouble(ARG_LNG)
            propertyAddress = it.getString(ARG_ADDRESS) ?: ""
        }

        Configuration.getInstance().load(
            requireContext(),
            requireContext().getSharedPreferences("osmdroid", 0)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // SIN VIEW BINDING - Usando findViewById
        return inflater.inflate(R.layout.dialog_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar vistas
        mapView = view.findViewById(R.id.mapView)
        btnClose = view.findViewById(R.id.btnClose)

        setupMap()
        setupCloseButton()
    }

    private fun setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        val propertyLocation = GeoPoint(latitude, longitude)
        mapView.controller.setZoom(17.0)
        mapView.controller.setCenter(propertyLocation)

        val marker = Marker(mapView)
        marker.position = propertyLocation
        marker.title = propertyAddress
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(marker)
        marker.showInfoWindow()
    }

    private fun setupCloseButton() {
        btnClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
}