package edu.ap.WcApp.ui.List

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.ap.WcApp.databinding.FragmentListBinding
import java.util.ArrayList
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import edu.ap.WcApp.*

class ListFragment : Fragment(R.layout.fragment_list) {

    private var _binding: FragmentListBinding? = null
    private var databaseHelper: DatabaseHelper? = null
    private var arrayList: ArrayList<ToiletViewModel>? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private lateinit var location: Location
    private val sharedViewModel: SharedViewModel by viewModels();

    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.linearLayoutManager = LinearLayoutManager(context)
        this.databaseHelper = DatabaseHelper(context)
    }

    override fun onStart() {
        super.onStart()
        location = SharedViewModel.location
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedViewModel.getDataFromDb()
        arrayList = arrayListOf()
        sharedViewModel.getSelected().observe(viewLifecycleOwner, Observer { list ->
            arrayList = list as ArrayList<ToiletViewModel>
            location = SharedViewModel.location
            readData()
        })
        _binding = FragmentListBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.button.setOnClickListener {
            databaseHelper!!.getData()
            sharedViewModel.getDataFromDb()
        }

        binding.man.setOnClickListener{
            SharedViewModel.man = binding.man.isChecked
            sharedViewModel.getDataFromDb()
        }

        binding.vrouw.setOnClickListener{
            SharedViewModel.vrouw = binding.vrouw.isChecked
            sharedViewModel.getDataFromDb()
        }

        binding.rolstoel.setOnClickListener{
            SharedViewModel.rolstoel = binding.rolstoel.isChecked
            sharedViewModel.getDataFromDb()
        }

        binding.luiertafel.setOnClickListener{
            SharedViewModel.luiertafel = binding.luiertafel.isChecked
            sharedViewModel.getDataFromDb()
        }
        return root
    }

    fun readData() {
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
        data.sortBy { it.distance }
        val adapter = CustomAdapter(data)
        recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
