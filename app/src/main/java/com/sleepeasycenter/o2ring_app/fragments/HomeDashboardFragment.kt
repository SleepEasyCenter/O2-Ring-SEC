package com.sleepeasycenter.o2ring_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.sleepeasycenter.o2ring_app.MainActivity
import com.sleepeasycenter.o2ring_app.databinding.FragmentHomeDashboardBinding

/**
 * A simple [Fragment] subclass.
 * Use the [HomeDashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeDashboardFragment : Fragment() {


    public val TAG: String = "HomeDashboardFragment"
    private var _binding: FragmentHomeDashboardBinding? = null;
    private val binding get() = _binding!!;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeDashboardBinding.inflate(inflater, container, false);
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
            HomeDashboardFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}