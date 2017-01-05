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
import com.coolbitx.coolwallet.callback.TransactionConfirmCallback;
import com.coolbitx.coolwallet.entity.APITx;
import com.coolbitx.coolwallet.entity.Account;
import com.coolbitx.coolwallet.entity.Address;
import com.coolbitx.coolwallet.entity.CWAccount;
import com.coolbitx.coolwallet.entity.Constant;
import com.coolbitx.coolwallet.entity.Transaction;
import com.coolbitx.coolwallet.entity.TxsConfirm;
import com.coolbitx.coolwallet.entity.UnSpentTxsBean;
import com.coolbitx.coolwallet.entity.dbAddress;
import com.coolbitx.coolwallet.general.AppPrefrence;
import com.coolbitx.coolwallet.general.BtcUrl;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.httpRequest.CwBtcNetWork;
import com.coolbitx.coolwallet.ui.InitialSecuritySettingActivity;
import com.coolbitx.coolwallet.ui.TransactionConfirmDialog;
import com.coolbitx.coolwallet.util.BTCUtils;
import com.coolbitx.coolwallet.util.Base58;
import com.coolbitx.coolwallet.util.ECKey;
import com.coolbitx.coolwallet.util.ValidationException;
import com.crashlytics.android.Crashlytics;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.ByteUtil;
import com.snscity.egdwlib.utils.LogUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
    private int trxStatus;
    private String otpCode;
    Transaction.Input[] signedInputs;
    Transaction.Output[] outputs;
    UnSpentTxsBean outputToSpend = new UnSpentTxsBean();
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
    private float editBtc;
    private boolean isTrxSuccess;
    private byte CwSecurityPolicyMaskOtp = 0x01;
    private byte CwSecurityPolicyMaskBtn = 0x02;
    private byte CwSecurityPolicyMaskWatchDog = 0x10;
    private byte CwSecurityPolicyMaskAddress = 0x20;
    List<Address> inputAddressList;

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

        //already do it on BaseFragment
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
        LogUtil.e("onCreateView");

        return view;
    }

    String recvAddress;
    String spendAmountStr;
    double spendAmount;
    Timer mTimer = null;

    @Override
    public void onClick(View v) {

        if (v == btnSend) {
            isTrxSuccess = false;
            errorCnt = 0;

            if (editSendAddress.getText().toString().isEmpty()) {
                PublicPun.showNoticeDialog(mContext, "Unable to send", "You didn't enter an address.");
                return;
            }
            if (editSendBtc.getText().toString().isEmpty()) {
                PublicPun.showNoticeDialog(mContext, "Unable to send", "Please enter an amount.");
                return;
            }

            recvAddress = editSendAddress.getText().toString().trim();
            spendAmountStr = editSendBtc.getText().toString().trim();// for some Europe's money format
            spendAmountStr = spendAmountStr.replace(",", ".");
            spendAmount = Double.parseDouble(spendAmountStr);
            LogUtil.e("click後對方接收地址=" + recvAddress + ";發送的金額=" + spendAmountStr);
            try {
                if (!BTCUtils.ValidateBitcoinAddress(recvAddress)) {
                    PublicPun.showNoticeDialog(mContext, "Unable to send", "Please enter a valid address.");
                    return;
                }
                LogUtil.d("valid address ok");

            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.log(new String(PublicPun.hexStringToByteArray(PublicPun.card.getCardId())) + ":Send Click failed:" + e.getMessage());
                PublicPun.showNoticeDialog(mContext, "Unable to send", "Please enter a valid address.");
                return;
            }

            double totalAmount = Double.valueOf(tvSendTitle.getText().toString());
            if ((totalAmount - spendAmount - 0.0001 < 0) || spendAmount <= 0) {
                PublicPun.showNoticeDialog(mContext, "Unable to send", "Amount is lower than balance");
//                Transaction fee: 0.0001 BTC
                return;
            }

            getSecpo();
            FunGetUnspentAddresses(currentAccount);

        } else if (v == imagScan) {
            IntentIntegrator scanIntegrator = new IntentIntegrator(getActivity());
            scanIntegrator.initiateScan();
        }
    }

    private void FunGetUnspentAddresses(int accountID) {
        LogUtil.i("FunGetUnspentAddresses");
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
                LogUtil.i("call unspent的地址: " + i + " =" + lisCwBtcAdd.get(i).getAddress());
                if (i == 0) {
                    InAddress += lisCwBtcAdd.get(i).getAddress();
                } else {
                    InAddress += "," + lisCwBtcAdd.get(i).getAddress();
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

            if (UnSpentTxsBeans.size() == 0) {
                PublicPun.showNoticeDialog(mContext, "Notification", getString(R.string.note_unspent));
                mProgress.dismiss();
            } else {
                LogUtil.d("UnSpentTxsBeans 取得完成有 " + UnSpentTxsBeans.size() + " 筆");
                genChangeAddress(Constant.CwAddressKeyChainInternal);
            }
            super.onPostExecute(UnSpentTxsBeans);
        }
    }

    public List<UnSpentTxsBean> getUnspentTxsByAddr(String mAddr) {

        List<UnSpentTxsBean> UnSpentTxsBeanList = null;
        ContentValues cv = new ContentValues();
        cv.put("addresses", mAddr);
        final String result = cwBtcNetWork.doGet(cv, BtcUrl.URL_BLOCKR_UNSPENT, null);
        if (!TextUtils.isEmpty(result)) {
//            if (result.equals("{\"errorCode\": 404}") || result.equals("{\"errorCode\": 400}") || result.equals("{\"errorCode\": 500}")) {
            if (result.contains("errorCode")) {
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
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            //failed 3 times,failed.
                            if(mProgress.isShowing()){
                                mProgress.dismiss();
                            }
                            PublicPun.showNoticeDialog(mContext, getString(R.string.send_notification_unable_to_send), getString(R.string.send_call_unspent_failed) + result);
                            Crashlytics.log(getString(R.string.send_call_unspent_failed) + getString(R.string.send_call_unspent_list_addresses) + InAddress);
                        }
                    });
                }
            } else {
                Crashlytics.log(getString(R.string.send_call_unspent_success) + getString(R.string.send_call_unspent_list_addresses) + InAddress);
                UnSpentTxsBeanList = PublicPun.jsonParserUnspent(result);
            }
        }
        return UnSpentTxsBeanList;
    }


    //產生收零地址(of output)
    public void genChangeAddress(final int keyChainId) {

        final int accountId = currentAccount;
        //對方接收資訊
//        final String recvAddress = editSendAddress.getText().toString().trim();
//        String spendAmountStr = editSendBtc.getText().toString().trim();

        String changeAddressStr = null;
        //先query 卡片內0交易的int addr;沒有的話再產生
        for (int i = 0; i < TabFragment.lisCwBtcAdd.size(); i++) {
            //query出來已有order by kid
            if (TabFragment.lisCwBtcAdd.get(i).getN_tx() == 0) {
                changeAddressStr = TabFragment.lisCwBtcAdd.get(i).getAddress();
                break;
            }
        }
        if (changeAddressStr != null) {
            //new 對方接收地址, 自己的找零地址, 發送的金額
            LogUtil.e("自己的找零地址=" + changeAddressStr);
            PreviousPrepareTransaction(recvAddress, changeAddressStr,BTCUtils.convertToSatoshisValue(spendAmountStr));
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
                            LogUtil.e("產生零錢地址kid hex=" + keyStr);
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
                            LogUtil.e("產生的找零地址=" + addressStr);
                            //new 對方接收地址, 自己的找零地址, 發送的金額
                            PreviousPrepareTransaction(recvAddress, addressStr, BTCUtils.convertToSatoshisValue(spendAmountStr));
                        }
                    }
                }
            });
        }
    }

    //    100,000,000 Satoshi = 1.00000000 ฿
    private static final int SATOSHIS_PER_COIN = 100000000;

    private void PreviousPrepareTransaction(final String outputAddress,final String changeAddress,final long amountToSend) {
        trxStatus = Constant.TrxStatusBegin;

        //its legal that Change address equals to recipient's address
        if (outputAddress.equals(changeAddress)) {
            cancelTrx();
            PublicPun.showNoticeDialog(mContext, "Notification",getString(R.string.send_notification_unable_to_send_with_change_error));
        }


        long availableAmount = 0;
        for (UnSpentTxsBean unSpentTxsBean : unSpentTxsBeanList) {
//            availableAmount += (long) (unSpentTxsBean.getAmount() * SATOSHIS_PER_COIN);
            availableAmount += BTCUtils.convertToSatoshisValue(new DecimalFormat("#.########").format(unSpentTxsBean.getAmount()));
        }
        long extraFee = BTCUtils.parseValue("0.0");
        LogUtil.e("帳戶 " + currentAccount + " 地址下有的餘額=" + availableAmount);
        LogUtil.e("帳戶 " + currentAccount + " 要發出的金額=" + amountToSend);
        TxsConfirm mTxsConfirm = null;
        try {
            processedTxData =
                    BTCUtils.calcFeeChangeAndSelectOutputsToSpend(mContext,unSpentTxsBeanList, amountToSend, extraFee, false);

            if(processedTxData==null){
                cancelTrx();
                PublicPun.showNoticeDialog(mContext, "Unable to send:","Can't find the unspent output.");
            }

            if (processedTxData.change == 0) {
                LogUtil.i("發送不用找零,發送地址=" + outputAddress);
                outputs = new Transaction.Output[]{
                        new Transaction.Output(processedTxData.amountForRecipient, Transaction.Script.buildOutput(outputAddress)),
                };
            } else {

                LogUtil.i("發送要找零=" + processedTxData.change + "; 發送金額=" +
                        processedTxData.amountForRecipient + "; 發送地址=" +
                        outputAddress + "; 找零地址=" + changeAddress);
                //the outputs of transation
                outputs = new Transaction.Output[]{
                        new Transaction.Output(processedTxData.amountForRecipient, Transaction.Script.buildOutput(outputAddress)),
                        new Transaction.Output(processedTxData.change, Transaction.Script.buildOutput(changeAddress)),
                };
            }
            LogUtil.e("outputs length=" + outputs.length);
            mTxsConfirm = new TxsConfirm(outputAddress,processedTxData.amountForRecipient,processedTxData.fee,
                    processedTxData.outputsToSpend.size(),processedTxData.valueOfUnspentOutputs,changeAddress,processedTxData.change);
        } catch (ValidationException ve) {
            cancelTrx();
            PublicPun.showNoticeDialog(mContext, "Unable to send:", ve.getMessage());
        } catch (IOException e) {
            cancelTrx();
            PublicPun.showNoticeDialog(mContext, "Unable to send:", e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            cancelTrx();
            PublicPun.showNoticeDialog(mContext, "Unable to send:", e.getMessage());
        } catch (Exception e) {
            cancelTrx();
            PublicPun.showNoticeDialog(mContext, "Unable to send:", e.getMessage());
            Crashlytics.log(e.getCause().toString() + "\n" + e.getMessage());
            LogUtil.e("錯誤="+e.getMessage());
        }

           if(mTxsConfirm==null){
               return;
           }

        // Ask if ready to send.
        new TransactionConfirmDialog(mContext,mTxsConfirm, new TransactionConfirmCallback() {
            @Override
            public void TransactionConfirm() {
                mProgress.show();
                TimeOutCheck();
                prepareTransaction(outputAddress,changeAddress,amountToSend);
            }

            @Override
            public void TransactionCancel() {
                cancelTrx();
            }
        }).show();
    }

    private void prepareTransaction(final String outputAddress, String changeAddress, long amountToSend) {

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

        try {
            //計算出了手續費和找零後的 UnspentInputList未花费的交易,input=要拿來發送$的地址
            signedInputs = new Transaction.Input[processedTxData.outputsToSpend.size()];//
            LogUtil.i("要拿來發送$的地址length:" + signedInputs.length);

            inputAddressList = new ArrayList<>();

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
                byte[] tempHash = null;
                try {
                    tempHash = Transaction.Script.hashTransactionForSigning(spendTxToSign);
                } catch (ValidationException e) {
                    e.printStackTrace();
                    LogUtil.e(e.getMessage());
                }

                final byte[] hash = tempHash;
                final int finalI = i;
                LogUtil.i("inputId=" + String.valueOf(finalI));
                dbAddress d = DatabaseHelper.querySendAddress(mContext, processedTxData.outputsToSpend.get(i).getAddress());
                if(d==null){
                    cancelTrx();
                    PublicPun.showNoticeDialog(mContext,getString(R.string.send_notification_unable_to_send),"Can't find Unspent addresses.");
                }

                final int dbKid = d.getKid();
                final int dbKcid = d.getKcid();
                final long dbBalance = d.getBalance();
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
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }
                        });
            }
        } catch (Exception e) {
            PublicPun.showNoticeDialog(mContext, "Notification", "Unable to Send");
            cancelTrx();
            Crashlytics.logException(e);
        }
    }

    private void TimeOutCheck() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isTrxSuccess) {
                    if (mProgress.isShowing()) {
                        mProgress.dismiss();
                    }
                    cancelTrx();
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            // do your work right here
                            PublicPun.showNoticeDialog(mContext, getString(R.string.send_notification_str_failed_title), getString(R.string.send_notification_str_failed_msg));
                        }
                    });
                }
            }
        }, 60000);////60s沒成功就自動cacel
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


    private void doTrxSign() {
        try {
            doTrxSignSuccessCnt = 0;
            scriptSigs = new ArrayList();
            for (int i = 0; i < signedInputs.length; i++) {
                LogUtil.i("input :addr 第" + i + "筆 " + inputAddressList.get(i).getAddress() + ";publickey =" + LogUtil.byte2HexString(inputAddressList.get(i).getPublickey()));
                doTrxSign(i, inputAddressList.get(i).getPublickey());
            }
        } catch (Exception e) {
            PublicPun.showNoticeDialog(mContext, "Notification", "doTrxSign  failed");
            cancelTrx();
            Crashlytics.logException(e);
        }
    }

    //the card response of waiting button_up is:01 06 54 52 58 2d 4f 4b ( fix value: TRX-OK )
    final byte[] successBtnPressesData = {0x54, 0x52, 0x58, 0x2d};

    private void didPrepareTransaction(String outputAddress) {

        if (!InitialSecuritySettingActivity.settingOptions[0] && InitialSecuritySettingActivity.settingOptions[1]) {
            didGetButton();
        }
//        cmdManager.getCmdProcessor().setButtonA006(); taptap otp
        cmdManager.cwCmdTrxBegin(PublicPun.user.getEncKey(),
                processedTxData.amountForRecipient,
                outputAddress,
                new CmdResultCallback() {
                    @Override
                    public void onSuccess(int status, byte[] outputData) {
                        if (!InitialSecuritySettingActivity.settingOptions[0] && InitialSecuritySettingActivity.settingOptions[1]) {
                            if (outputData != null) {
                                if (Arrays.equals(outputData, successBtnPressesData)) {

                                    trxStatus = Constant.TrxStatusGetBtn;
                                    LogUtil.i("case1.button_up pressed!!!!!!");
                                    if (btnTxBuilder.isShowing()) btnTxBuilder.dismiss();
                                    btnTxBuilder.dismiss();
                                    mProgress.setMessage("Sending Bitcoin...");
                                    mProgress.show();
                                    doTrxSign();
                                }
                            }
                        } else {
                            if ((status + 65536) == 0x9000) {
                                if (outputData != null) {
                                    LogUtil.i("cwCmdTrxBegin 成功!!");
                                    if (InitialSecuritySettingActivity.settingOptions[0]) {
                                        // 交易要otp
                                        didVerifyOtp();
                                    }
                                }
                            } else {
                                PublicPun.showNoticeDialog(mContext, "Notification", "CmdTrxBegin error:" + LogUtil.byte2HexString(outputData));
                            }
                        }
                    }
                });
    }

    private void cancelTrx() {

        FunTrxFinish();
        if (mProgress.isShowing()) {
            mProgress.dismiss();
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void didGetButton() {

        LogUtil.i("交易要button");
        trxStatus = Constant.TrxStatusWaitBtn;
        //發送一個給a006讀取特殊狀態的cmd
        cmdManager.getCmdProcessor().setButtonA006(true);
        if (mProgress != null) {
            mProgress.dismiss();
        }
        ClickFunction(mContext, "Send", "Please press Button On the Card");
    }

    int doTrxSignSuccessCnt;


    private void didVerifyOtp() {
//        verify OTP
        if (trxStatus == Constant.TrxStatusWaitOtp) {
            trxStatus = Constant.TrxStatusGetOtp;
        }
        AlertDialog.Builder otp_dialog = new AlertDialog.Builder(getActivity());
        final View item = LayoutInflater.from(getActivity()).inflate(R.layout.alert_dialog_otp_input, null);
        otp_dialog.setCancelable(false);
        otp_dialog.setView(item)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = (EditText) item.findViewById(R.id.alert_editotp);
                        otpCode = editText.getText().toString().trim();
                        LogUtil.i("otp press ok:" + editText.getText().toString());

                        if (InitialSecuritySettingActivity.settingOptions[1]) {
                            //發送一個給a006讀取特殊狀態的cmd
                            didGetButton();
                        }

                        cmdManager.trxVerifyOtp(otpCode, new CmdResultCallback() {
                            @Override
                            public void onSuccess(int status, byte[] outputData) {
                                if (InitialSecuritySettingActivity.settingOptions[1]) {
                                    if (outputData != null) {
                                        if (Arrays.equals(outputData, successBtnPressesData)) {
                                            //otp+button_up verify
                                            if (btnTxBuilder.isShowing()) btnTxBuilder.dismiss();
                                            mProgress.setMessage("Sending Bitcoin...");
                                            mProgress.show();
                                            LogUtil.i("case3. otp+button_up verify!");
                                            doTrxSign();
                                        }
                                    }
                                } else {
                                    if ((status + 65536) == 0x9000) {
                                        //otp verify only
                                        LogUtil.i("case2. otp verify only!!!!!!");
                                        //有幾筆input就sign幾次
                                        doTrxSign();

                                    } else {
                                        LogUtil.i("otp failed status=" + String.valueOf(status + 65536));
                                        cancelTrx();
                                        PublicPun.showNoticeDialog(mContext, "Notification", "OTP verify failed");
                                    }
                                }
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.strCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = (EditText) item.findViewById(R.id.alert_editotp);
                        dialog.dismiss();
                        cancelTrx();
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
                        }
                    }
                });
    }

    private void FunTrxFinish() {
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
            LogUtil.i("outputs[" + String.valueOf(i) + "].script=" + outputs[i].script.toString());
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
            int scriptLen = tx.getTxoutList().get(i).getAdd().length() / 2;//兩個byte一組hex
            LogUtil.i("Out-scriptLen=" + String.valueOf(scriptLen) + "Out-scriptLen hex=" + PublicPun.algorismToHEXString(scriptLen, 2) + "\n");
            sb.append(PublicPun.algorismToHEXString(scriptLen, 2));
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
        byte[] mScriptSig = null;
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
        } catch (Exception e) {
//            throw new VerificationException("Could not decode DER", e);
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
//                    final String recvAddress = editSendAddress.getText().toString().trim();
//                    final String amountStr = editSendBtc.getText().toString().trim();

                    FunTrxFinish();
                    isTrxSuccess = true;
                    if (mTimer != null) {
                        mTimer.cancel();
                        mTimer = null;
                    }
                    PublicPun.showNoticeDialog(mContext, "Sent", "Sent " + spendAmountStr + "btc to " + recvAddress);
                    //clear trx data
                    editSendAddress.setText("");
                    editSendBtc.setText("");
                    editSendUsd.setText("");

                    if (mProgress != null) {
                        mProgress.dismiss();
                    }

                    break;
                case HANDLER_SEND_BTC_ERROR:
                    if (mProgress != null) {
                        mProgress.dismiss();
                    }
                    PublicPun.showNoticeDialog(mContext, "Notification", "Send BTC to BLOCKCHAIN failed!");
                    break;
            }
        }
    };

    public void onQRcodeResult() {
        try {
            String scanContent = "";
            if (FragMainActivity.scanningResult != null) {
                scanContent = FragMainActivity.scanningResult.getContents();
            }
//            String scanFormat = FragMainActivity.scanningResult.getFormatName();
            LogUtil.i("QR code scanContent=" + scanContent);
            //sample qrCode: bitcoin:12Y7Rqmt3xEpG1jYgtXwhSo3EUKXkbVNrZ?amount=1
            String scanAddress = "";
            String scanAmount = "";

            if (scanContent != null) {
                if (scanContent.indexOf("=") == -1) {
                    scanAddress = scanContent.substring(scanContent.indexOf(":") + 1);
                    scanAmount = "0";
                } else {
                    scanAddress = scanContent.substring(scanContent.indexOf(":") + 1, scanContent.indexOf("?"));
                    scanAmount = scanContent.substring(scanContent.indexOf("=") + 1).replace(",", ".");

                }
                double currAmt = Double.valueOf(scanAmount) * AppPrefrence.getCurrentRate(mContext);
                editSendAddress.setText(scanAddress);
                editSendBtc.setText(scanAmount);
                editSendUsd.setText(TabFragment.currentFormatter.format(currAmt));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.log(new String(PublicPun.hexStringToByteArray(PublicPun.card.getCardId())) + ":" + e.getMessage());
        }
    }

    private void SetSecpo(boolean setWatchDog) {

        InitialSecuritySettingActivity.settingOptions[0] = true;
        InitialSecuritySettingActivity.settingOptions[1] = false;
//        settingOptions[2] = switchAddress.isChecked();

        InitialSecuritySettingActivity.settingOptions[3] = setWatchDog;

        LogUtil.i("set安全設置:otp=" + InitialSecuritySettingActivity.settingOptions[0] + ";button_up=" + InitialSecuritySettingActivity.settingOptions[1] +
                ";address" + InitialSecuritySettingActivity.settingOptions[2] + ";dog=" + InitialSecuritySettingActivity.settingOptions[3]);

        cmdManager.setSecpo(InitialSecuritySettingActivity.settingOptions, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
//                    PublicPun.toast(mContext, "CW Security Policy Set (45)");
                }
            }
        });
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
    }

    // for Listener used only
    String strEditBtc;
    String strEditCurrency;


    private void setListener() {

        editSendBtc.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    strEditBtc = editSendBtc.getText().toString().trim().replace(",", ".");
                    if (!TextUtils.isEmpty(strEditBtc)) {
                        editBtc = Float.valueOf(strEditBtc);
                        if (editBtc > 0) {
                            changeRate(mContext, editSendUsd, editBtc, "#.##");
                        }
                    }
                }
            }
        });

        editSendUsd.setOnFocusChangeListener(
                new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {
                            strEditCurrency = editSendUsd.getText().toString().trim();
                            if (!TextUtils.isEmpty(strEditCurrency)) {
                                editBtc = Float.valueOf(strEditCurrency);
                                if (editBtc > 0) {
                                    changeRate(mContext, editSendBtc, editBtc, "#.########");
                                }
                            }
                        }
                    }
                }
        );

        editSendBtc.setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            strEditBtc = editSendBtc.getText().toString().trim().replace(",", ".");
                            ;
                            if (!TextUtils.isEmpty(strEditBtc)) {
                                editBtc = Float.valueOf(strEditBtc);
                                if (editBtc > 0) {
                                    changeRate(mContext, editSendUsd, editBtc, "#.##");
                                }
                            }
                        }
                        return false;
                    }
                }
        );

        editSendUsd.setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            strEditCurrency = editSendUsd.getText().toString().trim();
                            if (!TextUtils.isEmpty(strEditCurrency)) {
                                editBtc = Float.valueOf(strEditCurrency);
                                if (editBtc > 0) {
                                    changeRate(mContext, editSendBtc, editBtc, "#.########");
                                }
                            }
                        }
                        return false;
                    }
                }
        );
    }

    AlertDialog btnTxBuilder;
    AlertDialog.Builder builder;

    private void ClickFunction(Context mContext, String mTitle, String mMessage) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View alert_view = inflater.inflate(R.layout.edit_dialog, null);//alert為另外做給alert用的layout
        final EditText mEditText = (EditText) alert_view.findViewById(R.id.etInputLabel);
        final TextView mDialogTitle = (TextView) alert_view.findViewById(R.id.dialog_title);
        final TextView mDialogMessage = (TextView) alert_view.findViewById(R.id.dialog_message);
        mEditText.setVisibility(View.INVISIBLE);
        mDialogMessage.setText(mMessage);
        mDialogTitle.setText(mTitle);
        //-----------產生輸入視窗--------
//        builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
//        builder.setView(alert_view);
//        btnTxBuilder = builder.show();

        btnTxBuilder = new AlertDialog.Builder(mContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                .setView(alert_view)
                .show();
    }

    private void changeRate(Context context, EditText toEditText, float amount, String format) {
        float mChangeAmt = 0;
        DecimalFormat mFormatter = null;
        if (toEditText.getId() == editSendBtc.getId()) {
            mChangeAmt = (amount / AppPrefrence.getCurrentRate(context));
        } else {
            mChangeAmt = (amount * AppPrefrence.getCurrentRate(context));
        }
        mFormatter = new DecimalFormat(format);
        LogUtil.e("mChangeAmt : " + mChangeAmt);
        toEditText.setText(mFormatter.format(mChangeAmt));
    }

    public void refresh(int account, IntentResult scanningResult) {
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