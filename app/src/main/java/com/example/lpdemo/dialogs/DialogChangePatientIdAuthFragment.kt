package com.example.lpdemo.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.lpdemo.databinding.FragmentSettingsChangePatientIdDialogBinding
import com.example.lpdemo.utils.hashString



/**
 * A simple [Fragment] subclass.
 * Use the [DialogChangePatientIdAuthFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DialogChangePatientIdAuthFragment(private var listener: ((success: Boolean) -> Unit)?) :
    DialogFragment() {
    private var binding: FragmentSettingsChangePatientIdDialogBinding? = null
    private val secretCode = hashString("Psalms118:8")


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction.
            val input = EditText(it)

// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            val builder = AlertDialog.Builder(it)
            builder
                .setTitle("Configure Patient - Enter Code")
                .setMessage("This function is for Sleep Easy Clinic staff only. Please do not use this if you are a patient.")
                .setView(input)
                .setPositiveButton("Enter") { dialog, id ->
                    dialog.dismiss()
                    val text = input.text.toString();
                    var hashed = hashString(text);
                    if (hashed == secretCode){
                        listener?.invoke(true)
                    }
                    else{
                        listener?.invoke(false)
                    }
                }
                .setNegativeButton("Cancel") { dialog, id ->
                    // User cancelled the dialog.
                    listener?.invoke(false)
                }
            // Create the AlertDialog object and return it.
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingsChangePatientIdDialogBinding.inflate(inflater, container, false);

        return binding?.root
    }

    companion object {
        @JvmStatic
        public val TAG: String = "DialogChangePatientIdAuthFragment"
    }
}