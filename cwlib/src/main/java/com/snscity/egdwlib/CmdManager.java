package com.snscity.egdwlib;


import android.util.Log;

import com.snscity.egdwlib.cmd.CmdCla;
import com.snscity.egdwlib.cmd.CmdIns;
import com.snscity.egdwlib.cmd.CmdPacket;
import com.snscity.egdwlib.cmd.CmdProcessor;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.AES;
import com.snscity.egdwlib.utils.ByteUtil;
import com.snscity.egdwlib.utils.HMAC;
import com.snscity.egdwlib.utils.LogUtil;
import com.snscity.egdwlib.utils.PBKDF;

import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class CmdManager {

    private final static String CHARSETNAME = "UTF-8";
    private final static String TAG = "coolwallet";
    private CmdProcessor cmdProcessor;

    private byte CwSecurityPolicyMaskOtp = 0x01;
    private byte CwSecurityPolicyMaskBtn = 0x02;
    private byte CwSecurityPolicyMaskWatchDog = 0x10;
    private byte CwSecurityPolicyMaskAddress = 0x20;

    private final byte CwHdwAccountInfoName = 0x00;
    private final byte CwHdwAccountInfoBalance = 0x01;
    private final byte CwHdwAccountInfoExtKeyPtr = 0x02;
    private final byte CwHdwAccountInfoIntKeyPtr = 0x03;
    private final byte CwHdwAccountInfoBlockAmount = 0x04;

    //******************************************************
    //Default Init data (set by init tool)
    //******************************************************
    private final static byte[] TEST_PUK = {0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x2b, 0x2c, 0x2d, 0x2e, 0x2f,
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x3b, 0x3c, 0x3d, 0x3e, 0x3f};
    private final static byte[] TEST_XCHSSEMK = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f,
            0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f};
    private final static byte[] TEST_XCHSOTPK = {0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x2b, 0x2c, 0x2d, 0x2e, 0x2f,
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x3b, 0x3c, 0x3d, 0x3e, 0x3f};
    private final static byte[] TEST_XCHSSMK = {0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f,
            0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b, 0x5c, 0x5d, 0x5e, 0x5f};

    //******************************************************
    //Default VMK
    //******************************************************
    private final static byte[] TEST_VMK = {0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f,
            0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b, 0x5c, 0x5d, 0x5e, 0x5f};
    /* Host description (64 bytes) */
    private final static byte[] PreHostDesc0 = {
            0x50, 0x72, 0x65, 0x2d, 0x72, 0x65, 0x67, 0x69, 0x73, 0x74, 0x65, 0x72, 0x65, 0x64, 0x20, 0x68,
            0x6f, 0x73, 0x74, 0x20, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private final static byte[] PreHostOtpKey0 = {
            (byte) 0xD0, (byte) 0xBD, (byte) 0xBC, (byte) 0x88, (byte) 0x84, 0x47, 0x54, (byte) 0xC5, (byte) 0xDF,
            (byte) 0x9C, 0x40, (byte) 0xDA, 0x30, (byte) 0x99, (byte) 0x95, (byte) 0x95, (byte) 0xF4, 0x12, 0x00,
            0x75, 0x15, 0x0B, 0x1B, (byte) 0xB2, (byte) 0xD3, 0x14, 0x5F, 0x6A, 0x3D, (byte) 0xC1, (byte) 0xB6, (byte) 0xE8};
    private final static byte[] PreHostDesc1 = {
            0x50, 0x72, 0x65, 0x2d, 0x72, 0x65, 0x67, 0x69, 0x73, 0x74, 0x65, 0x72, 0x65, 0x64, 0x20, 0x68,
            0x6f, 0x73, 0x74, 0x20, 0x31, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private final static byte[] PreHostOtpKey1 = {
            (byte) 0xF1, 0x49, 0x54, 0x11, (byte) 0xF8, (byte) 0xC0, 0x6A, (byte) 0xFC, (byte) 0xD2, (byte) 0xFF,
            0x61, (byte) 0x97, (byte) 0x99, (byte) 0x84, 0x69, 0x63, (byte) 0xB2, 0x63, 0x67, 0x30, 0x14, (byte) 0x85,
            0x51, 0x29, 0x25, (byte) 0xFA, 0x19, (byte) 0xFD, 0x41, 0x78, 0x43, 0x2A};
    private final static byte[] PreHostDesc2 = {
            0x50, 0x72, 0x65, 0x2d, 0x72, 0x65, 0x67, 0x69, 0x73, 0x74, 0x65, 0x72, 0x65, 0x64, 0x20, 0x68,
            0x6f, 0x73, 0x74, 0x20, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private final static byte[] PreHostOtpKey2 = {
            0x56, (byte) 0x87, 0x55, (byte) 0xDD, 0x22, (byte) 0x96, (byte) 0xEB, 0x70, (byte) 0x8E, (byte) 0x88, (byte) 0x90,
            (byte) 0xAB, 0x7C, 0x7E, (byte) 0x8C, (byte) 0xC1, 0x3D, (byte) 0xCF, 0x00, (byte) 0xD5, (byte) 0xD1, 0x42, 0x3A,
            0x05, (byte) 0xC8, 0x6D, (byte) 0xA8, (byte) 0x90, (byte) 0xC8, 0x28, 0x67, 0x26};
    private final static byte[] PreHostDesc3 = {
            0x50, 0x72, 0x65, 0x2d, 0x72, 0x65, 0x67, 0x69, 0x73, 0x74, 0x65, 0x72, 0x65, 0x64, 0x20, 0x68,
            0x6f, 0x73, 0x74, 0x20, 0x33, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private final static byte[] PreHostOtpKey3 = {
            0x1F, 0x16, (byte) 0xE0, (byte) 0x8C, 0x23, 0x68, (byte) 0xF8, (byte) 0xC0, 0x32, (byte) 0xD8, (byte) 0xED,
            (byte) 0xB5, (byte) 0xFA, 0x29, 0x1F, 0x51, (byte) 0xB5, (byte) 0xF4, (byte) 0xEA, 0x06, 0x6C, (byte) 0xE4,
            (byte) 0xF7, (byte) 0xE6, (byte) 0xDC, 0x0F, 0x2D, (byte) 0xC2, (byte) 0xF2, 0x6F, 0x43, 0x7B};
    private final static byte[] PreHostDesc4 = {
            0x50, 0x72, 0x65, 0x2d, 0x72, 0x65, 0x67, 0x69, 0x73, 0x74, 0x65, 0x72, 0x65, 0x64, 0x20, 0x68,
            0x6f, 0x73, 0x74, 0x20, 0x34, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private final static byte[] PreHostOtpKey4 = {
            0x6D, (byte) 0x98, 0x15, (byte) 0xBF, 0x1A, (byte) 0xD6, (byte) 0xB1, (byte) 0x8E, (byte) 0xDF, (byte) 0x8D,
            (byte) 0x97, 0x57, 0x33, 0x23, 0x42, (byte) 0xB1, 0x39, 0x73, 0x46, 0x62, 0x07, 0x0A, (byte) 0xA8, 0x6C,
            (byte) 0x80, 0x7F, (byte) 0xC8, 0x32, (byte) 0xDE, (byte) 0xEF, (byte) 0xF6, (byte) 0xC9};
    private final static byte[] PreHostDesc5 = {
            0x50, 0x72, 0x65, 0x2d, 0x72, 0x65, 0x67, 0x69, 0x73, 0x74, 0x65, 0x72, 0x65, 0x64, 0x20, 0x68,
            0x6f, 0x73, 0x74, 0x20, 0x35, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private final static byte[] PreHostOtpKey5 = {
            0x60, (byte) 0xAC, 0x13, 0x14, 0x29, (byte) 0x80, 0x36, 0x79, (byte) 0xB5, 0x49, (byte) 0xAF, 0x30, 0x46,
            0x0E, 0x14, (byte) 0x83, (byte) 0xEA, (byte) 0xED, 0x34, (byte) 0x8A, (byte) 0xBF, 0x54, 0x6D, 0x37,
            (byte) 0xD5, 0x5D, (byte) 0x98, (byte) 0x82, (byte) 0xAD, 0x47, (byte) 0xA2, (byte) 0xB0};
    private final static byte[] PreHostDesc6 = {
            0x50, 0x72, 0x65, 0x2d, 0x72, 0x65, 0x67, 0x69, 0x73, 0x74, 0x65, 0x72, 0x65, 0x64, 0x20, 0x68,
            0x6f, 0x73, 0x74, 0x20, 0x36, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private final static byte[] PreHostOtpKey6 = {
            0x18, (byte) 0xF3, (byte) 0xFB, 0x2D, 0x6D, 0x06, (byte) 0xA6, 0x21, (byte) 0xD3, (byte) 0xAA, 0x54, (byte) 0xE1,
            0x54, (byte) 0x89, (byte) 0xB6, 0x66, (byte) 0xE8, 0x01, (byte) 0xD4, 0x1C, (byte) 0xB7, 0x62, 0x65, (byte) 0xE7,
            (byte) 0xFA, 0x49, (byte) 0xBE, 0x51, 0x7E, 0x17, 0x64, (byte) 0xD0};

    public CmdManager() {
        cmdProcessor = CmdProcessor.getInstance();
    }


    public CmdProcessor getCmdProcessor(){
        return cmdProcessor;
    }

    public void hdwQryWaInfo(int infoId, CmdResultCallback cmdResultCallback) {
        //P1: infoId 1B (=00 status, 01 name, 02 accountPointer)
        //output:
        //infoId 1B (=00/01/02)
        //hwdInfo:
        //  hwdStatus 1B
        //  hwdName 32B
        //  hwdAccountPointer 4B
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.HDW_QUERY_WALLET_INFO)
                .setIns(CmdIns.HDW_QUERY_WALLET_INFO)
                .setPram1(infoId)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void trxFinish(CmdResultCallback cmdResultCallback) {
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.TRX_FINISH)
                .setIns(CmdIns.TRX_FINISH)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }


    public void hdwQueryAccountInfo(int infoId, int accountId, CmdResultCallback cmdResultCallback) {
        //P1: infoId: 1B (00 name, 01 balance, 02 ext key pointer, 03 int key pointer)
        //accountId 4B
        /*
        accountName 32B
        balance 8B
        extKeyPointer   4B
        intKeyPointer   4B
         */
        byte[] accId = ByteUtil.intToByteLittle(accountId, 4);//转换成小端模式 4b
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.HDW_QUERY_ACCOUNT_INFO)
                .setIns(CmdIns.HDW_QUERY_ACCOUNT_INFO)
                .setPram1(infoId)
                .setInputData(accId)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void hdwCreateAccount(int accId, String accName, CmdResultCallback cmdResultCallback) {
        byte[] name = new byte[32];
        byte[] accNameBytes = accName.getBytes(Charset.forName(CHARSETNAME));
        int accNameLength = accNameBytes.length;
        if (accNameLength <= 32) {
            for (int i = 0; i < accNameLength; i++) {
                name[i] = accNameBytes[i];
            }
        } else {
            return;
        }

        byte[] id = ByteUtil.intToByteLittle(accId, 4);//转换成小端模式 4b

        int length1 = id.length;
        int length2 = name.length;
        int length = length1 + length2;
        byte[] inputData = new byte[length];
        for (int i = 0; i < length1; i++) {
            inputData[i] = id[i];
        }

        for (int i = 0; i < length2; i++) {
            inputData[i + length1] = name[i];
        }

        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.HDW_CREATE_ACCOUNT)
                .setIns(CmdIns.HDW_CREATE_ACCOUNT)
                .setInputData(inputData)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void hdwSetAccInfo(byte[] macKey, byte infoId, int accountId, byte[] accountInfo,
                              CmdResultCallback cmdResultCallback) {
        byte[] accInfo = new byte[accountInfo.length];
        int accInfoLen = 0;
//        LogUtil.i("cmd hdwSetAccInfo:accID="+accountId+"; infoID="+infoId+"; accountInfo="+ LogUtil.byte2HexString(accountInfo));
        int accountInfoLen = accountInfo.length;
        if (accountInfoLen <= 32) {
            for (int i = 0; i < accountInfoLen; i++) {
                accInfo[i] = accountInfo[i];
            }
        }
        switch (infoId) {
            case CwHdwAccountInfoName:
                accInfoLen = 32;
                break;
            case CwHdwAccountInfoBalance:

//                accInfoLen = 8;
//                accInfo = new byte[accInfoLen];

                //reverse位置
                accInfoLen = 8;
                for (int i = 0; i < accInfoLen; i++) {
                    accInfo[i] = accountInfo[accInfoLen - i - 1];
                }
                break;
            case CwHdwAccountInfoExtKeyPtr:
                accInfoLen = 4;
//                accInfo = new byte[accInfoLen];
                break;
            case CwHdwAccountInfoIntKeyPtr:
                accInfoLen = 4;
//                accInfo = new byte[accInfoLen];
                break;
        }

        //Calc MAC by macKey
        byte[] mac = HMAC.getSignature(accInfo, macKey);
        byte[] accId = ByteUtil.intToByteLittle(accountId, 4);//转换成小端模式 4b
        int accIdLen = accId.length;

        int length = accIdLen + accInfoLen + mac.length;

        byte[] inputData = new byte[length];
        LogUtil.i("hdwSetAccInfo inputData length=" + inputData.length+" ;accIdLen="+accIdLen+" ;accInfoLen="+accInfoLen);

        // accIdLen=4
        for (int i = 0; i < accIdLen; i++) {
            inputData[i] = accId[i];
//            LogUtil.i("hdwSetAccInfo inputData length="+i+"="+accId[i]);
        }

        //accInfoLen(variable length)
        for (int i = 0; i < accInfoLen; i++) {
            inputData[i + accIdLen] = accInfo[i];
//            LogUtil.i("hdwSetAccInfo inputData length="+ i + accIdLen +"="+accInfo[i] );
        }

        for (int i = 0; i < 32; i++) {
//            LogUtil.i("mac length="+(i + accIdLen + accInfoLen));
            inputData[i + accIdLen + accInfoLen] = mac[i];
        }

        LogUtil.i("hdwSetAccInfo_hex:infoId= "+ infoId+"\n"+
                " ;accId= "+LogUtil.byte2HexString(accId)+"\n"+
                ";accinfo= "+LogUtil.byte2HexString((inputData)));

        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.HDW_SET_ACCOUNT_INFO)
                .setIns(CmdIns.HDW_SET_ACCOUNT_INFO)
                .setPram1(infoId)
                .setInputData(inputData)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void hdwInitWalletGenConfirm(byte[] activeCode, String sumOfSeeds,
                                        CmdResultCallback cmdResultCallback) {
        /*
        input
        activeCode 4b
        sumOfSeeds 6b

        output none
         */
        String sumOfSeedsSub = "";
        int length = sumOfSeeds.length();
        if (length > 6) {
            sumOfSeedsSub = sumOfSeeds.substring(length - 6);
        }

        byte[] sumOfSeedsSubBytes = sumOfSeedsSub.getBytes(Charset.forName(CHARSETNAME));

        int length1 = activeCode.length;
        int length2 = sumOfSeedsSubBytes.length;
        int length3 = length1 + length2;

        byte[] inputData = new byte[length3];
        for (int i = 0; i < length1; i++) {
            inputData[i] = activeCode[i];
        }

        for (int i = 0; i < length2; i++) {
            inputData[i + length1] = sumOfSeedsSubBytes[i];
        }

        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.HDW_INIT_WALLET_GEN_CONFIRM)
                .setIns(CmdIns.HDW_INIT_WALLET_GEN_CONFIRM)
                .setInputData(inputData)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void hdwInitWalletGen(String hdwName, int seedLen, CmdResultCallback cmdResultCallback) {
        byte[] name = new byte[32];
        byte[] sLen = new byte[1];

//        sLen[0] = (byte) (Integer.parseInt(Integer.toHexString(seedLen))/ 2) ;
        sLen[0] = (byte) (seedLen / 2); //dora
//        sLen[0] = (byte) (seedLen * 3 );
        byte[] passPhrase = {0x02, 0x30, 0x31};

        for (int i = 0; i < 32; i++) {
            name[i] = 0x00;
        }
        /*
        input
        hdwName 32b
        seedLen 1b(=24/36/48)
        passPhraseLen:1b(=0)

        output
        seedString 12/18/24b, bcd format
        activeCode 4b
        mac (seedString || activeCode)
         */

        //name
        byte[] hdwNameBytes = hdwName.getBytes(Charset.forName(CHARSETNAME));
        int length = hdwNameBytes.length;
        for (int i = 0; i < length; i++) {
            name[i] = hdwNameBytes[i];
        }

        int length1 = name.length;

        int length0 = length1 + 1 + 1;
//        byte [] inputData = new byte[length0];
//        for (int i = 0; i < length1; i++) {
//            inputData[i] = name[i];
//        }
//
//        inputData[length1] = sLen[0];
//        passPhrase[0] = 0x00;
//        inputData[length1 + 1] = passPhrase[0];


        byte[] inputData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x18, 0x00};


        LogUtil.e("Wallet init by card=" + Arrays.toString(inputData));


        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.HDW_INIT_WALLET_GEN)
                .setIns(CmdIns.HDW_INIT_WALLET_GEN)
                .setInputData(inputData)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void hdwInitWallet(String hdwName, String hdwSeed,
                              byte[] encKey, byte[] macKey,
                              CmdResultCallback cmdResultCallback) {
        try {
            byte[] name = new byte[32];

            //name
            byte[] hdwNameBytes = hdwName.getBytes(Charset.forName(CHARSETNAME));
            int length = hdwNameBytes.length;
            if (length <= 32) {
                for (int i = 0; i < length; i++) {
                    name[i] = hdwNameBytes[i];
                }
            } else {
                return;
            }

            //seed
//        String algorithm = "PBKDF2WithHmacSHA512";
            String algorithm = "HmacSHA512";
            int BIP32_SEED_LENGTH = 64;
            int REPETITIONS = 2048;
            String base_salt = "mnemonic";


            //US-ASCII
            byte[] salt = base_salt.getBytes(Charset.forName(CHARSETNAME));
            byte[] password = hdwSeed.getBytes(Charset.forName(CHARSETNAME));
//        byte [] seed = AES.getPBKDF2Encrypt(algorithm,
//                hdwSeed.toCharArray(),
//                salt,
//                REPETITIONS,
//                BIP32_SEED_LENGTH);
            byte[] seed = PBKDF.pbkdf2(
                    algorithm,
                    password,
                    salt,
                    REPETITIONS,
                    BIP32_SEED_LENGTH);
            LogUtil.e("seed==========" + seed.length);

            //Encrypt seed by encKey
            byte[] encSeed = AES.getAESEncrypt(seed, encKey);

            //Calc MAC by macKey
            byte[] mac = HMAC.getSignature(encSeed, macKey);

            //合并
            int length1 = name.length;
            int length2 = encSeed.length;
            int length3 = mac.length;

            int lengthInput = length1 + length2 + length3;
            byte[] inputData = new byte[lengthInput];

            for (int i = 0; i < length1; i++) {
                inputData[i] = name[i];
            }

            for (int i = 0; i < length2; i++) {
                inputData[i + length1] = encSeed[i];
            }

            for (int i = 0; i < length3; i++) {
                inputData[i + length1 + length2] = mac[i];
            }

            CmdPacket cmdPacket = new CmdPacket.Builder()
                    .setCla(CmdCla.HDW_INIT_WALLET)
                    .setIns(CmdIns.HDW_INIT_WALLET)
                    .setInputData(inputData)
                    .build();
            cmdPacket.setCmdResultListener(cmdResultCallback);
            cmdProcessor.addCmd(cmdPacket);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Verify Otp
     */

    public void trxVerifyOtp(String otp, CmdResultCallback cmdResultCallback) {
        byte[] inputData = otp.getBytes(Charset.forName(CHARSETNAME));
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.TRX_VERIFY_OTP)
                .setIns(CmdIns.TRX_VERIFY_OTP)
                .setInputData(inputData)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    /**
     * Get error
     */
    public void getError(CmdResultCallback cmdResultCallback) {
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.GET_ERROR)
                .setIns(CmdIns.GET_ERROR)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    //Get security policy setting
    public void getSecpo(CmdResultCallback cmdResultCallback) {
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.GET_PERSO)
                .setIns(CmdIns.GET_PERSO)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void setSecpo(boolean[] options, CmdResultCallback cmdResultCallback) {
        byte[] sp = new byte[4];
        if (options[0]) {
            sp[0] = (byte) (sp[0] | CwSecurityPolicyMaskOtp);
        }
        if (options[1]) {
            sp[0] = (byte) (sp[0] | CwSecurityPolicyMaskBtn);
        }
        if (options[2]) {
            sp[0] = (byte) (sp[0] | CwSecurityPolicyMaskAddress);
        }
        if (options[3]) {
            sp[0] = (byte) (sp[0] | CwSecurityPolicyMaskWatchDog);
        }

        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.SET_PERSO)
                .setIns(CmdIns.SET_PERSO)
                .setInputData(sp)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void getCardId(CmdResultCallback cmdResultCallback) {
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.GET_CARD_ID)
                .setIns(CmdIns.GET_CARD_ID)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void getCardName(CmdResultCallback cmdResultCallback) {
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.GET_CARD_NAME)
                .setIns(CmdIns.GET_CARD_NAME)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void persoSetData(byte[] macKey, boolean[] options, CmdResultCallback cmdResultCallback) {

        byte CwSecurityPolicyMaskOtp = 0x01;
        byte CwSecurityPolicyMaskBtn = 0x02;
        byte CwSecurityPolicyMaskWatchDog = 0x10;
        byte CwSecurityPolicyMaskAddress = 0x20;

        byte[] sp = {0x00, 0x00, 0x00, 0x00};
        //securePolicy 4B
        if (options[0]) {
            sp[0] = (byte) (sp[0] | CwSecurityPolicyMaskOtp);
        } else if (options[1]) {
            sp[0] = (byte) (sp[0] | CwSecurityPolicyMaskBtn);
        } else if (options[2]) {
            sp[0] = (byte) (sp[0] | CwSecurityPolicyMaskAddress);
        } else if (options[3]) {
            sp[0] = (byte) (sp[0] | CwSecurityPolicyMaskWatchDog);
        }

        //Calc MAC by macKey
        byte[] mac = HMAC.getSignature(sp, macKey);
        LogUtil.e("mac.length:" + mac.length);

        int length1 = sp.length;
        int length2 = mac.length;
        int length = length1 + length2;
        byte[] inputData = new byte[length];

        for (int i = 0; i < length1; i++) {
            inputData[i] = sp[i];
        }

        for (int i = 0; i < length2; i++) {
            inputData[i + length1] = mac[i];
        }

        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.PERSO_SET_DATA)
                .setIns(CmdIns.PERSO_SET_DATA)
                .setInputData(inputData)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void persoConfirm(CmdResultCallback cmdResultCallback) {
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.PERSO_CONFIRM)
                .setIns(CmdIns.PERSO_CONFIRM)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void getModeState(CmdResultCallback cmdResultCallback) {
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.GET_MODE_STATE)
                .setIns(CmdIns.GET_MODE_STATE)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }


    public void SetCurrencyRate(byte[] curncy, CmdResultCallback cmdResultCallback) {
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.SET_CURR_RATE)
                .setIns(CmdIns.SET_CURR_RATE)
                .setInputData(curncy)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    /**
     * Switch Display Account
     */
    public void McuSetAccountState(byte accId, CmdResultCallback cmdResultCallback) {
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.MCU_SET_ACCOUNT)
                .setIns(CmdIns.MCU_SET_ACCOUNT)
                .setPram1(accId)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    /**
     * 批准未认证的设备注册
     *
     * @param hostId
     * @param cmdResultCallback
     */
    public void bindRegApprove(byte hostId, CmdResultCallback cmdResultCallback) {
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.BIND_REG_APPROVE)
                .setIns(CmdIns.BIND_REG_APPROVE)
                .setPram1(hostId)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    /**
     * remove设备注册
     *
     * @param hostId
     * @param cmdResultCallback
     */
    public void bindRegRemove(byte hostId, CmdResultCallback cmdResultCallback) {
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.BIND_REG_REMOVE)
                .setIns(CmdIns.BIND_REG_REMOVE)
                .setPram1(hostId)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void bindRegInfo(byte hostId, CmdResultCallback cmdResultCallback) {
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.BIND_REG_INFO)
                .setIns(CmdIns.BIND_REG_INFO)
                .setPram1(hostId)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void bindFindHstid(String uuid, CmdResultCallback cmdResultCallback) {
        //uuid:69664dec30d2461fb87e7078dcdcd8cc
//        uuid="E8C54612B79E43A485DE6B5C8746DD28";
//        LogUtil.e("2丟出去的uuid:"+uuid);
//        byte[] inputData = uuid.getBytes();
        byte[] inputData = transformBytes(uuid.getBytes(Charset.forName(CHARSETNAME)), 32);
//        uuid.getBytes(Charset.forName(CHARSETNAME));
//        LogUtil.e("2丟出去的uuid length:"+inputData.length);
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.BIND_FIND_HOST_ID)
                .setIns(CmdIns.BIND_FIND_HOST_ID)
                .setInputData(inputData)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void reInitCard(final String mode, final byte hostId, final String initCardId,
                           final String defaultPinCode, final CmdResultCallback cmdResultCallback) {
        initVmkChlng(new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    LogUtil.e("获取vmk特征值成功");
                    if (mode.equals("NOHOST") || mode.equals("DISCONN")) {
                        initBackInit(hostId, outputData, new CmdResultCallback() {
                            @Override
                            public void onSuccess(int status, byte[] outputData) {
                                if ((status + 65536) == 0x9000) {
                                    setCardData(initCardId, defaultPinCode, cmdResultCallback);
                                }
                            }
                        });
                    } else {
                        setCardData(initCardId, defaultPinCode, cmdResultCallback);
                    }
                }
            }
        });
    }

    private void setCardData(String initCardId, String defaultPinCode, CmdResultCallback cmdResultCallback) {
        byte[] pinHash = encryptSHA256(defaultPinCode.getBytes(Charset.forName(CHARSETNAME)));
        initSetData(0, pinHash, 0, null);

        //set puk
        initSetData(1, TEST_PUK, 0, null);

        //set cardId
        initSetData(2, initCardId.getBytes(Charset.forName(CHARSETNAME)), 0, null);

        //set semk keys
        //initSetData(2, TEST_XCHSSEMK, 0, null);

        //set otpk keys
        //initSetData(4, TEST_XCHSOTPK, 0, null);

        //set smk keys
        //initSetData(5, TEST_XCHSSMK, 0, null);

        //set pre-reg host description
        initSetData(3, PreHostDesc0, 0, null);
        initSetData(3, PreHostDesc1, 1, null);
        initSetData(3, PreHostDesc2, 2, null);
        initSetData(3, PreHostDesc3, 3, null);
        initSetData(3, PreHostDesc4, 4, null);
        initSetData(3, PreHostDesc5, 5, null);
        initSetData(3, PreHostDesc6, 6, null);

        //set pre-reg host otp key
        initSetData(4, PreHostOtpKey0, 0, null);
        initSetData(4, PreHostOtpKey1, 1, null);
        initSetData(4, PreHostOtpKey2, 2, null);
        initSetData(4, PreHostOtpKey3, 3, null);
        initSetData(4, PreHostOtpKey4, 4, null);
        initSetData(4, PreHostOtpKey5, 5, null);
        initSetData(4, PreHostOtpKey6, 6, null);

        initConfirm(cmdResultCallback);
    }

    public void initConfirm(CmdResultCallback cmdResultCallback) {
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.INIT_CONFIRM)
                .setIns(CmdIns.INIT_CONFIRM)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void initSetData(int pram1, byte[] data, int pram2, CmdResultCallback cmdResultCallback) {
        LogUtil.e("第" + pram1 + pram2 + "个");

        //把传入的data数组插入到bytes数组中
        byte[] bytes = new byte[96];
        int length1 = data.length;
        for (int i = 0; i < length1; i++) {
            bytes[i] = data[i];
        }

        //对传入的data数组做sha256加密
        byte[] result = encryptSHA256(data);
        int length2 = result.length;

        //把结果继续插入到bytes数组中
        for (int i = 0; i < length2; i++) {
            bytes[i + length1] = result[i];
        }

        //截取data数组长度+32的长度的新数组
        int length = length1 + 32;
        byte[] inputData = new byte[length];
        for (int i = 0; i < length; i++) {
            inputData[i] = bytes[i];
        }

        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.INIT_SET_DATA)
                .setIns(CmdIns.INIT_SET_DATA)
                .setPram1(pram1)
                .setPram2(pram2)
                .setInputData(inputData)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void initVmkChlng(CmdResultCallback cmdResultCallback) {
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.INIT_VMK_CHLNG)
                .setIns(CmdIns.INIT_VMK_CHLNG)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void initBackInit(byte hostId, byte[] vmkChallenge, CmdResultCallback cmdResultCallback) {
        byte[] vmkResponse = AES.getAESEncrypt(vmkChallenge, TEST_VMK);
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.INIT_BACK_INIT)
                .setIns(CmdIns.INIT_BACK_INIT)
                .setPram1(hostId)
                .setInputData(vmkResponse)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void genResetOTP(CmdResultCallback cmdResultCallback) {
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.GEN_RESET_OTP)
                .setIns(CmdIns.GEN_RESET_OTP)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }


    public void verivyResetOTP(String optCode, CmdResultCallback cmdResultCallback) {

        byte[] inputData = optCode.getBytes();

        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.VERIFY_RESET_OTP)
                .setIns(CmdIns.VERIFY_RESET_OTP)
                .setInputData(inputData)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void bindBackNoHost(String mode, byte[] pinChllenge, String oldPin, String newPin,
                               CmdResultCallback cmdResultCallback) {
        byte[] pinResp = new byte[16];
        byte[] newPinHash = new byte[32];

        if (mode.equals("PERSO")) {
            newPinHash = encryptSHA256(newPin.getBytes(Charset.forName(CHARSETNAME)));
            newPinHash = AES.getAESEncrypt(pinChllenge, newPinHash);
        } else if (mode.equals("DISCONN")) {
            byte[] devkey = encryptSHA256(oldPin.getBytes(Charset.forName(CHARSETNAME)));
            pinResp = AES.getAESEncrypt(pinChllenge, devkey);
        }

        int length1 = pinResp.length;
        int length2 = newPinHash.length;
        int length = length1 + length2;
        byte[] inputData = new byte[length];

        for (int i = 0; i < length1; i++) {
            inputData[i] = pinResp[i];
        }

        for (int i = 0; i < length2; i++) {
            inputData[0 + length1] = newPinHash[i];
        }

        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.BIND_BACK_NO_HOST)
                .setIns(CmdIns.BIND_BACK_NO_HOST)
                .setInputData(inputData)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void getUniqueId(CmdResultCallback cmdResultCallback) {
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.GET_UID)
                .setIns(CmdIns.GET_UID)
                .setInputData(null)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void getFwVersion(CmdResultCallback cmdResultCallback) {
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.GET_FW_VERSION)
                .setIns(CmdIns.GET_FW_VERSION)
                .setInputData(null)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void bindLogin(String uuid, String optCode, byte[] loginChallenge, byte hostID,
                          CmdResultCallback cmdResultCallback) {

        StringBuilder sb = new StringBuilder();
        sb.append(uuid);//32
        sb.append(optCode);//6
        String info = sb.toString();//38

        byte[] devKey = encryptSHA256(info.getBytes(Charset.forName(CHARSETNAME)));//32
//        LogUtil.i("cmd devKey=" + LogUtil.byte2HexString(devKey));
        byte[] regresp = AES.getAESEncrypt(loginChallenge, devKey);//16
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.BIND_LOGIN)
                .setIns(CmdIns.BIND_LOGIN)
                .setPram1(hostID)
                .setInputData(regresp)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void bindLoginChlng(byte hostID, CmdResultCallback cmdResultCallback) {
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.BIND_LOGIN_CHLNG)//BIND_LOGIN_CHLNG = ClaType.KEEP_MEMORY;  //0x81
                .setIns(CmdIns.BIND_LOGIN_CHLNG)//BIND_LOGIN_CHLNG = 0xD6;
                .setPram1(hostID)
                .setInputData(null)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }


    public void bindRegFinish(byte[] handle, String uuid, String optCode, byte[] challenge,
                              CmdResultCallback cmdResultCallback) {

        Log.e(TAG, "handle=" + Arrays.toString(handle));
        Log.e(TAG, "uuid=" + uuid + " " + "optCode=" + optCode);
        Log.e(TAG, "特征值是" + Arrays.toString(challenge));
        Log.e(TAG, "特征值的长度是" + challenge.length);
//        Log.e(TAG, "pin码是" + Arrays.toString(pinresp));
        byte[] pinresp = new byte[16];


        StringBuilder sb = new StringBuilder();
        sb.append(uuid);//32
        sb.append(optCode);//6
        String info = sb.toString();
        byte[] devKey = encryptSHA256(info.getBytes(Charset.forName(CHARSETNAME)));
        Log.e(TAG, "devKey=" + Arrays.toString(devKey));

        byte[] regresp = AES.getAESEncrypt(challenge, devKey);
        Log.e(TAG, "regresp=" + Arrays.toString(regresp) + "/n" + "regresp长度=" + regresp.length);

        int length1 = handle.length;
        int length2 = regresp.length;
        int length3 = 16;//
        int size = length1 + length2 + length3;
        byte[] inputData = new byte[size];
        for (int i = 0; i < length1; i++) {
            inputData[i] = handle[i];
        }

        for (int i = 0; i < length2; i++) {
            inputData[i + length1] = regresp[i];
        }

        for (int i = 0; i < length3; i++) {
            inputData[i + length1 + length2] = pinresp[i];
        }

        Log.e(TAG, "inputData length=" + inputData.length + ",data=" + Arrays.toString(inputData));

        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.BIND_REG_FINISH)
                .setIns(CmdIns.BIND_REG_FINISH)
                .setInputData(inputData)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    /**
     * 獲取pin码
     */
    public void pinChlng(CmdResultCallback cmdResultCallback) {
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.PIN_CHLNG)
                .setIns(CmdIns.PIN_CHLNG)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void turnCurrency(boolean isTurn, CmdResultCallback cmdResultCallback) {

        byte para1;
        if (isTurn) {
            para1 = 0x01;
        } else {
            para1 = 0x00;
        }
        LogUtil.i("turnCurrency:" + para1);
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.TURN_CURRENCY)
                .setIns(CmdIns.TURN_CURRENCY)
                .setPram1(para1)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }


    public void setCardName(String cardName, CmdResultCallback cmdResultCallback) {
        byte[] nameOutput = new byte[32];
        byte[] cardNameBytes = cardName.getBytes(Charset.forName(CHARSETNAME));
        int length = cardNameBytes.length;
        if (length <= 32) {
            for (int i = 0; i < length; i++) {
                nameOutput[i] = cardNameBytes[i];
            }
        }

        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.SET_CARD_NAME)
                .setIns(CmdIns.SET_CARD_NAME)
                .setInputData(nameOutput)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }


    public void bindRegChlng(byte[] handle, CmdResultCallback cmdResultCallback) {

        byte[] inputData = handle;

        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.BIND_REG_CHLNG)
                .setIns(CmdIns.BIND_REG_CHLNG)
                .setInputData(inputData)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    /**
     * 拿到uuid和描述，通过sha256计算hash值，把他们合并到一个byte[]中返回
     *
     * @param uuid
     * @param description
     * @return
     */
    public void bindRegInit(String uuid, String description, CmdResultCallback cmdResultCallback) {
        byte[] uuidByte = transformBytes(uuid.getBytes(Charset.forName(CHARSETNAME)), 32);
        byte[] descByte = transformBytes(description.getBytes(Charset.forName(CHARSETNAME)), 64);

        byte[] uuidBytes = uuid.getBytes(Charset.forName(CHARSETNAME));
        byte[] descriptionBytes = description.getBytes(Charset.forName(CHARSETNAME));

        byte[] info = new byte[96];

        int length = info.length;
        for (int i = 0; i < length; i++) {
            info[i] = 0x00;
        }

        int count = 0;
        for (byte b : uuidBytes) {
            info[count] = b;
            count++;
        }

        for (byte b : descriptionBytes) {
            info[count] = b;
            count++;
        }

        byte[] hashByte = encryptSHA256(info);

        byte[] inputData = mergeBytes(uuidByte, descByte, hashByte);

        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.BIND_REG_INIT)
                .setIns(CmdIns.BIND_REG_INIT)
                .setPram1(0x00)//首次
                .setInputData(inputData)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    //status 1B (00 idle, 01 preparing, 02 begined, 03 opt veriried, 04 in process
    public void trxStatus(CmdResultCallback cmdResultCallback){
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.TRX_STATUS)
                .setIns(CmdIns.TRX_STATUS)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void cwCmdHdwPrepTrxSign(byte[] macKey, int inputId, int keyChainId,
                                    int accountId, int keyId,
                                    long amount, byte[] signatureMaterial,
                                    CmdResultCallback cmdResultCallback) {

        LogUtil.i("macKey=" + LogUtil.byte2HexString(macKey));
        LogUtil.i("inputId =" + inputId);
        LogUtil.i("keyChainId =" + keyChainId);
        LogUtil.i("accountId=" + accountId);
        LogUtil.i("keyId =" + keyId);
        LogUtil.i("amount =" + amount);
        LogUtil.i("signatureMaterial=" + LogUtil.byte2HexString(signatureMaterial));

        //input
        //P1: inputId 1B
        //P2: keyChainId 1B
        //accountId 4B
        //keyId 4B
        //amount 8B
        //signatureMaterial 32B
        //mac 32B (of accountId + keyId + amount + signatureMaterial

        //output
        //none
        int length = signatureMaterial.length;
        byte[] accBytes = ByteUtil.intToByteLittle(accountId, 4);//4 bytes, little-endian
        byte[] keyBytes = ByteUtil.intToByteLittle(keyId, 4);//4 bytes, little-endian
        byte[] amount_bn = ByteUtil.intToByteBig((int) amount, 8);
        LogUtil.i("amount=" + LogUtil.byte2HexString(amount_bn));
        //:  formate=00 00 00 00 00 00 27 10 byte,little =10 27 00 00 00 00 00 00 orige=00 00 27 10 00 00 00 00
        byte[] inputData = new byte[8 + 8 + length];
        for (int i = 0; i < 4; i++) {
            inputData[i] = accBytes[i];
        }

        for (int i = 0; i < 4; i++) {
            inputData[i + 4] = keyBytes[i];
        }

        for (int i = 0; i < 8; i++) {
            inputData[i + 4 + 4] = amount_bn[i];
        }

        for (int i = 0; i < length; i++) {
            inputData[i + 4 + 4 + 8] = signatureMaterial[i];
        }

        //inputData 48b
        byte[] mac = HMAC.getSignature(inputData, macKey);

        int inputDataLen = inputData.length;
        int macLen = mac.length;
        byte[] newInputData = new byte[inputDataLen + macLen];
        for (int i = 0; i < inputDataLen; i++) {
            newInputData[i] = inputData[i];
        }

        for (int i = 0; i < macLen; i++) {
            newInputData[i + inputDataLen] = mac[i];
        }
        LogUtil.i("PREP InputData 長度=" + newInputData.length);

        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.HDW_PREP_TRX_SIGN)
                .setIns(CmdIns.HDW_PREP_TRX_SIGN)
                .setPram1(inputId)
                .setPram2(keyChainId)
                .setInputData(newInputData)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void cwCmdTrxBegin(byte[] encKey, long amount, String recvAddr, CmdResultCallback cmdResultCallback) {

        byte[] recvAddrBytes = recvAddr.getBytes(Charset.forName(CHARSETNAME));

        byte[] addr = new byte[48];
        int length = recvAddrBytes.length;
        if (length <= 48) {
            for (int i = 0; i < length; i++) {
                addr[i] = recvAddrBytes[i];
            }
        }

        addr = AES.getAESEncrypt(addr, encKey);

        byte[] amountBn = ByteUtil.intToByteBig((int) amount, 8);//转成8字节大端模式
        LogUtil.i("txsBegin amount="+amount+" ;轉int"+(int) amount+" ;hex=" + LogUtil.byte2HexString(amountBn) +
                " ; addr="+recvAddr+" ;hex="+LogUtil.byte2HexString(recvAddrBytes));

        int addLen = addr.length;
        int len = 8 + addLen;
        byte[] inputData = new byte[len];
        for (int i = 0; i < 8; i++) {
            inputData[i] = amountBn[i];
        }

        for (int i = 0; i < addLen; i++) {
            inputData[i + 8] = addr[i];
        }

        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.TRX_BEGIN)
                .setIns(CmdIns.TRX_BEGIN)
                .setInputData(inputData)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdPacket.setTrxBtnFlag(true);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void trxSign(int inputId, CmdResultCallback cmdResultCallback) {
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.TRX_SIGN)
                .setIns(CmdIns.TRX_SIGN)
                .setPram1(inputId)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void hdwQueryAccountKeyInfo(int keyInfoId, int keyChainId, int accountId,
                                       int keyId, CmdResultCallback cmdResultCallback) {
        //input
        //keyInfoId 1B (00 address25B, 01 publickey 64B)
        //keyChainId 1B
        //accountId 4B
        //keyId 4B

        //output
        //keyInfo
        //  address 25B
        //  publicKey 64B
        //mac 32B (of KeyInfo)

        LogUtil.i("keyInfoId="+String.valueOf(keyInfoId)+" ;keyChainId"+String.valueOf(keyChainId)+
                " ;accnt="+String.valueOf(accountId)+" ;keyid="+String.valueOf(keyId));

        byte[] accBytes = ByteUtil.intToByteLittle(accountId, 4);//4 bytes, little-endian
        byte[] keyBytes = ByteUtil.intToByteLittle(keyId, 4);//4 bytes, little-endian
        byte[] inputData = new byte[8];
        for (int i = 0; i < 4; i++) {
            inputData[i] = accBytes[i];
        }

        for (int i = 0; i < 4; i++) {
            inputData[i + 4] = keyBytes[i];
        }

        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.HDW_QUERY_ACCOUNT_KEY_INFO)
                .setIns(CmdIns.HDW_QUERY_ACCOUNT_KEY_INFO)
                .setPram1(keyInfoId)
                .setPram2(keyChainId)
                .setInputData(inputData)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void hdwGetNextAddress(int keyChainId, int accountId, CmdResultCallback cmdResultCallback) {
        byte[] accBytes = ByteUtil.intToByteLittle(accountId, 4);//4 bytes, little-endian

        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.HDW_GET_NEXT_ADDRESS)
                .setIns(CmdIns.HDW_GET_NEXT_ADDRESS)
                .setPram1(keyChainId)
                .setInputData(accBytes)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void bindRegInit(String uuid, String description, int first, CmdResultCallback cmdResultCallback) {
        LogUtil.i("RegInit:" + first);
//        first=0x00;
        byte[] uuidByte = transformBytes(uuid.getBytes(Charset.forName(CHARSETNAME)), 32);
        byte[] descByte = transformBytes(description.getBytes(Charset.forName(CHARSETNAME)), 64);

        byte[] uuidBytes = uuid.getBytes(Charset.forName(CHARSETNAME));
        byte[] descriptionBytes = description.getBytes(Charset.forName(CHARSETNAME));

        byte[] info = new byte[96];

        int length = info.length;
        for (int i = 0; i < length; i++) {
            info[i] = 0x00;
        }

        int count = 0;
        for (byte b : uuidBytes) {
            info[count] = b;
            count++;
        }

        for (byte b : descriptionBytes) {
            info[count] = b;
            count++;
        }

        byte[] hashByte = encryptSHA256(info);

        byte[] inputData = mergeBytes(uuidByte, descByte, hashByte);


        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.BIND_REG_INIT)
                .setIns(CmdIns.BIND_REG_INIT)
                .setPram1(first)//0x01首次 其余0x00
                .setInputData(inputData)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }

    public void bindLogout(CmdResultCallback cmdResultCallback) {
        CmdPacket cmdPacket = new CmdPacket.Builder()
                .setCla(CmdCla.BIND_LOGOUT)
                .setIns(CmdIns.BIND_LOGOUT)
                .build();
        cmdPacket.setCmdResultListener(cmdResultCallback);
        cmdProcessor.addCmd(cmdPacket);
    }


    private byte[] transformBytes(byte[] bytes, int length) {
        if (length == 0) return null;
        if (bytes == null) return null;

        byte[] newBytes = new byte[length];

        int count = 0;
        for (byte b : bytes) {
            newBytes[count] = b;
            count++;
        }

        return newBytes;
    }

    private byte[] mergeBytes(byte[] b1, byte[] b2, byte[] b3) {
        byte[] bytes = new byte[128];

        int length1 = b1.length;
        for (int i = 0; i < length1; i++) {
            bytes[i] = b1[i];
        }

        int length2 = b2.length;
        for (int i = 0; i < length2; i++) {
            bytes[i + length1] = b2[i];
        }

        int length3 = b3.length;
        for (int i = 0; i < length3; i++) {
            bytes[i + length1 + length2] = b3[i];
        }

        return bytes;
    }

    private byte[] encryptSHA256(byte[] bytes) {
        byte[] digestByte;

        digestByte = encrypt(bytes, "SHA-256");
        Log.e(TAG, "digestByte=" + printBytes(digestByte));
        return digestByte;
    }

    private byte[] encrypt(byte[] bytes, String type) {
        MessageDigest digest;
        byte[] b = null;
        try {
            digest = MessageDigest.getInstance(type);
            b = digest.digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return b;
    }

    private String printBytes(byte[] bytes) {
        if (bytes == null) return null;
        StringBuilder sb = new StringBuilder();
        sb.append("byte[]=" + Arrays.toString(bytes));
        sb.append("/n");
        sb.append("byte[]_hash=" + byte2HexString(bytes));
        return sb.toString();
    }

    /**
     * byte数组转哈希字符串
     *
     * @param bytes
     * @return
     */
    private String byte2HexString(byte[] bytes) {
        if (bytes == null) return null;

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x ", b));
        }

        return sb.toString();
    }

}
