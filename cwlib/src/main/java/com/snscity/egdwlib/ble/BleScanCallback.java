package com.snscity.egdwlib.ble;

import android.bluetooth.BluetoothDevice;

public interface BleScanCallback {
    void onBleDeviceDiscovered(BluetoothDevice device, int rssi, byte[] scanRecord);
}
