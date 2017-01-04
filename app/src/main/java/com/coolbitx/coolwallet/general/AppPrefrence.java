package com.coolbitx.coolwallet.general;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.snscity.egdwlib.utils.LogUtil;

/**
 * Created by ShihYi  on 15/11/9.
 */
public class AppPrefrence {

    /**
     * Unspent info
     */
    public static void saveUnspent(Context context, String val) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        mSP.edit().putString("Unspent", val).commit();
    }

    public static String getUnspent(Context context) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        return mSP.getString("Unspent", "");
    }

    /**
     * Current Rate
     */
    public static void saveCurrentRate(Context context, float val) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        mSP.edit().putFloat("CurrentRate", val).commit();
    }

    public static float getCurrentRate(Context context) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        return mSP.getFloat("CurrentRate", 0.00f);
    }

    public static void saveCurrentCountry(Context context, String country) {
        LogUtil.i("Preference saveCurrentCountry=" + country);
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        mSP.edit().putString("CurrentCountry", country).commit();
    }

    public static String getCurrentCountry(Context context) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        return mSP.getString("CurrentCountry", "USD");
    }

    /**
     * Transation list
     */
    public static void saveAddressInfo(Context context, String val) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        mSP.edit().putString("AddressInfo", val).commit();
    }

    public static String getAddressInfo(Context context) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        return mSP.getString("AddressInfo", "");
    }


    public static String getCardName(Context context) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        return mSP.getString("CardName", "");
    }

    public static void saveCardName(Context context, String cardName) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        mSP.edit().putString("CardName", cardName).commit();
    }

    public static Boolean getCurrency(Context context) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        return mSP.getBoolean("TurnCurrency", false);
    }

    public static void saveCurrency(Context context, boolean isTurn) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        mSP.edit().putBoolean("TurnCurrency", isTurn).commit();
    }

    /**
     * 2016/12/30 add by Dorac
     * fastestFee: The lowest fee (in satoshis per byte) that will currently result in the fastest transaction confirmations (usually 0 to 1 block delay).
     * halfHourFee: The lowest fee (in satoshis per byte) that will confirm transactions within half an hour (with 90% probability).
     * hourFee: The lowest fee (in satoshis per byte) that will confirm transactions within an hour (with 90% probability).
     *
     * @param context
     * @param halfHourFee 80 is the default
     */
    public static void saveRecommendedHalfHourFees(Context context, int halfHourFee) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        mSP.edit().putInt("HalfHourFees", halfHourFee).commit();
    }

    public static int getRecommendedHalfHourFees(Context context) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        return mSP.getInt("HalfHourFees", 80);
    }

}
