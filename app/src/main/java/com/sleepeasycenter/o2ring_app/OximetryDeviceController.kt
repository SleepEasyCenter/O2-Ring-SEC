package com.sleepeasycenter.o2ring_app;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.lepu.blepro.ext.BleServiceHelper;
import com.lepu.blepro.constants.Ble;
import com.lepu.blepro.objs.Bluetooth;
import com.lepu.blepro.observer.BIOL;
import com.lepu.blepro.observer.BleChangeObserver;

public class OximetryDeviceManager implements BleChangeObserver {
    public static String TAG = "OximetryDeviceManager";
    private static OximetryDeviceManager instance;
    public MutableLiveData<Boolean> _connected = new MutableLiveData<>(false);
    public LiveData<Boolean> connected = _connected;

    // Private constructor prevents instantiation
    private OximetryDeviceManager() {
    }

    // Public method to provide access to the instance
    public static OximetryDeviceManager getInstance() {
        if (instance == null) {
            instance = new OximetryDeviceManager();
        }
        return instance;
    }

    public void connectDevice(Bluetooth device, BleServiceHelper serviceHelper, Context appCtx, Lifecycle lifecycle) {
        serviceHelper.setInterfaces(device.getModel());
        // add observer(ble state)
        lifecycle.addObserver(new BIOL(this, new int[]{device.getModel()}));
        serviceHelper.connect(appCtx, device.getModel(), device.getDevice());
    }

    @Override
    public void onBleStateChanged(int model, int state) {
        Log.d(TAG, "model " + model + ", state: " + state);
        _connected.setValue(state == Ble.State.CONNECTED);
        Log.d(TAG, "Connected? " + connected.getValue());
    }
}
