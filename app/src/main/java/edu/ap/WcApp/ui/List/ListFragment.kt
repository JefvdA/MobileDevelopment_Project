package edu.ap.WcApp.ui.List

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.ap.WcApp.CustomAdapter
import edu.ap.WcApp.DatabaseHelper
import edu.ap.WcApp.R
import edu.ap.WcApp.ToiletViewModel
import edu.ap.WcApp.databinding.FragmentListBinding
import java.util.ArrayList

class ListFragment : Fragment(R.layout.fragment_list) {

    private var _binding: FragmentListBinding? = null
    private var databaseHelper: DatabaseHelper? = null
    private var arrayList: ArrayList<String>? = null
    private var linearLayoutManager : LinearLayoutManager? = null

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
        val listViewModel =
            ViewModelProvider(this).get(ListViewModel::class.java)

        _binding = FragmentListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        arrayList = databaseHelper!!.allToilets()
        val recyclerView : RecyclerView = binding.recyclerview
        recyclerView.layoutManager = linearLayoutManager
        val data = ArrayList<ToiletViewModel>()
        arrayList!!.forEach{
            data.add(ToiletViewModel(it))
        }
        val adapter = CustomAdapter(data)
        recyclerView.adapter = adapter
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}