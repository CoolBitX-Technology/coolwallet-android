package com.coolbitx.coolwallet.entity;

import java.nio.charset.Charset;

/**
 * Created by MyPC on 2015/8/27.
 * 常量类
 */
public class Constant {

    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static final int NONSUPPORT_BLE = 0xA0;

    public static final int NONSUPPORT_BLUETOOTH = 0xA1;

    public static final int SCAN_BLUETOOTH_DEVICE = 0xA2;

    public static final int BLUETOOTH_DEVICE_CONNECTED = 0xA3;

    public static final int BLUETOOTH_DEVICE_DISCONNECT = 0xA4;

    public static final int SERVICES_DISCOVERED = 0xA5;

    public static final int SERVICES_UNDISCOVERED = 0xA6;

    /** a009是FC时返回的数据*/
    public static final int RESULT_RETURN = 0xB1;

    public static final byte CwAddressKeyChainExternal = 0x00;
    public static final byte CwAddressKeyChainInternal = 0x01;

    public static final byte CwAddressInfoAddress = 0x00;
    public static final byte CwAddressInfoPublicKey = 0x01;

    public static final byte CwHdwAccountKeyInfoAddress = 0x00;
    public static final byte CwHdwAccountKeyInfoPubKey = 0x01;
    public static final byte CwHdwAccountKeyInfoPubKeyAndChainCd = 0x02;

    //trxStatus
    public static final byte TrxStatusBegin = 0x00;
    public static final byte TrxStatusWaitOtp = 0x01;
    public static final byte TrxStatusGetOtp = 0x02;
    public static final byte TrxStatusWaitBtn = 0x03;
    public static final byte TrxStatusGetBtn = 0x04;
    public static final byte TrxStatusSigned = 0x05;
    public static final byte TrxStatusFinish = 0x06;
}
