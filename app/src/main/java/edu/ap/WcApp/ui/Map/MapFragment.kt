package edu.ap.WcApp.ui.Map

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import edu.ap.WcApp.DatabaseHelper
import edu.ap.WcApp.MyMapEventsReceiver
import edu.ap.WcApp.SharedViewModel
import edu.ap.WcApp.ToiletViewModel

import edu.ap.WcApp.databinding.FragmentMapBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.util.ArrayList

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null

    private var databaseHelper: DatabaseHelper? = null
    private var arrayList: ArrayList<ToiletViewModel>? = null

    private lateinit var mMapView: MapView

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var myLocationMarker: Marker
    private var locationoMarkerExists: Boolean = false
    private val sharedViewModel: SharedViewModel by viewModels();

    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var clearButton: Button

    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.databaseHelper = DatabaseHelper(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arrayList = arrayListOf()
        sharedViewModel.getDataFromDb()
        sharedViewModel.getSelected().observe(viewLifecycleOwner, Observer { list ->
            arrayList = list as ArrayList<ToiletViewModel>
            mMapView?.overlays?.clear()
            initMap()
        })
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val osmConfig = Configuration.getInstance()
        osmConfig.userAgentValue = requireContext().packageName
        val basePath = File(requireContext().cacheDir.absolutePath, "osmdroid")
        osmConfig.osmdroidBasePath = basePath
        val tileCache = File(osmConfig.osmdroidBasePath, "tile")
        osmConfig.osmdroidTileCache = tileCache

        mMapView = binding.mapview
        val mapController = mMapView.controller
        mapController.setZoom(9.5)

        sharedPrefs = requireActivity().getPreferences(Context.MODE_PRIVATE)

        clearButton = binding.clearButton

        clearButton.setOnClickListener {
            removeSavedLocation()
            clearButton.isEnabled = false
        }

        // Initialize map
        initMap()

        return root
    }

    private fun initMap() {
        mMapView.setTileSource(TileSourceFactory.WIKIMEDIA)

        mMapView.controller?.setZoom(17.0)

        mMapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        mMapView.setMultiTouchControls(true)

        val mReceiver = MyMapEventsReceiver(::addLocationMarker)
        val mEvOverlay = MapEventsOverlay(mReceiver)
        mMapView.overlays.add(mEvOverlay)

        val savedLat = sharedPrefs.getFloat("lat", 0f)
        val savedLong = sharedPrefs.getFloat("long", 0f)
        if (savedLat != 0f && savedLong != 0f){
            setCenter(GeoPoint(savedLat.toDouble(), savedLong.toDouble()), "Saved location")
        } else {
            getLocation()
        }

        addWcMarkers()
    }

    private fun removeSavedLocation() {
        sharedPrefs.edit().remove("lat").apply()
        sharedPrefs.edit().remove("long").apply()
        getLocation()
    }

    private fun getLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
        }
        val task = fusedLocationProviderClient.lastLocation

        // default = Ellermanstraat 33
        SharedViewModel.location.latitude = 51.23020595
        SharedViewModel.location.longitude = 4.41655480828479
        setCenter(GeoPoint(51.23020595, 4.41655480828479), "Campus Ellermanstraat")
        task.addOnSuccessListener {
            if (it != null) {
                setCenter(GeoPoint(it.latitude, it.longitude), "MyLocation")
                SharedViewModel.location.latitude = it.latitude
                SharedViewModel.location.longitude = it.longitude
            }
        }
        clearButton.isEnabled = false
    }

    private fun addWcMarkers() {
        arrayList!!.forEach {
            addMarker(GeoPoint(it.lat, it.lon), it.addres, false)
        }
    }

    private fun addLocationMarker(geoPoint: GeoPoint) {
        setCenter(geoPoint, "MyLocation")
        sharedPrefs.edit().putFloat("lat", geoPoint.latitude.toFloat()).apply()
        sharedPrefs.edit().putFloat("long", geoPoint.longitude.toFloat()).apply()
        SharedViewModel.location.latitude = geoPoint.latitude
        SharedViewModel.location.longitude = geoPoint.longitude
        clearButton.isEnabled = true
    }

    private fun addMarker(geoPoint: GeoPoint, name: String, isMyLocationMarker: Boolean) {
        val marker = Marker(mMapView)

        var imageResource = resources.getIdentifier("@drawable/ic_map_marker_pin_red", "drawable", requireActivity().packageName)

        if (isMyLocationMarker)
            imageResource = resources.getIdentifier("@drawable/ic_map_marker_pin_blue", "drawable", requireActivity().packageName)

        marker.icon = ResourcesCompat.getDrawable(resources, imageResource, resources.newTheme())

        marker.setOnMarkerClickListener { _ , _ ->  true }

        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

        if (isMyLocationMarker && locationoMarkerExists)
            mMapView.overlays.remove(myLocationMarker)

        if (isMyLocationMarker) {
            myLocationMarker = marker
            locationoMarkerExists = true
        }

        mMapView.overlays.add(marker)
    }

    private fun setCenter(geoPoint: GeoPoint, name: String) {
        mMapView?.controller?.setCenter(geoPoint)
        addMarker(geoPoint, name, true)
    }

    override fun onPause() {
        super.onPause()
        mMapView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        mMapView?.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}