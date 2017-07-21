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


    public static void saveIsBlockrApi(Context context,boolean var){
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        mSP.edit().putBoolean("IsBlockrApi", var).commit();
    }

    public static boolean getIsBlockrApi(Context context) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        return mSP.getBoolean("IsBlockrApi",false);
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
     */

    public static void saveRecommendedFastestFee(Context context, int fastestFee) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        mSP.edit().putInt("FastestFee", fastestFee).commit();
    }

    public static int getRecommendedFastestFee(Context context) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        return mSP.getInt("FastestFee", 90);
    }

    public static void saveRecommendedHalfHourFees(Context context, int halfHourFee) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        mSP.edit().putInt("HalfHourFees", halfHourFee).commit();
    }

    public static int getRecommendedHalfHourFees(Context context) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        return mSP.getInt("HalfHourFees", 80);
    }

    public static void saveRecommendedHourFee(Context context, int hourFee) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        mSP.edit().putInt("HourFee", hourFee).commit();
    }

    public static int getRecommendedHourFee(Context context) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        return mSP.getInt("HourFee", 70);
    }

    public static void saveRecommendedDefaultFee(Context context, long defaultFee) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        mSP.edit().putLong("DefaultFee", defaultFee).commit();
    }

    public static long getRecommendedDefaultFee(Context context) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        return mSP.getLong("DefaultFee", getRecommendedHalfHourFees(context));
    }

    public static void saveAutoFeeCheckBox(Context context, boolean autoFee) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        mSP.edit().putBoolean("AutoFee", autoFee).commit();
    }

    public static boolean getAutoFeeCheckBox(Context context) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        return mSP.getBoolean("AutoFee", true);
    }


    public static void saveManual(Context context, float manualFee) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        mSP.edit().putFloat("ManualFee", manualFee).commit();
    }

    public static float getManualFee(Context context) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        return mSP.getFloat("ManualFee", 0.0002f);
    }

    public static void saveIsResetSuccess(Context context, boolean isSuccess) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        mSP.edit().putBoolean("IsSuccess", isSuccess).commit();
    }

    public static boolean getIsResetSuccess(Context context) {
        SharedPreferences mSP = PreferenceManager.getDefaultSharedPreferences(context);
        return mSP.getBoolean("IsSuccess", true);
    }

}
