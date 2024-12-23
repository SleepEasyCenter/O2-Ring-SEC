package com.sleepeasycenter.o2ring_app

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.ext.oxy.DeviceInfo
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import no.nordicsemi.android.ble.observer.ConnectionObserver

class OximetryDeviceController// Private constructor prevents instantiation
private constructor() : BleChangeObserver {
    var _connected: MutableLiveData<Boolean> = MutableLiveData(false)
    var connected: LiveData<Boolean> = _connected

    var connected_device: Bluetooth? = null
        private set
        get() {
            if (connected.value != true) return null
            return field
        }

    var _filenames: MutableLiveData<Array<String>> = MutableLiveData(arrayOf())
    var filenames: LiveData<Array<String>> = _filenames

    // Initialises the event bus for this class
    fun initEventBus(mainActivity: MainActivity) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxySyncDeviceInfo)
            .observe(mainActivity) {

                val types = it.data as Array<String>
                for (type in types) {
                    Log.d(TAG, "$type success")
                }
            }

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyInfo).observe(mainActivity) {
            val data = it.data as DeviceInfo
            val list = data.fileList.split(",")
            val filtered = list.filter { file -> return@filter file != ""; }.toTypedArray()
            _filenames.postValue(filtered)
        }

        LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceDisconnectReason)
            .observe(mainActivity) {
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
                Toast.makeText(mainActivity, reason, Toast.LENGTH_SHORT).show()
            }
    }

    fun connectDevice(
        device: Bluetooth,
        serviceHelper: BleServiceHelper,
        appCtx: Context,
        lifecycle: Lifecycle
    ) {
        serviceHelper.setInterfaces(device.model)
        // add observer(ble state)
        lifecycle.addObserver(BIOL(this, intArrayOf(device.model)))
        serviceHelper.connect(appCtx, device.model, device.device)

        this.connected_device = device
    }

    override fun onBleStateChanged(model: Int, state: Int) {
        Log.d(TAG, "model $model, state: $state")
        _connected.setValue(state == Ble.State.CONNECTED)
        Log.d(TAG, "Connected? " + connected.value)
        BleServiceHelper.BleServiceHelper.oxyGetInfo(model)
    }


    companion object {
        var TAG: String = "OximetryDeviceManager(Singleton)"
        private var _instance: OximetryDeviceController? = null
            // Public method to provide access to the instance
            get() {
                if (field == null) {
                    field = OximetryDeviceController()
                }
                return field
            }
            private set
        val instance: OximetryDeviceController get() = _instance!!;
    }
}
