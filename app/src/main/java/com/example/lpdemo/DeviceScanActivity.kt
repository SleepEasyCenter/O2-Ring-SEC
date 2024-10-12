package com.example.lpdemo

import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.objs.BluetoothController
import com.lepu.blepro.observer.BleChangeObserver

class DeviceScanActivity : AppCompatActivity(), BleChangeObserver {
    val TAG: String = "DeviceScanActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_device_scan)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initView()
        initService()
    }

    private val models = intArrayOf(Bluetooth.MODEL_O2RING)
    fun initService() {
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

    fun initView(){
        LiveEventBus.get<Bluetooth>(EventMsgConst.Discovery.EventDeviceFound).observe(this) {
            Log.d(TAG, "EventDeviceFound")
            for (b in BluetoothController.getDevices()) {
                Log.d(TAG, "\t - "+b.name + " | " + b.macAddr)
            }
        }
    }

    override fun onBleStateChanged(model: Int, state: Int) {
        TODO("Not yet implemented")
    }
}