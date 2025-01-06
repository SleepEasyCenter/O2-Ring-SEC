package com.sleepeasycenter.o2ring_app.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sleepeasycenter.o2ring_app.OximetryDeviceController
import com.sleepeasycenter.o2ring_app.R
import com.sleepeasycenter.o2ring_app.databinding.FragmentHomeBinding
import com.sleepeasycenter.o2ring_app.databinding.FragmentHomeNodeviceBinding

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    public val TAG: String = "HomeFragment"
    private var _binding: FragmentHomeBinding? = null;
    private val binding get() = _binding!!;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false);
        val view = binding.root;
        OximetryDeviceController.instance.connected.observe(viewLifecycleOwner, { value ->
            val childFragment = when (value) {
                true -> HomeDeviceDashboard()
                false -> HomeNoDeviceFragment()
            }

            childFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView2, childFragment).commit()
        })
        return view;
    }
}