package com.coolbitx.coolwallet.ui.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.coolbitx.coolwallet.DataBase.DatabaseHelper;
import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.entity.APITx;
import com.coolbitx.coolwallet.entity.Account;
import com.coolbitx.coolwallet.entity.Address;
import com.coolbitx.coolwallet.entity.CWAccount;
import com.coolbitx.coolwallet.entity.Constant;
import com.coolbitx.coolwallet.entity.Transaction;
import com.coolbitx.coolwallet.entity.UnSpentTxsBean;
import com.coolbitx.coolwallet.entity.dbAddress;
import com.coolbitx.coolwallet.general.AppPrefrence;
import com.coolbitx.coolwallet.general.BtcUrl;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.ui.InitialSecuritySettingActivity;
import com.coolbitx.coolwallet.util.Arith;
import com.coolbitx.coolwallet.util.BTCUtils;
import com.coolbitx.coolwallet.util.Base58;
import com.coolbitx.coolwallet.util.CwBtcNetWork;
import com.coolbitx.coolwallet.util.ECKey;
import com.coolbitx.coolwallet.util.VerificationException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.ByteUtil;
import com.snscity.egdwlib.utils.LogUtil;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import com.google.zxing.integration.android.IntentIntegrator;
//import com.google.zxing.integration.android.IntentResult;

/**
 * Created by ShihYi on 2015/12/30.
 */

// the tag "error" means that dora remarks the code because the APP crush.
public class SendFragment extends BaseFragment implements View.OnClickListener {

    private TextView tvSendTitle;
    private TextView tvSendSubtitle;
    private TextView tvSendSubtitle_country;
    private TextView tvSendSubtitle_country2;
    private EditText editSendAddress;
    private ImageView imagScan;
    private EditText editSendBtc;
    private EditText editSendUsd;
    private Button btnSend;
    //    private Context mContext;
    private CmdManager cmdManager;
    CwBtcNetWork netWork = new CwBtcNetWork();
    UnSpentTxsAsyncTask unSpentTxsAsyncTask;
    private List<UnSpentTxsBean> unSpentTxsBeanList;
    private List<Account> cwAccountList = new ArrayList<>();
    private CWAccount cwAccount;
    private int secStatus;
    private int trxStatus;
    private String otpCode;
    Transaction.Input[] signedInputs;
    Transaction.Output[] outputs;
    UnSpentTxsBean outputToSpend = new UnSpentTxsBean();
    //    String testInAddress = "1BV1bWo4KvhiGpaq3uUVKkjyRSbZGHzDM3"; //test send to addr
    String InAddress = "";
    private static final int HANDLER_SEND_BTC_FINISH = 9527;
    private static final int HANDLER_SEND_BTC_ERROR = 9521;
    private ProgressDialog mProgress;
    String currUnsignedTx;
    private ArrayList<dbAddress> lisCwBtcAdd = new ArrayList<dbAddress>();
    CwBtcNetWork cwBtcNetWork;
    int currentAccount = -1;
    int errorCnt;
    //modify
    private String title = "";
    private String value = "";
    private static final String DATA_NAME = "name";
    private int id;
    private static final String DATA_ID = "id";
    BTCUtils.FeeChangeAndSelectedOutputs processedTxData;
    private float mAmount;

    private byte CwSecurityPolicyMaskOtp = 0x01;
    private byte CwSecurityPolicyMaskBtn = 0x02;
    private byte CwSecurityPolicyMaskWatchDog = 0x10;
    private byte CwSecurityPolicyMaskAddress = 0x20;
    List<Address> inputAddressList;
    double BTCFee = 0.0001;


    public static SendFragment newInstance(String title, int indicatorColor, int dividerColor, int iconResId, int accountId) {
        SendFragment f = new SendFragment();
        f.setTitle(title);
        f.setIndicatorColor(indicatorColor);
        f.setDividerColor(dividerColor);
        f.setIconResId(iconResId);

        //pass data
        Bundle args = new Bundle();
        args.putString(DATA_NAME, title);

        //modify
        args.putInt(DATA_ID, accountId);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        FragMainActivity mainActivity = (FragMainActivity) activity;
//        mContext = mainActivity;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get data
        title = getArguments().getString(DATA_NAME);

        //modify
        id = getArguments().getInt(DATA_ID);
        value = ((FragMainActivity) mContext).getAccountFrag(id);

        cwBtcNetWork = new CwBtcNetWork();
        cwAccount = new CWAccount();
        cmdManager = new CmdManager();
        mProgress = new ProgressDialog(getActivity());
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
        mProgress.setMessage("Sending Bitcoin...");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_send_bitcoin, container, false);
        initView(view);
        setListener();
        return view;
    }


    @Override
    public void onClick(View v) {

        if (v == btnSend) {
            errorCnt = 0;

            //對方接收資訊
            recvAddress = editSendAddress.getText().toString().trim();
            amountStr = editSendBtc.getText().toString().trim().replace(",", ".");
            amount = Double.parseDouble(amountStr);

            if (recvAddress.isEmpty()) {
                PublicPun.ClickFunction(mContext, "Unable to send", "You didn't enter an address.");
                return;
            }

            if (recvAddress.length() < 20) {
                PublicPun.ClickFunction(mContext, "Unable to send", "Please enter a valid address.");
                return;
            }

            byte[] addressWithCheckSumAndNetworkCode = BTCUtils.decodeBase58(recvAddress);
            if (addressWithCheckSumAndNetworkCode[0] != 0) {
                PublicPun.ClickFunction(mContext, "Unable to send", "Please enter a valid address.");
                return;
            }

            try {
                byte[] bareAddress = new byte[20];
                System.arraycopy(addressWithCheckSumAndNetworkCode, 1, bareAddress, 0, bareAddress.length);
                MessageDigest digestSha = MessageDigest.getInstance("SHA-256");
                digestSha.update(addressWithCheckSumAndNetworkCode, 0, addressWithCheckSumAndNetworkCode.length - 4);
                byte[] calculatedDigest = digestSha.digest(digestSha.digest());
                for (int i = 0; i < 4; i++) {
                    if (calculatedDigest[i] != addressWithCheckSumAndNetworkCode[addressWithCheckSumAndNetworkCode.length - 4 + i]) {
                        PublicPun.ClickFunction(mContext, "Unable to send", "Please enter a valid address.");
                        return;
                    }
                }
            } catch (Exception e) {
//                throw new RuntimeException(e);
                e.printStackTrace();
                PublicPun.ClickFunction(mContext, "Unable to send", "Please enter a valid address.");
                return;
            }


            if (amountStr.isEmpty()) {
                PublicPun.ClickFunction(mContext, "Unable to send", "Please enter an amount.");
                return;
            }

            if (amountStr.length() > 2) {
                LogUtil.i("check=" + amountStr.substring(0, 1) + ";" + amountStr.substring(1, 1));
                if (amountStr.substring(0, 1).equals("0") && !amountStr.substring(1, 2).equals(".")) {
                    PublicPun.ClickFunction(mContext, "Unable to send", "Incorrect amount format");
                    return;
                }
            }

            double totalAmount = Double.valueOf(tvSendTitle.getText().toString());

            if (Arith.sub(Arith.sub(totalAmount, amount), BTCFee) < 0) {
                LogUtil.i("2.sent:total=" + TabFragment.BtcFormatter.format(totalAmount) +
                        " ; amount=" + TabFragment.BtcFormatter.format(amount) +
                        " ; BTCFee=" + TabFragment.BtcFormatter.format(BTCFee) +
                        " ; final =" + TabFragment.BtcFormatter.format(totalAmount - amount - BTCFee));
                PublicPun.ClickFunction(mContext, "Unable to send", "Amount is lower than balance\nTransaction fee: 0.0001 BTC");
                return;
            }
            if (amount <= 0) {
                LogUtil.i("3.sent:total=" + TabFragment.BtcFormatter.format(totalAmount) +
                        " ; amount=" + TabFragment.BtcFormatter.format(amount) +
                        " ; BTCFee=" + TabFragment.BtcFormatter.format(BTCFee) +
                        " ; final =" + TabFragment.BtcFormatter.format(totalAmount - amount - BTCFee));
                PublicPun.ClickFunction(mContext, "Unable to send", "Amount is lower than balance\nTransaction fee: 0.0001 BTC");
                return;
            }

            //ready to send.
            mProgress.show();
            getSecpo();
            FunGetUnspentAddresses(currentAccount);
        } else if (v == imagScan) {
            IntentIntegrator scanIntegrator = new IntentIntegrator(getActivity());
            scanIntegrator.initiateScan();
        }
    }

    int unspendNum;

    private void FunGetUnspentAddresses(int accountID) {
        unspendNum = 0;
        LogUtil.i("run FunGetUnspentAddresses");
        lisCwBtcAdd = DatabaseHelper.queryAddress(getActivity(), accountID, -1);
        int mAddressCnt = lisCwBtcAdd.size();
//            if (mAddressCnt > 20) {
//                int reExcuteCnt = (int) Math.round(mAddressCnt / 20 + 0.5);
//                //BLOCKR 的addr上限筆數20,超過要分批跑
//                for (int i = 0; i < reExcuteCnt; i++) {
//                    for (int j = 0; j <= 20; j++) {
//                        if (j == 0) {
//                            InAddress += lisCwBtcAdd.get(i).getAddress();
//                        } else {
//                            InAddress += "," + lisCwBtcAdd.get(i).getAddress();
//                        }
//                    }
//                    InAddress += "?unconfirmed=1";
//                    LogUtil.i("InAddress=" + InAddress);
//                    unSpentTxsAsyncTask = new UnSpentTxsAsyncTask();
//                    unSpentTxsAsyncTask.execute(InAddress);
//                }
//            } else {s
        for (int i = 0; i < lisCwBtcAdd.size(); i++) {
            if (lisCwBtcAdd.get(i).getBalance() > 0) {
                unspendNum++;

                if (unspendNum > 1) {
                    InAddress += "," + lisCwBtcAdd.get(i).getAddress();
                    LogUtil.i("1.SEND的地址: " + i + " =" + InAddress);
                } else {
                    InAddress += lisCwBtcAdd.get(i).getAddress();
                    LogUtil.i("2.SEND的地址: " + i + " =" + InAddress);
                }
            }
        }
//        }
        InAddress += "?unconfirmed=1";
        LogUtil.i("InAddress=" + InAddress);
        unSpentTxsAsyncTask = new UnSpentTxsAsyncTask();
        unSpentTxsAsyncTask.execute(InAddress);
    }

    private class UnSpentTxsAsyncTask extends AsyncTask<String, Void, List<UnSpentTxsBean>> {
        @Override
        protected List<UnSpentTxsBean> doInBackground(String... strings) {
            LogUtil.i("doInBackground!!!!");
            unSpentTxsBeanList = getUnspentTxsByAddr(strings[0]);
            return unSpentTxsBeanList;
        }

        @Override
        protected void onPostExecute(List<UnSpentTxsBean> UnSpentTxsBeans) {

            //amount = 要寄出的錢
            if (UnSpentTxsBeans.size() > 0) {
                LogUtil.i("UnSpentTxsBeans 取得完成有 " + UnSpentTxsBeans.size() + " 筆");
                double unspend = 0;
                for (int i = 0; i < UnSpentTxsBeans.size(); i++) {
                    LogUtil.i("UnSpentTxsBeans 第 " + i + " 筆 " + TabFragment.BtcFormatter.format(UnSpentTxsBeans.get(i).getAmount()));
                    unspend += UnSpentTxsBeans.get(i).getAmount();
                }
                LogUtil.i("unspend=" + String.valueOf(unspend) + " ;fee=" + TabFragment.BtcFormatter.format(BTCFee) +
                        " ; sent amount=" + TabFragment.BtcFormatter.format(amount));
                if (unspend >= BTCFee + amount) {
                    genChangeAddress(Constant.CwAddressKeyChainInternal);
                } else {
                    PublicPun.ClickFunction(mContext, "Unable to send", "At least 1 confirmation needed before sending out.");
                    if (mProgress.isShowing()) {
                        mProgress.dismiss();
                    }
                    return;
                }
            } else {
                PublicPun.ClickFunction(mContext, "Unable to send", "At least 1 confirmation needed before sending out.");
                if (mProgress.isShowing()) {
                    mProgress.dismiss();
                }
                return;
            }
            super.onPostExecute(UnSpentTxsBeans);
        }
    }

    public List<UnSpentTxsBean> getUnspentTxsByAddr(String mAddr) {

        List<UnSpentTxsBean> UnSpentTxsBeanList = null;
        ContentValues cv = new ContentValues();
        cv.put("addresses", mAddr);
        String result = cwBtcNetWork.doGet(cv, BtcUrl.URL_BLOCKR_UNSPENT, null);
        if (!TextUtils.isEmpty(result)) {
            if (result.equals("{\"errorCode\": 404}") || result.equals("{\"errorCode\": 400}") || result.equals("{\"errorCode\": 500}")) {
                errorCnt++;
                if (errorCnt <= 3) {
                    //cacel old task
                    unSpentTxsAsyncTask.cancel(true);
                    //run new task
                    unSpentTxsAsyncTask = new UnSpentTxsAsyncTask();
                    unSpentTxsAsyncTask.execute(InAddress);
                } else {
                    //cacel old task
                    unSpentTxsAsyncTask.cancel(true);
                }
            } else {
                UnSpentTxsBeanList = PublicPun.jsonParserUnspent(result, unspendNum);
                errorCnt = 0;
            }
        }
        return UnSpentTxsBeanList;
    }

    String recvAddress;
    String amountStr;
    double amount;

    //產生收零地址(of output)
    public void genChangeAddress(final int keyChainId) {

        final int accountId = currentAccount;
        ArrayList<dbAddress> lisCwBtcAdd = new ArrayList<dbAddress>();
        // find only internal addr
        lisCwBtcAdd= DatabaseHelper.queryAddress(mContext, id - 1, 1);

        String addressStr = null;
        //先query 卡片內0交易的int addr;沒有的話再產生
        for (int i = 0; i < lisCwBtcAdd.size(); i++) {
            //query出來已有orde by kid
            if (lisCwBtcAdd.get(i).getN_tx() == 0) {
                addressStr = lisCwBtcAdd.get(i).getAddress();
                break;
            }
        }
        if (addressStr != null) {
            //new 對方接收地址, 自己的找零地址, 發送的金額
            LogUtil.i("對方接收地址=" + recvAddress + ";DB的找零地址=" + addressStr + ";發送的金額=" +
                    amountStr + ";轉型=" + BTCUtils.convertToSatoshisValue(amountStr));

            prepareTransaction(recvAddress, addressStr, BTCUtils.convertToSatoshisValue(amountStr));
        } else {
            cmdManager.hdwGetNextAddress(keyChainId, accountId, new CmdResultCallback() {
                @Override
                public void onSuccess(int status, byte[] outputData) {
                    if ((status + 65536) == 0x9000) {
                        if (outputData != null) {
                            Address address = new Address();
                            address.setAccountId(accountId);
                            address.setKeyChainId(keyChainId);

                            int length = outputData.length;
                            byte[] keyIdBytes = new byte[4];
                            if (length >= 4) {
                                for (int i = 0; i < 4; i++) {
                                    keyIdBytes[i] = outputData[i];
                                }
                            }
                            //get kid
//                        String keyStr =  LogUtil.byte2HexStringNoBlank((keyIdBytes));
                            String keyStr = PublicPun.byte2HexString(keyIdBytes[0]);
                            LogUtil.i("零錢地址kid hex=" + keyStr);
                            int keyId = Integer.valueOf(keyStr, 16);
                            address.setKeyId(keyId);

                            byte[] addressBytes = new byte[25];
                            if (length >= 29) {
                                for (int i = 0; i < 25; i++) {
                                    addressBytes[i] = outputData[i + 4];
                                }
                            }

                            byte[] addrBytes = Base58.encode(addressBytes);//34b
                            String addressStr = new String(addrBytes, Constant.UTF8);
                            DatabaseHelper.insertAddress(mContext, accountId, addressStr, 0, keyId, 0, 0);
                            LogUtil.i("對方接收地址=" + recvAddress + ";新產的找零地址=" + addressStr + "發送的金額=" + amountStr);
                            //new 對方接收地址, 自己的找零地址, 發送的金額
                            prepareTransaction(recvAddress, addressStr, BTCUtils.convertToSatoshisValue(amountStr));
                        }
                    }
                }
            });
        }
    }

    //    100,000,000 Satoshi = 1.00000000 ฿
    private final long SATOSHIS_PER_COIN = 100000000;

    private void prepareTransaction(final String outputAddress, String changeAddress, long amountToSend) {

        trxStatus = Constant.TrxStatusBegin;
        long availableAmount = 0;
        for (UnSpentTxsBean unSpentTxsBean : unSpentTxsBeanList) {
            availableAmount += BTCUtils.convertToSatoshisValue(TabFragment.BtcFormatter.format(unSpentTxsBean.getAmount()));
        }

        long extraFee = BTCUtils.parseValue("0.0");
//        long extraFee = 0;
        LogUtil.e("帳戶 " + currentAccount + " 地址下有的餘額=" + availableAmount);
        LogUtil.e("帳戶 " + currentAccount + " 要發出的金額=" + amountToSend);

//        final BTCUtils.FeeChangeAndSelectedOutputs processedTxData =
        processedTxData =
                BTCUtils.calcFeeChangeAndSelectOutputsToSpend(unSpentTxsBeanList, amountToSend, extraFee, false);
        if (processedTxData.change == 0) {
            LogUtil.i("發送不用找零, 發送金額=" +
                    processedTxData.amountForRecipient + "; 發送地址=" +
                    outputAddress );
            outputs = new Transaction.Output[]{
                    new Transaction.Output(processedTxData.amountForRecipient, Transaction.Script.buildOutput(outputAddress)),
            };
        } else {
            if (outputAddress.equals(changeAddress)) {
                PublicPun.ClickFunction(mContext, "Notification", "Change address equals to recipient's address, it is likely an error.");
            }

            LogUtil.i("發送要找零=" + processedTxData.change + "; 發送金額=" +
                    processedTxData.amountForRecipient + "; 發送地址=" +
                    outputAddress + "; 找零地址=" + changeAddress);

            //the outputs of transation
            outputs = new Transaction.Output[]{
                    new Transaction.Output(processedTxData.amountForRecipient, Transaction.Script.buildOutput(outputAddress)),
                    new Transaction.Output(processedTxData.change, Transaction.Script.buildOutput(changeAddress)),
            };
        }

        //close transaction if exists
        cmdManager.trxFinish(new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    if (trxStatus == Constant.TrxStatusFinish) {
                        LogUtil.i("trxFinish 成功!!");
//                            didSignTransaction();
                    }
                    trxStatus = Constant.TrxStatusBegin;
                    LogUtil.i("trxStatus=" + trxStatus);
                }
            }
        });


        //計算出了手續費和找零後的 UnspentInputList未花费的交易,input=要拿來發送$的地址
        signedInputs = new Transaction.Input[processedTxData.outputsToSpend.size()];//
        LogUtil.i("要拿來發送$的地址length:" + signedInputs.length);

        if (inputAddressList == null) {
            inputAddressList = new ArrayList<>();
        }
        final boolean[] prepRsult = new boolean[signedInputs.length];

        for (int i = 0; i < signedInputs.length; i++) {
            Transaction.Input[] unsignedInputs = new Transaction.Input[signedInputs.length];
            for (int j = 0; j < unsignedInputs.length; j++) { //有幾個input
                outputToSpend = processedTxData.outputsToSpend.get(j);
                //dora modify
                byte[] byteTx = PublicPun.hexStringToByteArray(outputToSpend.getTx());//outputToSpend.getTx().getBytes()
                //my send addr
                LogUtil.i(outputToSpend.getAddress() + "的HEX=" + LogUtil.byte2HexString((byteTx)));

                Transaction.OutPoint outPoint = new Transaction.OutPoint(byteTx, outputToSpend.getN());//outputToSpend.getN

                byte[] mScripts = PublicPun.hexStringToByteArray(outputToSpend.getScript());//outputToSpend.getScript().getBytes()
                LogUtil.i("第" + j + "個input=" + " tx:" + outputToSpend.getTx() + " ; " + outputToSpend.getN() + "; Script:" + LogUtil.byte2HexString((mScripts)));

                if (j == i) {
                    //this input we are going to sign (remove the part of sig and filled in the Scripts)
                    unsignedInputs[j] = new Transaction.Input(outPoint,
                            //dora modify
                            new Transaction.Script(mScripts),
                            0xffffffff);
                } else {
                    unsignedInputs[j] = new Transaction.Input(outPoint, null, 0xffffffff);
                }
            }
            Transaction spendTxToSign = new Transaction(unsignedInputs, outputs, 0);
            final byte[] hash = Transaction.Script.hashTransactionForSigning(spendTxToSign);

            final int finalI = i;
            LogUtil.i("inputId=" + String.valueOf(finalI));
            dbAddress d = DatabaseHelper.querySendAddress(mContext, processedTxData.outputsToSpend.get(i).getAddress());
            final int dbKid = d.getKid();
            final int dbKcid = d.getKcid();
            final long dbBalance =  BTCUtils.convertToSatoshisValue(String.valueOf(processedTxData.outputsToSpend.get(i).getAmount()));
            final byte CwAddressKeyChainExternal;
            if (dbKcid == 0) {
                CwAddressKeyChainExternal = 0x00;
            } else {
                CwAddressKeyChainExternal = 0x01;
            }

            final byte[] mPublicKey = new byte[32];
            // 1.QUERY KEY 2.PREP TRX 3. TRX BEGIN
            cmdManager.hdwQueryAccountKeyInfo(Constant.CwHdwAccountKeyInfoPubKey,
                    dbKcid,
                    currentAccount,
                    dbKid,
                    new CmdResultCallback() {
                        @Override
                        public void onSuccess(int status, byte[] outputData) {
                            if ((status + 65536) == 0x9000) {
                                if (outputData != null) {
                                    byte[] publicKeyBytes = new byte[64];
                                    int length = outputData.length;

                                    if (length >= 64) {
                                        for (int i = 0; i < 64; i++) {
                                            publicKeyBytes[i] = outputData[i];
                                            if (i < 32) {
                                                mPublicKey[i] = outputData[i];
                                            }
                                        }
                                        LogUtil.i("hdwQueryAccountKeyInfo publicKey =" + LogUtil.byte2HexString(outputData));
                                    }

                                    final Address address = new Address();
                                    address.setAccountId(currentAccount);
                                    address.setKeyChainId(dbKcid);
                                    address.setAddress(processedTxData.outputsToSpend.get(finalI).getAddress()); //account 0 的 internal地址之一
                                    address.setKeyId(dbKid);
                                    address.setPublickey(publicKeyBytes);
                                    LogUtil.i("run " + finalI + ": getAddressInfo=" + processedTxData.outputsToSpend.get(finalI).getAddress() +
                                            " ;publicKey=" + LogUtil.byte2HexString(address.getPublickey()));
                                    inputAddressList.add(address);

                                }
                            }else {
                                PublicPun.ClickFunction(mContext, "Unable to sent", "Get PublicKey error:" + LogUtil.byte2HexString(outputData));
                            }
                        }
                    });

            //         big endian  ex:("0000000000002710");
            cmdManager.cwCmdHdwPrepTrxSign(PublicPun.user.getMacKey(),
                    finalI,
                    CwAddressKeyChainExternal,
                    currentAccount,
                    dbKid,
                    dbBalance,
                    hash,
                    new CmdResultCallback() {
                        @Override
                        public void onSuccess(int status, byte[] outputData) {
                            if ((status + 65536) == 0x9000) {

                                LogUtil.i("cwCmdHdwPrepTrxSign 成功!");
                                LogUtil.i("Address=" + processedTxData.outputsToSpend.get(finalI).getAddress());
                                LogUtil.i("balance=" + String.valueOf(dbBalance));

                                prepRsult[finalI] = true;
                                boolean isdidPrepareTransaction = false;
                                for (int i = 0; i < prepRsult.length; i++) {
                                    if (prepRsult[i]) {
                                        isdidPrepareTransaction = true;
                                    } else {
                                        isdidPrepareTransaction = false;
                                        break;
                                    }
                                }
                                LogUtil.i("length=" + prepRsult.length + " check prep=" + isdidPrepareTransaction);

                                if (isdidPrepareTransaction) {
                                    didPrepareTransaction(outputAddress);
                                }
                            } else {
                                PublicPun.ClickFunction(mContext, "Unable to sent", "PrepTrxSign error:" + LogUtil.byte2HexString(outputData));
                            }
                        }
                    });
        }

    }

    //the card response of waiting button_up is:01 06 54 52 58 2d 4f 4b ( fix value: TRX-OK )
    final byte[] successBtnPressesData = new byte[]{0x54, 0x52, 0x58, 0x2d};

    private void didPrepareTransaction(String outputAddress) {


        waitResponse = null;
        //generate OTP
        cmdManager.cwCmdTrxBegin(PublicPun.user.getEncKey(),
                processedTxData.amountForRecipient,
                outputAddress,
                new CmdResultCallback() {
                    @Override
                    public void onSuccess(int status, byte[] outputData) {

                        if ((status + 65536) == 0x9000) {
                            if (outputData != null) {
                                LogUtil.i("xxcwCmdTrxBegin 成功!!" + LogUtil.byte2HexString(outputData));
                                if (mProgress.isShowing()) {
                                    mProgress.dismiss();
                                }
                                if (!InitialSecuritySettingActivity.settingOptions[0] && InitialSecuritySettingActivity.settingOptions[1]) {
                                    didGetButton();
                                    //監聽a006,是否PRESS BUTTON
                                    cmdManager.getCmdProcessor().readButton();
                                }
                                if (InitialSecuritySettingActivity.settingOptions[0]) {
                                    // 交易要otp
                                    //監聽a006,是否透過CLICK or TAPTAP取得OTP
                                    cmdManager.getCmdProcessor().setButtonA006(true);
                                    cmdManager.getCmdProcessor().readButton();
                                    //走outputData.length>6 case
                                    LogUtil.i("READ A006=" + LogUtil.byte2HexString(outputData));
                                    didVerifyOtp();
                                }
                            }
                        } else if (Arrays.equals(outputData, successBtnPressesData)) {
                            //button only
                            mAlertDialog.dismiss();
                            trxStatus = Constant.TrxStatusGetBtn;
                            LogUtil.i("case1.button_up pressed!!!!!!");
                            mProgress.setMessage("Sending Bitcoin...");
                            mProgress.show();
                            doTrxSign();
                        } else if (outputData.length > 6) {
                            waitResponse = cmdManager.getCmdProcessor().ReadWaitResponse();
                            LogUtil.i("taptap resp=" + waitResponse);

                            if (waitResponse != null) {
                                editText.setText(waitResponse);
                            }

                        } else {
                            cancelTrx(true);
                            PublicPun.ClickFunction(mContext, "Unable to sent", "CmdTrxBegin error:" + LogUtil.byte2HexString(outputData));
                        }
                    }
                });
    }

    String waitResponse;

    private void doTrxSign() {
        doTrxSignSuccessCnt = 0;
        scriptSigs = new ArrayList();
        for (int i = 0; i < signedInputs.length; i++) {
            LogUtil.i("input :addr " + i + inputAddressList.get(i).getAddress() + ";punlickey =" + LogUtil.byte2HexString(inputAddressList.get(i).getPublickey()));
            doTrxSign(i, inputAddressList.get(i).getPublickey());
        }
    }

    private void cancelTrx(boolean isRefresh) {
        cmdManager.getCmdProcessor().setButtonA006(false);
        FunTrxFinish(isRefresh);
        if (mProgress.isShowing()) {
            mProgress.dismiss();
        }
    }

    private void didGetButton() {
        //做一個丟監聽006的cmd

        LogUtil.i("交易要button");
        trxStatus = Constant.TrxStatusWaitBtn;
        //發送一個給a006讀取特殊狀態的cmd
        cmdManager.getCmdProcessor().setButtonA006(true);
        if (mProgress != null) {
            mProgress.dismiss();
        }
        ClickFunction("Send", "Please press Button On the Card", false, true);
    }

    int doTrxSignSuccessCnt;

    EditText editText;

    private void didVerifyOtp() {
        final View item = LayoutInflater.from(mContext).inflate(R.layout.alert_dialog_otp_input, null);
        AlertDialog.Builder otp_dialog = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
        editText = (EditText) item.findViewById(R.id.alert_editotp);
        otp_dialog.setView(item);
        otp_dialog.setCancelable(false);
//        verify OTP
        if (trxStatus == Constant.TrxStatusWaitOtp) {
            trxStatus = Constant.TrxStatusGetOtp;
        }

        otp_dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                cmdManager.getCmdProcessor().setButtonA006(false);
                otpCode = editText.getText().toString().trim();
                LogUtil.i("xxotp enter ok:" + otpCode);

                if (otpCode.isEmpty()) {
                    ClickFunction("OTP incorrect", "", true, false);
                }

                cmdManager.trxVerifyOtp(otpCode, new CmdResultCallback() {
                    @Override
                    public void onSuccess(int status, byte[] outputData) {
                        LogUtil.i("trxVerifyOtp status:" + String.valueOf(status) + " ;output=" + LogUtil.byte2HexString(outputData));
                        if ((status + 65536) == 0x9000) {
                            if (InitialSecuritySettingActivity.settingOptions[1]) {
                                LogUtil.i("case3.otp + button pressed(STEP1)");
                                //發送一個給a006讀取特殊狀態的cmd
                                didGetButton();
                                cmdManager.getCmdProcessor().readButton();
                            } else {
                                //case2.otp verify only
                                LogUtil.i("case2. otp verify only");
                                mProgress.setMessage("Sending Bitcoin...");
                                mProgress.show();
                                //有幾筆input就sign幾次
                                doTrxSign();
                            }
                        } else if (Arrays.equals(outputData, successBtnPressesData)) {
                            LogUtil.i("case3.otp + button pressed(STEP2)");
                            mAlertDialog.dismiss();
                            trxStatus = Constant.TrxStatusGetBtn;
                            mProgress.setMessage("Sending Bitcoin...");
                            mProgress.show();
                            doTrxSign();
                        } else {
                            LogUtil.i("otp failed!!!!! status=" + String.valueOf(status + 65536));
                            cancelTrx(false);
                            ClickFunction("OTP incorrect", "", true, false);
                        }
                    }
                });
            }
        })
                .setNegativeButton(R.string.strCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        EditText editText = (EditText) item.findViewById(R.id.alert_editotp);
                        dialog.dismiss();
                        cancelTrx(true);
                    }
                });
        otp_dialog.show();

    }

    List<byte[]> scriptSigs;

    private void doTrxSign(final int inputID, byte[] publicKey) {
        final byte[] addPublicKey = publicKey;
        LogUtil.i("doTrxSign.inputID=" + inputID);
        cmdManager.trxSign(inputID,
                new CmdResultCallback() {
                    @Override
                    public void onSuccess(int status, byte[] outputData) {
                        if ((status + 65536) == 0x9000) {

                            if (outputData != null) {
                                int length = outputData.length;
                                LogUtil.i("trxSign成功,length=" + length);
                                if (length >= 64) {
                                    byte[] signOfTx = new byte[64];
                                    for (int i = 0; i < 64; i++) {
                                        signOfTx[i] = outputData[i];
                                    }
                                    LogUtil.i("取得 signOfTx=" + LogUtil.byte2HexString(signOfTx));
                                    doTrxSignSuccessCnt++;
//                                    LogUtil.i("xxxxxxxxxx :addr " + inputID + inputAddressList.get(inputID).getAddress() +
//                                            ";punlickey =" + LogUtil.byte2HexString(BTCUtils.reverse(inputAddressList.get(inputID).getPublickey())));
                                    scriptSigs.add(genScriptSig(signOfTx, inputAddressList.get(inputID).getPublickey()));

                                    if (doTrxSignSuccessCnt == signedInputs.length) {
                                        currUnsignedTx = genRawTxData(scriptSigs);
                                        LogUtil.i("取得 currUnsignedTx=" + currUnsignedTx);
                                        PublishToNetwork(currUnsignedTx);
                                    }
                                }
                            }
                        }else{
                            cancelTrx(true);
                            PublicPun.ClickFunction(mContext, "Unable to sent", "trxSign error:" + LogUtil.byte2HexString(outputData));
                        }
                    }
                });
    }

    private void FunTrxFinish(boolean isRefresh) {
//        end transaction if exists
        cmdManager.trxFinish(new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    LogUtil.i("trxFinish成功");
                    trxStatus = Constant.TrxStatusFinish;
                }
            }
        });

        if (isRefresh) {
            byte ByteAccId = (byte) currentAccount;
            FragMainActivity.cmdManager.McuSetAccountState(ByteAccId, new CmdResultCallback() {
                        @Override
                        public void onSuccess(int status, byte[] outputData) {
                            if ((status + 65536) == 0x9000) {
                            }
                        }
                    }
            );
        }
    }

    private String genRawTxData(List<byte[]> scriptSigs) {

        APITx.Tx ctx = new APITx.Tx();

        ctx.setVersion(1);
        ctx.setTxinCnt(signedInputs.length);
        ctx.setTxoutCnt(outputs.length);
        ctx.setLock(0x00000000);

        ArrayList<APITx.Txin> txinList = new ArrayList<APITx.Txin>();

        for (int i = 0; i < signedInputs.length; i++) {
            LogUtil.i("signedInputs " + i);
            APITx.Txin ctxin = new APITx.Txin();

            byte[] ByteinTx = PublicPun.hexStringToByteArrayRev(processedTxData.outputsToSpend.get(i).getTx());
//            Collections.reverse(ByteinTx);
            LogUtil.i("ByteinTx=" + LogUtil.byte2HexStringNoBlank(ByteinTx));
            byte[] revId = new byte[32];
            String tid;

            for (int j = 0; j < 32; j++) {
                revId[j] = ByteinTx[ByteinTx.length - j - 1];
            }
            tid = LogUtil.byte2HexStringNoBlank(revId);
            LogUtil.i("tid=" + tid);
            ctxin.setTx(tid);
//            ctxin.setIndex(outputToSpend.getN());
            ctxin.setIndex(processedTxData.outputsToSpend.get(i).getN());
            ctxin.setScriptSigLen(scriptSigs.get(i).length);
            ctxin.setScriptSig(scriptSigs.get(i));
            txinList.add(ctxin);

        }
        ctx.setTxinList(txinList);

        ArrayList<APITx.Txout> txoutList = new ArrayList<APITx.Txout>();

        for (int i = 0; i < outputs.length; i++) {
            APITx.Txout ctxout = new APITx.Txout();
            ctxout.setAdd(outputs[i].script.toString());
            LogUtil.i("outputs[i].script.toString()=" + outputs[i].script.toString());
            ctxout.setValue((outputs[i].value));
            txoutList.add(ctxout);
        }
        ctx.setTxoutList(txoutList);
        return txToHex(ctx);
    }

    private String txToHex(APITx.Tx tx) {

        StringBuilder sb = new StringBuilder();
        // version no.
        sb.append("01000000");
        LogUtil.i("getVersion=" + PublicPun.algorismToHEXString(tx.getVersion(), 2));
        // In-counter
        sb.append(PublicPun.algorismToHEXString(tx.getTxinCnt(), 2));
        LogUtil.i("getTxinCnt=" + PublicPun.algorismToHEXString(tx.getTxinCnt(), 2));

        //List of inputs
        for (int i = 0; i < tx.getTxinCnt(); i++) {
            //prehash
            sb.append(tx.getTxinList().get(i).getTx());
            LogUtil.i("hash=" + tx.getTxinList().get(i).getTx());
            sb.append(LogUtil.byte2HexStringNoBlank(ByteUtil.intToByteLittle(tx.getTxinList().get(i).getIndex(), 4)));
            LogUtil.i("in index=" + PublicPun.algorismToHEXString(tx.getTxinList().get(i).getIndex(), 2));
            //script len
            sb.append(PublicPun.algorismToHEXString(tx.getTxinList().get(i).getScriptSigLen(), 2));
            LogUtil.i("in getScriptSigLen=" + PublicPun.algorismToHEXString(tx.getTxinList().get(i).getScriptSigLen(), 2));
            //scriptSig
            sb.append(LogUtil.byte2HexStringNoBlank(tx.getTxinList().get(i).getScriptSig()));
            LogUtil.i("getScriptSig=" + LogUtil.byte2HexStringNoBlank(tx.getTxinList().get(i).getScriptSig()));
            //sequence_no
            sb.append("ffffffff");
        }

        // Out-counter
        sb.append(PublicPun.algorismToHEXString(tx.getTxoutCnt(), 2));
        LogUtil.i("Out-counter=" + PublicPun.algorismToHEXString(tx.getTxoutCnt(), 2));
        //List of outputs
        for (int i = 0; i < tx.getTxoutCnt(); i++) {
            //value
//            sb.append("1027000000000000");
//            PublicPun.algorismToHEXString((int)tx.getTxoutList().get(i).getValue(), 16)
            byte[] value = ByteUtil.intToByteLittle((int) tx.getTxoutList().get(i).getValue(), 8);
            LogUtil.i("Out-value=" + String.valueOf(tx.getTxoutList().get(i).getValue()) + " ;hex=" + LogUtil.byte2HexString(value));
            sb.append(LogUtil.byte2HexStringNoBlank(value));

            //script len
            int scriptLen = tx.getTxoutList().get(i).getAdd().length();
            LogUtil.i("Out-scriptLen len=" + PublicPun.algorismToHEXString(scriptLen, 2));
            sb.append("19");
//            LogUtil.i("Out-scriptLen len=" + PublicPun.algorismToHEXString(scriptLen, 2));
            //script
            sb.append(tx.getTxoutList().get(i).getAdd());
            LogUtil.i("Out-script=" + tx.getTxoutList().get(i).getAdd());
            //sequence_no
//            sb.append(PublicPun.algorismToHEXString(0, 2));
        }
        //block lock time
        sb.append("00000000");
        String info = sb.toString();
        info.replace(" ", "");
        LogUtil.i("api hex=" + info);

        return info;
    }

    private byte[] genScriptSig(byte[] bytes, byte[] mPublicKey) {

        byte[] xPublicKey = new byte[32];
        //input data
        byte[] mCalKey = new byte[34];
        byte[] SignatureDER;
        byte mType;
        byte PUSHDATA;

        for (int i = 0; i < 32; i++) {
            xPublicKey[i] = mPublicKey[i];
        }

        SignatureDER = encodeToDER(bytes);
        LogUtil.i("取得 SignatureDER=" + LogUtil.byte2HexString(SignatureDER));
        LogUtil.i("取得 publicKey=" + LogUtil.byte2HexString(mPublicKey));
        LogUtil.i("取得 publicKey lastone=" + PublicPun.byte2HexString(mPublicKey[63]));
        //mCalKey=34B (PUSHDATA + type(1b) + x publicKey(32b)+)
        PUSHDATA = (byte) 0x21; // 33B=type(1b)+ x publicKey(32b)
        LogUtil.i("genScriptSig mType判斷 =" + Integer.toBinaryString(mPublicKey[63]));
        int mLastKey = Integer.parseInt(PublicPun.byte2HexString(mPublicKey[63]), 16);
        //
        if (mLastKey % 2 == 1) {
            mType = (byte) 0x03;
        } else {
            mType = (byte) 0x02;
        }

        LogUtil.i("mLastKey=" + mLastKey + ";mType:" + mType);

        mCalKey[0] = PUSHDATA;
        mCalKey[1] = mType;

        for (int i = 0; i < xPublicKey.length; i++) {
            mCalKey[i + 2] = xPublicKey[i];
        }
        LogUtil.i("取得 mCalKey=" + LogUtil.byte2HexString(mCalKey));
        byte[] mSig = new byte[SignatureDER.length + mCalKey.length];


        for (int i = 0; i < SignatureDER.length; i++) {
//            LogUtil.i("SignatureDER loop="+PublicPun.byte2HexString(SignatureDER[i]));
            mSig[i] = SignatureDER[i];

        }

        for (int i = 0; i < mCalKey.length; i++) {
//            LogUtil.i("mCalKey loop="+PublicPun.byte2HexString(mCalKey[i]));
            mSig[i + SignatureDER.length] = mCalKey[i];
        }
        LogUtil.i("取得 mSig=" + LogUtil.byte2HexString(mSig));
        return mSig;

    }

    private byte[] encodeToDER(byte[] bytes) {
        byte[] mScriptSig;
        byte[] derX = new byte[32];
        byte[] derY = new byte[32];

        //x data
        for (int i = 0; i < 32; i++) {
            derX[i] = bytes[i];
//            LogUtil.i("derX loop="+PublicPun.byte2HexString(derX[i]));
        }
        LogUtil.i("derX loop=" + LogUtil.byte2HexString(derX));
        //y data
        for (int i = 0; i < 32; i++) {
            derY[i] = bytes[i + 32];
//            LogUtil.i("derY loop="+PublicPun.byte2HexString(derY[i]));
        }
        LogUtil.i("derY loop=" + LogUtil.byte2HexString(derY));
        try {
            ECKey.ECDSASignature ecSig = new ECKey.ECDSASignature(new BigInteger(1, derX), new BigInteger(1, derY));
            byte[] mDERSig = ecSig.encodeToDER();
            LogUtil.i("取得 encodeToDER=" + LogUtil.byte2HexString(mDERSig));

            byte PushData = (byte) (mDERSig.length + 1);
            mScriptSig = new byte[1 + mDERSig.length + 1];
            LogUtil.i("mScriptSig.length=" + mScriptSig.length);
            mScriptSig[0] = PushData;

            for (int i = 0; i < mDERSig.length; i++) {
                mScriptSig[i + 1] = mDERSig[i];
            }
            //SIGHASH_ALL
            mScriptSig[mDERSig.length + 1] = (byte) 0x01;

            LogUtil.i("取得 mScriptSig=" + LogUtil.byte2HexString(mScriptSig));
        } catch (IllegalArgumentException e) {
            throw new VerificationException("Could not decode DER", e);
        }
        return mScriptSig;
    }

    private void PublishToNetwork(final String sendTx) {

        new Thread(runnable).start();
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // TODO: http request.
            int postDecodeResult = -1;
            int postPushResult = -1;
            int hadlerMsg = 0;

            postDecodeResult = netWork.doPost(BtcUrl.URL_BLOCKR_SERVER_SITE + BtcUrl.URL_BLICKR_DECODE, currUnsignedTx);
            if (postDecodeResult == 200) {
                postPushResult = netWork.doPost(BtcUrl.URL_BLOCKR_SERVER_SITE + BtcUrl.URL_BLICKR_PUSH, currUnsignedTx);
                if (postPushResult == 200) {
                    hadlerMsg = HANDLER_SEND_BTC_FINISH;
                } else {
                    hadlerMsg = HANDLER_SEND_BTC_ERROR;
                }
            } else {
                hadlerMsg = HANDLER_SEND_BTC_ERROR;
            }

            mHandler.sendMessage(mHandler.obtainMessage(hadlerMsg));
        }
    };

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_SEND_BTC_FINISH:

                    cancelTrx(true);
                    PublicPun.ClickFunction(mContext, "Sent", "Sent " + amountStr + "btc to " + recvAddress);
                    //clear trx data
                    editSendAddress.setText("");
                    editSendBtc.setText("");
                    editSendUsd.setText("");

                    break;
                case HANDLER_SEND_BTC_ERROR:
                    if (mProgress != null) {
                        mProgress.dismiss();
                    }
                    PublicPun.ClickFunction(mContext, "Unable to send", "Please check your internet connection.");
                    break;
            }
        }
    };

    public void onQRcodeResult() {
        try {
            String scanContent = FragMainActivity.scanningResult.getContents();
//            String scanFormat = FragMainActivity.scanningResult.getFormatName();
            LogUtil.i("QR code scanContent=" + scanContent);
            //sample addr: bitcoin:12Y7Rqmt3xEpG1jYgtXwhSo3EUKXkbVNrZ?amount=1
            String scanAddress = "";
            String scanAmount = "";

            if (scanContent != null) {
                if (scanContent.indexOf("=") == -1) {
                    scanAddress = scanContent.substring(scanContent.indexOf(":") + 1);
                    scanAmount = "0";
                } else {
                    scanAddress = scanContent.substring(scanContent.indexOf(":") + 1, scanContent.indexOf("?"));
                    scanAmount = scanContent.substring(scanContent.indexOf("=") + 1);
                }
                double currAmt = Double.valueOf(scanAmount) * AppPrefrence.getCurrentRate(mContext);
                editSendAddress.setText(scanAddress);
                editSendBtc.setText(scanAmount);
                editSendUsd.setText(TabFragment.currentFormatter.format(currAmt));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initView(View view) {
        tvSendTitle = (TextView) view.findViewById(R.id.tv_send_title);
        tvSendSubtitle = (TextView) view.findViewById(R.id.tv_send_subtitle);
        tvSendSubtitle_country = (TextView) view.findViewById(R.id.tv_send_subtitle_country);
        tvSendSubtitle_country2 = (TextView) view.findViewById(R.id.tv_send_subtitle_country2);
        editSendAddress = (EditText) view.findViewById(R.id.edit_send_address);
        imagScan = (ImageView) view.findViewById(R.id.imageView);
        editSendBtc = (EditText) view.findViewById(R.id.edit_send_btc);
        editSendUsd = (EditText) view.findViewById(R.id.edit_send_usd);
        btnSend = (Button) view.findViewById(R.id.btn_send);
        btnSend.setOnClickListener(this);
        imagScan.setOnClickListener(this);
        editSendBtc.setFilters(new InputFilter[]{PublicPun.lengthfilter, new InputFilter.LengthFilter(99)});
    }

    private void setListener() {
        editSendBtc.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!TextUtils.isEmpty(editSendBtc.getText().toString())) {
                        mAmount = Float.valueOf(editSendBtc.getText().toString());
                        if (mAmount > 0) {
                            changeRate(mContext, editSendUsd, mAmount, "#.##");
                        }
                    }
                }
            }
        });

        editSendBtc.setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            if (!TextUtils.isEmpty(editSendBtc.getText().toString())) {
                                mAmount = Float.valueOf(editSendBtc.getText().toString());
                                if (mAmount > 0) {
                                    changeRate(mContext, editSendUsd, mAmount, "#.##");
                                }
                            }
                        }
                        return false;
                    }
                }
        );

        editSendUsd.setOnFocusChangeListener(
                new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {

                            if (!TextUtils.isEmpty(editSendUsd.getText().toString())) {
                                mAmount = Float.valueOf(editSendUsd.getText().toString());
                                if (mAmount > 0) {
                                    changeRate(mContext, editSendBtc, mAmount, "#.########");
                                }
                            }
                        }
                    }
                }
        );

        editSendUsd.setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            if (!TextUtils.isEmpty(editSendUsd.getText().toString())) {
                                mAmount = Float.valueOf(editSendUsd.getText().toString());
                                if (mAmount > 0) {
                                    changeRate(mContext, editSendBtc, mAmount, "#.########");
                                }
                            }
                        }
                        return false;
                    }
                }
        );
    }

    AlertDialog mAlertDialog;

    private void ClickFunction(String mTitle, String mMessage, final boolean isReOTP, boolean isWaitButton) {

        String buttonStr = "";
        if (isReOTP) {
            buttonStr = "Try again";
        } else {
            buttonStr = "OK";
        }
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View alert_view = inflater.inflate(R.layout.edit_dialog, null);//alert為另外做給alert用的layout
        final EditText mEditText = (EditText) alert_view.findViewById(R.id.etInputLabel);
        final TextView mDialogTitle = (TextView) alert_view.findViewById(R.id.dialog_title);
        final TextView mDialogMessage = (TextView) alert_view.findViewById(R.id.dialog_message);
        mEditText.setVisibility(View.INVISIBLE);
        mDialogMessage.setText(mMessage);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT);
        mDialogTitle.setText(mTitle);
        builder.setView(alert_view);
        if (!isWaitButton) {
            //-----------產生輸入視窗--------

            builder.setPositiveButton(buttonStr, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (isReOTP) {
                        //交易otp要重出,要重做sign & begin Trx
                        mProgress.setMessage("Regenerating OTP...");
                        mProgress.show();
                        genChangeAddress(Constant.CwAddressKeyChainInternal);
                    } else {
                    }
                }
            });
        }
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                cmdManager.getCmdProcessor().setButtonA006(false);
                cancelTrx(true);
            }
        });
//        builder.show();
        builder.setCancelable(false);
        mAlertDialog = builder.show();
    }


    private void getSecpo() {

        cmdManager.getSecpo(new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    if (outputData != null && outputData.length > 0) {
                        if ((outputData[0] & CwSecurityPolicyMaskOtp) >= 1) {
                            InitialSecuritySettingActivity.settingOptions[0] = true;
                        } else {
                            InitialSecuritySettingActivity.settingOptions[0] = false;
                        }

                        if ((outputData[0] & CwSecurityPolicyMaskBtn) >= 1) {
                            InitialSecuritySettingActivity.settingOptions[1] = true;
                        } else {
                            InitialSecuritySettingActivity.settingOptions[1] = false;
                        }

                        if ((outputData[0] & CwSecurityPolicyMaskAddress) >= 1) {
                            InitialSecuritySettingActivity.settingOptions[2] = true;
                        } else {
                            InitialSecuritySettingActivity.settingOptions[2] = false;
                        }

                        if ((outputData[0] & CwSecurityPolicyMaskWatchDog) >= 1) {
                            InitialSecuritySettingActivity.settingOptions[3] = true;
                        } else {
                            InitialSecuritySettingActivity.settingOptions[3] = false;
                        }
                        LogUtil.i("安全設置:otp=" + InitialSecuritySettingActivity.settingOptions[0] +
                                ";button_up=" + InitialSecuritySettingActivity.settingOptions[1] +
                                ";address" + InitialSecuritySettingActivity.settingOptions[2] +
                                ";dog=" + InitialSecuritySettingActivity.settingOptions[3]);
//                        SetSecpo(true);
                    }

                }
            }
        });
    }

    private void changeRate(Context context, EditText toEditText, float amount, String format) {
        float mChangeAmt = 0;
        DecimalFormat mFormatter = null;
        if (toEditText.getId() == editSendBtc.getId()) {
            mChangeAmt = (float) (amount / AppPrefrence.getCurrentRate(context));
        } else {
            mChangeAmt = (float) (amount * AppPrefrence.getCurrentRate(context));
        }
        mFormatter = new DecimalFormat(format);
        LogUtil.e("mChangeAmt : " + mChangeAmt);
        toEditText.setText(mFormatter.format(mChangeAmt));
    }

    public void refresh(int account) {
        currentAccount = account;
        int final_balance = 0;
        double btcAmt = 0;
        for (int i = 0; i < TabFragment.lisCwBtcAdd.size(); i++) {
            final_balance += TabFragment.lisCwBtcAdd.get(i).getBalance();
        }
        btcAmt = final_balance * PublicPun.SATOSHI_RATE;
        tvSendTitle.setText(TabFragment.BtcFormatter.format(btcAmt));
        tvSendSubtitle_country.setText(AppPrefrence.getCurrentCountry(mContext));
        tvSendSubtitle_country2.setText(AppPrefrence.getCurrentCountry(mContext));
        double currRate = btcAmt * AppPrefrence.getCurrentRate(mContext);
        tvSendSubtitle.setText(TabFragment.currentFormatter.format(currRate));

        // QR code result
        onQRcodeResult();
    }

}