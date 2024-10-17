package com.sleepeasycenter.o2ring_app.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment


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