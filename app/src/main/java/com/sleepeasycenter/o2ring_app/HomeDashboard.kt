package com.sleepeasycenter.o2ring_app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sleepeasycenter.o2ring_app.databinding.FragmentHomeDashboardBinding
import com.sleepeasycenter.o2ring_app.databinding.FragmentHomeNodeviceBinding

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeDashboardBinding.inflate(inflater, container, false);

        // Inflate the layout for this fragment
        val view = binding.root;
        return view;
    }
}