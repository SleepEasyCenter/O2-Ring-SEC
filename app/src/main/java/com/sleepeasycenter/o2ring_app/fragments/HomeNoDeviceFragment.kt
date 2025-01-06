package com.sleepeasycenter.o2ring_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

import com.sleepeasycenter.o2ring_app.MainActivity
import com.sleepeasycenter.o2ring_app.OximetryDeviceController
import com.sleepeasycenter.o2ring_app.R
import com.sleepeasycenter.o2ring_app.databinding.FragmentHomeNodeviceBinding

/**
 * A simple [Fragment] subclass.
 * Use the [HomeNoDeviceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeNoDeviceFragment : Fragment() {

    public val TAG: String = "HomeNoDeviceFragment"
    private var _binding: FragmentHomeNodeviceBinding? = null;
    private val binding get() = _binding!!;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeNodeviceBinding.inflate(inflater, container, false);
        // Inflate the layout for this fragment
        val view = binding.root;

        binding.btnScanDevice.setOnClickListener(View.OnClickListener { v ->
                this.btnStartScan_callback(v)
        });



        return view;
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null;
    }

    public fun btnStartScan_callback(view: View?) {
//        Log.d(TAG, "Hello world!. This is the start scan button!");
        (activity as MainActivity).startScanActivity()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeDashboard.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeNoDeviceFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}