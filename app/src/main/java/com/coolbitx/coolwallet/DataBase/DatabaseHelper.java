package com.coolbitx.coolwallet.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.bean.CWAccountKeyInfo;
import com.coolbitx.coolwallet.bean.CwBtcTxs;
import com.coolbitx.coolwallet.bean.dbAddress;
import com.coolbitx.coolwallet.general.PublicPun;
import com.crashlytics.android.Crashlytics;
import com.snscity.egdwlib.utils.LogUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ShihYi on 2015/10/12.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "com.coolbitx.coolwallet.db";
    private static final int DATABASE_VERSION = 5; //version must be >=1
    private static String sql = "";
    private static String wid;
    private static boolean isUpgrade = false;
    private static DatabaseHelper mInstance;
    SQLiteDatabase db;
    private Context mContext;

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        wid = PublicPun.card.getCardId();//存的是hex
        this.mContext = context;
    }

//    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
//        super(context, name, factory, version, errorHandler);
//    }

    public DatabaseHelper(Context context) {
        this(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public synchronized static DatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DatabaseHelper(context);
        }
        return mInstance;
    }

    public static boolean insertAccountKeyInfo(Context context, int account,  int kcid, String pubKey,String chainCode) {
        DatabaseHelper mOpenHelper = new DatabaseHelper(context);
//        String nullColumnHack = "WID";
        boolean mResult = false;
        long resultID;
        long updateResultID;
        // 取出資料庫物件, 並且是可以寫入狀態
        // 當APP空間不夠時, 該方法會呈唯讀狀態

        SQLiteDatabase mDatabase = mOpenHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("WID", wid);
            values.put("ACCOUNT_ID", account);
            values.put("KCID", kcid);
            values.put("PUBLICKEY", pubKey);
            values.put("CHAINCODE", chainCode);

            resultID = mDatabase.insert(DbName.DB_TABLE_KEYINFO,null, values);

            if (resultID == -1) {
//                mResult = false;
                updateResultID = mDatabase.update(DbName.DB_TABLE_KEYINFO, values, "WID='" + wid + "' AND " + " ACCOUNT_ID=" + account , null);
                LogUtil.d("update table KEYINFO: " + updateResultID + " ;ACCOUNT_ID:" + account + " ,KCID:" +
                        kcid+ " ,PUBLICKEY:" + pubKey+ " ,CHAINCODE:" + chainCode);
                mResult = updateResultID != -1;
            } else {
                LogUtil.d("insert table KEYINFO: " + resultID + "; ACCOUNT_ID:" + account + " ,KCID:" +
                        kcid+ " ,PUBLICKEY:" + pubKey+ " ,CHAINCODE:" + chainCode);
                mResult = true;
            }

            return mResult;
        } catch (Exception e) {
            LogUtil.d("sql insert KEYINFO Error: " + e.toString());
            return mResult;
        } finally {
            mDatabase.close();

        }
    }


    /**
     *
     * @param context
     * @param accountIndex
     * @return ArrayList<CWAccountKeyInfo>
     */

    public static ArrayList<CWAccountKeyInfo> queryAccountKeyInfo(Context context, int accountIndex) {
        ArrayList<CWAccountKeyInfo> arraylist = new ArrayList<CWAccountKeyInfo>();
        int mCount = 0;
        DatabaseHelper mOpenHelper = new DatabaseHelper(context);
        // 取得唯讀模式資料庫
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        // 透過query來查詢資料
        Cursor c = null;

        try {
            if (accountIndex == -1) {//query all data
                c = db.query(DbName.DB_TABLE_KEYINFO,                       // 資料表名字
                        new String[]{"ACCOUNT_ID","KCID", "PUBLICKEY", "CHAINCODE"},     // 要取出的欄位資料
                        "WID = '" + wid + "'",                              // 查詢條件式
                        null,                                              // 查詢條件值字串陣列
                        null,                                              // Group By字串語法
                        null,                                              // Having字串法
                        "ACCOUNT_ID",                                 // Order By字串語法(排序)
                        null);                                             // Limit字串語法
            } else {
                c = db.query(DbName.DB_TABLE_KEYINFO,                                 // 資料表名字
                        new String[]{"ACCOUNT_ID","KCID", "PUBLICKEY", "CHAINCODE"},  // 要取出的欄位資料
                        "WID = '" + wid + "' AND ACCOUNT_ID=" + accountIndex,                                              // 查詢條件式
                        null,                                              // 查詢條件值字串陣列
                        null,                                              // Group By字串語法
                        null,                                              // Having字串法
                        "KCID",                                              // Order By字串語法(排序)
                        null);                                             // Limit字串語法
            }


            while (c.moveToNext()) {
                CWAccountKeyInfo cw = new CWAccountKeyInfo();
                cw.setAccId(c.getInt(0));
                cw.setKcid(c.getInt(1));
                cw.setPublicKey(c.getString(2));
                cw.setChainCode(c.getString(3));
                arraylist.add(cw);
                LogUtil.i("query " + DbName.DB_TABLE_KEYINFO + " record " + mCount + "=" +
                        c.getInt(0) + " ;" + c.getInt(1) + " ;" + c.getString(2)+ " ;" + c.getString(3));
                mCount++;
            }

        } catch (Exception e) {

        } finally {
            // 釋放資源
            c.close();
            db.close();
            return arraylist;
        }
    }


    public static boolean insertCurrent(Context context, String country, double rates) {
        DatabaseHelper mOpenHelper = new DatabaseHelper(context);
        String nullColumnHack = "id";
        boolean mResult = false;
        long resultID;
        long updateResultID;
        // 取出資料庫物件, 並且是可以寫入狀態
        // 當APP空間不夠時, 該方法會呈唯讀狀態
        SQLiteDatabase mDatabase = mOpenHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("WID", wid);
            values.put("COUNTRY", country);
            values.put("RATES", rates);
            resultID = mDatabase.insert(DbName.DB_TABLE_CURRENT, nullColumnHack, values);
            if (resultID == -1) {
//                mResult = false;
                updateResultID = mDatabase.update(DbName.DB_TABLE_CURRENT, values, "WID='" + wid + "' AND " + " COUNTRY='" + country + "'", null);
                LogUtil.d("update CURRENT: " + updateResultID + " ,COUNTRY:" + country + " ,RATES:" + rates);
                mResult = updateResultID != -1;
            } else {
                LogUtil.d("insert CURRENT: " + resultID + " ,COUNTRY:" + country + " ,RATES:" + rates);
                mResult = true;
            }

            return mResult;
        } catch (Exception e) {
            LogUtil.i("sql insert Error: " + e.toString());
            return mResult;
        } finally {
            mDatabase.close();

        }
    }

    public static ArrayList<String> queryLogin(Context context) {
        ArrayList<String> arraylist = new ArrayList<String>();
        boolean result = false;
        int mCount = 0;
        DatabaseHelper mOpenHelper = new DatabaseHelper(context);
        // 取得唯讀模式資料庫
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        // 透過query來查詢資料
        Cursor c = null;

        try {
            c = db.query(DbName.DB_TABLE_LOGIN,                                 // 資料表名字
                    new String[]{"WID", "UUID", "OTP"},  // 要取出的欄位資料
                    "WID = '" + wid + "'",                                              // 查詢條件式
                    null,                                              // 查詢條件值字串陣列
                    null,                                              // Group By字串語法
                    null,                                              // Having字串法
                    "WID",                                              // Order By字串語法(排序)
                    null);                                             // Limit字串語法

            while (c.moveToNext()) {
                mCount++;
                arraylist.add(c.getString(0));
                arraylist.add(c.getString(1));
                arraylist.add(c.getString(2));
                LogUtil.i("query " + DbName.DB_TABLE_LOGIN + " record " + mCount + "=" + c.getString(0) + " ;" + c.getString(1) + " ;" + c.getString(2));
            }

        } catch (Exception e) {

        } finally {
            // 釋放資源
            c.close();
            db.close();
            return arraylist;
        }
    }

    public static dbAddress querySendAddress(Context context, String addr) {
        dbAddress result = null;
        int mCount = 0;
        DatabaseHelper mOpenHelper = new DatabaseHelper(context);
        // 取得唯讀模式資料庫
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        // 透過query來查詢資料
        LogUtil.d("querySendAddress=" + addr);
        Cursor c = null;
        try {
            c = db.query(DbName.DB_TABLE_ADDR,                                 // 資料表名字
                    new String[]{"KCID,KID,ADDRESS_BALANCE"},  // 要取出的欄位資料
                    "ADDRESS = '" + addr + "'",                                              // 查詢條件式
                    null,                                              // 查詢條件值字串陣列
                    null,                                              // Group By字串語法
                    null,                                              // Having字串法
                    null,                                                 // Order By字串語法(排序)
                    null);                                             // Limit字串語法

            while (c.moveToNext()) {
                mCount++;
                result = new dbAddress();
                result.setKcid(c.getInt(0));
                result.setKid(c.getInt(1));
                result.setBalance(c.getLong(2));
            }
        } catch (Exception e) {
            LogUtil.e("querySendAddress exception=" + e.getMessage());
            Crashlytics.log("querySendAddress exception=" + e.getMessage());
        } finally {
            // 釋放資源
            c.close();
            db.close();
            return result;
        }
    }

    public static boolean insertLogin(Context context, String uuid, String otp) {
        SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS",
                Locale.TAIWAN);
        String saveTempDtString = sdfDateTime.format(new Date());

        DatabaseHelper mOpenHelper = new DatabaseHelper(context);
        String nullColumnHack = "id";
        boolean mResult = false;
        long resultID;
        long updateResultID;
        // 取出資料庫物件, 並且是可以寫入狀態
        // 當APP空間不夠時, 該方法會呈唯讀狀態
        SQLiteDatabase mDatabase = mOpenHelper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("DATE", saveTempDtString);
            values.put("WID", wid);
            values.put("UUID", uuid);
            values.put("OTP", otp);

            resultID = mDatabase.insert(DbName.DB_TABLE_LOGIN, nullColumnHack, values);
            LogUtil.i("sql insert " + DbName.DB_TABLE_LOGIN + " :" + resultID + " ,WID:" + wid + " ,DATE:" + saveTempDtString + " ,UUID:" +
                    uuid + " ,OTP:" + otp);
            if (resultID == -1) {
                ContentValues values1 = new ContentValues();
                values1.put("DATE", saveTempDtString);
                values1.put("UUID", uuid);
                values1.put("OTP", otp);

                updateResultID = mDatabase.update(DbName.DB_TABLE_LOGIN, values1, "WID='" + wid + "'", null);
                LogUtil.i("sql update " + DbName.DB_TABLE_LOGIN + " :" + resultID + " ,WID:" + wid + " ,DATE:" + saveTempDtString + " ,UUID:" +
                        uuid + " ,OTP:" + otp);
                mResult = updateResultID != -1;
            } else {
                mResult = true;
            }

        } catch (Exception e) {
            LogUtil.i("sql insert " + DbName.DB_TABLE_LOGIN + " error: " + e.getMessage());
            mResult = false;
        } finally {
            mDatabase.close();
        }
        return mResult;
    }

    /***
     * update all addresses balance & n_tx
     */
//      DatabaseHelper.updateAddress(mContext, accountID, mAddr,address_N_tx, mBalance);
    public static boolean updateAddress(Context context, int accountID, String addr, int address_N_tx, long mBalance) {
        DatabaseHelper mOpenHelper = new DatabaseHelper(context);
        String nullColumnHack = "id";
        boolean mResult = false;
        long updateResultID;
        // 取出資料庫物件, 並且是可以寫入狀態
        // 當APP空間不夠時, 該方法會呈唯讀狀態
        SQLiteDatabase mDatabase = mOpenHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("N_TX", address_N_tx);
            values.put("ADDRESS_BALANCE", mBalance);

            updateResultID = mDatabase.update(DbName.DB_TABLE_ADDR, values, "WID='" + wid + "' AND ACCOUNT_ID=" + accountID + " AND ADDRESS='" + addr + "'", null);
            LogUtil.i("sql update " + DbName.DB_TABLE_ADDR + " :" + updateResultID + " ,WID=" + wid +
                    "' AND ACCOUNT_ID=" + accountID + " ,ADDR=" + addr + " ,n_tx=" + address_N_tx + " ,ADDRESS_BALANCE=" + mBalance);

            mResult = updateResultID != -1;
        } catch (Exception e) {
            mResult = false;
        } finally {
            mDatabase.close();
            return mResult;
        }
    }

    /***
     * update receive addresses label
     */
    public static boolean updateAddress(Context context, String addr, String mLabel) {
        DatabaseHelper mOpenHelper = new DatabaseHelper(context);
        String nullColumnHack = "id";
        boolean mResult = false;
        long updateResultID;
        // 取出資料庫物件, 並且是可以寫入狀態
        // 當APP空間不夠時, 該方法會呈唯讀狀態
        SQLiteDatabase mDatabase = mOpenHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("ADDRESS_LABEL", mLabel);

            updateResultID = mDatabase.update(DbName.DB_TABLE_ADDR, values, "WID='" + wid + "' AND ADDRESS='" + addr + "'", null);
            LogUtil.i("sql update " + DbName.DB_TABLE_ADDR + " :" + updateResultID + " ,WID=" + wid + " ,ADDR=" + addr + " ,label=" + mLabel);

            mResult = updateResultID != -1;
        } catch (Exception e) {
            mResult = false;
        } finally {
            mDatabase.close();
            return mResult;
        }
    }

    /***
     * update dividual address
     */
    public static boolean updateAddress(Context context, int accountID, String addr, int kcid, int kid, int n_tx, long balance) {
        DatabaseHelper mOpenHelper = new DatabaseHelper(context);
        String nullColumnHack = "id";
        boolean mResult = false;
        long updateResultID;
        // 取出資料庫物件, 並且是可以寫入狀態
        // 當APP空間不夠時, 該方法會呈唯讀狀態
        SQLiteDatabase mDatabase = mOpenHelper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("WID", wid);
            values.put("ACCOUNT_ID", accountID);
            values.put("ADDRESS", addr);
            values.put("KCID", kcid);
            values.put("KID", kid);
            values.put("N_TX", n_tx);
            values.put("ADDRESS_BALANCE", balance);

            updateResultID = mDatabase.update(DbName.DB_TABLE_ADDR, values, "WID='" + wid + "' AND ADDRESS='" + addr + "'", null);
            LogUtil.i("sql update " + DbName.DB_TABLE_ADDR + " :" + updateResultID + " ,WID=" + wid + " ,ADDR=" + addr);

            mResult = updateResultID != -1;
        } catch (Exception e) {
            mResult = false;
        } finally {
            mDatabase.close();
            return mResult;
        }
    }

    public static boolean insertAddress(Context context, int accountID, String addr, int kcid, int kid, int n_tx, long balance) {

        DatabaseHelper mOpenHelper = new DatabaseHelper(context);
        String nullColumnHack = "id";
        boolean mResult = false;
        long resultID;
        // 取出資料庫物件, 並且是可以寫入狀態
        // 當APP空間不夠時, 該方法會呈唯讀狀態
        SQLiteDatabase mDatabase = mOpenHelper.getWritableDatabase();
//        mDatabase.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("WID", wid);
            values.put("ACCOUNT_ID", accountID);
            values.put("ADDRESS", addr);
            values.put("KCID", kcid);
            values.put("KID", kid);
            values.put("N_TX", n_tx);
            values.put("ADDRESS_BALANCE", balance);

            resultID = mDatabase.insert(DbName.DB_TABLE_ADDR, nullColumnHack, values);
            mResult = resultID != -1;
            LogUtil.i("sql insert: " + resultID + " ,WID:" + wid + " ,ACCOUNT_ID:" + accountID + " ,ADDRESS:" +
                    addr + " ,KCID:" + kcid + " ,KID:" + kid + " ,N_TX:" + n_tx + " ,ADDRESS_BALANCE:" + balance);

            return mResult;
        } catch (Exception e) {
            LogUtil.i("sql insert Error: " + e.toString());

        } finally {
//            mDatabase.endTransaction();
            mDatabase.close();
            return mResult;
        }
    }

    public static boolean insertTxs(Context context, CwBtcTxs mCwBtcTxs) {

        DatabaseHelper mOpenHelper = new DatabaseHelper(context);
        String nullColumnHack = "id";
        boolean mResult = false;
        long resultID;
        // 取出資料庫物件, 並且是可以寫入狀態
        // 當APP空間不夠時, 該方法會呈唯讀狀態
        SQLiteDatabase mDatabase = mOpenHelper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("WID", wid);
            values.put("ACCOUNT_ID", mCwBtcTxs.getAccount_ID());
            values.put("ADDRESS", mCwBtcTxs.getAddress());
            values.put("TX_ID", mCwBtcTxs.getTxs_TransationID());
            values.put("TX_ADDRESS", mCwBtcTxs.getTxs_Address());
            values.put("TX_RESULT", mCwBtcTxs.getTxs_Result());
            values.put("TX_DATE", mCwBtcTxs.getTxs_Date());
            values.put("TX_CONFIRMATION", mCwBtcTxs.getTxs_Confirmation());

            resultID = mDatabase.insert(DbName.DB_TABLE_TXS, nullColumnHack, values);
            mResult = resultID != -1;
            LogUtil.i("sql insert " + DbName.DB_TABLE_TXS + "第" + resultID + "筆: " + " ,WID:" + mCwBtcTxs.getWID()
                    + " ,ACCOUNT_ID:" + mCwBtcTxs.getAccount_ID()
                    + " ,ADDRESS:" + mCwBtcTxs.getAddress()
                    + " ,TX_ID:" + mCwBtcTxs.getTxs_TransationID()
                    + " ,TX_ADDRESS:" + mCwBtcTxs.getTxs_Address()
                    + " ,TX_RESULT:" + mCwBtcTxs.getTxs_Result()
                    + " ,TX_DATE:" + mCwBtcTxs.getTxs_Date());

            return mResult;
        } catch (Exception e) {
            LogUtil.i("sql insert Error: " + e.toString());

        } finally {
            mDatabase.close();
            return mResult;
        }


    }

    public static ArrayList<CwBtcTxs> queryTxs(Context context, int accountIndex) {
        ArrayList<CwBtcTxs> listResult = new ArrayList<CwBtcTxs>();
        int mCount = 0;

        DatabaseHelper mOpenHelper = new DatabaseHelper(context);
        // 取得唯讀模式資料庫
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        // 透過query來查詢資料
        Cursor c = null;
        try {
            if (accountIndex == -1) {
                c = db.query("TXS",                                 // 資料表名字
                        new String[]{"WID", "ACCOUNT_ID", "ADDRESS", "TX_ID", "TX_ADDRESS", "TX_RESULT", "TX_DATE", "TX_CONFIRMATION"},  // 要取出的欄位資料
                        "WID='" + wid + "'",                              // 查詢條件式
                        null,                                              // 查詢條件值字串陣列
                        null,                                              // Group By字串語法
                        null,                                              // Having字串法
                        "WID,ACCOUNT_ID,Date(TX_DATE)",                        // Order By字串語法(排序)
                        null);                                             // Limit字串語法
            } else {
                c = db.query("TXS",                                 // 資料表名字
                        new String[]{"WID", "ACCOUNT_ID", "ADDRESS", "TX_ID", "TX_ADDRESS", "TX_RESULT", "TX_DATE", "TX_CONFIRMATION"},  // 要取出的欄位資料
                        "WID='" + wid + "' AND ACCOUNT_ID=" + accountIndex,                                              // 查詢條件式
                        null,                                              // 查詢條件值字串陣列
                        null,                                              // Group By字串語法
                        null,                                              // Having字串法
                        "WID,ACCOUNT_ID,Date(TX_DATE)",                        // Order By字串語法(排序)
                        null);                                             // Limit字串語法
            }
            while (c.moveToNext()) {
                mCount++;
                CwBtcTxs d = new CwBtcTxs();
                d.setWID(c.getString(0));
                d.setAccount_ID(c.getInt(1));
                d.setAddress(c.getString(2));
                d.setTxs_TransationID(c.getString(3));
                d.setTxs_Address(c.getString(4));
                d.setTxs_Result(c.getLong(5));
                d.setTxs_Date(c.getString(6));
                d.setTxs_Confirmation(c.getInt(7));

                LogUtil.i("query Txs record-" + mCount
                        + ":WID=" + c.getString(0)
                        + ",AccountID=" + c.getInt(1)
                        + ",TX_ID=" + c.getString(3)
                        + ",TX_ADDRESS=" + c.getString(4)
                        + ",TX_RESULT=" + c.getLong(5)
                        + ",TX_DATE=" + c.getString(6));

                listResult.add(d);
            }
        } finally {
            // 釋放資源
            c.close();
            db.close();
            return listResult;
        }
    }

    public static ArrayList<dbAddress> queryAddress(Context context, int accountIndex, int pointer) {
        //accountIndex 0 代表只抓ext addr
        //accountIndex 1 代表只抓int addr
        ArrayList<dbAddress> listResult = new ArrayList<dbAddress>();
        int mCount = 0;
        DatabaseHelper mOpenHelper = new DatabaseHelper(context);
        // 取得唯讀模式資料庫
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        // 透過query來查詢資料
        Cursor c = null;
        try {
            if (pointer == 0 || pointer == 1) {
//                LogUtil.i("queryAddress: wid=" + wid + " ;" + accountIndex + " AND POINTER=" + pointer);
                c = db.query("ADDR",                                 // 資料表名字
                        new String[]{"WID", "ACCOUNT_ID", "ADDRESS", "KCID", "KID", "N_TX", "ADDRESS_BALANCE", "ADDRESS_LABEL"},  // 要取出的欄位資料
                        "WID='" + wid + "' AND ACCOUNT_ID=" + accountIndex + " and KCID=" + pointer,                                              // 查詢條件式
                        null,                                              // 查詢條件值字串陣列
                        null,                                              // Group By字串語法
                        null,                                              // Having字串法
                        "WID,ACCOUNT_ID,KCID,KID",                        // Order By字串語法(排序)
                        null);                                             // Limit字串語法
            } else {
//                LogUtil.i("queryAddress: wid" + wid + " ;" + accountIndex + " AND POINTER all");
                c = db.query("ADDR",                                 // 資料表名字
                        new String[]{"WID", "ACCOUNT_ID", "ADDRESS", "KCID", "KID", "N_TX", "ADDRESS_BALANCE", "ADDRESS_LABEL"},  // 要取出的欄位資料
                        "WID='" + wid + "' AND ACCOUNT_ID=" + accountIndex,                                                            // 查詢條件式
                        null,                                              // 查詢條件值字串陣列
                        null,                                              // Group By字串語法
                        null,                                              // Having字串法
                        "WID,ACCOUNT_ID,KCID,KID",                        // Order By字串語法(排序)
                        null);                                             // Limit字串語法
            }

            while (c.moveToNext()) {
                mCount++;
                dbAddress d = new dbAddress();
                d.setWid(c.getString(0));
                d.setAccountID(c.getInt(1));
                d.setAddress(c.getString(2));
                d.setKcid(c.getInt(3));
                d.setKid(c.getInt(4));
                d.setN_tx(c.getInt(5));
                d.setBalance(c.getLong(6));
                d.setAddLabel(c.getString(7));
                if(c.getLong(6)>0) {
                    LogUtil.i("query address(balance>0):" + mCount + "=" + c.getString(0) + ",AccountID=" + c.getInt(1) + ",kcID=" + c.getInt(3) + ",KID=" + c.getInt(4)
                            + ",ADDR=" + c.getString(2)
                            + ",N_tx=" + c.getInt(5)
                            + "," + c.getLong(6));
                }
                listResult.add(d);
            }
        } catch (Exception e) {

        } finally {
            // 釋放資源
            c.close();
            db.close();

            return listResult;
        }
    }

    public static ArrayList<String> queryExchangeRate(Context context) {
        ArrayList<String> arraylist = new ArrayList<String>();
        int mCount = 0;
        DatabaseHelper mOpenHelper = new DatabaseHelper(context);
        // 取得唯讀模式資料庫
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        // 透過query來查詢資料
        Cursor c = null;
        try {
            c = db.query("CURRENT",                                 // 資料表名字
                    new String[]{"COUNTRY", "RATES"},  // 要取出的欄位資料
                    "WID='" + wid + "' AND COUNTRY <>'BTC' ",     // 查詢條件式
                    null,                                              // 查詢條件值字串陣列
                    null,                                              // Group By字串語法
                    null,                                              // Having字串法
                    "COUNTRY",                        // Order By字串語法(排序)
                    null);                                             // Limit字串語法

            while (c.moveToNext()) {
                mCount++;
                arraylist.add(c.getString(0));
                LogUtil.i("query record " + mCount + "=" + c.getString(0));
            }
        } catch (Exception e) {

        } finally {
            // 釋放資源
            c.close();
            db.close();

            return arraylist;
        }
    }

    public static int queryAddrKid(Context context, String addr) {

        int result = -1;
        DatabaseHelper mOpenHelper = new DatabaseHelper(context);
        // 取得唯讀模式資料庫
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        // 透過query來查詢資料
        Cursor c = null;
        try {
            c = db.query("ADDR",                                 // 資料表名字
                    new String[]{"KID"},  // 要取出的欄位資料
                    "WID='" + wid + "' AND ADDRESS='" + addr + "'",                                              // 查詢條件式
                    null,                                              // 查詢條件值字串陣列
                    null,                                              // Group By字串語法
                    null,                                              // Having字串法
                    null,                                              // Order By字串語法(排序)
                    null);                                             // Limit字串語法

            while (c.moveToNext()) {

                result = c.getInt(0);
            }
            LogUtil.d(addr + " queryAddrKid " + result);
            // 釋放資源
        } catch (Exception e) {

        } finally {
            c.close();
            db.close();

            return result;
        }
    }

    public static int queryAccountByAddress(Context context, String addr) {

        int resultAccount = -1;
        DatabaseHelper mOpenHelper = new DatabaseHelper(context);
        // 取得唯讀模式資料庫
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        // 透過query來查詢資料
        Cursor c = null;
        try {
            c = db.query("ADDR",                                 // 資料表名字
                    new String[]{"ACCOUNT_ID"},  // 要取出的欄位資料
                    "WID='" + wid + "' AND ADDRESS='" + addr + "'",                                              // 查詢條件式
                    null,                                              // 查詢條件值字串陣列
                    null,                                              // Group By字串語法
                    null,                                              // Having字串法
                    null,                                              // Order By字串語法(排序)
                    null);                                             // Limit字串語法

            while (c.moveToNext()) {

                resultAccount = c.getInt(0);
            }
            LogUtil.d(addr + " queryAccountByAddress " + resultAccount);
            // 釋放資源
        } catch (Exception e) {

        } finally {
            c.close();
            db.close();

            return resultAccount;
        }
    }

//
//    public static ArrayList<dbAddress> queryUnspend(Context context, int accountIndex, int pointer) {
//        //accountIndex -1 代表只抓ext addr
//        //accountIndex -2 代表只抓int addr
//        ArrayList<dbAddress> listResult = new ArrayList<dbAddress>();
//        int mCount = 0;
//        DatabaseHelper mOpenHelper = new DatabaseHelper(context);
//        // 取得唯讀模式資料庫
//        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
//        // 透過query來查詢資料
//        Cursor c = null;
//        try {
//            if (pointer == 0 || pointer == 1) {
//                LogUtil.i("queryAddress: wid=" + wid + " ;" + accountIndex + " AND POINTER=" + pointer);
//                c = db.query("ADDR",                                 // 資料表名字
//                        new String[]{"WID", "ACCOUNT_ID", "ADDRESS", "KCID", "KID", "N_TX", "ADDRESS_BALANCE", "ADDRESS_LABEL"},  // 要取出的欄位資料
//                        "WID='" + wid + "' AND ACCOUNT_ID=" + accountIndex + " and KCID=" + pointer,                                              // 查詢條件式
//                        null,                                              // 查詢條件值字串陣列
//                        null,                                              // Group By字串語法
//                        null,                                              // Having字串法
//                        "ADDRESS_BALANCE DESC",                        // Order By字串語法(排序)
//                        null);                                             // Limit字串語法
//            } else {
//                LogUtil.i("queryAddress: wid" + wid + " ;" + accountIndex + " AND POINTER all");
//                c = db.query("ADDR",                                 // 資料表名字
//                        new String[]{"WID", "ACCOUNT_ID", "ADDRESS", "KCID", "KID", "N_TX", "ADDRESS_BALANCE", "ADDRESS_LABEL"},  // 要取出的欄位資料
//                        "WID='" + wid + "' AND ACCOUNT_ID=" + accountIndex,                                                            // 查詢條件式
//                        null,                                              // 查詢條件值字串陣列
//                        null,                                              // Group By字串語法
//                        null,                                              // Having字串法
//                        "ADDRESS_BALANCE DESC",                        // Order By字串語法(排序)
//                        null);                                             // Limit字串語法
//            }
//
//            while (c.moveToNext()) {
//                mCount++;
//                dbAddress d = new dbAddress();
//                d.setWid(c.getString(0));
//                d.setAccountID(c.getInt(1));
//                d.setAddress(c.getString(2));
//                d.setKcid(c.getInt(3));
//                d.setKid(c.getInt(4));
//                d.setN_tx(c.getInt(5));
//                d.setBalance(c.getLong(6));
//                d.setAddLabel(c.getString(7));
//                LogUtil.i("query record:" + mCount + "=" + c.getString(0) + ",AccountID=" + c.getInt(1) + ",kcID=" + c.getInt(3) + ",KID=" + c.getInt(4)
//                        + ",ADDR=" + c.getString(2)
//                        + ",N_tx=" + c.getInt(5)
//                        + "," + c.getLong(6));
//
//                listResult.add(d);
//            }
//        } catch (Exception e) {
//
//        } finally {
//            // 釋放資源
//            c.close();
//            db.close();
//
//            return listResult;
//        }
//    }

    public static double queryCurrent(Context context, String country) {
        double rate = 0;
        DatabaseHelper mOpenHelper = new DatabaseHelper(context);
        // 取得唯讀模式資料庫
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        // 透過query來查詢資料
        Cursor c = null;
        LogUtil.d("queryCurrency country=" + country);
        try {
            c = db.query("CURRENT",                                 // 資料表名字
                    new String[]{"RATES"},  // 要取出的欄位資料
                    "WID='" + wid + "' and COUNTRY='" + country + "'",                                              // 查詢條件式
                    null,                                              // 查詢條件值字串陣列
                    null,                                              // Group By字串語法
                    null,                                              // Having字串法
                    "COUNTRY",                                         // Order By字串語法(排序)
                    null);                                             // Limit字串語法

            while (c.moveToNext()) {
                rate = c.getDouble(0);

            }
            LogUtil.d("queryCurrency =" + rate);
            // 釋放資源
        } catch (Exception e) {
            LogUtil.e("queryCurrency failed=" + e.getMessage());
        } finally {
            c.close();
            db.close();
            return rate;
        }
    }

    public static int deleteTableByAccountAndKcid(Context context, String table_name, int account, int kcid) {

        DatabaseHelper mOpenHelper = new DatabaseHelper(context);
        SQLiteDatabase mDatabase = mOpenHelper.getReadableDatabase();

        int result = -99;
        try {
            result = mDatabase.delete(table_name, "WID='" + wid + "' AND ACCOUNT_ID=" + account + " and KCID=" + kcid, null);
            LogUtil.i("delete " + table_name + "=" + result);
        } catch (Exception e) {
            LogUtil.i("delete Error: " + e.toString());
        } finally {
            mDatabase.close();
        }
        return result;
    }

//    public static double queryCurrent(Context context, String country) {
//        double BtcRates = 0;
//        double ChooseRate = 0;
//        double rate = 0;
//        int mCount = 0;
//        DatabaseHelper mOpenHelper = new DatabaseHelper(context);
//        // 取得唯讀模式資料庫
//        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
//        // 透過query來查詢資料
//        Cursor c = null;
//        try {
//            c = db.query("CURRENT",                                 // 資料表名字
//                    new String[]{"COUNTRY", "RATES"},  // 要取出的欄位資料
//                    "WID='" + wid + "'",                                              // 查詢條件式
//                    null,                                              // 查詢條件值字串陣列
//                    null,                                              // Group By字串語法
//                    null,                                              // Having字串法
//                    "COUNTRY",                        // Order By字串語法(排序)
//                    null);                                             // Limit字串語法
//
//            while (c.moveToNext()) {
//                mCount++;
//                if (c.getString(0).equals("BTC")) {
//                    BtcRates = c.getDouble(1);
//                }
//                if (c.getString(0).equals(country)) {
//                    ChooseRate = c.getDouble(1);
//                }
//                rate = ChooseRate / BtcRates;
//
//            }
//            LogUtil.i("query rate " + mCount + " BTC=" + String.valueOf(BtcRates) + ",ChooseRate=" + String.valueOf(ChooseRate) + " ;rate=" + rate);
//            // 釋放資源
//        } catch (Exception e) {
//
//        } finally {
//            c.close();
//            db.close();
//
//            return rate;
//        }
//    }

    public static int deleteTableByAccount(Context context, String table_name, int account) {

        DatabaseHelper mOpenHelper = new DatabaseHelper(context);
        SQLiteDatabase mDatabase = mOpenHelper.getReadableDatabase();
//        mDatabase.beginTransaction();
        int result = -99;
        try {
            result = mDatabase.delete(table_name, "WID='" + wid + "' AND ACCOUNT_ID=" + account, null);
            LogUtil.i("delete " + table_name + "=" + result);
        } catch (Exception e) {
            LogUtil.i("delete Error: " + e.toString());
        } finally {
//            mDatabase.endTransaction();
            mDatabase.close();
        }
        return result;
    }

    public static int deleteTable(Context context, String table_name) {

        DatabaseHelper mOpenHelper = new DatabaseHelper(context);
        SQLiteDatabase mDatabase = mOpenHelper.getReadableDatabase();

        int result = -99;
        try {
            result = mDatabase.delete(table_name, "WID='" + wid + "'", null);
            LogUtil.i("delete " + table_name + " where wid='" + wid + "' ;result=" + result);
        } catch (Exception e) {
            LogUtil.i("delete Error: " + e.toString());
        } finally {
            mDatabase.close();
        }
        return result;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String tableName = "";
        try {
            LogUtil.i("Create Table start");
            String[] table_array = mContext.getResources().getStringArray(R.array.db_table_name);
            for (int i = 0; i < table_array.length; i++) {
                int indentify = mContext.getResources().getIdentifier(table_array[i], "array",
                        mContext.getPackageName());
                String[] col_array = mContext.getResources().getStringArray(indentify);

                tableName = table_array[i];
                //更新時不變動login file
                if (isUpgrade) {
                    if (!tableName.equals(DbName.DB_TABLE_LOGIN)) {
                        sql = "DROP TABLE  IF  EXISTS " + tableName;
                        db.execSQL(sql);
                    }
                } else {
                    sql = "DROP TABLE  IF  EXISTS " + tableName;
                    db.execSQL(sql);
                }
                sql = "CREATE TABLE IF NOT EXISTS " + tableName + "(";
                for (int j = 0; j < col_array.length; j++) {
                    sql = sql + col_array[j] + ",";
                }
                sql = sql.substring(0, sql.length() - 1) + ")";
                db.execSQL(sql);
                LogUtil.i("Table " + tableName + " has been created ok");
            }
        } catch (Exception e) {
            LogUtil.i("Create Table FAILED:" + tableName + " error=" + e.toString());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtil.i("onUpgrade Ver: " + newVersion);
        isUpgrade = true;
        onCreate(db);
    }

    @Override
    public String getDatabaseName() {
        return super.getDatabaseName();
    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
    }

    @Override
    public synchronized void close() {
        super.close();
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }


}
