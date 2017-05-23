package com.coolbitx.coolwallet;

import android.app.Application;
import android.provider.Settings;

import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.util.ByteUtils;
import com.coolbitx.coolwallet.util.ExtendedKey;
import com.snscity.egdwlib.utils.LogUtil;

/**
 * Created by Dorac on 2017/5/8.
 */

public class RecoveryAddrByPubKeyTest extends ApplicationTest {

//    ExtendedKey km;


    public void test() {

//        LogUtil.e("scriptToAddr="+getAddressFromScript("76a91473eed41ea058b31e556fa7f3d56e763612f4c48b88ac"));
//        System.out.print(";scriptToAddr="+getAddressFromScript("76a914f4f440d102e02adb9768f96e68095fc4dd28e44588ac"));
        getAddressFromScript("76a914f4f440d102e02adb9768f96e68095fc4dd28e44588ac");
    }

    private String getAddressFromScript(String script) {
        byte[] bareAddress = new byte[20];
        //(來源陣列，起始索引值，目的陣列，起始索引值，複製長度)
//        System.arraycopy( PublicPun.hexStringToByteArray("76a914934abe98a533cab0946a85d3bad409778a077c7088ac"),3,bareAddress, 0, bareAddress.length);
        System.arraycopy(PublicPun.hexStringToByteArray(script), 3, bareAddress, 0, bareAddress.length);
        LogUtil.e("bare2=" + PublicPun.byte2HexStringNoBlank(bareAddress));
        byte[] baddr = new byte[bareAddress.length + 1];
        baddr[0] = 0;
        System.arraycopy(bareAddress, 0, baddr, 1, bareAddress.length);
        LogUtil.e("bare3=" + PublicPun.byte2HexStringNoBlank(baddr));
        return ByteUtils.toBase58WithChecksum(baddr);
    }

}
