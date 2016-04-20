package com.snscity.egdwlib.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by MyPC on 2015/9/9.
 */
public class HMAC {

    private static final String HMAC_SHA256 = "HmacSHA256";

    public static byte [] getSignature(byte [] data, byte [] key){
//        LogUtil.i("data="+LogUtil.byte2HexString(data)+";key="+LogUtil.byte2HexString(key));
        Mac mac = null;
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key, HMAC_SHA256);
            mac = Mac.getInstance(HMAC_SHA256);
            mac.init(signingKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
//        LogUtil.i("mac="+LogUtil.byte2HexString(mac.doFinal(data)));
        return mac.doFinal(data);
    }
}
