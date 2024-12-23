package com.sleepeasycenter.o2ring_app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.objs.Bluetooth
import com.sleepeasycenter.o2ring_app.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), DeviceSelectCallback {
    private val TAG: String = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navhost) as NavHostFragment;
        navController = navHostFragment.navController;
        binding.bottomNavigationView.setupWithNavController(navController);

        OximetryDeviceController.instance.initEventBus(this);

        OximetryDeviceController.instance.connected.observe(this, { value ->
            if (value) {
                navController.navigate(R.id.action_home_nodevice_to_home_dashboard)
            }
        })
    }

    public fun startScanActivity() {
        val intent: Intent = Intent(this@MainActivity, DeviceScanActivity::class.java);
        startActivity(intent)
        DeviceScanActivity.resultCallback = this
    }

    public fun startPatientEditActivity() {
        val intent: Intent = Intent(this@MainActivity, ConfigurePatientActivity::class.java);
        startActivity(intent)
    }

    override fun onDeviceSelect(device: Bluetooth) {
        // connect
        Toast.makeText(this, "Connecting to O2 Ring..." + device.name, Toast.LENGTH_SHORT).show()
        OximetryDeviceController.instance.connectDevice(
            device,
            BleServiceHelper.BleServiceHelper,
            applicationContext,
            lifecycle
        );

    }
}