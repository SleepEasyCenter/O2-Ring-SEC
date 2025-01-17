package com.sleepeasycenter.o2ring_app

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.objs.BluetoothController
import com.permissionx.guolindev.PermissionX
import com.sleepeasycenter.o2ring_app.adapters.DeviceListViewAdapter
import com.sleepeasycenter.o2ring_app.databinding.ActivityDeviceScanBinding
import no.nordicsemi.android.ble.observer.ConnectionObserver
import android.provider.Settings

interface DeviceSelectCallback {
    fun onDeviceSelect(device: Bluetooth)
}
class DeviceScanActivity : AppCompatActivity() {


    companion object {
        var resultCallback: DeviceSelectCallback? = null
    }

    private val TAG = "DeviceScanActivity"
    private lateinit var binding: ActivityDeviceScanBinding

    private val models = intArrayOf(
        Bluetooth.MODEL_O2RING, Bluetooth.MODEL_O2M,
        Bluetooth.MODEL_O2RING_S, Bluetooth.MODEL_S8_AW,   // OxyIIActivity
        Bluetooth.MODEL_CHECKME,   // CheckmeActivity
    )


    private var devices_list = arrayListOf<Bluetooth>()
    private var adapter = DeviceListViewAdapter(devices_list)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.actionbar)
        supportActionBar?.setTitle("Scanning for devices...")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.show()


        initView()
        initEventBus()
        needPermission()
    }

    private fun initView(){
        val recyclerView = binding.deviceList;
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setAdapter(adapter);
        adapter.listener = DeviceListViewAdapter.OnItemClickListener { item ->
            val device = item.device;

            resultCallback?.onDeviceSelect(device)
            BluetoothController.clear()
            finish()
        }

    }



    private fun onPermissionGranted() {
        if (!openBluetooth()) {
            Toast.makeText(this, "Failed to turn on bluetooth! Check permissions?", Toast.LENGTH_SHORT).show()
            return
        }
        needService()
    }

    private fun needPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionX.init(this).permissions(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,

            ).onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList, "We require these permissions", "ok", "ignore"
                )
            }.onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList, "Permissions Required", "ok", "ignore"
                )
            }.request { allGranted, grantedList, deniedList ->
                Log.d(TAG, "permission (Above Version 'Tiramisu') : $allGranted, $grantedList, $deniedList")
                
                //permission OK, check Bluetooth status
                if (allGranted) onPermissionGranted()
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionX.init(this).permissions(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ).onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList, "location permission", "ok", "ignore"
                )
            }.onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList, "location setting", "ok", "ignore"
                )
            }.request { allGranted, grantedList, deniedList ->
                Log.d(TAG, "permission (Above Version 'S'): $allGranted, $grantedList, $deniedList")
                //permission OK, check Bluetooth status
                if (allGranted) onPermissionGranted()
            }
        } else {
            PermissionX.init(this).permissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ).onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList, "location permission", "ok", "ignore"
                )
            }.onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList, "location setting", "ok", "ignore"
                )
            }.request { allGranted, grantedList, deniedList ->
                Log.d(TAG, "permission (Below Version 'S') : $allGranted, $grantedList, $deniedList")

                //permission OK, check Bluetooth status
                if (allGranted) onPermissionGranted()
            }
        }
    }

    private fun openBluetooth(): Boolean {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Toast.makeText(this, "Bluetooth is not supported", Toast.LENGTH_SHORT).show()
            return false
        }

        if (adapter.isEnabled) return true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasConnectPerms = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED;

            if (!hasConnectPerms) {
                return false
            }
        }
        return adapter.enable();
    }

    private fun needService() {
        var gpsEnabled = false
        var networkEnabled = false
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        if (!gpsEnabled && !networkEnabled) {
            val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
            dialog.setMessage("open location service")
            dialog.setPositiveButton("ok") { _, _ ->
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(myIntent, 888)
            }
            dialog.setNegativeButton("cancel") { _, _ ->
                finish()
            }
            dialog.setCancelable(false)
            dialog.show()
        } else {
            initService()
        }
    }

    /// Start scanning for models
    private fun initService() {
        if (BleServiceHelper.BleServiceHelper.checkService()) {
            // BleService already init
            BleServiceHelper.BleServiceHelper.startScan(models)
        } else {
            // Save the original file path. Er1, VBeat and HHM1 are currently supported
            val rawFolders = SparseArray<String>()
            rawFolders.set(Bluetooth.MODEL_ER1, "${getExternalFilesDir(null)?.absolutePath}/er1")
            rawFolders.set(Bluetooth.MODEL_HHM1, "${getExternalFilesDir(null)?.absolutePath}/er1")
            rawFolders.set(Bluetooth.MODEL_ER1S, "${getExternalFilesDir(null)?.absolutePath}/er1")
            rawFolders.set(Bluetooth.MODEL_ER1_S, "${getExternalFilesDir(null)?.absolutePath}/er1")
            rawFolders.set(Bluetooth.MODEL_ER1_H, "${getExternalFilesDir(null)?.absolutePath}/er1")
            rawFolders.set(Bluetooth.MODEL_ER1_W, "${getExternalFilesDir(null)?.absolutePath}/er1")
            rawFolders.set(Bluetooth.MODEL_ER1_L, "${getExternalFilesDir(null)?.absolutePath}/er1")
            Log.d(TAG, "Initialising BleService!")
            // initRawFolder必须在initService之前调用
            BleServiceHelper.BleServiceHelper.initRawFolder(rawFolders).initService(application)
                .initLog(false)
        }
    }

    private fun initEventBus() {
        Log.d(TAG,"Initialising event bus subscribers...")
        LiveEventBus.get<Boolean>(EventMsgConst.Ble.EventServiceConnectedAndInterfaceInit)
            .observe(this) {
                // BleService init success
                BleServiceHelper.BleServiceHelper.startScan(models)
                Log.d(TAG, "EventServiceConnectedAndInterfaceInit")
            }
        LiveEventBus.get<Bluetooth>(EventMsgConst.Discovery.EventDeviceFound).observe(this) {
            // scan result
            devices_list.clear()
            Log.d(TAG, "EventDeviceFound")
            for (b in BluetoothController.getDevices()) {
                devices_list.add(b)
                Log.d(TAG, "- " + b.name)
            }
            adapter.notifyDataSetChanged()
        }
    }



    override fun onSupportNavigateUp(): Boolean {
        BleServiceHelper.BleServiceHelper.stopScan()
        finish()
        return false;
    }
}