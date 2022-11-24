package edu.ap.WcApp.ui.List

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import edu.ap.WcApp.CustomAdapter
import edu.ap.WcApp.DatabaseHelper
import edu.ap.WcApp.R
import edu.ap.WcApp.ToiletViewModel
import edu.ap.WcApp.databinding.FragmentListBinding
import org.osmdroid.util.Delay
import org.osmdroid.util.GeoPoint
import java.util.ArrayList
import android.location.Location.distanceBetween
import android.widget.CheckBox

class ListFragment : Fragment(R.layout.fragment_list) {

    private var _binding: FragmentListBinding? = null
    private var databaseHelper: DatabaseHelper? = null
    private var arrayList: ArrayList<ToiletViewModel>? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var location: Location

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.linearLayoutManager = LinearLayoutManager(context)
        this.databaseHelper = DatabaseHelper(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        location = Location("start")
        location.longitude = 0.0
        location.latitude = 0.0
        getLocation()
        _binding = FragmentListBinding.inflate(inflater, container, false)
        val root: View = binding.root
        readData()
        binding.button.setOnClickListener {
            databaseHelper!!.getData()
            readData()
        }

        binding.man.setOnClickListener{
            databaseHelper!!.man = binding.man.isChecked
            readData()
        }

        binding.vrouw.setOnClickListener{
            databaseHelper!!.vrouw = binding.vrouw.isChecked
            readData()
        }

        binding.rolstoel.setOnClickListener{
            databaseHelper!!.rolstoel = binding.rolstoel.isChecked
            readData()
        }

        binding.luiertafel.setOnClickListener{
            databaseHelper!!.luiertafel = binding.luiertafel.isChecked
            readData()
        }
        return root
    }

    fun readData() {
        arrayList = databaseHelper!!.allToilets()
        val recyclerView: RecyclerView = binding.recyclerview
        recyclerView.layoutManager = linearLayoutManager
        var data = ArrayList<ToiletViewModel>()
        arrayList!!.forEach {
            val dest = Location("dest")
            dest.longitude = it.lon
            dest.latitude = it.lat
            it.distance = location.distanceTo(dest)
            data.add(it)
        }
        arrayList!!.sortedWith(compareBy({ it.distance }))
        val adapter = CustomAdapter(data)
        recyclerView.adapter = adapter
    }

    fun getLocation() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
        }
        val task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener {
            if (it != null) {
                location.latitude = it.latitude
                location.longitude = it.longitude
                Log.d("loc", location.toString())
                readData()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
