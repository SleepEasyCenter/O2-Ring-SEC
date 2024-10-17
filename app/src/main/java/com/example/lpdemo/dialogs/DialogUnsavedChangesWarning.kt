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
 * Use the [DialogUnsavedChangesWarning.newInstance] factory method to
 * create an instance of this fragment.
 */
class DialogUnsavedChangesWarning(private var listener: ((shouldLeave: Boolean) -> Unit)?) :
    DialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            .setTitle("Unsaved Changes")
            .setMessage("If you leave now, any unsaved changes will not be saved!")
            .setPositiveButton("Leave", { dialog, id -> listener?.invoke(true)})
            .setNegativeButton("Cancel", { dialog, id -> listener?.invoke(false)})
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}