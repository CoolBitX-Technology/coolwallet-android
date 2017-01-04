package com.snscity.egdwlib;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.widget.Toast;

import com.snscity.egdwlib.ble.BleGattCallback;
import com.snscity.egdwlib.ble.BleScanCallback;
import com.snscity.egdwlib.ble.BleStateCallback;
import com.snscity.egdwlib.cmd.CmdProcessor;
import com.snscity.egdwlib.utils.LogUtil;
import com.snscity.egdwlib.utils.ValidationException;

import java.util.ArrayList;
import java.util.List;


public class BleManager {
    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BleScanCallback bleScanCallback;
    private BluetoothGatt bluetoothGatt;
    private String bluetoothAddress;
    private boolean isScanning;
    private BleStateCallback bleStateCallback;
    private List<BluetoothGattCharacteristic> gattCharacteristics;
    private BleGattCallback bleGattCallback;

    private CmdProcessor cmdProcessor;

    private Handler handler;

    public BleManager(Context context)  {
        this.context = context;
        handler = new Handler();
        try {
            initialize();
        }catch(ValidationException ve){
//            LogUtil.ClickFunction(context,"Alert Message",ve.getMessage());
            Toast.makeText(context,ve.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private void initialize() throws ValidationException {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            LogUtil.e("your device not support bluetooth");
            throw new ValidationException("your device not support bluetooth");
//            return;
        }
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            LogUtil.e("your device not support ble");
            throw new ValidationException("your device not support bluetooth");
        }
    }

    public boolean isOpen() {

        return bluetoothAdapter.isEnabled();
    }

    public boolean openBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            return bluetoothAdapter.enable();
        }
        return true;
    }

    public boolean closeBluetooth() {
        if (bluetoothAdapter.isEnabled()) {
            return bluetoothAdapter.disable();
        }
        return true;
    }

    public boolean startScanBle(BleScanCallback bleScanCallback) {
        this.bleScanCallback = bleScanCallback;
        if (bluetoothAdapter.isEnabled() && !isScanning) {
            isScanning = bluetoothAdapter.startLeScan(leScanCallback);
            return isScanning;
        }
        return false;
    }

    public void stopScanBle() {
        LogUtil.i("stopScanBle");
        if (bluetoothAdapter.isEnabled() && isScanning) {
            bluetoothAdapter.stopLeScan(leScanCallback);
            isScanning = false;
        }
    }

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            final BluetoothDevice bluetoothDevice = device;
            final BleScanCallback callback = bleScanCallback;
//            LogUtil.e("discover ble device:" + device.getName() + "(" + bluetoothDevice.getAddress() + ")");b4

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onBleDeviceDiscovered(bluetoothDevice, rssi, scanRecord);
                    }
                }
            });
        }
    };

    public boolean connectBle(BluetoothDevice device, BleStateCallback bleStateCallback) {
        return connectBle(device.getAddress(), bleStateCallback);
    }

    public boolean connectBle(String address, BleStateCallback bleStateCallback) {
        this.bleStateCallback = bleStateCallback;
        if (bluetoothAddress != null && address.equals(bluetoothAddress) && bluetoothGatt != null) {
            return bluetoothGatt.connect();
        }
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            return false;
        }
        bluetoothGatt = device.connectGatt(context.getApplicationContext(), false, mGattCallback);
        bluetoothAddress = address;
        return true;
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                LogUtil.e("device connected");
                if (bluetoothGatt != null) {
                    bluetoothGatt.discoverServices();

                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                LogUtil.e("device disconnected");
                final BleStateCallback callback = bleStateCallback;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onBleDisConnected();
                        }
                    }
                });
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                LogUtil.e("services discovered");
                gattCharacteristics = new ArrayList<>();
                if (bluetoothGatt != null) {
                    getGattCharacteristics(bluetoothGatt.getServices());
                }
                try {
                    cmdProcessor = CmdProcessor.getInstance();
                    cmdProcessor.init(bluetoothGatt, gattCharacteristics);
                    setBleGattCallback(cmdProcessor);
                } catch (Exception e) {
                    e.getStackTrace();
                }
                final BleStateCallback callback = bleStateCallback;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onBleConnected();
                        }
                    }
                });
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                onCharacteristicReadOrWrite(characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                onCharacteristicReadOrWrite(characteristic);
            }
        }
    };

    private void getGattCharacteristics(List<BluetoothGattService> gattServices) {
        if (gattServices == null) {
            return;
        }
        for (BluetoothGattService gattService : gattServices) {

            if (gattService.getUuid().toString().startsWith("0000a000")) {
                List<BluetoothGattCharacteristic> bluetoothGattCharacteristics = gattService.getCharacteristics();
                for (BluetoothGattCharacteristic bluetoothGattCharacteristic : bluetoothGattCharacteristics) {
                    if (bluetoothGattCharacteristic.getUuid().toString().startsWith("0000a006")) {
                        gattCharacteristics.add(0, bluetoothGattCharacteristic);
                        continue;
                    }
                    if (bluetoothGattCharacteristic.getUuid().toString().startsWith("0000a007")) {
                        gattCharacteristics.add(1, bluetoothGattCharacteristic);
                        continue;
                    }
                    if (bluetoothGattCharacteristic.getUuid().toString().startsWith("0000a008")) {
                        gattCharacteristics.add(2, bluetoothGattCharacteristic);
                        continue;
                    }
                    if (bluetoothGattCharacteristic.getUuid().toString().startsWith("0000a009")) {
                        gattCharacteristics.add(3, bluetoothGattCharacteristic);
                    }
                }
            }
        }
    }

    private void onCharacteristicReadOrWrite(BluetoothGattCharacteristic characteristic) {
        final String uuid = characteristic.getUuid().toString();
        final byte[] data = characteristic.getValue();

        if (gattCharacteristics != null && gattCharacteristics.size() == 4) {
            if (uuid.startsWith("0000a007")) {
//                LogUtil.e("onWriteToA007");
                bleGattCallback.onWriteToA007(data);
            }
            if (uuid.startsWith("0000a008")) {
//                LogUtil.e("onWriteToA008");
                bleGattCallback.onWriteToA008(data);
            }
            if (uuid.startsWith("0000a006")) {
//                LogUtil.e("onReadFromA006");
                bleGattCallback.onReadFromA006(data);
            }
            if (uuid.startsWith("0000a009")) {
//                LogUtil.e("onReadFromA009");
                bleGattCallback.onReadFromA009(data);
            }
        }
    }

    public void setBleGattCallback(BleGattCallback bleGattCallback) {
        this.bleGattCallback = bleGattCallback;
    }

    public void disConnectBle() {
        LogUtil.i("BleManager disConnectBle()");
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            closeGatt();
        }
    }

    private void closeGatt() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.disconnect();
        bluetoothGatt = null;
        gattCharacteristics = null;
        cmdProcessor = null;
    }
}
