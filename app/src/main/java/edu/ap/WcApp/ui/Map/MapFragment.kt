package edu.ap.WcApp.ui.Map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.livedata.core.R
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import edu.ap.WcApp.DatabaseHelper
import edu.ap.WcApp.ToiletViewModel

import edu.ap.WcApp.databinding.FragmentMapBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.OverlayItem
import java.io.File
import java.net.URL
import java.net.URLEncoder
import java.util.ArrayList

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null

    private var databaseHelper: DatabaseHelper? = null
    private var arrayList: ArrayList<ToiletViewModel>? = null

    private lateinit var mMapView: MapView
    private var mMyLocationOverlay: ItemizedOverlay<OverlayItem>? = null
    private var items = ArrayList<OverlayItem>()
    private var searchField: EditText? = null
    private var searchButton: Button? = null
    private var clearButton: Button? = null
    private val urlNominatim = "https://nominatim.openstreetmap.org/"

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // This property is only valid between onCreateView and
    // onDestroyView.
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
        val mapViewModel =
            ViewModelProvider(this).get(MapViewModel::class.java)

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

        searchField = binding.searchTxtview
        searchButton = binding.searchButton
        searchButton?.setOnClickListener {
            val url = URL(urlNominatim + "search?q=" + URLEncoder.encode(searchField?.text.toString(), "UTF-8") + "&format=json")
            it.hideKeyboard()
            //val task = MyAsyncTask()
            //task.execute(url)
            getAddressOrLocation(url)
        }

        clearButton = binding.clearButton
        clearButton?.setOnClickListener {
            mMapView?.overlays?.clear()
            // Redraw map
            mMapView?.invalidate()
        }

        // Initialize map
        initMap()

        return root
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun initMap() {
        mMapView?.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        // add receiver to get location from tap
        val mReceive: MapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                //Toast.makeText(baseContext, p.latitude.toString() + " - " + p.longitude, Toast.LENGTH_LONG).show()
                val url = URL(urlNominatim + "reverse?lat=" + p.latitude.toString() + "&lon=" + p.longitude.toString() + "&format=json")
                //val task = MyAsyncTask()
                //task.execute(url)
                getAddressOrLocation(url)
                return false
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                return false
            }
        }
        mMapView?.overlays?.add(MapEventsOverlay(mReceive))

        // MiniMap
        //val miniMapOverlay = MinimapOverlay(this, mMapView!!.tileRequestCompleteHandler)
        //this.mMapView?.overlays?.add(miniMapOverlay)

        mMapView?.controller?.setZoom(17.0)

        mMapView.setTileSource(TileSourceFactory.WIKIMEDIA)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
        }
        val task = fusedLocationProviderClient.lastLocation
        // default = Ellermanstraat 33
        setCenter(GeoPoint(51.23020595, 4.41655480828479), "Campus Ellermanstraat")
        task.addOnSuccessListener {
            if (it != null) {
                setCenter(GeoPoint(it.latitude, it.longitude), "MyLocation")
            }
        }

        addWcMarkers()
    }

    private fun addWcMarkers() {
        arrayList = databaseHelper!!.allToilets()
        arrayList!!.forEach {
            addMarker(GeoPoint(it.lat, it.lon), it.addres, false)
        }
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

        mMapView.overlays.add(marker)
    }

    private fun setCenter(geoPoint: GeoPoint, name: String) {
        mMapView?.controller?.setCenter(geoPoint)
        addMarker(geoPoint, name, true)
    }

    private fun getAddressOrLocation(url : URL) {

        var searchReverse = false

        Thread(Runnable {
            searchReverse = (url.toString().indexOf("reverse", 0, true) > -1)
            val client = OkHttpClient()
            val response: Response
            val request = Request.Builder()
                .url(url)
                .build()
            response = client.newCall(request).execute()

            val result = response.body!!.string()

            requireActivity().runOnUiThread {
                val jsonString = StringBuilder(result!!)
                Log.d("be.ap.edu.mapsaver", jsonString.toString())

                val parser: Parser = Parser.default()

                if (searchReverse) {
                    val obj = parser.parse(jsonString) as JsonObject

//                    createNotification(
//                        R.drawable.ic_menu_compass,
//                        "Reverse lookup result",
//                        obj.string("display_name")!!,
//                        "my_channel_01")
                }
                else {
                    val array = parser.parse(jsonString) as JsonArray<JsonObject>

                    if (array.size > 0) {
                        val obj = array[0]
                        // mapView center must be updated here and not in doInBackground because of UIThread exception
                        val geoPoint = GeoPoint(obj.string("lat")!!.toDouble(), obj.string("lon")!!.toDouble())
                        setCenter(geoPoint, obj.string("display_name")!!)
                    }
                    else {
                        Toast.makeText(requireContext(), "Address not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }).start()
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