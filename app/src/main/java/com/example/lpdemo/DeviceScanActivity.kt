package com.example.lpdemo

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.lpdemo.adapters.DeviceListViewAdapter
import com.example.lpdemo.databinding.ActivityDeviceScanBinding
import com.example.lpdemo.utils._bleState
import com.example.lpdemo.utils.bleState

import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.objs.BluetoothController
import com.lepu.blepro.observer.BleChangeObserver
import no.nordicsemi.android.ble.observer.ConnectionObserver

class DeviceScanActivity : AppCompatActivity(), BleChangeObserver {

    private lateinit var btRegisterForResult: ActivityResultLauncher<Intent>
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
        btRegisterForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            bluetoothEnableCallback(result)
        }
        setSupportActionBar(binding.actionbar)
        supportActionBar?.setTitle("Scanning for devices...")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.show()

        val recyclerView = binding.deviceList;
        recyclerView.layoutManager =  LinearLayoutManager(this)
        recyclerView.setAdapter(adapter);

        initEventBus()
        needPermission()
    }

    private fun needService() {
        var networkEnabled = false
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        initService()
    }

    private fun bluetoothEnableCallback(result: ActivityResult){
        if (result.resultCode == Activity.RESULT_OK){
            needService()
            Toast.makeText(this, "Bluetooth open successfully", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(this, "Bluetooth open failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startBluetooth(){
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        btRegisterForResult.launch(enableBtIntent)
    }
    private fun needPermission() {
        checkBt()
    }

    private fun checkBt() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Toast.makeText(this, "Bluetooth is not supported", Toast.LENGTH_SHORT).show()
            return
        }

        if (!adapter.isEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    startBluetooth()
                } else {
                    Toast.makeText(this, "Bluetooth open failed. (Permission not granted!)", Toast.LENGTH_SHORT).show()
                }
            } else {
                startBluetooth()
            }
        } else {
            needService()
        }
    }

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

            // initRawFolder必须在initService之前调用
            BleServiceHelper.BleServiceHelper.initRawFolder(rawFolders).initService(application).initLog(true)
        }
    }

    private fun initEventBus() {
        LiveEventBus.get<Boolean>(EventMsgConst.Ble.EventServiceConnectedAndInterfaceInit)
            .observe(this) {
                // BleService init success
                BleServiceHelper.BleServiceHelper.startScan(models)
                Log.d(TAG, "EventServiceConnectedAndInterfaceInit")
            }
        LiveEventBus.get<Bluetooth>(EventMsgConst.Discovery.EventDeviceFound)
            .observe(this) {
                // scan result
                devices_list.clear()
                Log.d(TAG, "EventDeviceFound")
                for (b in BluetoothController.getDevices()) {
                    devices_list.add(b)
                    Log.d(TAG, "- " + b.name)
                }
                adapter.notifyDataSetChanged()
            }
        LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceDisconnectReason)
            .observe(this) {
                // ConnectionObserver.REASON_NOT_SUPPORTED: SDK will not auto reconnect device, services error, try to reboot device
                val reason = when (it) {
                    ConnectionObserver.REASON_UNKNOWN -> "The reason of disconnection is unknown."
                    ConnectionObserver.REASON_SUCCESS -> "The disconnection was initiated by the user."
                    ConnectionObserver.REASON_TERMINATE_LOCAL_HOST -> "The local device initiated disconnection."
                    ConnectionObserver.REASON_TERMINATE_PEER_USER -> "The remote device initiated graceful disconnection."
                    ConnectionObserver.REASON_LINK_LOSS -> "This reason will only be reported when ConnectRequest.shouldAutoConnect() was called and connection to the device was lost. Android will try to connect automatically."
                    ConnectionObserver.REASON_NOT_SUPPORTED -> "The device does not hav required services."
                    ConnectionObserver.REASON_TIMEOUT -> "The connection timed out. The device might have reboot, is out of range, turned off or doesn't respond for another reason."
                    else -> "disconnect"
                }

                Toast.makeText(this, reason, Toast.LENGTH_SHORT).show()
            }

    }


    override fun onBleStateChanged(model: Int, state: Int) {
        // Ble.State
        Log.d(TAG, "model $model, state: $state")
        _bleState.value = state == Ble.State.CONNECTED
        Log.d(TAG, "bleState $bleState")
    }

    override fun onSupportNavigateUp(): Boolean {
        BleServiceHelper.BleServiceHelper.stopScan()
        finish()
        return false;
    }
}