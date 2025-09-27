package com.sleepeasycenter.o2ring_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sleepeasycenter.o2ring_app.OximetryDeviceController
import com.sleepeasycenter.o2ring_app.OximetryIIDeviceController
import com.sleepeasycenter.o2ring_app.R
import com.sleepeasycenter.o2ring_app.databinding.FragmentHomeBinding

/**
 * A simple [Fragment] subclass. Use the [HomeFragment.newInstance] factory method to create an
 * instance of this fragment.
 */
class HomeFragment : Fragment() {
    public val TAG: String = "HomeFragment"
    private var _binding: FragmentHomeBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        fun updateChild() {
            val v1 = OximetryDeviceController.instance.connected.value == true
            val v2 = OximetryIIDeviceController.instance.connected.value == true
            val childFragment: Fragment =
                    if (v1 || v2) HomeDeviceDashboard() else HomeNoDeviceFragment()
            childFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView2, childFragment)
                    .commit()
        }

        // Observe both controllers; update UI when either changes
        OximetryDeviceController.instance.connected.observe(viewLifecycleOwner) { updateChild() }
        OximetryIIDeviceController.instance.connected.observe(viewLifecycleOwner) { updateChild() }

        // Initialize once based on current values
        updateChild()

        return view
    }
}
