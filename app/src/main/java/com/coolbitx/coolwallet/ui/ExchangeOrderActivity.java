package com.coolbitx.coolwallet.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.coolbitx.coolwallet.DataBase.DatabaseHelper;
import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.callback.APIResultCallback;
import com.coolbitx.coolwallet.entity.Address;
import com.coolbitx.coolwallet.entity.Constant;
import com.coolbitx.coolwallet.entity.ExchangeOrder;
import com.coolbitx.coolwallet.entity.Transaction;
import com.coolbitx.coolwallet.entity.UnSpentTxsBean;
import com.coolbitx.coolwallet.entity.dbAddress;
import com.coolbitx.coolwallet.general.BtcUrl;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.httpRequest.CwBtcNetWork;
import com.coolbitx.coolwallet.util.BTCUtils;
import com.coolbitx.coolwallet.util.Base58;
import com.coolbitx.coolwallet.util.ExchangeAPI;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.ByteUtil;
import com.snscity.egdwlib.utils.LogUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Created by ShihYi on 2015/12/25.
 */
public class ExchangeOrderActivity extends BaseActivity implements View.OnClickListener {

    private CmdManager cmdManager;
    private Context mContext;
    private ExchangeAPI mExchangeAPI;
    Button btnCompleteOrder;
    private ProgressDialog mProgress;
    TextView tvAddr;
    TextView tvAmount;
    TextView tvPrice;
    TextView tvOrderNum;
    TextView tvAccount;
    TextView tvExp;

    //for complete order
    private int trxStatus;
    int unspendNum;
    private ArrayList<dbAddress> lisCwBtcAdd = new ArrayList<dbAddress>();
    String InAddress = "";
    private List<UnSpentTxsBean> unSpentTxsBeanList;
    UnSpentTxsAsyncTask unSpentTxsAsyncTask;
    double BTCFee = 0.0001;
    double amount = 0;
    int orderAccount = -1;
    String recvAddress;
    String amountStr;
    BTCUtils.FeeChangeAndSelectedOutputs processedTxData;
    Transaction.Output[] outputs;
    Transaction.Input[] signedInputs;
    List<Address> inputAddressList;
    UnSpentTxsBean outputToSpend = new UnSpentTxsBean();
    CwBtcNetWork cwBtcNetWork;
    int errorCnt;
    ExchangeOrder xchsOrder;
    byte[] trxHandle;
    String orderID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_order);
        mContext = this;
        initToolbar();
        initViews();
        initValues();
        cwBtcNetWork = new CwBtcNetWork();
    }

    private void initViews() {
        btnCompleteOrder = (Button) findViewById(R.id.btn_complete_order);
        btnCompleteOrder.setOnClickListener(this);

        tvAddr = (TextView) findViewById(R.id.order_tvAddress);
        tvAmount = (TextView) findViewById(R.id.order_tvAnount);
        tvPrice = (TextView) findViewById(R.id.order_tvPrice);
        tvOrderNum = (TextView) findViewById(R.id.order_tvNum);
        tvAccount = (TextView) findViewById(R.id.order_tvAccount);
        tvExp = (TextView) findViewById(R.id.order_tvExpiration);

    }


    private void initValues() {

        xchsOrder = (ExchangeOrder) getIntent().getSerializableExtra("ExchangeOrder");

        orderID = xchsOrder.getOrderId();


        cmdManager = new CmdManager();
        mExchangeAPI = new ExchangeAPI(mContext, cmdManager);
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
        mProgress.setMessage("Completing the order...");

        tvAddr.setText(xchsOrder.getAddr());
        tvAmount.setText(String.valueOf(new DecimalFormat("#.########").format(xchsOrder.getAmount())) + " BTC");
        tvPrice.setText("$" + String.valueOf(xchsOrder.getPrice()));
        tvOrderNum.setText(xchsOrder.getOrderId());
        tvAccount.setText(String.valueOf(xchsOrder.getAccount()));
        tvExp.setText(xchsOrder.getExpiration());

        orderAccount = xchsOrder.getAccount();
        recvAddress = xchsOrder.getAddr();

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        toolbar.setTitle("Order details");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        // 打開 up button_up
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        finish();
        return null;
    }

    @Override
    public void onClick(View v) {
        LogUtil.d("before complete");
        if (v == btnCompleteOrder) {
            completeOrder();
        }
    }

    private Timer mTimer;

    private void completeOrder() {
        byte[] svrResp = null;
        trxHandle = new byte[4];

        // prepareTransaction
        // 1.getTrxInfo (需不需要exBlockInfo)
        // 2.TrxSignLogin
        // 3.prepareTransaction
        // 4.
        final int inputId = 0;

        mExchangeAPI.doExGetTrxInfo(xchsOrder.getOrderId(), new APIResultCallback() {
            @Override
            public void success(String[] msg) {
                LogUtil.d("doExGetTrxInfo ok " + msg[0]);

                //XCHS Transaction Prepare
                cmdManager.XchsTrxsignLogin(PublicPun.hexStringToByteArray(msg[0]), new CmdResultCallback() {
                    @Override
                    public void onSuccess(int status, byte[] outputData) {
                        if ((status + 65536) == 0x9000) {
                            trxHandle = outputData;
                            LogUtil.d("XchsTrxsignLogin trxHandle=" + PublicPun.byte2HexString(trxHandle));

                            PrepareUnsignedToXchs();


                        } else {
                            //for debug error code
                            cmdManager.getError(new CmdResultCallback() {
                                @Override
                                public void onSuccess(int status, byte[] outputData) {
//                                    LogUtil.d("Login failed = " + Integer.toHexString(status));
                                }
                            });

                            AlertDialog.Builder mBuilder =
                                    PublicPun.CustomNoticeDialog(mContext, "Unable to login Exchange  Transaction", "Error:" + Integer.toHexString(status));
                            mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    finish();
                                }
                            }).show();

                        }
                    }
                });
            }

            @Override
            public void fail(String msg) {

                LogUtil.d("doExGetTrxInfo failed:" + msg);
                //exchangeSite Logout()
            }
        });
    }

    private void composePrepareBlocks(byte[] trxHandle, int accountId, int keyChainId,
                                      int keyId, String out1Address, String out2Address,
                                      byte[] signatureMaterial) {


        //[trxHandle][ACCID] [KCID] [KID][OUT1ADDR] [OUT2ADDR][SIGMTRL]

        LogUtil.d("trxHandle=" + LogUtil.byte2HexString(trxHandle));
        LogUtil.d("accountId=" + accountId);
        LogUtil.d("keyChainId =" + keyChainId);
        LogUtil.d("keyId =" + keyId);
        LogUtil.d("out1Address =" + out1Address);
        LogUtil.d("out2Address =" + out2Address);
        LogUtil.d("signatureMaterial=" + LogUtil.byte2HexString(signatureMaterial));

        int length = signatureMaterial.length;
        byte[] accBytes = ByteUtil.intToByteLittle(accountId, 4);//4 bytes, little-endian
        byte[] kcId = ByteUtil.intToByteBig(keyChainId, 1);
        byte[] keyBytes = ByteUtil.intToByteLittle(keyId, 4);//4 bytes, little-endian
        byte[] out1Addr = out1Address.getBytes();
        byte[] out2Addr = out2Address.getBytes();

        byte[] amount_bn = ByteUtil.intToByteBig((int) amount, 8);
        LogUtil.i("amount=" + LogUtil.byte2HexString(amount_bn));

        //:  formate=00 00 00 00 00 00 27 10 byte,little =10 27 00 00 00 00 00 00 orige=00 00 27 10 00 00 00 00

        byte[] inputData = new byte[4 + 1 + 4 + 25 + 25 + length];

        for (int i = 0; i < 4; i++) {
            inputData[i] = accBytes[i];
        }

        for (int i = 0; i < 1; i++) {
            inputData[i + 1] = kcId[i];
        }

        for (int i = 0; i < 4; i++) {
            inputData[i + 1 + 4] = keyBytes[i];
        }
        for (int i = 0; i < 25; i++) {
            inputData[i + 4 + 1 + 4] = out1Addr[i];
        }

        for (int i = 0; i < 25; i++) {
            inputData[i + 4 + 1 + 4] = out2Addr[i];
        }
        for (int i = 0; i < length; i++) {
            inputData[i + 4 + 1 + 4 + 25 + 25] = signatureMaterial[i];
        }

        LogUtil.d(LogUtil.byte2HexStringNoBlank(inputData));
//        IN: {"blks":[{"idx":1,"blk":"11223344556677881122334455667788"},{"idx":2,"blk":"11223344556677881122334455667788"}]}


    }

    private void doExGetTrxPrepareBlocks(String postData) {
        mExchangeAPI.doExGetTrxPrepareBlocks(postData, orderID, new APIResultCallback() {
            @Override
            public void success(String[] msg) {
                LogUtil.d("doExGetTrxPrepareBlocks ok " + msg[0]);

            }

            @Override
            public void fail(String msg) {
                LogUtil.d("doExGetTrxPrepareBlocks failed:" + msg);
                //logout
            }
        });
    }

    private void PrepareUnsignedToXchs() {
        // 1.In id
        // 2.amount
        // 3.kcid
        // 4.kid
        // 5.out1 addr
        // 6.out2 addr
        // 7.sigmtrl

        unspendNum = 0;
        amount = 0;

        int accountID = xchsOrder.getAccount();
        amount = xchsOrder.getAmount();

        LogUtil.i("run PrepareUnsignedToXchs");
        lisCwBtcAdd = DatabaseHelper.queryAddress(this, accountID, -1);

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


            super.onPostExecute(UnSpentTxsBeans);
        }
    }

    //    //產生收零地址(of output)
    public void genChangeAddress(final int keyChainId) {

        amountStr = String.valueOf(amount);

        final int accountId = 0;//orderAccount
        ArrayList<dbAddress> lisCwBtcAdd = new ArrayList<dbAddress>();
        // find only internal addr

        lisCwBtcAdd = DatabaseHelper.queryAddress(mContext, orderAccount, 1);
        LogUtil.d("genChangeAddress account=" + orderAccount + ";筆數=" + lisCwBtcAdd.size());

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
                    amountStr + ";轉型=" + BTCUtils.convertToSatoshisValueForDIsplay(amountStr));

//            prepareTransaction(recvAddress, addressStr, BTCUtils.convertToSatoshisValue(amountStr));
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
//                            prepareTransaction(recvAddress, addressStr, BTCUtils.convertToSatoshisValue(amountStr));
                        }
                    }
                }
            });
        }
    }

    //    100,000,000 Satoshi = 1.00000000 ฿
    private final long SATOSHIS_PER_COIN = 100000000;

//    private void prepareTransaction(final String outputAddress, final String changeAddress, long amountToSend) {
//        trxStatus = Constant.TrxStatusBegin;
//        long availableAmount = 0;
//        for (UnSpentTxsBean unSpentTxsBean : unSpentTxsBeanList) {
//            availableAmount += BTCUtils.convertToSatoshisValue(TabFragment.BtcFormatter.format(unSpentTxsBean.getAmount()));
//        }
//
//        long extraFee = BTCUtils.parseValue("0.0");
////        long extraFee = 0;
//        LogUtil.e("帳戶 " + orderAccount + " 地址下有的餘額=" + availableAmount);
//        LogUtil.e("帳戶 " + orderAccount + " 要發出的金額=" + amountToSend);
//
////        final BTCUtils.FeeChangeAndSelectedOutputs processedTxData =
//        try {
//            processedTxData =
//                    BTCUtils.calcFeeChangeAndSelectOutputsToSpend(unSpentTxsBeanList, amountToSend, extraFee, false);
//        }catch(ValidationException ve){
////            cancelTrx(false);
//            PublicPun.showNoticeDialog(mContext, "Unable to send", ve.getMessage());
//        }
//        if (processedTxData.change == 0) {
//            LogUtil.i("發送不用找零, 發送金額=" +
//                    processedTxData.amountForRecipient + "; 發送地址=" +
//                    outputAddress);
//            outputs = new Transaction.Output[]{
//                    new Transaction.Output(processedTxData.amountForRecipient, Transaction.Script.buildOutput(outputAddress)),
//            };
//        } else {
//            if (outputAddress.equals(changeAddress)) {
//                PublicPun.showNoticeDialog(mContext, "Notification", "Change address equals to recipient's address, it is likely an error.");
//            }
//
//            LogUtil.i("發送要找零=" + processedTxData.change + "; 發送金額=" +
//                    processedTxData.amountForRecipient + "; 發送地址=" +
//                    outputAddress + "; 找零地址=" + changeAddress);
//
//            //the outputs of transation
//            outputs = new Transaction.Output[]{
//                    new Transaction.Output(processedTxData.amountForRecipient, Transaction.Script.buildOutput(outputAddress)),
//                    new Transaction.Output(processedTxData.change, Transaction.Script.buildOutput(changeAddress)),
//            };
//        }
//
//        //close transaction if exists
//        cmdManager.trxFinish(new CmdResultCallback() {
//            @Override
//            public void onSuccess(int status, byte[] outputData) {
//                if ((status + 65536) == 0x9000) {
//                    if (trxStatus == Constant.TrxStatusFinish) {
//                        LogUtil.i("trxFinish 成功!!");
////                            didSignTransaction();
//                    }
//                    trxStatus = Constant.TrxStatusBegin;
//                    LogUtil.i("trxStatus=" + trxStatus);
//                }
//            }
//        });
//
//
//        //計算出了手續費和找零後的 UnspentInputList未花费的交易,input=要拿來發送$的地址
//        signedInputs = new Transaction.Input[processedTxData.outputsToSpend.size()];//
//        LogUtil.i("要拿來發送$的地址length:" + signedInputs.length);
//
//        if (inputAddressList == null) {
//            inputAddressList = new ArrayList<>();
//        }
//        final boolean[] prepRsult = new boolean[signedInputs.length];
//
//        for (int i = 0; i < signedInputs.length; i++) {
//            Transaction.Input[] unsignedInputs = new Transaction.Input[signedInputs.length];
//            for (int j = 0; j < unsignedInputs.length; j++) { //有幾個input
//                outputToSpend = processedTxData.outputsToSpend.get(j);
//                //dora modify
//                byte[] byteTx = PublicPun.hexStringToByteArray(outputToSpend.getTx());//outputToSpend.getTx().getBytes()
//                //my send addr
//                LogUtil.i(outputToSpend.getAddress() + "的HEX=" + LogUtil.byte2HexString((byteTx)));
//
//                Transaction.OutPoint outPoint = new Transaction.OutPoint(byteTx, outputToSpend.getN());//outputToSpend.getN
//
//                byte[] mScripts = PublicPun.hexStringToByteArray(outputToSpend.getScript());//outputToSpend.getScript().getBytes()
//                LogUtil.i("第" + j + "個input=" + " tx:" + outputToSpend.getTx() + " ; " + outputToSpend.getN() + "; Script:" + LogUtil.byte2HexString((mScripts)));
//
//                if (j == i) {
//                    //this input we are going to sign (remove the part of sig and filled in the Scripts)
//                    unsignedInputs[j] = new Transaction.Input(outPoint,
//                            //dora modify
//                            new Transaction.Script(mScripts),
//                            0xffffffff);
//                } else {
//                    unsignedInputs[j] = new Transaction.Input(outPoint, null, 0xffffffff);
//                }
//            }
//            Transaction spendTxToSign = new Transaction(unsignedInputs, outputs, 0);
//            final byte[] hash = Transaction.Script.hashTransactionForSigning(spendTxToSign);
//
//            final int finalI = i;
//            LogUtil.i("inputId=" + String.valueOf(finalI));
//            dbAddress d = DatabaseHelper.querySendAddress(mContext, processedTxData.outputsToSpend.get(i).getAddress());
//            final int dbKid = d.getKid();
//            final int dbKcid = d.getKcid();
//            final long dbBalance = BTCUtils.convertToSatoshisValue(String.valueOf(processedTxData.outputsToSpend.get(i).getAmount()));
//            final byte CwAddressKeyChainExternal;
//            if (dbKcid == 0) {
//                CwAddressKeyChainExternal = 0x00;
//            } else {
//                CwAddressKeyChainExternal = 0x01;
//            }
//
//            final byte[] mPublicKey = new byte[32];
//            // 1.QUERY KEY 2.PREP TRX 3. TRX BEGIN
//            cmdManager.hdwQueryAccountKeyInfo(Constant.CwHdwAccountKeyInfoPubKey,
//                    dbKcid,
//                    orderAccount,
//                    dbKid,
//                    new CmdResultCallback() {
//                        @Override
//                        public void onSuccess(int status, byte[] outputData) {
//                            if ((status + 65536) == 0x9000) {
//                                if (outputData != null) {
//                                    byte[] publicKeyBytes = new byte[64];
//                                    int length = outputData.length;
//
//                                    if (length >= 64) {
//                                        for (int i = 0; i < 64; i++) {
//                                            publicKeyBytes[i] = outputData[i];
//                                            if (i < 32) {
//                                                mPublicKey[i] = outputData[i];
//                                            }
//                                        }
//                                        LogUtil.i("hdwQueryAccountKeyInfo publicKey =" + LogUtil.byte2HexString(outputData));
//                                    }
//
//                                    final Address address = new Address();
//                                    address.setAccountId(orderAccount);
//                                    address.setKeyChainId(dbKcid);
//                                    address.setAddress(processedTxData.outputsToSpend.get(finalI).getAddress()); //account 0 的 internal地址之一
//                                    address.setKeyId(dbKid);
//                                    address.setPublickey(publicKeyBytes);
//                                    LogUtil.i("run " + finalI + ": getAddressInfo=" + processedTxData.outputsToSpend.get(finalI).getAddress() +
//                                            " ;publicKey=" + LogUtil.byte2HexString(address.getPublickey()));
//                                    inputAddressList.add(address);
//
//                                }
//                                //丟api prepare
////                                doExGetTrxPrepareBlocks(trxHandle,finalI,
////                                        changeAddress,
////                                        CwAddressKeyChainExternal,
////                                        orderAccount,
////                                        dbKid,
////                                        dbBalance,
////                                        hash);
//
////                                [trxHandle][ACCID] [KCID] [KID][OUT1ADDR] [OUT2ADDR][SIGMTRL]
//                                composePrepareBlocks(trxHandle, orderAccount, CwAddressKeyChainExternal,
//                                        dbKid, xchsOrder.getAddr(), changeAddress, hash);
//
//                            } else {
//                                PublicPun.showNoticeDialog(mContext, "Unable to sent", "Get PublicKey error:" + LogUtil.byte2HexString(outputData));
//                            }
//                        }
//                    });
//        }
//
//        String postdata ="{\"blks\":[{\"idx\":1,\"blk\":\"000000000007000000314a76765956314a536b4e324c355a436b3255736362397678000000000000000000000000000000000000000000000000005c50460e7f92b6b4b791ff04f232246f6d551d044d570493850ba01f004edf3f\"},"+
//                "{\"idx\":2,\"blk\":\"000000000027000000314a76765956314a536b4e324c355a436b325573636239767800000000000000000000000000000000000000000000000000326d62f1089deac784ba6c4ca1343bc2256a36d84cf4471f590dca4fa56cb49a\"}]}";
//        doExGetTrxPrepareBlocks(postdata);
//    }

    public List<UnSpentTxsBean> getUnspentTxsByAddr(String mAddr) {

        List<UnSpentTxsBean> UnSpentTxsBeanList = null;
        ContentValues cv = new ContentValues();
        cv.put("addresses", mAddr);
        LogUtil.d("addressesXX=" + cv.getAsString("addresses"));
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
                UnSpentTxsBeanList = PublicPun.jsonParserUnspent(result);
                errorCnt = 0;
            }
        }
        return UnSpentTxsBeanList;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

    }
//    byte[] cancelBlkInfo = new byte[72];
//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//        final View item = LayoutInflater.from(mContext).inflate(R.layout.alert_dialog_otp_input, null);
//        AlertDialog.Builder otp_dialog = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT);
//        otp_dialog.setView(item);
//        otp_dialog.setCancelable(false);
//        otp_dialog.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
////                cancelTrx(truP e);
//                //queryAccountInfo
//
//                mExchangeAPI.getExUnBlock(FragMainActivity.orderId, new APIResultCallback() {
//                    @Override
//                    public void success(String[] msg) {
//
////                        {"orderId":"15847930","okToken":"abc123","unblockTkn":"abc123",
////                                "mac":"dbe57d18f1c176606f40361a11c755ed655804a319d7b7120cdb1e729786d5dd"}
//                        cancelBlkInfo = PublicPun.hexStringToByteArray(msg[0] + msg[1] + msg[2] + msg[3] + msg[4]);
//
//                        cmdManager.XchsCancelBlock(cancelBlkInfo, new CmdResultCallback() {
//                            @Override
//                            public void onSuccess(int status, byte[] outputData) {
//
//                                if ((status + 65536) == 0x9000) {//-28672//36864
//                                    LogUtil.d("XchsCancelBlock  success = " + PublicPun.byte2HexString(outputData));
//                                    cancelBlkInfo = outputData;
//
//
//                                } else {
//                                    LogUtil.d("XchsCancelBlock fail");
//                                    //for debug error code
//                                    cmdManager.getError(new CmdResultCallback() {
//                                        @Override
//                                        public void onSuccess(int status, byte[] outputData) {
//                                            LogUtil.d("Login failed = "+(status + 65536) +";" + outputData);
//                                        }
//                                    });
//                                }
//
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void fail(String msg) {
//                        LogUtil.d("getExRequestOrderBlock failed:" + msg);
//                        //exchangeSite Logout()
//
//                    }
//                });
//
//            }
//        });
//        otp_dialog.show();
//    }


}
