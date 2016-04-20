package com.snscity.egdwlib.ble;

public interface BleGattCallback {
    void onWriteToA007(byte[] data);

    void onWriteToA008(byte[] data);

    void onReadFromA006(byte[] data);

    void onReadFromA009(byte[] data);
}
