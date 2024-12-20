package com.sleepeasycenter.o2ring_app

import android.Manifest
import android.app.AlertDialog
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
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.objs.BluetoothController
import com.lepu.blepro.observer.BleChangeObserver
import com.permissionx.guolindev.PermissionX
import com.sleepeasycenter.o2ring_app.adapters.DeviceListViewAdapter
import com.sleepeasycenter.o2ring_app.databinding.ActivityDeviceScanBinding
import com.sleepeasycenter.o2ring_app.utils._bleState
import com.sleepeasycenter.o2ring_app.utils.bleState
import no.nordicsemi.android.ble.observer.ConnectionObserver
import android.provider.Settings

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

        setSupportActionBar(binding.actionbar)
        supportActionBar?.setTitle("Scanning for devices...")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.show()

        val recyclerView = binding.deviceList;
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setAdapter(adapter);

        initEventBus()
        needPermission()
    }

    private fun onPermissionGranted() {
        if (!openBluetooth()){
            Toast.makeText(this, "Failed to turn on bluetooth!", Toast.LENGTH_SHORT).show()
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
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
            ).onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList, "location permission", "ok", "ignore"
                )
            }.onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList, "location setting", "ok", "ignore"
                )
            }.request { allGranted, grantedList, deniedList ->
                Log.d(TAG, "permission : $allGranted, $grantedList, $deniedList")

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
                Log.d(TAG, "permission : $allGranted, $grantedList, $deniedList")

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
                Log.d(TAG, "permission : $allGranted, $grantedList, $deniedList")

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

            if (hasConnectPerms) {
                return adapter.enable();
            } else {
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

            // initRawFolder必须在initService之前调用
            BleServiceHelper.BleServiceHelper.initRawFolder(rawFolders).initService(application)
                .initLog(true)
        }
    }

    private fun initEventBus() {
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
        LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceDisconnectReason).observe(this) {
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