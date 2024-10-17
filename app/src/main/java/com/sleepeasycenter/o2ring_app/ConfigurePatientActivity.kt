package com.sleepeasycenter.o2ring_app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sleepeasycenter.o2ring_app.databinding.ActivityConfigurePatientBinding
import com.sleepeasycenter.o2ring_app.dialogs.DialogUnsavedChangesWarning
import com.sleepeasycenter.o2ring_app.utils.readPatientId
import com.sleepeasycenter.o2ring_app.utils.setPatientId

class ConfigurePatientActivity : AppCompatActivity() {
    public lateinit var binding: ActivityConfigurePatientBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigurePatientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar2);
        supportActionBar?.setTitle("Configure Patient")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.show()

        binding.btnConfigSave.setOnClickListener { btnSaveDetails_callback() }
        binding.inputConfigPatientId.setText(readPatientId(this));
    }

    fun btnSaveDetails_callback() {
        val inputPatientId = binding.inputConfigPatientId.text;
//        val inputPatientName = binding.inputConfigPatientId.text;
        if (inputPatientId.isBlank()) {
            Toast.makeText(this, "Patient Id cannot be blank!", Toast.LENGTH_SHORT).show()
            return;
        }
        setPatientId(this, inputPatientId.toString());
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