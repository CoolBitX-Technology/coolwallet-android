package com.coolbitx.coolwallet.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coolbitx.coolwallet.DataBase.DatabaseHelper;
import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.bean.APITx;
import com.coolbitx.coolwallet.bean.Address;
import com.coolbitx.coolwallet.bean.Constant;
import com.coolbitx.coolwallet.bean.ExchangeOrder;
import com.coolbitx.coolwallet.bean.Transaction;
import com.coolbitx.coolwallet.bean.TrxBlks;
import com.coolbitx.coolwallet.bean.TxsConfirm;
import com.coolbitx.coolwallet.bean.UnSpentTxsBean;
import com.coolbitx.coolwallet.bean.dbAddress;
import com.coolbitx.coolwallet.callback.APIResultCallback;
import com.coolbitx.coolwallet.callback.GetExchangeAddrCallback;
import com.coolbitx.coolwallet.callback.TransactionConfirmCallback;
import com.coolbitx.coolwallet.general.AppPrefrence;
import com.coolbitx.coolwallet.general.BtcUrl;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.httpRequest.CwBtcNetWork;
import com.coolbitx.coolwallet.util.BTCUtils;
import com.coolbitx.coolwallet.util.Base58;
import com.coolbitx.coolwallet.util.ECKey;
import com.coolbitx.coolwallet.util.ExchangeAPI;
import com.coolbitx.coolwallet.util.ValidationException;
import com.crashlytics.android.Crashlytics;
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
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static com.coolbitx.coolwallet.general.PublicPun.HANDLER_SEND_BTC_ERROR;
import static com.coolbitx.coolwallet.general.PublicPun.HANDLER_SEND_BTC_FINISH;

/**
 * Created by ShihYi on 2015/12/25.
 */
public class ExchangeCompleteOrderActivity extends BaseActivity implements View.OnClickListener {

    private CmdManager cmdManager;
    private Context mContext;
    private ExchangeAPI mExchangeAPI;
    Button btnCompleteOrder, btnCancelOrder, btnSubmittedOk;
    private ProgressDialog mProgress;
    TextView tvAddr;
    TextView tvAmount;
    TextView tvPrice;
    TextView tvOrderNum;
    TextView tvAccount;
    TextView tvExp;

    //for complete order
    int unspendNum;
    private ArrayList<dbAddress> lisCwBtcAdd = new ArrayList<dbAddress>();
    String InAddress;
    private List<UnSpentTxsBean> unSpentTxsBeanList;
    UnSpentTxsAsyncTask unSpentTxsAsyncTask;
    double BTCFee = 0.0001;
    double amount = 0;
    int orderAccount = -1;
    String recvAddress;
    BTCUtils.FeeChangeAndSelectedOutputs processedTxData;
    Transaction.Output[] outputs;
    UnSpentTxsBean outputToSpend = new UnSpentTxsBean();
    CwBtcNetWork cwBtcNetWork;
    int errorCnt;
    ExchangeOrder xchsOrder;
    byte[] trxHandle;
    String orderID = "";
    boolean isTrxSuccess;
    AlertDialog btnTxBuilder;
    final byte[] successBtnPressesData = {0x54, 0x52, 0x58, 0x2d};
    int doTrxSignSuccessCnt;
    List<byte[]> scriptSigs;
    String currUnsignedTx;
    private String otpCode;
    byte[] nonce;
    private boolean isBlockr;
    private String UrlUnspent;
    TransactionConfirmDialog trxDialog;
    TxsConfirm mTxsConfirm;

    LinearLayout linSbmitted, linUnsbmitted;


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

    @Override
    protected void onResume() {
        super.onResume();
        if (cmdManager == null) {
            cmdManager = new CmdManager();
        }
        if (cwBtcNetWork == null) {
            cwBtcNetWork = new CwBtcNetWork();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStart() {
        super.onStart();
        //註冊監聽
//        registerBroadcast(this, cmdManager);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        unRegisterBroadcast(this);
    }

    private void initViews() {
        btnCompleteOrder = (Button) findViewById(R.id.btn_complete_order);
        btnCompleteOrder.setOnClickListener(this);
        btnCancelOrder = (Button) findViewById(R.id.btn_cancel_order);
        btnCancelOrder.setOnClickListener(this);
        tvAddr = (TextView) findViewById(R.id.order_tvAddress);
        tvAmount = (TextView) findViewById(R.id.order_tvAnount);
        tvPrice = (TextView) findViewById(R.id.order_tvPrice);
        tvOrderNum = (TextView) findViewById(R.id.order_tvNum);
        tvAccount = (TextView) findViewById(R.id.order_tvAccount);
        tvExp = (TextView) findViewById(R.id.order_tvExpiration);
        btnSubmittedOk = (Button) findViewById(R.id.btn_submitted_ok);
        btnSubmittedOk.setOnClickListener(this);
        linSbmitted = (LinearLayout) findViewById(R.id.lin_sbmitted);
        linUnsbmitted = (LinearLayout) findViewById(R.id.lin_unsbmitted);
    }


    private void initValues() {

        xchsOrder = (ExchangeOrder) getIntent().getSerializableExtra("ExchangeOrder");

        orderID = xchsOrder.getOrderId();
        cmdManager = new CmdManager();
        mExchangeAPI = new ExchangeAPI(mContext, cmdManager);
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
        mProgress.setMessage(getString(R.string.preparing_to_complete_order));

        tvAddr.setText(xchsOrder.getAddr());
        tvAmount.setText(new DecimalFormat("#.########").format(xchsOrder.getAmount()) + " BTC");
        tvPrice.setText("$" + String.valueOf(xchsOrder.getPrice()));
        tvOrderNum.setText(xchsOrder.getOrderId());
        tvAccount.setText(String.valueOf(xchsOrder.getAccount() + 1));//顯示用account需要加1
        tvExp.setText(xchsOrder.getExpiration());

        if (xchsOrder.getType().equals("buy")) {
            btnCompleteOrder.setVisibility(View.INVISIBLE);
        }

        if (xchsOrder.isSubmitted()) {
            linSbmitted.setVisibility(View.VISIBLE);
            linUnsbmitted.setVisibility(View.INVISIBLE);
        } else {
            linUnsbmitted.setVisibility(View.VISIBLE);
            linSbmitted.setVisibility(View.INVISIBLE);
        }


        orderAccount = xchsOrder.getAccount();
        recvAddress = xchsOrder.getAddr();

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        toolbar.setTitle(getString(R.string.order_details));
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
        isBlockr = AppPrefrence.getIsBlockrApi(mContext);
        if (v == btnCompleteOrder) {

            getSecpo();
//            completeOrder();
            int infoid = 1;

            cmdManager.XchsGetOtp(infoid, new CmdResultCallback() {
                @Override
                public void onSuccess(int status, byte[] outputData) {
                    if ((status + 65536) == 0x9000) {
                        BlockVerifyOtp();
                        //16進制的9000在10進制是36864;6645是26181;status=91717
                    } else if ((status + 65536) == 0x16645) {
                        PublicPun.showNoticeDialog(mContext, getString(R.string.unable_to_generate_otp), getString(R.string.plz_try_again));
                    } else {
//                    LogUtil.i("status="+String.valueOf(status));
                        PublicPun.showNoticeDialog(mContext, getString(R.string.unable_to_generate_otp), getString(R.string.error) + ":" + Integer.toHexString(status));
                    }
                }
            });
        } else if (v == btnCancelOrder || v == btnSubmittedOk) {
//            CancelTrx();
            finish();
        }
    }

//    private void CancelTrx() {
//
//        mExchangeAPI.cancelTrx(orderID, new APIResultCallback() {
//            @Override
//            public void success(String[] msg) {
//                LogUtil.e("cancel trx ok:" + msg[0]);
//                // String.format(getResources().getString(R.string.str_estimated_fee_content),
//
//            }
//
//            @Override
//            public void fail(String msg) {
//                PublicPun.showNoticeDialog(mContext, getString(R.string.str_unable_cancel), getString(R.string.str_reason));
//            }
//        });
//    }

    private void clickToFinish(String title, String msg) {
        AlertDialog.Builder mBuilder =
                PublicPun.CustomNoticeDialog(mContext, title, msg);
        mBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        }).show();
    }


    byte[] okTkn = new byte[4];
    byte[] unblockTkn = new byte[16];

    private void BlockVerifyOtp() {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.alert_dialog_otp_input, null);
        AlertDialog.Builder otp_dialog = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT);
        final EditText editText = (EditText) view.findViewById(R.id.alert_editotp);
        otp_dialog.setView(view);
        otp_dialog.setCancelable(false);
//        verify OTP
        otp_dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mProgress.setMessage(getString(R.string.begin_to_block_order) + "...");
                mProgress.show();
                //1.transit to server

                String blockOtp = editText.getText().toString();
                LogUtil.d("block orderID=" + orderID + ";blockOtp=" + blockOtp);


                mExchangeAPI.getTrxBlock(orderID, blockOtp, new APIResultCallback() {
                    @Override
                    public void success(String[] msg) {
                        LogUtil.d("getTrxBlock ok " + msg[0]);//orderId(4B)/accId(4B)/amount(8B)/mac1(32B)/nonce(16B)

                        // 2.SE cmd (F9)
                        byte[] srvOrderBlock = PublicPun.hexStringToByteArray(msg[0]);
                        cmdManager.XchsBlockBtc(srvOrderBlock, new CmdResultCallback() {
                            @Override
                            public void onSuccess(int status, byte[] outputData) {

                                if ((status + 65536) == 0x9000) {//-28672//36864
                                    LogUtil.d("XchsBlockBtc  success = " + PublicPun.byte2HexStringNoBlank(outputData));

                                    //blockSignature(32B)/okTkn(4B)/encUblkTkn(16B)/mac2(32B)/nonce(16B)

                                    System.arraycopy(outputData, 32, okTkn, 0, 4);
                                    System.arraycopy(outputData, 36, unblockTkn, 0, 16);

                                    cmdManager.XchsBlockInfo(okTkn, new CmdResultCallback() {
                                        @Override
                                        public void onSuccess(int status, byte[] outputData) {
                                            LogUtil.d("Block資料=" + PublicPun.byte2HexString(outputData));
                                            qryBlockBalance(xchsOrder.getAccount());
                                        }
                                    });

                                    mExchangeAPI.doExWriteOKToken(orderID, PublicPun.byte2HexStringNoBlank(okTkn), PublicPun.byte2HexStringNoBlank(unblockTkn),
                                            new APIResultCallback() {
                                                @Override
                                                public void success(String[] msg) {
                                                    LogUtil.d("ExWriteOKToken " + msg[0]);
//                                                    completeOrder();
                                                    mProgress.dismiss();
                                                }

                                                @Override
                                                public void fail(String msg) {
                                                    LogUtil.d("ExWriteOKToken failed:" + msg);
                                                    mProgress.dismiss();
                                                    PublicPun.showNoticeDialog(mContext, getString(R.string.unable_to_block), getString(R.string.error) + ":" + msg);
                                                }
                                            });

                                } else {
                                    LogUtil.d("XchsBlockBtc fail");

                                    cmdManager.XchsBlockInfo(okTkn, new CmdResultCallback() {
                                        @Override
                                        public void onSuccess(int status, byte[] outputData) {
                                            LogUtil.d("Block資料=" + PublicPun.byte2HexString(outputData));
                                            qryBlockBalance(xchsOrder.getAccount());
                                        }
                                    });

                                    mProgress.dismiss();
                                    PublicPun.showNoticeDialog(mContext, getString(R.string.unable_to_block), getString(R.string.error) + ":" + Integer.toHexString(status));
                                }
                            }
                        });
                    }

                    @Override
                    public void fail(String msg) {
                        LogUtil.d("getTrxBlock failed:" + msg);
                        mProgress.dismiss();
                        PublicPun.showNoticeDialog(mContext, getString(R.string.unable_to_block), msg);


                    }
                });
            }
        }).setNegativeButton(R.string.strCancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        otp_dialog.show();
    }

    private void qryBlockBalance(int account) {

//        final byte cwHdwAllAccountInfo = 0x05;
        final byte cwHdwAccountInfoBlockAmount = 0x04;
        //[B5]
        cmdManager.hdwQueryAccountInfo(cwHdwAccountInfoBlockAmount, account, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                LogUtil.e("帳戶block金額："+PublicPun.byte2HexStringNoBlank(outputData));
            }
        });
    }

    byte[] cancelBlkInfo;

    private void CancelBlock() {
        AlertDialog.Builder otp_dialog = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT);
//        otp_dialog.setView(item);
        otp_dialog.setCancelable(true);
        otp_dialog.setMessage(getString(R.string.cancel_block_order));
        otp_dialog.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
//                cancelTrx(truP e);
                //queryAccountInfo
                mProgress.setMessage(getString(R.string.canceling_block_order));
                mProgress.show();
                mExchangeAPI.getExUnBlock(orderID, new APIResultCallback() {
                    @Override
                    public void success(String[] msg) {

//                        {"orderId":"15847930","okToken":"abc123","unblockTkn":"abc123",
//                                "mac":"dbe57d18f1c176606f40361a11c755ed655804a319d7b7120cdb1e729786d5dd"}

                        cancelBlkInfo = PublicPun.hexStringToByteArray(msg[1] + msg[2] + msg[3] + msg[4] + msg[5]);

                        LogUtil.e("cancelBlkInfo=trxID:" + msg[0] + ";cwOrder:" + msg[1] + ";OKTKN:" + msg[2] + ";UBLKTKN:" + msg[3] +
                                ";MAC:" + msg[4] + ";NONCE:" + msg[5]);

                        cmdManager.XchsCancelBlock(cancelBlkInfo, new CmdResultCallback() {
                            @Override
                            public void onSuccess(int status, byte[] outputData) {

                                if ((status + 65536) == 0x9000) {//-28672//36864
                                    LogUtil.d("cmd XchsCancelBlock  success = " + PublicPun.byte2HexString(outputData));
                                    cancelBlkInfo = outputData;

                                    mExchangeAPI.cancelTrx(orderID, new APIResultCallback() {
                                        @Override
                                        public void success(String[] msg) {
                                            LogUtil.d("cancelTrx  success = " + msg[0]);
                                            mProgress.dismiss();
                                        }

                                        @Override
                                        public void fail(String msg) {
                                            LogUtil.d("cancelTrx  failed = " + msg);
                                            mProgress.dismiss();
                                            PublicPun.showNoticeDialog(mContext, getString(R.string.unable_to_cancel_block), getString(R.string.error) + ":" + msg);
                                        }
                                    });
                                } else {
                                    mProgress.dismiss();
                                    LogUtil.d("XchsCancelBlock fail");
                                    //for debug error code
                                    PublicPun.showNoticeDialog(mContext, getString(R.string.unable_to_cancel_block), getString(R.string.error) + ":" + Integer.toHexString(status)
                                            + "-" + PublicPun.byte2HexString(outputData));
                                    cmdManager.getError(new CmdResultCallback() {
                                        @Override
                                        public void onSuccess(int status, byte[] outputData) {
                                            LogUtil.d("Get Error = " + Integer.toHexString(status));
                                        }
                                    });
                                }
                            }
                        });
                    }

                    @Override
                    public void fail(String msg) {
                        LogUtil.d("getTrxBlock failed:" + msg);
                        mProgress.dismiss();
                        PublicPun.showNoticeDialog(mContext, getString(R.string.unable_to_cancel_block), getString(R.string.error) + ":" + msg);
                    }
                });

            }
        });
        otp_dialog.show();
    }

    private Timer mTimer;

    private void completeOrder() {
        byte[] svrResp = null;
        trxHandle = new byte[4];
        isTrxSuccess = false;
        // prepareTransaction
        // 1.getTrxInfo (需不需要exBlockInfo)
        // 2.TrxSignLogin
        // 3.prepareTransaction
        // 4.
        byte[] bValue = new byte[16];
        new Random().nextBytes(bValue);
        nonce = bValue;
        LogUtil.d("nonce=" + PublicPun.byte2HexStringNoBlank(nonce));
//        cmdManager.trxFinish(new CmdResultCallback() {
//            @Override
//            public void onSuccess(int status, byte[] outputData) {
//                LogUtil.d("Trx finish");
//            }
//        });

        mProgress.setMessage(getString(R.string.preparing_to_complete_order));
        mProgress.show();

        PrepareToGetUnspentTrx();
    }

    private void PrepareToGetUnspentTrx() {
        //開始組prepare資料

        InAddress = "";
        int accountID = xchsOrder.getAccount();


        LogUtil.i("run XchsTrxSignPrepare");
        lisCwBtcAdd = DatabaseHelper.queryAddress(this, accountID, -1);

        String v_separator;
        if (!isBlockr) {
            UrlUnspent = BtcUrl.URL_BLOCKCHAIN_UNSPENT;
            v_separator = "|";
        } else {
            UrlUnspent = BtcUrl.URL_BLOCKR_UNSPENT;
            v_separator = ",";
        }

        for (int i = 0; i < lisCwBtcAdd.size(); i++) {
            if (lisCwBtcAdd.get(i).getBalance() > 0) {
                if (i == 0) {
                    InAddress += lisCwBtcAdd.get(i).getAddress();
                } else {
                    InAddress += v_separator + lisCwBtcAdd.get(i).getAddress();
                }
            }
        }
        if (isBlockr) {
            InAddress += "?unconfirmed=1";
        }
        System.out.print("unSpent addresses=" + InAddress);
//        unSpentTxsAsyncTask = new UnSpentTxsAsyncTask();
//        unSpentTxsAsyncTask.execute(InAddress);


        new UnSpentTxsAsyncTask().execute(InAddress);
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

            if (UnSpentTxsBeans == null || UnSpentTxsBeans.size() == 0) {
                PublicPun.showNoticeDialog(mContext, getString(R.string.error_msg), getString(R.string.note_unspent));
                mProgress.dismiss();
            } else {
                LogUtil.d("UnSpentTxsBeans 取得完成有 " + UnSpentTxsBeans.size() + " 筆");
                PreviousPrepareTransaction(xchsOrder.getAddr(), BTCUtils.BTCconvertToSatoshisValue(xchsOrder.getAmount()));
            }
        }
    }

    String changeAddr = "";

    private void PreviousPrepareTransaction(final String outputAddress, final long amountToSend) {

        long availableAmount = 0;
        for (UnSpentTxsBean unSpentTxsBean : unSpentTxsBeanList) {
            /**
             *  DecimalFormat  can format a number in a customized format for a particular locale,ex. 0.5=>0,5(Europe).
             */

            availableAmount += BTCUtils.BTCconvertToSatoshisValue(unSpentTxsBean.getAmount());
        }
        long extraFee = BTCUtils.parseValue("0.0");
        LogUtil.e("帳戶 " + xchsOrder.getAccount() + " 地址下有的餘額=" + availableAmount + " 要發出的金額=" + amountToSend);
        try {
            processedTxData =
                    BTCUtils.calcFeeChangeAndSelectOutputsToSpendii(mContext, unSpentTxsBeanList, amountToSend, extraFee, true, false);

            if (processedTxData == null) {
                cancelTrx();
                PublicPun.showNoticeDialog(mContext, getString(R.string.unable_to_send) + ":", getString(R.string.can_not_find_unspent));
                return;
            }

            //--------------- process change address.---------------------
            if (processedTxData.isDust) {
                LogUtil.d("發送不用找零,發送地址=" + outputAddress + "零錢地址＝" + changeAddr);
                outputs = new Transaction.Output[]{
                        new Transaction.Output(processedTxData.amountForRecipient, Transaction.Script.buildOutput(outputAddress)),
                };
                transactionConfirm(outputAddress);
            } else {
                genChangeAddress(Constant.CwAddressKeyChainInternal, new GetExchangeAddrCallback() {
                    @Override
                    public void onSuccess(String addr) throws NoSuchAlgorithmException, IOException, ValidationException {
                        LogUtil.d("發送要找零=" + processedTxData.change + "; 發送金額=" +
                                processedTxData.amountForRecipient + "; 發送地址=" +
                                outputAddress + "; 找零地址=" + addr);

                        //its legal that Change address equals to recipient's address
                        if (outputAddress.equals(addr)) {
                            cancelTrx();
                            PublicPun.showNoticeDialog(mContext, getString(R.string.error_msg), getString(R.string.send_notification_unable_to_send_with_change_error));
                            return;
                        }

                        changeAddr = addr;
                        //the outputs of transation

                        outputs = new Transaction.Output[]{
                                new Transaction.Output(processedTxData.amountForRecipient, Transaction.Script.buildOutput(outputAddress)),
                                new Transaction.Output(processedTxData.change, Transaction.Script.buildOutput(addr)),
                        };
                        transactionConfirm(outputAddress);

                    }

                    @Override
                    public void onFailed(String msg) {

                        cancelTrx();
                        PublicPun.showNoticeDialog(mContext, getString(R.string.unable_to_send), msg);
                    }
                });
//                }
            }
        } catch (ValidationException ve) {
            cancelTrx();
            PublicPun.showNoticeDialog(mContext, getString(R.string.unable_to_send), ve.getMessage());
        } catch (IOException e) {
            cancelTrx();
            PublicPun.showNoticeDialog(mContext, getString(R.string.unable_to_send), e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            cancelTrx();
            PublicPun.showNoticeDialog(mContext, getString(R.string.unable_to_send), e.getMessage());
        } catch (Exception e) {
            cancelTrx();
            PublicPun.showNoticeDialog(mContext, getString(R.string.unable_to_send), e.getMessage());
            Crashlytics.log(e.getCause().toString() + "\n" + e.getMessage());
            LogUtil.e("錯誤=" + e.getMessage());
        }


    }

    private void transactionConfirm(String mOutputAddress) {
        //--------------- process change address end.---------------------

        LogUtil.e("outputs length=" + outputs.length);

        if (mProgress.isShowing()) {
            mProgress.dismiss();
        }
        mTxsConfirm = new TxsConfirm(mOutputAddress, processedTxData.amountForRecipient, processedTxData.fee,
                processedTxData.outputsToSpend.size(), processedTxData.valueOfUnspentOutputs, changeAddr, processedTxData.change, processedTxData.isDust);

        // Ask if ready to send
        trxDialog = new TransactionConfirmDialog(mContext, mTxsConfirm, new TransactionConfirmCallback() {
            @Override
            public void TransactionConfirm(String outputAddr, String changeAddr, long spendAmount) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgress.setMessage(getString(R.string.preparing_transaction));
                        mProgress.show();
                    }
                });
                PrepXchsTrxSign(mTxsConfirm);

            }

            @Override
            public void TransactionCancel() {

                CancelBlock();

            }
        });
        trxDialog.show();
    }

    Transaction.Input[] signedInputs;
    ArrayList<TrxBlks> lisTrxBlks;
    int exeCnt;

    private void PrepXchsTrxSign(final TxsConfirm mTxsConfirm) {

        try {
            //計算出了手續費和找零後的 UnspentInputList未花费的交易,input=要拿來發送$的地址
            LogUtil.e(" processedTxData.outputsToSpend.size():" + processedTxData.outputsToSpend.size());
            signedInputs = new Transaction.Input[processedTxData.outputsToSpend.size()];//
            LogUtil.e("要拿來發送$的地址length:" + signedInputs.length);

            lisTrxBlks = new ArrayList<>();
            final boolean[] prepRsult = new boolean[signedInputs.length];
            exeCnt = 0;
            for (int i = 0; i < signedInputs.length; i++) {
                Transaction.Input[] unsignedInputs = new Transaction.Input[signedInputs.length];
                for (int j = 0; j < unsignedInputs.length; j++) { //有幾個input
                    outputToSpend = processedTxData.outputsToSpend.get(j);
                    //dora modify
                    LogUtil.e("跑hash " + i + "-" + j + "次");
                    byte[] byteTx = PublicPun.hexStringToByteArray(outputToSpend.getTx());//outputToSpend.getTx().getBytes()

                    //my send addr
                    LogUtil.d("Transaction.OutPoint:" + outputToSpend.getAddress() + "的HEX=" + LogUtil.byte2HexString(byteTx) + ";outputToSpend.getN()=" + outputToSpend.getN());

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
                LogUtil.i("inputId=" + String.valueOf(finalI) + ";addr=" + processedTxData.outputsToSpend.get(i).getAddress());
                dbAddress d = DatabaseHelper.querySendAddress(mContext, processedTxData.outputsToSpend.get(i).getAddress());
                if (d == null) {
                    cancelTrx();
                    PublicPun.showNoticeDialog(mContext, getString(R.string.unable_to_send), getString(R.string.can_not_find_unspent));
                }

                final int dbKid = d.getKid();
                final int dbKcid = d.getKcid();
                final long unspentBalance = BTCUtils.BTCconvertToSatoshisValue(processedTxData.outputsToSpend.get(i).getAmount());

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
                        xchsOrder.getAccount(),
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

                                        TrxBlks trxblks = new TrxBlks();
                                        trxblks.setTrxHandle(trxHandle);
                                        trxblks.setAccid(xchsOrder.getAccount());
                                        trxblks.setInputId(finalI);
                                        trxblks.setKcid(dbKcid);
                                        trxblks.setKid(dbKid);
                                        trxblks.setPublickey(publicKeyBytes);
//                                        byte[] out1 = Base58.decode(mTxsConfirm.getOutput_addrese());
//                                        byte[] out2 = mTxsConfirm.getChange_address().getBytes();
                                        trxblks.setOut1Addr(Base58.decode(mTxsConfirm.getOutput_addrese()));
                                        trxblks.setOut2Addr(Base58.decode(mTxsConfirm.getChange_address()));
                                        trxblks.setChangeKid(DatabaseHelper.queryAddrKid(ExchangeCompleteOrderActivity.this, mTxsConfirm.getChange_address()));
                                        trxblks.setSigmtrl(hash);
                                        lisTrxBlks.add(trxblks);

                                        exeCnt++;

                                        if (exeCnt == signedInputs.length) {


                                            mExchangeAPI.doExGetTrxInfo(xchsOrder.getOrderId(), new APIResultCallback() {
                                                @Override
                                                public void success(final String[] msg) {
                                                    LogUtil.d("doExGetTrxInfo ok " + msg[0]);
                                                    //order id, oktoken, unblock token,accid,send amount, mac
                                                    //{"loginblk":"3488162291780ad0c38e7ebbc44406347d770086fc2c54da0300000000000000000027100ba5ea57e799ef00e33dcb58836c6fbee9820ce0616b26dff7c51c3bdd89eb08",
                                                    // "out1addr":"1PHcgbVxMCNsB5RnfvKdUC7hidzSdovL2s"}

                                                    // XCHS Transaction Prepare
                                                    // orderId/okTkn/encBulkTkn/accId/dealAmnt/mac

                                                    //開始組prepare資料
//                            inId: 1B
//                            accId: 4B
//                            kcId: 1B / kId: 4B
//                            out1Addr: 25B
//                            out2Addr: 25B
//                            sigMtrl: 32B
//                            mac: 32B

                                                    cmdManager.XchsTrxsignLogin(PublicPun.hexStringToByteArray(msg[0]), new CmdResultCallback() {
                                                        @Override
                                                        public void onSuccess(int status, byte[] outputData) {
                                                            if ((status + 65536) == 0x9000) {
                                                                trxHandle = outputData;
                                                                LogUtil.d("XchsTrxSignLogin trxHandle=" + PublicPun.byte2HexStringNoBlank(trxHandle));

                                                                doTrxSignPrepare(lisTrxBlks);

                                                            } else {
                                                                //for debug error code

//                            XchsTrxsignLogout(trxHandle);
                                                                cmdManager.getError(new CmdResultCallback() {
                                                                    @Override
                                                                    public void onSuccess(int status, byte[] outputData) {
                                                                        LogUtil.d("Login failed = " + Integer.toHexString(status) + ";outputData=" + PublicPun.byte2HexString(outputData));
                                                                    }
                                                                });

                                                                AlertDialog.Builder mBuilder =
                                                                        PublicPun.CustomNoticeDialog(mContext, getString(R.string.sign_login_failed), getString(R.string.error) + ":" + Integer.toHexString(status));
                                                                mBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
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
//                XchsTrxsignLogout(trxHandle);
                                                    //exchangeSite Logout()
                                                }
                                            });


                                        }
                                    }
                                }
                            }
                        });

            }

        } catch (Exception e) {
            PublicPun.showNoticeDialog(mContext, getString(R.string.error_msg), getString(R.string.unable_to_send));
            cancelTrx();
            Crashlytics.logException(e);
        }
    }

    int exePrepCnt;

    private void doTrxSignPrepare(final ArrayList<TrxBlks> mLisTrxBlks) {

        //[trxHandle][ACCID] [KCID] [KID][OUT1ADDR] [OUT2ADDR][SIGMTRL]

        LogUtil.e("mLisTrxBlks.size=" + String.valueOf(mLisTrxBlks.size()));
        if (mLisTrxBlks.size() > 0) {


            for (TrxBlks mTrxBlks : mLisTrxBlks) {
                LogUtil.e("trxHandle=" + PublicPun.byte2HexStringNoBlank(mTrxBlks.getTrxHandle()));
                LogUtil.e("accountId=" + mTrxBlks.getAccid());
                LogUtil.e("keyChainId =" + mTrxBlks.getKcid());
                LogUtil.e("keyId =" + mTrxBlks.getKid());
                LogUtil.e("out1Addr =" + PublicPun.byte2HexStringNoBlank(mTrxBlks.getOut1Addr()));
                LogUtil.e("out2Addr =" + PublicPun.byte2HexStringNoBlank(mTrxBlks.getOut2Addr()));
                LogUtil.e("sigmtrl =" + PublicPun.byte2HexStringNoBlank(mTrxBlks.getSigmtrl()));
            }
        }


        mExchangeAPI.doExGetTrxPrepareBlocks(xchsOrder.getOrderId(), mLisTrxBlks, new APIResultCallback() {
            @Override
            public void success(String[] msg) {
                LogUtil.d("doTrxSignPrepare ok " + msg[0]);

                String[] input = PublicPun.jsonParserXchsPrepare(msg[0]);

                exePrepCnt = 0;

                if (input != null && input.length != 0) {
                    for (int i = 0; i < mLisTrxBlks.size(); i++) {

                        byte[] rspInfo = PublicPun.hexStringToByteArray(input[i]);
                        LogUtil.e("trxHandle=" + PublicPun.byte2HexStringNoBlank(trxHandle) + " ; " + "rspInfo=" + PublicPun.byte2HexStringNoBlank(rspInfo));
                        byte[] prepInfo = new byte[rspInfo.length + trxHandle.length];
                        System.arraycopy(trxHandle, 0, prepInfo, 0, trxHandle.length);
                        System.arraycopy(rspInfo, 0, prepInfo, trxHandle.length, rspInfo.length);
                        LogUtil.e("trxHandle=" + PublicPun.byte2HexStringNoBlank(prepInfo));
                        cmdManager.XchsTrxsignPrepare(mLisTrxBlks.get(i).getInputId(), prepInfo, new CmdResultCallback() {
                            @Override
                            public void onSuccess(int status, byte[] outputData) {
                                if ((status + 65536) == 0x9000) {
                                    LogUtil.e("cwCmdHdwPrepTrxSign 成功!");
                                    exePrepCnt++;
                                    if (exePrepCnt == mLisTrxBlks.size()) {
                                        doTrxBegin(mTxsConfirm.getOutput_addrese());
                                    }

                                } else {
                                    cmdManager.getError(new CmdResultCallback() {
                                        @Override
                                        public void onSuccess(int status, byte[] outputData) {
                                            LogUtil.e("getError :" + PublicPun.byte2HexString(outputData));
                                        }
                                    });
//                                    XchsTrxsignLogout(trxHandle);
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void fail(String msg) {
                mProgress.dismiss();
                LogUtil.d("doTrxSignPrepare failed:" + msg);
//                XchsTrxsignLogout(trxHandle);
            }
        });

    }

    private void doGetButton() {

        LogUtil.i("交易要button");
        //發送一個給a006讀取特殊狀態的cmd
        cmdManager.getCmdProcessor().setButtonA006(true);
        if (mProgress != null) {
            mProgress.dismiss();
        }
        ClickFunction(mContext, getString(R.string.send), getString(R.string.plz_press_button));
    }

    private void doTrxBegin(final String outputAddress) {

        if (!settingOptions[0] && settingOptions[1]) {
            doGetButton();
        }

        for (int i = 0; i < 4; i++) {
            LogUtil.e("settingOptions=" + settingOptions[i]);

        }

        scriptSigs = new ArrayList();

        cmdManager.cwCmdTrxBegin(PublicPun.user.getEncKey(),
                processedTxData.amountForRecipient,
                outputAddress,
                new CmdResultCallback() {
                    @Override
                    public void onSuccess(int status, byte[] outputData) {
//                        if ((status + 65536) == 0x9000) {
//                            if (InitialSecuritySettingActivity.settingOptions[0]) {
//                                doVerifyOtp(outputAddress);
//                            } else {
//                                //case 1.button only
//                                doGetButton(outputAddress);
//                            }
//                        }

                        if (!settingOptions[0] && settingOptions[1]) {
                            if (outputData != null) {
                                if (Arrays.equals(outputData, successBtnPressesData)) {
                                    LogUtil.i("case1.button_up pressed!!!!!!");
                                    if (btnTxBuilder.isShowing()) btnTxBuilder.dismiss();
                                    btnTxBuilder.dismiss();
                                    mProgress.setMessage(getString(R.string.sending_bitcoins));
                                    mProgress.show();
                                    for (int i = 0; i < signedInputs.length; i++) {
                                        doTrxSign(i, lisTrxBlks.get(i).getPublickey());
                                    }
                                }
                            }
                        } else {
                            if ((status + 65536) == 0x9000) {
                                if (outputData != null) {
                                    LogUtil.i("cwCmdTrxBegin 成功!!");
                                    if (settingOptions[0]) {
                                        // 交易要otp
                                        doVerifyOtp();
                                    }
                                }
                            } else {
                                PublicPun.showNoticeDialog(mContext, getString(R.string.error_msg), getString(R.string.cmd_trx_begin_error) + ":" + LogUtil.byte2HexString(outputData));
                            }
                        }
                    }
                });
    }


    private void doVerifyOtp() {

        AlertDialog.Builder otp_dialog = new AlertDialog.Builder(this, ProgressDialog.THEME_HOLO_LIGHT);
        final View item = LayoutInflater.from(this).inflate(R.layout.alert_dialog_otp_input, null);
        otp_dialog.setCancelable(false);
        otp_dialog.setView(item)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = (EditText) item.findViewById(R.id.alert_editotp);
                        otpCode = editText.getText().toString().trim();
                        LogUtil.i("otp press ok:" + editText.getText().toString());

                        if (settingOptions[1]) {
                            //發送一個給a006讀取特殊狀態的cmd
                            doGetButton();
                        }

                        cmdManager.trxVerifyOtp(otpCode, new CmdResultCallback() {
                            @Override
                            public void onSuccess(int status, byte[] outputData) {
                                if (settingOptions[1]) {
                                    if (outputData != null) {
                                        if (Arrays.equals(outputData, successBtnPressesData)) {
                                            //otp+button_up verify
                                            if (btnTxBuilder.isShowing()) btnTxBuilder.dismiss();
                                            mProgress.setMessage(getString(R.string.sending_bitcoins));
                                            mProgress.show();
                                            LogUtil.i("case3. otp+button_up verify!");
                                            for (int i = 0; i < signedInputs.length; i++) {
                                                doTrxSign(i, lisTrxBlks.get(i).getPublickey());
                                            }
                                        }
                                    }
                                } else {
                                    if ((status + 65536) == 0x9000) {
                                        //otp verify only
                                        LogUtil.i("case2. otp verify only!!!!!!");
                                        //有幾筆input就sign幾次
                                        for (int i = 0; i < signedInputs.length; i++) {
                                            doTrxSign(i, lisTrxBlks.get(i).getPublickey());
                                        }

                                    } else {
                                        LogUtil.i("otp failed status=" + String.valueOf(status + 65536));
                                        cancelTrx();
                                        PublicPun.showNoticeDialog(mContext, getString(R.string.error_msg), getString(R.string.failed_to_verify_otp));
                                    }
                                }
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.strCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        cancelTrx();
                    }
                });
        otp_dialog.show();

    }

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
                                    scriptSigs.add(genScriptSig(signOfTx, lisTrxBlks.get(inputID).getPublickey()));

                                    if (doTrxSignSuccessCnt == signedInputs.length) {
                                        currUnsignedTx = genRawTxData(scriptSigs);
                                        LogUtil.e("取得 currUnsignedTx=" + currUnsignedTx + ";length=" + currUnsignedTx.length());
                                        LogUtil.e("byte長度=" + PublicPun.hexStringToByteArray(currUnsignedTx).length);

                                        FunApiSubmit(PublicPun.hexStringToByteArray(currUnsignedTx));

                                    }
                                }
                            }
                        } else {
                            cmdManager.getError(new CmdResultCallback() {
                                @Override
                                public void onSuccess(int status, byte[] outputData) {
                                    LogUtil.e("TrxSign failed getError=" + PublicPun.byte2HexString(outputData));
                                }
                            });
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
            LogUtil.i("ByteinTx=" + PublicPun.byte2HexStringNoBlank(ByteinTx));
            byte[] revId = new byte[32];
            String tid;

            for (int j = 0; j < 32; j++) {
                revId[j] = ByteinTx[ByteinTx.length - j - 1];
            }
            tid = PublicPun.byte2HexStringNoBlank(revId);
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
            sb.append(PublicPun.byte2HexStringNoBlank(ByteUtil.intToByteLittle(tx.getTxinList().get(i).getIndex(), 4)));
            LogUtil.i("in index=" + PublicPun.algorismToHEXString(tx.getTxinList().get(i).getIndex(), 2));
            //script len
            sb.append(PublicPun.algorismToHEXString(tx.getTxinList().get(i).getScriptSigLen(), 2));
            LogUtil.i("in getScriptSigLen=" + PublicPun.algorismToHEXString(tx.getTxinList().get(i).getScriptSigLen(), 2));
            //scriptSig
            sb.append(PublicPun.byte2HexStringNoBlank(tx.getTxinList().get(i).getScriptSig()));
            LogUtil.i("getScriptSig=" + PublicPun.byte2HexStringNoBlank(tx.getTxinList().get(i).getScriptSig()));
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
            sb.append(PublicPun.byte2HexStringNoBlank(value));

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

            postDecodeResult = cwBtcNetWork.doPostII(BtcUrl.URL_BLOCKCHAIN_SERVER_SITE + BtcUrl.URL_BLICKCHAIN_DECODE, currUnsignedTx);
            if (postDecodeResult == 200) {
                postPushResult = cwBtcNetWork.doPostII(BtcUrl.URL_BLOCKCHAIN_SERVER_SITE + BtcUrl.URL_BLICKCHAIN_PUSH, currUnsignedTx);
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

                    if (mTimer != null) {
                        mTimer.cancel();
                    }
                    isTrxSuccess = true;
                    //clear trx data

                    if (mProgress != null) {
                        mProgress.dismiss();
                    }

                    FunTrxFinish();

                    PublicPun.showNoticeDialog(mContext, getString(R.string.sent), getString(R.string.sent) + " " + mTxsConfirm.getOutput_amount() + getString(R.string.btc_to) + " " + recvAddress);

                    PublicPun.CustomNoticeDialog(mContext, getString(R.string.sent), getString(R.string.sent) + " " + mTxsConfirm.getOutput_amount() + getString(R.string.btc_to) + " " + recvAddress)
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    finish();

                                }
                            }).show();


                    break;
                case HANDLER_SEND_BTC_ERROR:
                    if (mProgress != null) {
                        mProgress.dismiss();
                    }
                    if (mTimer != null) {
                        mTimer.cancel();
                    }
                    PublicPun.showNoticeDialog(mContext, getString(R.string.error_msg), getString(R.string.failed_to_broadcast_transaction));
                    break;
            }
        }
    };

    private void doGetButton(final String outputAddress) {
        LogUtil.i("交易要button");
        ClickFunction(mContext, getString(R.string.send), getString(R.string.plz_press_button));
        cmdManager.trxButton(new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if (outputData != null) {
                    if (Arrays.equals(outputData, successBtnPressesData)) {
                        LogUtil.e("button_up pressed!!!!!!");
                        if (btnTxBuilder.isShowing()) {
                            btnTxBuilder.dismiss();
                        }
                        mProgress.setMessage(getString(R.string.sending_bitcoins));
                        mProgress.show();

                        for (int i = 0; i < signedInputs.length; i++) {
                            doTrxSign(i, lisTrxBlks.get(i).getPublickey());
                        }
                    }
                }
            }
        });
    }

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

//    private void XchsTrxsignLogout(byte[] trxHandle) {
//        //[trxHandle]: 8d 3e 99 86 ; [NONCE]:
//        mProgress.dismiss();
////        byte[] trxHandleLogOut = PublicPun.hexStringToByteArray("8d3e9986");
//
//        cmdManager.XchsTrxsignLogout(trxHandle, nonce, new CmdResultCallback() {
//            @Override
//            public void onSuccess(int status, byte[] outputData) {
//
//                if ((status + 65536) == 0x9000) {
//                    LogUtil.d("XchsTrxsignLogout 成功");
//                } else {
//                    LogUtil.d("XchsTrxsignLogout 失敗");
//                    cmdManager.getError(new CmdResultCallback() {
//                        @Override
//                        public void onSuccess(int status, byte[] outputData) {
//                            LogUtil.d("Login failed = " + Integer.toHexString(status) + ";outputData=" + PublicPun.byte2HexString(outputData));
//                        }
//                    });
//                }
//            }
//        });
//    }

    String genChangeAddressResult;

    //產生收零地址(of output)
    private void genChangeAddress(final int keyChainId, final GetExchangeAddrCallback mGetExchangeAddrCallback) throws NoSuchAlgorithmException, IOException, ValidationException {
        genChangeAddressResult = "";
        String addr = "";
        amount = 0;
        amount = xchsOrder.getAmount();
        final int accountId = xchsOrder.getAccount();
        ArrayList<dbAddress> lisCwBtcAdd = new ArrayList<dbAddress>();
        // find only internal addr
        lisCwBtcAdd = DatabaseHelper.queryAddress(mContext, accountId, keyChainId);

        String changeAddressStr = null;
        //先query 卡片內0交易的int addr;沒有的話再產生
        for (int i = 0; i < lisCwBtcAdd.size(); i++) {
            //query出來已有order by kid
            if (lisCwBtcAdd.get(i).getN_tx() == 0) {
                changeAddressStr = lisCwBtcAdd.get(i).getAddress();
                break;
            }
        }

        if (changeAddressStr != null) {
            //new 對方接收地址, 自己的找零地址, 發送的金額
            LogUtil.e("自己的找零地址=" + changeAddressStr);
//            genChangeAddressResult = changeAddressStr;
            mGetExchangeAddrCallback.onSuccess(changeAddressStr);
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
//                            genChangeAddressResult = addressStr;
                            try {
                                mGetExchangeAddrCallback.onSuccess(addressStr);
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (ValidationException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        mGetExchangeAddrCallback.onFailed(getString(R.string.failed_to_get_change_address) + getString(R.string.error) + ":" + Integer.toHexString(status));
                    }
                }
            });
        }
//        return genChangeAddressResult;
    }


    public List<UnSpentTxsBean> getUnspentTxsByAddr(String mAddr) {

        List<UnSpentTxsBean> UnSpentTxsBeanList = null;
        ContentValues cv = new ContentValues();
        cv.put("addresses", mAddr);
        LogUtil.d("addressesXX=" + cv.getAsString("addresses"));
        String result = cwBtcNetWork.doGet(cv, UrlUnspent, null);
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
//                UnSpentTxsBeanList = PublicPun.jsonParseBlockrUnspent(result);
                if (isBlockr) {
                    UnSpentTxsBeanList = PublicPun.jsonParseBlockrUnspent(result);
                } else {
                    UnSpentTxsBeanList = PublicPun.jsonParseBlockChainInfoUnspent(result);
                }
                errorCnt = 0;
            }
        }
        return UnSpentTxsBeanList;
    }

    private void cancelTrx() {

//        FunTrxFinish();
        if (mProgress.isShowing()) {
            mProgress.dismiss();
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void failedTrx() {
        mProgress.dismiss();
        FunTrxFinish();
        PublicPun.showNoticeDialog(mContext, getString(R.string.send_notification_str_failed_title), getString(R.string.msg_xchs_get_raw_addr));
    }


    private void FunApiSubmit(final byte[] txHash) {

        cmdManager.XchsTrxsignLogout(trxHandle, nonce, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                LogUtil.d("XchsTrxsignLogout 成功");

                final String trxReceipt = PublicPun.byte2HexStringNoBlank(outputData);
                // get Receipt
                //API Single Address
                //CALL XCHS SUBMIT API

//                mProgress.setMessage("Synchronizing to Exchange Site...");
//                mProgress.show();

//                mExchangeAPI.getBlockChainRawAddress(xchsOrder.getAddr(), new APIResultCallback() {
//                    @Override
//                    public void success(String[] msg) {
//                        String trxId = msg[0];
//                        LogUtil.e("trxID=" + trxId);
//
//                        if (trxId == "" || trxId == null) {
//                            failedTrx();
//                        } else {

//                            if(trxId!=txId){
//                                failedTrx();
//                                return;
//                            }

                byte[] doubleSha256TxHash = PublicPun.encryptSHA256(PublicPun.encryptSHA256(txHash));
                String txId = PublicPun.byte2HexStringNoBlank(BTCUtils.reverse(doubleSha256TxHash));
                String out2Addr = mTxsConfirm.getChange_address();

                mExchangeAPI.doTrxSubmit(mTxsConfirm.getInput_count(), xchsOrder.getOrderId(), txId, out2Addr, trxReceipt, PublicPun.uid, PublicPun.byte2HexStringNoBlank(nonce), new APIResultCallback() {
                    @Override
                    public void success(String[] msg) {
                        if (mProgress.isShowing()) {
                            mProgress.dismiss();
                        }

                        PublishToNetwork(currUnsignedTx);

                    }

                    @Override
                    public void fail(String msg) {
                        //add cancel block

                        failedTrx();

                    }
                });
//                        }
//                    }
//
//                    @Override
//                    public void fail(String msg) {
//                        failedTrx();
//                    }
//                });
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
                }
            }
        });


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
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // do your work right here
                            PublicPun.showNoticeDialog(mContext, getString(R.string.send_notification_str_failed_title), getString(R.string.send_notification_str_failed_msg));
                        }
                    });
                }
            }
        }, 60000);////60s沒成功就自動cacel
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

    }

}
