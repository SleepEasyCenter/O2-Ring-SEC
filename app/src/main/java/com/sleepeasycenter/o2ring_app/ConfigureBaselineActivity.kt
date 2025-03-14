package com.sleepeasycenter.o2ring_app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sleepeasycenter.o2ring_app.databinding.ActivityConfigureBaselinesBinding
import com.sleepeasycenter.o2ring_app.dialogs.DialogUnsavedChangesWarning
import com.sleepeasycenter.o2ring_app.utils.setPatientOxyBaseline
import com.sleepeasycenter.o2ring_app.utils.setPatientPRBaseline

class ConfigureBaselineActivity: AppCompatActivity() {
    public lateinit var binding: ActivityConfigureBaselinesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigureBaselinesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar2);
        supportActionBar?.setTitle("Configure Baselines")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.show()

        binding.btnConfigSave.setOnClickListener {btnSaveDetails_callback()}
    }

    fun btnSaveDetails_callback() {
        val inputOxyBaseline = binding.inputConfigOxybaseline.text
        val inputPRBaseline = binding.inputConfigPrbaseline.text

        if (inputOxyBaseline.isBlank() || inputPRBaseline.isBlank()) {
            Toast.makeText(this, "Fields cannot be blank!", Toast.LENGTH_SHORT).show()
            return
        }
        else if (inputOxyBaseline.toString().toFloat() !in 80f..99f
                || inputPRBaseline.toString().toFloat() !in 40f..75f) {
            Toast.makeText(this, "Invalid baseline!", Toast.LENGTH_SHORT).show()
            return
        }

        setPatientOxyBaseline(this, inputOxyBaseline.toString())
        setPatientPRBaseline(this, inputPRBaseline.toString())
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {

        DialogUnsavedChangesWarning({ x ->
            if (x) {
                finish()
            }
        }).show(supportFragmentManager, "UNSAVED_CHANGES_DIALOG")

        return false
    }

}