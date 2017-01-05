package com.coolbitx.coolwallet.general;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.coolbitx.coolwallet.DataBase.DatabaseHelper;
import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.entity.Account;
import com.coolbitx.coolwallet.entity.Card;
import com.coolbitx.coolwallet.entity.Constant;
import com.coolbitx.coolwallet.entity.CwBtcTxs;
import com.coolbitx.coolwallet.entity.ExchangeRate;
import com.coolbitx.coolwallet.entity.Host;
import com.coolbitx.coolwallet.entity.Info;
import com.coolbitx.coolwallet.entity.ParsingAddress;
import com.coolbitx.coolwallet.entity.PasingWallet;
import com.coolbitx.coolwallet.entity.Txs;
import com.coolbitx.coolwallet.entity.UnSpentTxsBean;
import com.coolbitx.coolwallet.entity.User;
import com.coolbitx.coolwallet.entity.Wallet;
import com.coolbitx.coolwallet.entity.socketByAddress;
import com.coolbitx.coolwallet.ui.BleActivity;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.snscity.egdwlib.utils.LogUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by MyPC on 2015/8/27.
 * 公共方法和公共变量类
 */
public class PublicPun {

    public final static String csvFilename = "Login";
    public final static String oldPin = "12345678";
    public final static String newPin = "12345678";
    /**
     * 卡片的工作模式
     */
    public static String modeState = "";

    /**
     * 卡片的版本号
     */
    public static String fwVersion = "";

    /**
     * 卡片的唯一标识符
     */
    public static String uid = "";

    public static String cardId = "";

    public static Card card = new Card();

    public static String cardName = "";

    public static HdwInfoBean hdwInfoBean = null;

    public static User user = new User();

    public static List<Host> hostList = new ArrayList<>();


    public static boolean[] accountSocketReg = new boolean[5];

    public static boolean[] accountRefresh = new boolean[5];

//    public static Host[] hostList = new Host[3];

    public static Wallet wallet = new Wallet();

    /**
     * 管理fragment栈
     */
//    public static List<Fragment> backStackList = new ArrayList<>();

    /**
     * Encryption key
     */
    public static byte[] BIND_SENCK = new byte[16];

    /** */
    public static byte[] BIND_SMACK = new byte[16];

    public static List<HostBean> hostBeanList = new ArrayList<>();

    public static Account account = new Account();

    //Blockchain data
    static ArrayList<ExchangeRate> lisExchangeRate = new ArrayList<ExchangeRate>();
    public static double SATOSHI_RATE = 0.00000001;


    public static int FindScreenSize(Context mContext) {
        //Find screen size
        WindowManager manager = (WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3 / 4;
        return smallerDimension;
    }


    public static byte[] hexStringToByteArray(String s) {
        byte[] data = null;
        try {
            int len = s.length();
            data = new byte[len / 2];

            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
            }
        } catch (Exception e) {

        }
        return data;
    }

    public static byte[] hexStringToByteArrayRev(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    public static String jSonGen(String address) {
        String mResult = null;
        //準備資料
        String strNetwork = "BTC";
        String type = "address";

        //開始拼接字串
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"network\":\"" + strNetwork + "\",");
        sb.append("\"type\":\"" + type + "\",");
        sb.append("\"address\":\"" + address + "\"");
        sb.append("}");
        mResult = sb.toString();

        return mResult;
    }

    public static ArrayList<UnSpentTxsBean> jsonParserUnspentII(String jsonString) {

        ArrayList<UnSpentTxsBean> lisUnSpentTxs = new ArrayList<UnSpentTxsBean>();
        String UnSpentTxsAddr = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            Gson gson = new Gson();
            JSONArray jsonArrayData = jsonObject.getJSONArray("data");
        } catch (Exception e) {
            LogUtil.e("Unspent JSON Parsing error:" + e.toString());
            e.printStackTrace();
        } finally {
            return lisUnSpentTxs;
        }
    }

    public static ArrayList<UnSpentTxsBean> jsonParserUnspent(String jsonString) {

//      JSONObject  json = RestManager.getJSONfromURL(myuri); // retrieve the entire json stream
        ArrayList<UnSpentTxsBean> lisUnSpentTxs = new ArrayList<UnSpentTxsBean>();
        Gson gson = new Gson();
        String UnSpentTxsAddr;
        Object jsonData;
        JSONArray jsonDataJsonArray;
        JSONObject jsonDataJsonObject;

        try {
            LogUtil.e("jsonString=" + jsonString);
            jsonData = new JSONObject(jsonString).get("data");
            LogUtil.e("JSONObject" + jsonData.toString());

            if (jsonData instanceof JSONArray) {
                // It's an array
                jsonDataJsonArray = (JSONArray) jsonData;

                for (int i = 0; i < jsonDataJsonArray.length(); i++) {
                    JSONObject jsonObjectDate = jsonDataJsonArray.getJSONObject(i);
                    UnSpentTxsAddr = jsonObjectDate.getString("address");
                    LogUtil.e("JSONArray1" + UnSpentTxsAddr.toString());
                    JSONArray jsonArrayUnspentData = jsonObjectDate.getJSONArray("unspent");
                    LogUtil.e("JSONArray2" + jsonArrayUnspentData.toString());
                    for (int j = 0; j < jsonArrayUnspentData.length(); j++) {
                        JSONObject jsonObjectUnspentData = jsonArrayUnspentData.getJSONObject(j);
                        UnSpentTxsBean mUnSpentTxsBean = gson.fromJson(jsonObjectUnspentData.toString(), UnSpentTxsBean.class);
                        mUnSpentTxsBean.setAddress(UnSpentTxsAddr);
                        LogUtil.i("mUnSpentTxsBean第" + String.valueOf(j) + " 筆= " + mUnSpentTxsBean.getAddress() + " ; " + String.valueOf(mUnSpentTxsBean.getAmount()) + ";" + mUnSpentTxsBean.getTx());
                        //skip the data of confirmation=0
                        if (mUnSpentTxsBean.getConfirmations() != 0) {
                            lisUnSpentTxs.add(mUnSpentTxsBean);
                        }
                    }
                }
            } else if (jsonData instanceof JSONObject) {
                // It's an object
                jsonDataJsonObject = (JSONObject) jsonData;
                UnSpentTxsAddr = jsonDataJsonObject.getString("address");
                LogUtil.e("JSONObject1" + UnSpentTxsAddr.toString());
                JSONArray jsonArrayUnspentData = jsonDataJsonObject.getJSONArray("unspent");
                LogUtil.e("JSONObject2" + jsonArrayUnspentData.toString());
                for (int j = 0; j < jsonArrayUnspentData.length(); j++) {
                    JSONObject jsonObjectUnspentData = jsonArrayUnspentData.getJSONObject(j);
                    UnSpentTxsBean mUnSpentTxsBean = gson.fromJson(jsonObjectUnspentData.toString(), UnSpentTxsBean.class);
                    mUnSpentTxsBean.setAddress(UnSpentTxsAddr);
                    LogUtil.i("mUnSpentTxsBean第" + String.valueOf(j) + " 筆= " + mUnSpentTxsBean.getAddress() + " ; " + String.valueOf(mUnSpentTxsBean.getAmount()) + ";" + mUnSpentTxsBean.getTx());
                    //skip the data of confirmation=0
                    if (mUnSpentTxsBean.getConfirmations() != 0) {
                        lisUnSpentTxs.add(mUnSpentTxsBean);
                    }
                }
            } else {
                LogUtil.e("jSon type wrong");
            }

        } catch (Exception e) {
            LogUtil.e("JSONException:" + e.toString());
            e.printStackTrace();
        } finally {
            return lisUnSpentTxs;
        }
    }


    public static socketByAddress jsonParserSocketAddress(String jsonString) {
        socketByAddress socketAddress = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            //建立Gson類別並將JSON資料裝入class物件裡
            Gson gson = new Gson();
            if (jsonObject.get("type").equals("address")) {
                socketAddress = new socketByAddress();
                JSONObject jsonObjectSocket = jsonObject.getJSONObject("data");
                socketAddress = gson.fromJson(jsonObjectSocket.toString(), socketByAddress.class);
                if (Double.valueOf(socketAddress.getAmount_received()) > 0) {
                    socketAddress.setTx_type("Received");
                    socketAddress.setBtc_amount(Double.valueOf(socketAddress.getAmount_received()));
                } else {
                    socketAddress.setTx_type("Sent");
                    socketAddress.setBtc_amount(Double.valueOf(socketAddress.getAmount_sent()));
                }
            } else {
                socketAddress = null;
//                LogUtil.e("jsonParserSocketAddress:" + jsonObject.get("type"));
            }
        } catch (Exception e) {
//            LogUtil.e("recovery GsonEception:" + e.toString());
            e.printStackTrace();
            socketAddress = null;
        } finally {

            return socketAddress;
        }
    }

    public static boolean jsonParserRate(Context mContext, String jsonString) {

        boolean mResult = false;
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArrayExchangeRate = jsonObject.getJSONArray("data");

            for (int i = 0; i < jsonArrayExchangeRate.length(); i++) {
                JSONObject jsonObjectExchangeRate = jsonArrayExchangeRate.getJSONObject(i).getJSONObject("rates");

                for (int j = 0; j < jsonObjectExchangeRate.names().length(); j++) {
                    String strRates = jsonObjectExchangeRate.get(jsonObjectExchangeRate.names().getString(j)).toString();
                    double rates = Double.parseDouble(strRates);
                    String key = jsonObjectExchangeRate.names().getString(j);
//                    LogUtil.i("key = " + key + " value = " + rates);
                    DatabaseHelper.insertCurrent(mContext, key, rates);
                }
            }
            mResult = true;
        } catch (Exception e) {
            mResult = false;
            LogUtil.e("GsonEception:" + e.toString());
            e.printStackTrace();
        } finally {
            return mResult;
        }
    }


    public static boolean jsonParsingFeeaRate(Context mContext, String jsonString) {
        boolean mResult = false;
        try {
            LogUtil.d("jsonParsingFeeaRate="+jsonString);
            JSONObject jsonObjectFeesRate = new JSONObject(jsonString);
            int halfHourFee = jsonObjectFeesRate.getInt("halfHourFee");
            LogUtil.d("halfHourFee="+halfHourFee);
            AppPrefrence.saveRecommendedHalfHourFees(mContext,halfHourFee);

            mResult = true;
        } catch (Exception e) {
            mResult = false;
            LogUtil.e("GsonEception:" + e.toString());
            e.printStackTrace();
        } finally {
            return mResult;
        }

    }

    public static boolean jsonParserBlockChainRate(Context mContext, String jsonString) {

        boolean mResult = false;
        try {
            JSONObject jsonObjectExchangeRate = new JSONObject(jsonString);
            for (int j = 0; j < jsonObjectExchangeRate.names().length(); j++) {
//                    String strRates = jsonObjectExchangeRate.get(jsonObjectExchangeRate.names().getString(j)).toString();
                Object objList = jsonObjectExchangeRate.getString(jsonObjectExchangeRate.names().getString(j));
                JSONObject jsonObjList = new JSONObject(objList.toString());
                String strRates = jsonObjList.get("last").toString();
                double rates = Double.parseDouble(strRates);
                String key = jsonObjectExchangeRate.names().getString(j);
//                    LogUtil.d("匯率: " + key + " value = " + rates);
                DatabaseHelper.insertCurrent(mContext, key, rates);
            }
            mResult = true;
        } catch (Exception e) {
            mResult = false;
            LogUtil.e("GsonEception:" + e.toString());
            e.printStackTrace();
        } finally {
            return mResult;
        }
    }

    public static int jsonParserRefresh(Context mContext, String jsonString, int accountID, boolean isAddressesUpdate) {

        int wallet_txs_cnt = 0;
        String mTxAddr = "";
        long mResult = 0;
        long tx_value = 0;
        String mDate = "";
        String mTransationID = "";
        String mAddr = "";
        String wid = PublicPun.card.getCardId();
        long mBalance = 0;
        int address_N_tx = 0;
        int latest_block = 0;
        int block_height = 0;
        int confirmations = 0;
//        ArrayList<ParsingAddress> lisAddress = new ArrayList<ParsingAddress>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            //建立Gson類別並將JSON資料裝入class物件裡
            Gson gson = new Gson();

            //Info
            JSONObject jsonObjectInfo = jsonObject.getJSONObject("info");
            Info mInfo = gson.fromJson(jsonObjectInfo.toString(), Info.class);
            latest_block = mInfo.getLatest_block().getHeight();
            LogUtil.d("Info getLatest_block.height: " + latest_block);


            //wallet
            JSONObject jsonObjectWallet = jsonObject.getJSONObject("wallet");
            PasingWallet mWallet = gson.fromJson(jsonObjectWallet.toString(), PasingWallet.class);
            LogUtil.d("Wallet N_tx: " + mWallet.getN_tx());
            wallet_txs_cnt = mWallet.getN_tx();

            if (isAddressesUpdate) {
                //address
                JSONArray jsonArrayAddress = jsonObject.getJSONArray("addresses");
                for (int i = 0; i < jsonArrayAddress.length(); i++) {
                    JSONObject jsonObjectAddress = jsonArrayAddress.getJSONObject(i);
                    ParsingAddress mAddress = gson.fromJson(jsonObjectAddress.toString(), ParsingAddress.class);
                    //有交易變動的才需要update
                    address_N_tx = mAddress.getN_tx();
                    if (address_N_tx > 0) {
                        mAddr = mAddress.getAddress();
                        mBalance = mAddress.getFinal_balance();
                        LogUtil.d("refresh parsing addresses,account=" + accountID + " ;addr=" + mAddr + " ;balance=" + mBalance + " N_tx=" + address_N_tx);
                        DatabaseHelper.updateAddress(mContext, accountID, mAddr, address_N_tx, mBalance);
                    }
                }
            }
            //Txs
            JSONArray jsonArrayTxs = jsonObject.getJSONArray("txs");
            ArrayList<Txs> lisTxs = new ArrayList<Txs>();
            for (int i = 0; i < jsonArrayTxs.length(); i++) {
                JSONObject jsonObjectTxs = jsonArrayTxs.getJSONObject(i);
                Txs mTxs = gson.fromJson(jsonObjectTxs.toString(), Txs.class);
                lisTxs.add(mTxs);

                //設定日期格式
                Date ts = new Date(lisTxs.get(i).getTime() * 1000);
                mDate = new SimpleDateFormat("dd-MMM-yyyy h:mm a").format(ts);

                mResult = lisTxs.get(i).getResult();
                mTransationID = lisTxs.get(i).getHash();
                block_height = lisTxs.get(i).getBlock_height();
                if (block_height == 0) {
                    confirmations = 0;
                } else {
                    confirmations = latest_block - block_height + 1;
                }
                if (mResult > 0) {
                    //RECV BTC
//                    for(int j = 0 ; j <lisTxs.get(i).getInputs().length ; j++)
//                    { LogUtil.i("txs AAddr=" + lisTxs.get(i).getInputs()[j].getPrev_out().getAddr());}
                    //取第一筆
                    mTxAddr = lisTxs.get(i).getInputs()[0].getPrev_out().getAddr();
//                    tx_value = lisTxs.get(i).getInputs()[0].getPrev_out().getValue();
                    tx_value = mResult;
                } else {
                    //SEND BTC
//                    for (int j = 0; j < lisTxs.get(i).getOut().length; j++) {
                    //取第一筆
                    mTxAddr = lisTxs.get(i).getOut()[0].getAddr();
                    tx_value = (10000 + lisTxs.get(i).getOut()[0].getValue()) * -1; //要加fees,先寫死,後面改浮動fees
//                    }
                }
//                LogUtil.i("account=" + accountID + " ;txAddr=" + mTxAddr + " ;mDate=" + mDate + " ;tx_value=" + tx_value);
                CwBtcTxs mCwBtcTxs = new CwBtcTxs();
                mCwBtcTxs.setWID(wid);
                mCwBtcTxs.setAccount_ID(accountID);
                mCwBtcTxs.setAddress(mAddr);
                mCwBtcTxs.setTxs_TransationID(mTransationID);
                mCwBtcTxs.setTxs_Address(mTxAddr);
                mCwBtcTxs.setTxs_Date(mDate);
                mCwBtcTxs.setTxs_Result(tx_value);
                mCwBtcTxs.setTxs_TransationID(mTransationID);
                mCwBtcTxs.setTxs_Confirmation(confirmations);

                DatabaseHelper.insertTxs(mContext, mCwBtcTxs);
            }
        } catch (Exception e) {
            LogUtil.e("JsonEception:" + e.toString());
            Crashlytics.log("jsonParserRefresh failed jSon=" + jsonString);
            e.printStackTrace();

        } finally {
        }
        return wallet_txs_cnt;
    }


    /**
     * recovery or new Host , get individual address blockChain info
     * * @param mContext
     *
     * @param jsonString
     * @param accountID
     * @param addr
     * @param kcid
     * @param kid
     * @param isInsert
     * @return
     */
    public static boolean jsonParserRecoveryAddresses(Context mContext, String jsonString,
                                                      int accountID, String addr, int kcid, int kid, boolean isInsert) {

        long finalBalance = 0;
        int address_N_tx = 0;
        boolean mResult = true;

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            //建立Gson類別並將JSON資料裝入class物件裡
            Gson gson = new Gson();
            //Wallet
            JSONObject jsonObjectWallet = jsonObject.getJSONObject("wallet");
            PasingWallet mWallet = gson.fromJson(jsonObjectWallet.toString(), PasingWallet.class);
//            finalBalance = mWallet.getFinal_balance() * SATOSHI_RATE; /存satoshi/顯示的時候再轉換btc單位
            finalBalance = mWallet.getFinal_balance();
            address_N_tx = mWallet.getN_tx();

            if (isInsert) {
                DatabaseHelper.insertAddress(mContext, accountID, addr, kcid, kid, address_N_tx, finalBalance);
            } else {
                DatabaseHelper.updateAddress(mContext, accountID, addr, kcid, kid, address_N_tx, finalBalance);
            }
            if (address_N_tx == 0) {
                //no txs
                mResult = false;
            } else {
                mResult = true;
            }
        } catch (Exception e) {
            LogUtil.e("recovery GsonEception:" + e.toString());
            e.printStackTrace();

        } finally {

            return mResult;
        }
    }

    public static byte[] calcKey(byte[] devKey, String s, byte[] loginChallenge) {
        byte[] ss = s.getBytes(Constant.UTF8);
        int length1 = loginChallenge.length;
        int length2 = devKey.length;
        int length3 = ss.length;

        int length = length1 + length2 + length3;
        byte[] bytes = new byte[length];
        for (int i = 0; i < length1; i++) {
            bytes[i] = loginChallenge[i];
        }

        for (int i = 0; i < length2; i++) {
            bytes[i + length1] = devKey[i];
        }

        for (int i = 0; i < length3; i++) {
            bytes[i + length1 + length2] = ss[i];
        }

//        LogUtil.e(PublicPun.printBytes(bytes));
        LogUtil.i("calcKey=" + LogUtil.byte2HexString(bytes));
        return bytes;
    }

//    public static void showNoticeDialog(Context mContext, String mTitle, String mMessage) {
//        LayoutInflater inflater = LayoutInflater.from(mContext);
//        View alert_view = inflater.inflate(R.layout.edit_dialog, null);//alert為另外做給alert用的layout
////        final EditText mEditText = (EditText) alert_view.findViewById(R.id.etInputLabel);
//        final TextView mDialogTitle = (TextView) alert_view.findViewById(R.id.dialog_title);
//        final TextView mDialogMessage = (TextView) alert_view.findViewById(R.id.dialog_message);
////        mEditText.setVisibility(View.INVISIBLE);
//        //-----------產生輸入視窗--------
//        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
//        mDialogTitle.setText(mTitle);
//        mDialogMessage.setText(mMessage);
//        builder.setView(alert_view);
//        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//
//            }
//        });
//        LogUtil.e("clickFunction");
//        builder.show();
//    }

    /**
     * show Dialog Messahe
     **/
    public static void showNoticeDialog(Context mContext, String mTitle, String mMessage) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View alert_view = inflater.inflate(R.layout.edit_dialog, null);//alert為另外做給alert用的layout
        final TextView mDialogTitle = (TextView) alert_view.findViewById(R.id.dialog_title);
        final TextView mDialogMessage = (TextView) alert_view.findViewById(R.id.dialog_message);
        mDialogTitle.setText(mTitle);
        mDialogMessage.setText(mMessage);
        //-----------產生輸入視窗--------
        new AlertDialog.Builder(mContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                .setView(alert_view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }).show();
    }


    public static void showNoticeDialogToFinish(final Context mContext, String mTitle, String mMessage) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View alert_view = inflater.inflate(R.layout.edit_dialog, null);//alert為另外做給alert用的layout
        final EditText mEditText = (EditText) alert_view.findViewById(R.id.etInputLabel);
        final TextView mDialogTitle = (TextView) alert_view.findViewById(R.id.dialog_title);
        final TextView mDialogMessage = (TextView) alert_view.findViewById(R.id.dialog_message);
//        mEditText.setVisibility(View.INVISIBLE);
        //-----------產生輸入視窗--------
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setCancelable(false);
        mDialogTitle.setText(mTitle);
        mDialogMessage.setText(mMessage);
        builder.setView(alert_view);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                BleActivity.bleManager.disConnectBle();
                ((Activity) mContext).finish(); // 離開程式
                System.exit(0);
            }
        });
        builder.show();
    }

    /**
     * 短土司
     *
     * @param context
     * @param text
     */
    public static void toast(Context context, CharSequence text) {
        Toast.makeText(context, (text == null ? "空" : text), Toast.LENGTH_SHORT).show();
    }

    public static String printBytes(byte[] bytes) {
        if (bytes == null) return null;
        StringBuilder sb = new StringBuilder();
        sb.append("byte[]=" + Arrays.toString(bytes));
        sb.append("/n");
        sb.append("byte[]_hash=" + byte2HexString(bytes));
        return sb.toString();
        //handle=byte[]=[121, -42, -36, -26]/nbyte[]_hash=79 d6 dc e6
    }

    /**
     * byte数组转哈希字符串
     *
     * @param bytes
     * @return
     */
    public static String byte2HexString(byte[] bytes) {
        if (bytes == null) return null;

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x ", b));
        }

        return sb.toString();
    }

    public static String byte2HexString(byte b) {
        return String.format("%02x", b);
    }

    public static byte[] encryptSHA256(byte[] bytes) {
        byte[] digestByte;

        digestByte = encrypt(bytes, "SHA-256");
        LogUtil.e(printBytes(digestByte));
        return digestByte;
    }

    public static byte[] encrypt(byte[] bytes, String type) {
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

    public static String selectMode(String mode) {
        LogUtil.e("Mode=" + mode);
        String s = "unknown mode";
        if (mode.equals("00")) {
            s = "INIT";
        } else if (mode.equals("01")) {
            s = "PERSO";
        } else if (mode.equals("02")) {
            s = "NORMAL";
        } else if (mode.equals("03")) {
            s = "AUTH";
        } else if (mode.equals("04")) {
            s = "LOCK";
        } else if (mode.equals("05")) {
            s = "ERROR";
        } else if (mode.equals("06")) {
            s = "NOHOST";
        } else if (mode.equals("07")) {
            s = "DISCONN";
        }

        return s;
    }

    /**
     * 将十进制转换为指定长度的十六进制字符串
     *
     * @param algorism  int 十进制数字
     * @param maxLength int 转换后的十六进制字符串长度
     * @return String 转换后的十六进制字符串
     */
    public static String algorismToHEXString(int algorism, int maxLength) {
        String result = "";
        result = Integer.toHexString(algorism);

        if (result.length() % 2 == 1) {
            result = "0" + result;
        }
        return patchHexString(result.toUpperCase(), maxLength);
    }

    /**
     * HEX字符串前补0，主要用于长度位数不足。
     *
     * @param str       String 需要补充长度的十六进制字符串
     * @param maxLength int 补充后十六进制字符串的长度
     * @return 补充结果
     */
    static public String patchHexString(String str, int maxLength) {
        String temp = "";
        for (int i = 0; i < maxLength - str.length(); i++) {
            temp = "0" + temp;
        }
        str = (temp + str).substring(0, maxLength);
        return str;
    }


//    /**
//     * Author :
//     * for set the progress dialog of the ap
//     * Date : 2015/9/12
//     *
//     * @param title set title of the dialog
//     */
//    public static Dialog showProgressDialog(Context context,String title) {
//        Dialog mProgressDialog = null;
//        mProgressDialog = new Dialog(context, R.style.MessageDialog);
//        View mView = LayoutInflater.from(context).inflate(R.layout.progress_dialog_layout, null);
//        WindowManager.LayoutParams mDialogParams = new WindowManager.LayoutParams();
//        mDialogParams.copyFrom(mProgressDialog.getWindow().getAttributes());
//        mDialogParams.width = (int) ((720 * DensityConvert.mMagnificationWidth) * 0.8f);
//        mDialogParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        mProgressDialog.setContentView(mView, mDialogParams);
//        TextView txtDialogTitle = (TextView) mProgressDialog.findViewById(R.id.txtDialogTitle);
//
//        txtDialogTitle.setText(title);
//        mProgressDialog.setCancelable(false);
//        mProgressDialog.show();
//
//        return mProgressDialog;
//    }

}
