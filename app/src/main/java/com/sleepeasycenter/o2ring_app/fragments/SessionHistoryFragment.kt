package com.sleepeasycenter.o2ring_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sleepeasycenter.o2ring_app.R
import com.sleepeasycenter.o2ring_app.databinding.FragmentHomeNodeviceBinding
import com.sleepeasycenter.o2ring_app.databinding.FragmentSessionHistoryBinding

/**
 * A simple [Fragment] subclass.
 * Use the [SessionHistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SessionHistoryFragment : Fragment() {
    public val TAG: String = "SessionHistoryFragment"
    private var _binding: FragmentSessionHistoryBinding? = null;
    private val binding get() = _binding!!;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSessionHistoryBinding.inflate(inflater);
        // Inflate the layout for this fragment
        val view = binding.root;
        return view;
    }

}