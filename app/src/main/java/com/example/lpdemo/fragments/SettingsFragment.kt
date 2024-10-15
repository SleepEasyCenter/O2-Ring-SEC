package com.example.lpdemo.fragments

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.example.lpdemo.databinding.FragmentSettingsBinding
import com.example.lpdemo.dialogs.DialogChangePatientIdAuthFragment
import com.example.lpdemo.utils.SharedPref_PatientID_Key
import com.example.lpdemo.utils.getAppSharedPref

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
        sharedPref = getAppSharedPref(activity as Activity);
        val patient_id = sharedPref.getString(SharedPref_PatientID_Key, null);
        binding!!.settingsTextView.text = "Patient Id: " + patient_id;
        return binding?.root
    }

    fun openEditPatientIdDialog(){
        DialogChangePatientIdAuthFragment({success ->
            if (success){
                // open up new dialog to change patient id
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