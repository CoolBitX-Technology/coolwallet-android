package com.snscity.egdwlib.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UUIDUtil {

    public static String getUUID(Context context) {
        String m_szLongID = getIMIE(context) + getBtMac()
                + getDeviceId();
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        m.update(m_szLongID.getBytes(), 0, m_szLongID.length());
        byte p_md5Data[] = m.digest();
        String m_szUniqueID = new String();
        for (int i = 0; i < p_md5Data.length; i++) {
            int b = (0xFF & p_md5Data[i]);
            if (b <= 0xF)
                m_szUniqueID += "0";
            m_szUniqueID += Integer.toHexString(b);
        }
        m_szUniqueID = m_szUniqueID.toUpperCase();
        return m_szUniqueID;
    }

    private static String getIMIE(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    private static String getBtMac() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            return bluetoothAdapter.getAddress();
        }
        return null;
    }

    private static String getDeviceId() {
        String deviceId = "00" +
                Build.BOARD.length() % 10 +
                Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 +
                Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 +
                Build.HOST.length() % 10 +
                Build.ID.length() % 10 +
                Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 +
                Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 +
                Build.TYPE.length() % 10 +
                Build.USER.length() % 10;
        return deviceId;
    }
}
