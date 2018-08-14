package com.coolbitx.coolwallet;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.coolbitx.coolwallet.Service.BTConfig;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.util.BTCUtils;
import com.coolbitx.coolwallet.util.ByteUtils;
import com.snscity.egdwlib.utils.ByteUtil;
import com.snscity.egdwlib.utils.LogUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import dalvik.annotation.TestTarget;

/**
 * Created by Dorac on 2017/5/22.
 */

public class ExampleTest extends InstrumentationTestCase {



//    1Q3cumLg391Nq6ye5AiE2reWcpZghTm5mZ

    public  byte[] encryptSHA256(byte[] bytes) {
        byte[] digestByte;

        digestByte = encrypt(bytes, "SHA-256");

        LogUtil.e("ENC_UBLKTKN="+PublicPun.byte2HexStringNoBlank(digestByte));

        return digestByte;
    }

    public  byte[] encrypt(byte[] bytes, String type) {
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

    public void checkEnc(){
        byte[] unblockTokn=PublicPun.hexStringToByteArray("577a7f80ef34700a0000000000002710");
        encryptSHA256(unblockTokn);
    }

    public void checkMac(){

        //TRXID || OKTKN || UBLKTKN
        //50248716,79671697,05507eecbe8cf7240000000000002710
        //73763383,98e15903,c94950c14b3078d32c2381bec49a7f8b
        byte[] key = PublicPun.hexStringToByteArray("377c08482daaa80a243f542b3044a5c5b5ad27b708a51f55afa78aa5aadf27ba");//+MAC+"4d4143"
        byte[] data =PublicPun.hexStringToByteArray("502487167967169705507eecbe8cf7240000000000002710");


        byte[] mac = hmacsha256(key,data);
        LogUtil.e("mac="+PublicPun.byte2HexStringNoBlank(mac));

    }

    public void decryptMac(){
        byte[] mac = PublicPun.hexStringToByteArray("a52b62d583f911df742512858528d7bbe588a7bbeecc23d33492af12fbc2fe25");


    }

    public void testNotification() {
//        LogUtil.e("testNotification");
//        String ExchangeMessage = "Congrats!! Your sell order has a match. Please connect with CoolWallet CW001306 to complete the trade.";
//        final Intent intent = new Intent(BTConfig.XCHS_NOTIFICATION);
//        intent.putExtra("ExchangeMessage", ExchangeMessage);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    public String skmac(String... data) {

        String sk ="a0f7e76e8fc3510df3ba491e7c422db06ef33387cfefe365438c0d1716f5e572";
        StringBuilder sb = new StringBuilder();
        for (String s : data) {
            sb.append(s);
        }

        return ByteUtils.toHex(hmacsha256(ByteUtils.fromHex(sk), ByteUtils.fromHex(sb.toString())));
    }

    public static byte[] hmacsha256(byte[] key, byte[] data) {
        final String HMAC_SHA256 = "HmacSHA256";
        Mac mac = null;
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key, HMAC_SHA256);
            mac = Mac.getInstance(HMAC_SHA256);

            mac.init(signingKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mac.doFinal(data);
    }


    public void test() throws Exception {

        checkMac();

        byte[] txHash = PublicPun.hexStringToByteArray("0100000001107d8bc70e6f79922e9427f3382d189b1bc4fb3d6f097f8db1a4134f90e28411010000006A473044022032a139c65a5a746a6c01dba7a42b5c55b948d311d4773cdc4f7df0d01bbd78da022002eb605776b2c1682175b8937836432fabf605fc2929db679887a2eb07940fc10121033c38fb17a6351e39275f1867b9fdfb007cccde1bf6afd320d307756827f7a8f5ffffffff0110270000000000001976a9142c70e05d5ec6fcaf2c6e60d0b6474ec53172ae7988ac00000000");
        byte[] doubleSha256TxHash = PublicPun.encryptSHA256(PublicPun.encryptSHA256(txHash));
        LogUtil.e(PublicPun.byte2HexStringNoBlank(doubleSha256TxHash));

        byte[] txid =
                ByteBuffer.wrap(doubleSha256TxHash).order(ByteOrder.LITTLE_ENDIAN).array();

        byte[] txid2=
                ByteBuffer.allocate(32).wrap(doubleSha256TxHash).order(ByteOrder.LITTLE_ENDIAN).array();

        byte[] txid3=
                ByteBuffer.allocate(32).put(doubleSha256TxHash).order(ByteOrder.LITTLE_ENDIAN).array();


        byte[] txid4=
                ByteBuffer.allocate(32).order(ByteOrder.LITTLE_ENDIAN).put(doubleSha256TxHash).array();


        byte[] revTxId = BTCUtils.reverse(PublicPun.hexStringToByteArray("69216b8aaa35b76d6613e5f527f4858640d986e1046238583bdad79b35e938dc"));
        LogUtil.e("revTxId="+PublicPun.byte2HexStringNoBlank(revTxId));
//        BTCUtils.reverse(doubleSha256TxHash);

//        LogUtil.e("txid="+PublicPun.byte2HexStringNoBlank(txid));
//        LogUtil.e("txid2="+PublicPun.byte2HexStringNoBlank(txid2));
//        LogUtil.e("txid3="+PublicPun.byte2HexStringNoBlank(txid3));
//        LogUtil.e("txid4="+PublicPun.byte2HexStringNoBlank(BTCUtils.reverse(doubleSha256TxHash)));

    }

}
