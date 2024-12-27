package com.sleepeasycenter.o2ring_app

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.sleepeasycenter.o2ring_app.adapters.DeviceFileListViewAdapter
import com.sleepeasycenter.o2ring_app.databinding.FragmentHomeDashboardBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeDashboard.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeDashboard : Fragment() {

    public val TAG: String = "HomeDashboard"
    private var _binding: FragmentHomeDashboardBinding? = null;
    private val binding get() = _binding!!;

    private var adapter = DeviceFileListViewAdapter(arrayListOf())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        OximetryDeviceController.instance.filenames.observe(this, { newValue ->
            Log.d(TAG, "NEW VALUE: FILE COUNT: " + newValue.count())
            adapter.items = newValue.toCollection(ArrayList());
            adapter.notifyDataSetChanged()
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeDashboardBinding.inflate(inflater, container, false);
        val recyclerView = binding.fileListRecyclerView;
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setAdapter(adapter);
        // Inflate the layout for this fragment
        val view = binding.root;
        return view;
    }
}