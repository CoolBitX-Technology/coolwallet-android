package com.coolbitx.coolwallet.ui;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.NetworkOnMainThreadException;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.coolbitx.coolwallet.DataBase.DatabaseHelper;
import com.coolbitx.coolwallet.DataBase.DbName;
import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.bean.Account;
import com.coolbitx.coolwallet.bean.Constant;
import com.coolbitx.coolwallet.bean.CwBtcTxs;
import com.coolbitx.coolwallet.bean.dbAddress;
import com.coolbitx.coolwallet.callback.RefreshCallback;
import com.coolbitx.coolwallet.general.BtcUrl;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.general.RefreshBlockChainInfo;
import com.coolbitx.coolwallet.httpRequest.CwBtcNetWork;
import com.coolbitx.coolwallet.ui.Fragment.FragMainActivity;
import com.coolbitx.coolwallet.util.BIP39;
import com.coolbitx.coolwallet.util.BTCUtils;
import com.coolbitx.coolwallet.util.ExtendedKey;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.ByteUtil;
import com.snscity.egdwlib.utils.LogUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ShihYi on 2015/10/20.
 */
public class RecovWalletActivity extends BaseActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener, TextWatcher {//,View.OnKeyListener

    //    for secpo
    public static boolean[] settingOptions = new boolean[4];
    CwBtcNetWork cwBtcNetWork;
    int accountCNT = 5;
    int addrAuccess;
    Pattern p;
    Matcher m;
    int preSpacePos;
    int lastPos;
    PopupWindow pop;
    String hdwSeed;
    int msgSetAccInfoExcute = 0;
    private Button btnCreateWallet;
    //    private Button btnCreateWallet1;
//    private Button btnCreateWallet2;
    private Spinner seedSpinner;
    private EditText edtHdWord;
    private String[] strSeed = { "Words","Numbers"};
    private boolean isSeedOn = true;
    private CmdManager cmdManager;
    private ArrayAdapter<String> listSeed;
    private Context mContext;
    private ProgressDialog mProgress;
    private boolean isSumChk;
    private List<Account> cwAccountList;
    private byte CwSecurityPolicyMaskOtp = 0x01;
    private byte CwSecurityPolicyMaskBtn = 0x02;
    private byte CwSecurityPolicyMaskWatchDog = 0x10;
    private byte CwSecurityPolicyMaskAddress = 0x20;
    private ProgressDialog mHorizontalDialog = null;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String result = data.getString("resultMsg");
            switch (msg.what) {
                case 1:
                    addrAuccess++;
                    LogUtil.i("addrAuccess=" + addrAuccess + " & " + String.valueOf(accountCNT * 2));
                    mHorizontalDialog.incrementProgressBy(6);
                    if (addrAuccess == accountCNT * 2) {
                        //代表10個thread都跑完了(5個account再帶各自的ext、int)

                        for (int i = 0; i < accountCNT; i++) {
                            final int mAccount = i;
                            RefreshBlockChainInfo refreshBlockChainInfo = new RefreshBlockChainInfo(mContext, mAccount);
                            refreshBlockChainInfo.callTxsRunnable(new RefreshCallback() {
                                @Override
                                public void success() {
                                    FunhdwSetAccInfo(mAccount);
                                    mHorizontalDialog.incrementProgressBy(3);
                                }

                                @Override
                                public void fail(String msg) {
                                    PublicPun.showNoticeDialog(mContext, "Unstable internet connection", msg);
                                    FunhdwSetAccInfo(mAccount);
                                }

                            });
                        }
                    }
                    break;
                case 0:
//                    mProgress.dismiss();
                    dialogDismiss();
                    PublicPun.showNoticeDialog(mContext, "Erro Message", result);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recov_wallet_ii);
        initViews();
//
//        seedSpinner.setSelection(1, true);
//        edtHdWord.addTextChangedListener(null);

        initToolbar();
        mContext = this;
        cmdManager = new CmdManager();
        mProgress = new ProgressDialog(RecovWalletActivity.this, ProgressDialog.THEME_HOLO_DARK);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
        cwBtcNetWork = new CwBtcNetWork();

        PublicPun.showNoticeDialog(mContext, getString(R.string.reminder), getString(R.string.put_coolwallet_on_coollink));
        GetSecpo();
    }

    private void GetSecpo() {
        cmdManager.getSecpo(new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    if (outputData != null && outputData.length > 0) {
                        settingOptions[0] = (outputData[0] & CwSecurityPolicyMaskOtp) >= 1;

                        settingOptions[1] = (outputData[0] & CwSecurityPolicyMaskBtn) >= 1;

                        settingOptions[2] = (outputData[0] & CwSecurityPolicyMaskAddress) >= 1;

                        settingOptions[3] = (outputData[0] & CwSecurityPolicyMaskWatchDog) >= 1;

                        LogUtil.i("get安全設置:otp=" + settingOptions[0] + ";button_up=" + settingOptions[1] +
                                ";address" + settingOptions[2] + ";dog=" + settingOptions[3]);
                    }

                    SetSecpo(true);
                }
            }
        });
    }

    //初始化安全设置
    private void SetSecpo(boolean setWatchDog) {
//        settingOptions[0] = switchOtp.isChecked();
//        settingOptions[1] = switchEnablePressButton.isChecked();
//        settingOptions[2] = switchAddress.isChecked();

        settingOptions[3] = setWatchDog;

        LogUtil.i("set安全設置:otp=" + settingOptions[0] + ";button_up=" + settingOptions[1] +
                ";address" + settingOptions[2] + ";dog=" + settingOptions[3]);

        cmdManager.setSecpo(settingOptions, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
//                    PublicPun.toast(mContext, "CW Security Policy Set");
                }
            }
        });
    }
    //-----------------------------------CHECK hdwSumStr end----------------------------------//

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {


    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    //-----------------------------------CHECK hdwSumStr begin----------------------------------//
    @Override
    public void afterTextChanged(Editable s) {
        String chkStr = "";
        String mInput = s.toString();
        if (isSeedOn) {
            p = Pattern.compile("[0-9\\s]*");
            m = p.matcher(mInput);
            if (!m.matches()) {
                PublicPun.showNoticeDialog(mContext, getString(R.string.notice_recovery_invalid), getString(R.string.notice_recovery_invalid_numbers));
                edtHdWord.setText(s.toString().substring(0, s.length() - 1));
                edtHdWord.setSelection(edtHdWord.getText().toString().length());
            }

        } else {
            p = Pattern.compile("[a-zA-Z\\s]*");
            m = p.matcher(mInput);
            if (!m.matches()) {
                PublicPun.showNoticeDialog(mContext, getString(R.string.notice_recovery_invalid), getString(R.string.notice_recovery_invalid_word));
                edtHdWord.setText(s.toString().substring(0, s.length() - 1));
                edtHdWord.setSelection(edtHdWord.getText().toString().length());
            }
        }
        if (s.length() > 1) {
            lastPos = s.length() - 1;
            char last = s.charAt(lastPos);

            if (last == ' ') {
                preSpacePos = mInput.substring(0, lastPos - 1).lastIndexOf(' ') + 1;
                LogUtil.i("input str=" + mInput + ";prePos=" + preSpacePos + ";last pos=" + String.valueOf(lastPos));
                chkStr = mInput.substring(preSpacePos, lastPos);
                boolean isSeedCorrect = false;
                if (isSeedOn) {
                    //NUMBER
                    if (lastPos >= 6) {
//                        chkStr = mInput.substring(lastPos - 6, lastPos);
                        if ((lastPos - preSpacePos) != 6) {
                            PublicPun.showNoticeDialog(mContext, "Seed Invalid", "The numbers of characters are not correct!");
                        } else {
                            isSeedCorrect = ChkSumByNumbers(chkStr);
                        }
                    } else {
                        PublicPun.showNoticeDialog(mContext, "Seed Invalid", "The numbers of characters are not correct!");
                    }
                } else {
                    //WORD
                    isSeedCorrect = BIP39.chkSeedWords(chkStr);
                }
                LogUtil.e("ChkSum:" + (isSeedOn ? "numbers" : "words") + "=" + isSeedCorrect);

                if (isSeedCorrect) {

                } else {
                    PublicPun.showNoticeDialog(mContext, "Seed Invalid", "'" + chkStr + "' is an invalid seed.");
                }
            }
        }
    }

    private boolean ChkSumByNumbers(String chkStr) {
        int mDigits;
        int mSum = 0;
        int mChkDigits = 0;
        for (int i = 0; i < chkStr.length(); i++) {
            mDigits = Integer.valueOf(chkStr.substring(i, i + 1));
            if (i == 5) {
                mChkDigits = mSum % 10;
                return mChkDigits == mDigits;
            }
            mSum += mDigits;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        if (view == btnCreateWallet) {

            if (checkSeedLength()) {
                DatabaseHelper.deleteTable(mContext, DbName.DB_TABLE_TXS);
                DatabaseHelper.deleteTable(mContext, DbName.DB_TABLE_ADDR);
                DatabaseHelper.deleteTable(mContext, DbName.DB_TABLE_CURRENT);
                SetSecpo(true);
                InitWallet();
            }
        }
    }

    private boolean checkSeedLength() {
        boolean result;
        List<String> allMatches = Arrays.asList(edtHdWord.getText().toString().trim().split(" "));
        LogUtil.d("allMatches長度=" + allMatches.size());
        if (isSeedOn) {
            if (allMatches.size() == 8 || allMatches.size() == 12 || allMatches.size() == 16) {
                result = true;
            } else {
                PublicPun.showNoticeDialog(mContext, getString(R.string.notice_recovery_invalid), getString(R.string.notice_recovery_invalid_numbers_length));
                result = false;
            }
        } else {
            if (allMatches.size() == 12 || allMatches.size() == 18 || allMatches.size() == 24) {
                result = true;
            } else {
                PublicPun.showNoticeDialog(mContext, getString(R.string.notice_recovery_invalid), getString(R.string.notice_recovery_invalid_word_length));
                result = false;
            }
        }
        return result;
    }

    private void InitWallet() {
        final String name = "";
        final String strhdwSeed = edtHdWord.getText().toString().trim();
        /**
         * 判斷是否為舊用戶(which create wallet replace " "->""; but recovery not)
         */
        if (BIP39.isSimpleEntropy(strhdwSeed)) {
            hdwSeed = strhdwSeed.replace(" ", "");
            LogUtil.e("是舊用戶=" + hdwSeed);
        } else {
            hdwSeed = strhdwSeed;
            LogUtil.e("是新用戶=" + strhdwSeed);
        }

        LayoutInflater lf = LayoutInflater.from(mContext);
        View layout = lf.inflate(R.layout.pupwindow_bg, null);
        pop = new PopupWindow(layout,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                true);
        pop.setBackgroundDrawable(new ColorDrawable(0xffffff));//支持點擊back虛擬鍵退出
        pop.showAtLocation(findViewById(R.id.tl_create), Gravity.NO_GRAVITY, 0, 0);
        showHorizontalDialog();

        new Thread() {
            @Override
            public void run() {
                super.run();

                //PublicPun.user.getEncKey(), PublicPun.user.getMacKey()這兩個bleActivity已抓
                // 1. Create wallet
                cmdManager.hdwInitWallet(name, hdwSeed, PublicPun.user.getEncKey(), PublicPun.user.getMacKey(), new CmdResultCallback() {
                    @Override
                    public void onSuccess(int status, byte[] outputData) {
                        if ((status + 65536) == 0x9000) {

                            byte infoId = 0x02;
                            cmdManager.hdwQryWaInfo(infoId, new CmdResultCallback() {
                                @Override
                                public void onSuccess(int status, byte[] outputData) {
                                    if ((status + 65536) == 0x9000) {
                                        LogUtil.i("hdwQryWaInfot account  HEX=" + LogUtil.byte2HexString(outputData));

//                                        Intent intent = new Intent(getApplicationContext(), FragMainActivity.class);
//                                        startActivity(intent);
                                        //
                                        byte[] hdwAccountPointer = outputData;
                                        LogUtil.e("Recovery account數=" + LogUtil.byte2HexString(outputData));
                                        if (hdwAccountPointer != null && hdwAccountPointer.length == 4) {
                                            int accountPointer = Integer.parseInt(PublicPun.byte2HexString(hdwAccountPointer[0]));

                                            //if not enough 5 account, create new account util to 5 account.
                                            int accountId = 0;
                                            addrAuccess = 0;//thread 從這裡分出.

                                            for (int i = accountId; i < accountCNT; i++) {
                                                CreateNewAccount(i);
                                            }
                                        }
                                    } else {
                                        dialogDismiss();
                                        PublicPun.showNoticeDialog(mContext, "Error Message", "Error:" + Integer.toHexString(status));
                                    }
                                }
                            });
                        } else {
//                            mProgress.dismiss();
                            dialogDismiss();
                            PublicPun.showNoticeDialog(mContext, "Error Message", "Error:" + Integer.toHexString(status));
                        }
                    }
                });
            }
        }.start();
    }

    private void CreateNewAccount(final int accountId) {
        String accName = "";
        cmdManager.hdwCreateAccount(accountId, accName, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    byte[] balance = new byte[8];

                    // set default balance
                    cmdManager.hdwSetAccInfo(PublicPun.user.getMacKey(), (byte) 0x01, accountId, balance, new CmdResultCallback() {
                        @Override
                        public void onSuccess(int status, byte[] outputData) {
                            if ((status + 65536) == 0x9000) {
                                LogUtil.i("CwAccount " + accountId + " created!");
                                mHorizontalDialog.incrementProgressBy(2);
                                //Get public key & chainCode to generate  address
                                FunQueryAccountKeyInfo(accountId);
                            }
                        }
                    });
                } else {
                    dialogDismiss();
                    PublicPun.showNoticeDialog(mContext, "Error Message", "CreateNewAccount Error:" + Integer.toHexString(status));
                }
            }
        });
    }

    private void FunQueryAccountKeyInfo(final int accountId) {
        final byte cwHdwAccountInfoName = 0x00;
        final byte cwHdwAccountInfoBalance = 0x01;
        final byte cwHdwAccountInfoExtKeyPtr = 0x02;
        final byte cwHdwAccountInfoIntKeyPtr = 0x03;
        final byte cwHdwAccountInfoBlockAmount = 0x04;

        final boolean[] flag = new boolean[5];

        final byte[] cwHdwAccountInfo = new byte[]{cwHdwAccountInfoName, cwHdwAccountInfoBalance,
                cwHdwAccountInfoExtKeyPtr, cwHdwAccountInfoIntKeyPtr, cwHdwAccountInfoBlockAmount};

        cwAccountList = new ArrayList<>();
        final Account account = new Account();
        LogUtil.i("QueryAccountKeyInfo accountId=" + accountId);
        account.setId(accountId);

        for (int i = 0; i < cwHdwAccountInfo.length; i++) {
            final int qryAcctInfoIndex = i;
            cmdManager.hdwQueryAccountInfo(cwHdwAccountInfo[i], accountId, new CmdResultCallback() {
                @Override
                public void onSuccess(int status, byte[] outputData) {
                    if ((status + 65536) == 0x9000) {

                        if (outputData != null) {
                            LogUtil.i("SWITCH=" + cwHdwAccountInfo[qryAcctInfoIndex]);
                            switch (cwHdwAccountInfo[qryAcctInfoIndex]) {
                                case cwHdwAccountInfoName:

                                    String accName = new String(outputData, Constant.UTF8).trim();
                                    LogUtil.e("accName=" + accName);
                                    account.setName(accName);
                                    flag[0] = true;
                                    break;

                                case cwHdwAccountInfoBalance:

//                                    String strbalance = PublicPun.byte2HexString(outputData).replace(" ", "");
//                                    long balance = Long.valueOf(strbalance, 16);
//                                    double TotalBalance = balance * PublicPun.SATOSHI_RATE;
//                                    LogUtil.e("balance hex=" + strbalance);
//                                    LogUtil.i("balance long=" + balance);
//                                    LogUtil.i("balance double=" + TotalBalance);
//                                    account.setTotalBalance(TotalBalance);
                                    flag[1] = true;
                                    break;

                                case cwHdwAccountInfoExtKeyPtr:

                                    String extKey = PublicPun.byte2HexString(BTCUtils.reverse(outputData)).replace(" ", "");
                                    int IntExtKey = Integer.valueOf(extKey, 16);
                                    LogUtil.i("hdwQueryAccountInfo accID=" + accountId + "; key=" + cwHdwAccountInfoExtKeyPtr + "; ExtKey pointer=" + IntExtKey + " ;" + outputData[0]);
                                    account.setOutputIndex(IntExtKey);

                                    getAccountKeyInfo(accountId, IntExtKey, Constant.CwAddressKeyChainExternal);
                                    flag[2] = true;
                                    break;

                                case cwHdwAccountInfoIntKeyPtr:

                                    String intKey = PublicPun.byte2HexString(BTCUtils.reverse(outputData)).replace(" ", "");
                                    int IntIntKey = Integer.valueOf(intKey, 16);
                                    LogUtil.i("hdwQueryAccountInfo accID=" + accountId + "; key=" + cwHdwAccountInfoIntKeyPtr + "; intKey pointer=" + IntIntKey + " ;hex=" + intKey);
                                    account.setInputIndex(IntIntKey);

                                    getAccountKeyInfo(accountId, IntIntKey, Constant.CwAddressKeyChainInternal);
                                    flag[3] = true;
                                    break;

                                case cwHdwAccountInfoBlockAmount:

                                    String blockAmount = PublicPun.byte2HexString(outputData).replace(" ", "");
                                    LogUtil.e("blockAmount=" + blockAmount);
                                    account.setBlockAmount(Double.parseDouble(blockAmount));
                                    flag[4] = true;
                                    break;
                            }
                            if (flag[0] && flag[1] && flag[2] && flag[3] && flag[4]) {
                                cwAccountList.add(account);
                            }
                        }
                    }
                }
            });
        }
    }

    private void getAccountKeyInfo(final int accountId, final int kid, final byte kcId) {

        cmdManager.hdwQueryAccountKeyInfo(Constant.CwHdwAccountKeyInfoPubKeyAndChainCd,
                kcId,
                accountId,
                kid,
                new CmdResultCallback() {
                    @Override
                    public void onSuccess(int status, byte[] outputData) {
                        if ((status + 65536) == 0x9000) {
                            if (outputData != null) {
                                byte[] publicKeyBytes = new byte[64];
                                byte[] chainCodeBytes = new byte[32];
                                int length = outputData.length;
                                byte[] extendPub = new byte[33];
                                if (length >= 96) {

                                    for (int i = 0; i < 64; i++) {
                                        publicKeyBytes[i] = outputData[i];
                                    }
                                    for (int j = 64; j < 96; j++) {
                                        chainCodeBytes[j - 64] = outputData[j];
                                    }
                                    //最後兩個字元一起
                                    LogUtil.i("建地址的public key=" + LogUtil.byte2HexString(publicKeyBytes));
                                    LogUtil.i("建地址的chainCodeBytes=" + LogUtil.byte2HexString(chainCodeBytes));

                                    int mFirstKey = Integer.parseInt(PublicPun.byte2HexString(publicKeyBytes[63]), 16);
                                    LogUtil.i("public key的最後字元=" + PublicPun.byte2HexString(publicKeyBytes[63]) +
                                            ";轉int=" + mFirstKey);

                                    //format last charactors
                                    if (mFirstKey % 2 == 0) {
                                        extendPub[0] = 02;
                                    } else {
                                        extendPub[0] = 03;
                                    }
                                    for (int a = 0; a < 32; a++) {
                                        extendPub[a + 1] = publicKeyBytes[a];
                                    }
                                }
                                DatabaseHelper.insertAccountKeyInfo(mContext, accountId, kcId,
                                        PublicPun.byte2HexStringNoBlank(extendPub), PublicPun.byte2HexStringNoBlank(chainCodeBytes));

                                ContentValues cv = new ContentValues();
                                cv.put("URL", BtcUrl.URL_BLICKCHAIN_TXS_MULTIADDR);
                                cv.put("ACCOUNT_ID", accountId);
                                cv.put("KCID", kcId);
                                cv.put("PUBLIC_KEY", extendPub);
                                cv.put("CHAIN_CODE", chainCodeBytes);
                                //getAddressesTxs

                                new Thread(new MyRunnable(mHandler, cv, 0, 0)).start();
                            }
                        }
                    }
                });
    }

    private void FunhdwSetAccInfo(int account) {

        LogUtil.e("這是FunhdwSetAccInfo=" + account);

        //for card display
        byte ByteAccId = (byte) account;
        cmdManager.McuSetAccountState(ByteAccId, new CmdResultCallback() {
                    @Override
                    public void onSuccess(int status, byte[] outputData) {
                        if ((status + 65536) == 0x9000) {
                            LogUtil.i("McuSetAccountState !!!");
                        }
                    }
                }
        );

        //set CW Card
        final byte cwHdwAccountInfoName = 0x00;
        final byte cwHdwAccountInfoBalance = 0x01;
        final byte cwHdwAccountInfoExtKeyPtr = 0x02;
        final byte cwHdwAccountInfoIntKeyPtr = 0x03;

        byte[] accountInfo = new byte[32];
        long TotalBalance = 0;
        int extKey = 0;
        int intKey = 0;
        ArrayList<dbAddress> listAddress = new ArrayList<dbAddress>();
        listAddress = DatabaseHelper.queryAddress(RecovWalletActivity.this, account, -1);//all addr(ext int)

        for (int i = 0; i < listAddress.size(); i++) {
            TotalBalance += listAddress.get(i).getBalance();
            if (listAddress.get(i).getKcid() == 0) {
                extKey++;
            }
            if (listAddress.get(i).getKcid() == 1) {
                intKey++;
            }
        }

        final boolean[] flag = new boolean[4];

        final byte[] cwHdwAccountInfo = new byte[]{cwHdwAccountInfoName, cwHdwAccountInfoBalance,
                cwHdwAccountInfoExtKeyPtr, cwHdwAccountInfoIntKeyPtr};


        for (int i = 0; i < cwHdwAccountInfo.length; i++) {
            final int setAcctInfoIndex = i;
            LogUtil.i("SWITCH=" + cwHdwAccountInfo[setAcctInfoIndex]);
            switch (cwHdwAccountInfo[setAcctInfoIndex]) {
                case cwHdwAccountInfoName:
                    //00h: Account name (32 bytes)
                    accountInfo = new byte[32];
                    break;

                case cwHdwAccountInfoBalance:
                    accountInfo = new byte[8];
                    //204E000000000000
//                    byte[] newBalanceBytes = ByteUtil.intToByteLittle(TotalBalance, 8);
                    byte[] newBalanceBytes =
                            ByteBuffer.allocate(8).putLong(TotalBalance).order(ByteOrder.BIG_ENDIAN).array();
                    accountInfo = newBalanceBytes;
                    break;

                case cwHdwAccountInfoExtKeyPtr:
                    accountInfo = new byte[4];
                    accountInfo = ByteUtil.intToByteLittle(extKey, 4);
//                    accountInfo = ByteUtil.intToByteLittle(5, 4);
                    break;

                case cwHdwAccountInfoIntKeyPtr:
                    accountInfo = new byte[4];
                    accountInfo = ByteUtil.intToByteLittle(intKey, 4);
//                    accountInfo = ByteUtil.intToByteLittle(10, 4);
                    break;
            }
            LogUtil.i("hdwSetAccInfo:" + account + "的 " + setAcctInfoIndex + " =" + PublicPun.byte2HexString(accountInfo));
            cmdManager.hdwSetAccInfo(PublicPun.user.getMacKey(), cwHdwAccountInfo[setAcctInfoIndex], account, accountInfo, new CmdResultCallback() {
                        @Override
                        public void onSuccess(int status, byte[] outputData) {
                            if ((status + 65536) == 0x9000) {
                                flag[setAcctInfoIndex] = true;

                                if (flag[0] && flag[1] && flag[2] && flag[3]) {
                                    mHorizontalDialog.incrementProgressBy(3);
                                    msgSetAccInfoExcute++;
                                    LogUtil.i("setAccountInfo msgSetAccInfoExcute=" + msgSetAccInfoExcute + ";accountCNT=" + accountCNT);
                                }

                                if (msgSetAccInfoExcute == accountCNT) {//set accountInfo (account 1~account 5)//accountCNT
                                    // recovey watch dog setting
                                    SetSecpo(false);

                                    //for card display
                                    byte ByteAccId = (byte) 0;
                                    cmdManager.McuSetAccountState(ByteAccId, new CmdResultCallback() {
                                                @Override
                                                public void onSuccess(int status, byte[] outputData) {
                                                    if ((status + 65536) == 0x9000) {
                                                        LogUtil.i("McuSetAccountState !!!");
                                                    }
                                                }
                                            }
                                    );

                                    dialogDismiss();
                                    PublicPun.toast(mContext, "Recovery finish!");
                                    Intent intent = new Intent();
                                    intent.setClass(getApplicationContext(), FragMainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//关掉所要到的界面中间的activity
                                    intent.putExtra("Parent", RecovWalletActivity.class.getSimpleName());
                                    startActivity(intent);
                                    finish();
//                                    FunQueryAccountKeyInfo(0);
                                }
                            } else {
                                LogUtil.i("setAccountInfo failed.");
                                PublicPun.showNoticeDialog(mContext, "Recovery Message", "setAccountInfo failed!");
//                                mProgress.dismiss();
                                dialogDismiss();
                            }
                        }
                    }
            );
        }
    }

    private void initViews() {
        btnCreateWallet = (Button) findViewById(R.id.btn_hdw_create);
        edtHdWord = (EditText) findViewById(R.id.hdw_word);
        seedSpinner = (Spinner) findViewById(R.id.seed_spinner);

        listSeed = new ArrayAdapter<String>(RecovWalletActivity.this, R.layout.spinner_textview, strSeed);
        //設置下拉列表的風格
        listSeed.setDropDownViewResource(R.layout.spinner_textview);
        seedSpinner.setAdapter(listSeed);
        btnCreateWallet.setOnClickListener(this);
        seedSpinner.setOnItemSelectedListener(this);
//        edtHdWord.setOnKeyListener(this);
        edtHdWord.addTextChangedListener(this);

        edtHdWord.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //按下完成鍵要執行的動作
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                boolean isOpen = imm.isActive();//isOpen若返回true，則表示輸入法打開
                if (isOpen) {
                    imm.hideSoftInputFromWindow(edtHdWord.getWindowToken(), 0); //強制隱藏鍵盤
                }
                return true;
            }
        });
        //當介面再次顯示時，資料清空歸零
        isSeedOn = true;
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.import_seed));
        setSupportActionBar(toolbar);
        // Navigation Icon設定在 setSupoortActionBar後才有作用,否則會出現 back button_up
        toolbar.setNavigationIcon(R.mipmap.menu_3x);
        ActionBar actionBar = getSupportActionBar();
        // 打開 up button_up
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if (parent == seedSpinner) {
            if (position == 0) {

                //by words gen seed(BIP32)
//                edtHdWord.setInputType(InputType.TYPE_CLASS_TEXT);
                isSeedOn = false;
                edtHdWord.setText("");
            } else {

                //by numbers gen seed
                // edtHdWord.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);
                isSeedOn = true;
                edtHdWord.setText("");
            }
            LogUtil.i("choose seed type:" + position + "=" + (isSeedOn ? "words" : "numbers" ));
        }
    }

    private void showHorizontalDialog() {
        mHorizontalDialog = new ProgressDialog(mContext, R.style.CustomProgressDialog);
        mHorizontalDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mHorizontalDialog.setProgress(0);
        mHorizontalDialog.setMax(100);
        mHorizontalDialog.setIndeterminate(false);
        mHorizontalDialog.setCancelable(false);
        mHorizontalDialog.getWindow().setGravity(Gravity.BOTTOM);
        mHorizontalDialog.show();
    }

    private void dialogDismiss() {
        if (mHorizontalDialog != null) mHorizontalDialog.dismiss();
        pop.dismiss();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (settingOptions[3]) {
            SetSecpo(false);
        }
    }

    @Override
    protected void onResume() {

        super.onResume();
        //註冊監聽
        registerBroadcast(this, cmdManager);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unRegisterBroadcast(this);
    }


    private class MyRunnable implements Runnable {
        ContentValues cv;
        int what = -1;
        Handler handler;
        int interval;
        String extralUrl;
        byte kcId;
        byte[] extendPub;
        byte[] chainCodeBytes;
        int getAddrKID;
        int noTrsCnt;
        int accountId;
        int msgCnt;

        public MyRunnable(Handler handler, ContentValues cv, int what, int interval) {
            this.cv = cv;
            this.what = what;
            this.handler = handler;
            this.interval = interval;

            this.extralUrl = cv.getAsString("URL");
            this.accountId = cv.getAsInteger("ACCOUNT_ID");
            this.kcId = cv.getAsByte("KCID");
            this.extendPub = cv.getAsByteArray("PUBLIC_KEY");
            this.chainCodeBytes = cv.getAsByteArray("CHAIN_CODE");

            this.noTrsCnt = 0;
            this.getAddrKID = 0;
            this.msgCnt = 0;
        }

        @Override
        public void run() {
            String result = "";

            while (noTrsCnt < 5) {
                //noTrsCnt累積到5就break,有交易的話就重新歸0
                ExtendedKey km = ExtendedKey.createCwEk(extendPub, chainCodeBytes);
                LogUtil.i("account=" + accountId + " ;kcid=" + kcId + " ;publicKey=" + LogUtil.byte2HexString(extendPub) + " ;chainCode=" + LogUtil.byte2HexString(chainCodeBytes) + " ;產地址的serializepub=" + km.serializepub(true));

                ExtendedKey k = null;
                String addr = "";
                boolean isHaveTrs;
                try {
                    k = km.getChild(getAddrKID);
                    addr = k.getAddress();
                    LogUtil.i("ExtendedKey:第 " + accountId + "account的" + +kcId + "kcid的第 " + String.valueOf(getAddrKID) + " 個地址= " + k.getAddress());
                    DatabaseHelper.insertAddress(mContext, accountId, addr, kcId, getAddrKID, 0, 0);
                    //18xNmo8ZoiZwuCfUaLjACUtntbpwpZ2jc9; address which is no trs.
                    cv.put("addresses", addr);

                } catch (Exception e) {
                    LogUtil.i("ExtendedKey:" + kcId + " 第 " + String.valueOf(getAddrKID) + " 個地址,error:" + e.getMessage());
                    Message msg = handler.obtainMessage();
                    Bundle data = new Bundle();
                    data.putString("resultMsg", e.getMessage() + " create error!");
                    msg.setData(data);
                    msgCnt = 0;
                    msg.what = msgCnt;
                    handler.sendMessage(msg);
//                    mProgress.dismiss();
                    dialogDismiss();
                }

                try {
                    //CALL BLOCKCHAIN to get Addresses info
                    result = cwBtcNetWork.doGet(cv, extralUrl, null);
                    ArrayList<CwBtcTxs> lisCwBtcTxs = new ArrayList<CwBtcTxs>();

                    if (result != null && !result.contains("errorCode")) {
                        if (!PublicPun.jsonParserRecoveryAddresses(mContext, result, accountId, addr, kcId, getAddrKID, false)) {
                            //no txs accumulate to 5 times,stop to get addresses.
                            noTrsCnt++;
                        } else {
                            noTrsCnt = 0;
                        }
                        LogUtil.i("ACCOUNT:" + accountId + "-" + kcId + "的noTrsCnt 數量:" + noTrsCnt);

                        if (noTrsCnt == 5) {
                            msgCnt = 1;
                            LogUtil.i("sendMessage:" + msgCnt);
                            Message msg = handler.obtainMessage();
                            Bundle data = new Bundle();
                            data.putString("result", String.valueOf(accountId) + String.valueOf(kcId));
                            msg.setData(data);
                            msg.what = msgCnt;
                            handler.sendMessage(msg);
                        }
                    }
                } catch (NetworkOnMainThreadException e) {
                    LogUtil.i("doGet 錯誤:" + e.toString());
                }
                getAddrKID++;//addresses index(KID)
            }
        }
    }



}
