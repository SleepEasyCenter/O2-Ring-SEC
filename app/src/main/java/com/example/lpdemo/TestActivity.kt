package com.example.lpdemo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lpdemo.databinding.ActivityTestBinding
import doab.private

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        lateinit var binding: ActivityTestBinding
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_test)
        // Retrieve the data passed from MainActivity
        val deviceName = intent.getStringExtra("device_name") ?: "Unknown Device"
        val batteryLevel = intent.getIntExtra("battery_percentage", -1)
        val clockTime = intent.getStringExtra("device_clock") ?: "N/A"

        // Display values
        binding.deviceName.text = "Device Name: $deviceName"
        binding.deviceBattery.text = "Battery: $batteryLevel%"
        binding.deviceTime.text = "Clock Time: $clockTime"
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}