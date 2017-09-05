package com.coolbitx.coolwallet;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.coolbitx.coolwallet.Service.BTConfig;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.util.BTCUtils;
import com.coolbitx.coolwallet.util.ByteUtils;
import com.snscity.egdwlib.utils.LogUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import dalvik.annotation.TestTarget;

/**
 * Created by Dorac on 2017/5/22.
 */

public class ExampleTest extends InstrumentationTestCase {


    private void testNotification() {
//        LogUtil.e("testNotification");
//        String ExchangeMessage = "Congrats!! Your sell order has a match. Please connect with CoolWallet CW001306 to complete the trade.";
//        final Intent intent = new Intent(BTConfig.XCHS_NOTIFICATION);
//        intent.putExtra("ExchangeMessage", ExchangeMessage);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    public void checkMac(){


//      2005335801000000000000000000271076e577cc803084e5a1b4b3c30e1f5fa357a31220a88332fc9f15a06935486d22a527b20a39b965be130b9da7d25cbb4a

        String mac = skmac("20053358010000000000000000002710");
        LogUtil.e("mac="+mac);

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


//        BTCUtils.reverse(doubleSha256TxHash);

        LogUtil.e("txid="+PublicPun.byte2HexStringNoBlank(txid));
        LogUtil.e("txid2="+PublicPun.byte2HexStringNoBlank(txid2));
        LogUtil.e("txid3="+PublicPun.byte2HexStringNoBlank(txid3));
        LogUtil.e("txid4="+PublicPun.byte2HexStringNoBlank(BTCUtils.reverse(doubleSha256TxHash)));

    }

}
