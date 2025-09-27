package com.sleepeasycenter.o2ring_app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sleepeasycenter.o2ring_app.databinding.ActivityConfigureBloodpressureBaselinesBinding
import com.sleepeasycenter.o2ring_app.utils.setPatientSPCalib
import com.sleepeasycenter.o2ring_app.utils.setPatientDBPCalib
import com.sleepeasycenter.o2ring_app.utils.readPatientSPCalib
import com.sleepeasycenter.o2ring_app.utils.readPatientDBPCalib

class ConfigureBloodpressureBaselinesActivity : AppCompatActivity() {
  public lateinit var binding: ActivityConfigureBloodpressureBaselinesBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityConfigureBloodpressureBaselinesBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setSupportActionBar(binding.toolbar2)
    supportActionBar?.setTitle("Configure Blood Pressure Baselines")
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.show()

    // Load current values
    binding.inputConfigOxybaseline.setText(readPatientSPCalib(this))
    binding.inputConfigPrbaseline.setText(readPatientDBPCalib(this))

    binding.btnConfigSave?.setOnClickListener { btnSaveDetails_callback() }
  }

  fun btnSaveDetails_callback() {
    val inputSPCalib = binding.inputConfigOxybaseline.text
    val inputDBPCalib = binding.inputConfigPrbaseline.text

    if (inputSPCalib.isBlank() || inputDBPCalib.isBlank()) {
      Toast.makeText(this, "Error: Please fill in all fields.", Toast.LENGTH_SHORT).show()
      return
    }

    // Save the blood pressure baseline values
    setPatientSPCalib(this, inputSPCalib.toString())
    setPatientDBPCalib(this, inputDBPCalib.toString())

    Toast.makeText(this, "Blood pressure baselines saved!", Toast.LENGTH_SHORT).show()
    finish()
  }

  override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return true
  }
}
