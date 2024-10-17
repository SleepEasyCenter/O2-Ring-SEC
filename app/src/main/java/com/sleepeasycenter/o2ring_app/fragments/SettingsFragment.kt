package com.sleepeasycenter.o2ring_app.fragments

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.sleepeasycenter.o2ring_app.MainActivity
import com.sleepeasycenter.o2ring_app.databinding.FragmentSettingsBinding
import com.sleepeasycenter.o2ring_app.dialogs.DialogChangePatientIdAuthFragment
import com.sleepeasycenter.o2ring_app.utils.readPatientId

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {
    private var binding: FragmentSettingsBinding? = null
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        binding!!.btnPatientProfileEdit.setOnClickListener { x ->
            openEditPatientIdDialog()
        }
        binding!!.settingsTextView.text = "Patient Id: " + readPatientId(activity as Activity);
        return binding?.root
    }

    fun openEditPatientIdDialog(){
        DialogChangePatientIdAuthFragment({success ->
            if (success){
                // open up new dialog to change patient id
                (activity as MainActivity).startPatientEditActivity()
            }
            else{
                Toast.makeText(context, "Error: Wrong Code.", Toast.LENGTH_SHORT).show()
            }
        }).show(parentFragmentManager, "AUTH_CHANGE_PATIENT_ID")
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment settings.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}