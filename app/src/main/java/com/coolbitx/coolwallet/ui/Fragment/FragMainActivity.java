package com.coolbitx.coolwallet.ui.Fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.coolbitx.coolwallet.DataBase.DatabaseHelper;
import com.coolbitx.coolwallet.DataBase.DbName;
import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.Service.BTConfig;
import com.coolbitx.coolwallet.Service.BlockSocketHandler;
import com.coolbitx.coolwallet.bean.Account;
import com.coolbitx.coolwallet.bean.Address;
import com.coolbitx.coolwallet.bean.CWAccountKeyInfo;
import com.coolbitx.coolwallet.bean.Constant;
import com.coolbitx.coolwallet.bean.XchsSync;
import com.coolbitx.coolwallet.bean.dbAddress;
import com.coolbitx.coolwallet.bean.socketByAddress;
import com.coolbitx.coolwallet.callback.RefreshCallback;
import com.coolbitx.coolwallet.general.AppPrefrence;
import com.coolbitx.coolwallet.general.BtcUrl;
import com.coolbitx.coolwallet.general.CSVReadWrite;
import com.coolbitx.coolwallet.general.NotificationReceiver;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.general.RefreshBlockChainInfo;
import com.coolbitx.coolwallet.httpRequest.CwBtcNetWork;
import com.coolbitx.coolwallet.ui.BaseActivity;
import com.coolbitx.coolwallet.ui.CoolWalletCardActivity;
import com.coolbitx.coolwallet.ui.ExchangeLogin;
import com.coolbitx.coolwallet.ui.HostDeviceActivity;
import com.coolbitx.coolwallet.ui.InitialCreateWalletIIActivity;
import com.coolbitx.coolwallet.ui.InitialSecuritySettingActivity;
import com.coolbitx.coolwallet.ui.LogOutActivity;
import com.coolbitx.coolwallet.ui.RecovWalletActivity;
import com.coolbitx.coolwallet.ui.SettingActivity;
import com.coolbitx.coolwallet.ui.ShareAddress;
import com.coolbitx.coolwallet.util.Base58;
import com.crashlytics.android.Crashlytics;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.ByteUtil;
import com.snscity.egdwlib.utils.LogUtil;

import org.json.JSONStringer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ShihYi on 2015/12/4.
 */
//        FragmentActivity
public class FragMainActivity extends BaseActivity {//implements CompoundButton.OnCheckedChangeListener

    //左側選單圖片
    private static final int[] MENU_ITEMS_PIC = new int[]{
            R.mipmap.host, R.mipmap.cwcard, R.mipmap.security, R.mipmap.settings, R.drawable.exchange,
            R.drawable.exchange, R.mipmap.ic_feedback_white_24dp, R.mipmap.logout, R.mipmap.ic_share_white_24dp};
    //     左側選單文字項目
    private static final String[] MENU_ITEMS = new String[]{
            "Host devices", "CoolWallet card", "Security", "Settings", "Exchange",
            "Exchange Login", "Issue Feedback", "Logout", "Share address\n(beta)"
    };

//    private static final int[] MENU_ITEMS_PIC = new int[]
//            {R.mipmap.host, R.mipmap.cwcard, R.mipmap.security, R.mipmap.settings,
//                    R.mipmap.ic_feedback_white_24dp, R.mipmap.logout, R.mipmap.ic_share_white_24dp};
//    // 左側選單文字項目
//    private static final String[] MENU_ITEMS = new String[]{
//            "Host devices", "CoolWallet card", "Security", "Settings",
//            "Issue Feedback", "Logout", "Share address\n(beta)"
//    };

    public static int ACCOUNT_CNT = 0;//這裡要改為抓qryWalletInfo的
    public static boolean refreshFlag = false;
    public CmdManager cmdManager;
    public static DecimalFormat BtcFormatter = new DecimalFormat("#.########");
    public static BlockSocketHandler socketHandler;
    public static IntentResult scanningResult;
    private static OnResumeFromBackCallBack mOnResumeFromBackCallBack = null;
    private static Boolean isExit = false;
    private static Boolean hasTask = false;
    int addrAuccess = 0;
    Bundle mSavedInstanceState;
    CwBtcNetWork cwBtcNetWork;
    Context mContext;
    StringBuilder sbAdd = new StringBuilder();
    int msgSetAccInfoExcute;
    boolean isFromRecovery;
    boolean isNewUser = false;
    boolean isPointerSame;
    int needToRefreshCnt;
    int IntExtKey = 0;
    int IntIntKey = 0;
    TabFragment tabFragment;

    private NotificationReceiver brocastNR;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String identify = data.getString("identify");
            String val = data.getString("result");
            try {
                if (val != null) {
                    if (identify.equals(BtcUrl.URL_BLOCKCHAIN_EXCHANGE_RATE)) {

                        SetCurrencyRate(mContext);
                        //card is not keep the setting.
                        if (AppPrefrence.getCurrency(mContext)) {
                            cmdManager.turnCurrency(AppPrefrence.getCurrency(mContext), new CmdResultCallback() {
                                @Override
                                public void onSuccess(int status, byte[] outputData) {
                                    if ((status + 65536) == 0x9000) {

                                    }
                                }
                            });
                        }
                    }
                }
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        }
    };
    int issueCnt;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private LinearLayout mLlvDrawerContent;
    private ListView mLsvDrawerMenu;
    private List<HashMap<String, Object>> mHashMaps;
    private HashMap<String, Object> map;
    private List<Account> cwAccountList = new ArrayList<>();
    private int getWallteStatus = 0x03;
    private Address address;
    private SimpleAdapter mAdapter;
    private CSVReadWrite mLoginCsv;
    private ProgressDialog mProgress;
    private String mParentActivityName = null;
    private Timer mTimer;
    private TextView tvVer;
    private Handler socketMsgHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub

            switch (msg.what) {
                case BSConfig.HANDLER_SOCKET:
                    socketByAddress socket = (socketByAddress) msg.obj;
                    final int mAccount = DatabaseHelper.queryAccountByAddress(mContext, socket.getAddress());
                    if (socket.getTx_type().equals("Received") && socket.getConfirmations() <= 1) {
                        String socketTitle = "BitCoin Received";
                        String socketMsg = "Account " + (mAccount + 1) + "\n"
                                + "Address:" + "\n"
                                + socket.getAddress() + "\n"
                                + socket.getTx_type() + " Amount:" + TabFragment.BtcFormatter.format(socket.getBtc_amount()) + " BTC" + "\n"
                                + "Confirmations: " + socket.getConfirmations();
//                        PublicPun.showNoticeDialog(mContext, socketTitle, socketMsg);
                    }
                    break;
                case BSConfig.HANDLER_DISCONN:
                    String title = "CoolWallet Disconnected";
                    String noteMsg = PublicPun.card.getCardName() + " Disconnected";
//                    PublicPun.showNoticeDialogToFinish(mContext, title, noteMsg);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public static void registerOnResumeFromBackCallBack(OnResumeFromBackCallBack cb) {
        mOnResumeFromBackCallBack = cb;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frag_main);

        LogUtil.d("lifeCycle FragMainActivity onCreate");

        Intent intent = getIntent();
        mSavedInstanceState = savedInstanceState;
        if (intent != null) {
            mParentActivityName = intent.getStringExtra("Parent");
            if (mParentActivityName != null) {
                LogUtil.d("mParentActivityName=" + mParentActivityName + ";getSimpleName=" + RecovWalletActivity.class.getSimpleName());
                isFromRecovery = mParentActivityName.equals(RecovWalletActivity.class.getSimpleName());
            }

        }

        //LogUtil.e("class name = " + mParentActivityName);

        mContext = this;
        cwBtcNetWork = new CwBtcNetWork();
        cmdManager = new CmdManager();
        address = new Address();
        mLoginCsv = new CSVReadWrite(mContext);

        initToolbar();
        initView();

        mProgress.setMessage("Synchronizing data...");
        mProgress.show();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (mProgress.isShowing()) {
                            mProgress.dismiss();
                        }
                    }
                });
                mTimer.cancel();
            }
        }, 20000);////20s沒成功就自動cacel

        try {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    socketHandler = new BlockSocketHandler(FragMainActivity.this);
                }
            };
            thread.start();

            getModeState();
            getHosts();//獲取設備資訊
            getCardId();//獲取卡片id
            getCardName();//獲取卡片名稱
            getCurrRate();
            queryWallteInfo(getWallteStatus);
//            registerBroadcast();


        } catch (Exception e) {
            e.printStackTrace();
//            Crashlytics.log("FragMainActivity onCreate failed=" + e.getMessage());
        }
    }

    public CmdManager getCmdManager() {
        return cmdManager;
    }


    private void initView() {

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.cw_refresh);
//        tvVer = (TextView) findViewById(R.id.main_tvVer);

//        try {
//            PackageInfo info = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
//            tvVer.setText("V" + info.versionName);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
        mProgress = new ProgressDialog(FragMainActivity.this, ProgressDialog.THEME_HOLO_DARK);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
    }

    private void initTabFragment(Bundle savedInstanceState) {
        try {
            if (savedInstanceState == null) {
                if (tabFragment == null) {
                    tabFragment = new TabFragment();
                    getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.content_fragment, tabFragment)
                            .commitAllowingStateLoss();
                }
            } else {
                if (tabFragment == null) {
                    tabFragment = new TabFragment();
                    getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.content_fragment, tabFragment)
                            .commit();
                }
            }
        } catch (Exception e) {
            Crashlytics.log("initTabFragment failed:" + e.getMessage());
        }
    }

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(getResources().getDrawable(R.mipmap.logo2));
//        toolbar.setLogo(getResources().getDrawable(R.drawable.actionbar_logo));
//        String title="";
//        if(tabFragment==null){
//            title = "Account";
//        }else{
//            if(tabFragment.getPageType()==0){
//                title = "Account";
//            }else if(tabFragment.getPageType()==1){
//                title = "Send";
//            }else{
//                title = "Receive";
//            }
//        }
//        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.menu_3x);
        // Navigation Icon設定在 setSupoortActionBar後才有作用,否則會出現 back button_up

        ActionBar actionBar = getSupportActionBar();
        // 左上角顯示返回建
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
//        actionBar.setIcon(
//                new ColorDrawable(getResources().getColor(android.R.color.transparent))); //在这里把图标改成透明色了
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // 實作 drawer toggle 並放入 toolbar
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        mActionBarDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);

        setDrawerMenu();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 判斷是否按下Back
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 是否要退出
            if (isExit == false) {
                isExit = true; //記錄下一次要退出
                Toast.makeText(getBaseContext(), "Press once again to exit!", Toast.LENGTH_SHORT).show();
                // 如果超過兩秒則恢復預設值
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        isExit = false; // 取消退出
                        hasTask = true;
                    }
                }, 2000); // 如果2秒鐘內沒有按下返回鍵，就啟動定時器取消剛才執行的任務
            } else {
                finish(); // 離開程式
                System.exit(0);
            }
        }
        return false;
    }

    private void queryWallteInfo(int infoId) {

//        00h: HDW status (1 byte)
//        01h: HDWname (32 bytes)
//        02h: HDW account pointer (4 bytes)
//        03h: All HDW info (37 bytes)
        cmdManager.hdwQryWaInfo(infoId, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    //01 status
                    if (outputData[0] == 0x02) {
                        PublicPun.wallet.setStatus("ACTIVE");
                    } else {
                        //00 is INACTIVE ->進CREATE WALLET
                        //01 is WAITACTV ->進CREATE WALLET
                        //02 IS ACTIVE   ->進MAIN PAGE
                        Intent intent = new Intent(getApplicationContext(), InitialCreateWalletIIActivity.class);
                        startActivity(intent);
                    }
                    //如果是激活狀態,繼續獲取名字和帳戶id
                    //02 name
                    byte[] acountNamer = new byte[32];
                    for (int i = 0; i < 32; i++) {
                        acountNamer[i] = outputData[1 + i];
                    }
                    String hdwName = new String(outputData, Constant.UTF8).toString().trim();
                    LogUtil.i("hdwName=" + hdwName);
                    PublicPun.wallet.setName(hdwName);
                    byte[] hdwAccountPointer = new byte[4];
                    //03 accountPointer
                    for (int i = 0; i < 4; i++) {
                        hdwAccountPointer[i] = outputData[33 + i];
                    }
                    if (hdwAccountPointer != null && hdwAccountPointer.length == 4) {
                        ACCOUNT_CNT = Integer.parseInt(PublicPun.byte2HexString(hdwAccountPointer[0]));
                    }
                    LogUtil.i("hdwQryWaInfo 回傳 accountPoint=" + ACCOUNT_CNT);
                    PublicPun.wallet.setAccountIndex(ACCOUNT_CNT);
                    if (ACCOUNT_CNT == 0) {
                        //没有帳戶,需要去創建帳戶
                        //for 如果是用別支手機reset的case
                        DatabaseHelper.deleteTable(mContext, DbName.DB_TABLE_TXS);
                        DatabaseHelper.deleteTable(mContext, DbName.DB_TABLE_ADDR);
                        isNewUser = true;
                        String accName = "";
                        cmdManager.hdwCreateAccount(ACCOUNT_CNT, accName, new CmdResultCallback() {
                            @Override
                            public void onSuccess(int status, byte[] outputData) {
                                if ((status + 65536) == 0x9000) {
                                    byte[] balance = new byte[8];
                                    cmdManager.hdwSetAccInfo(PublicPun.user.getMacKey(), (byte) 0x01, ACCOUNT_CNT, balance, new CmdResultCallback() {
                                        @Override
                                        public void onSuccess(int status, byte[] outputData) {
                                            if ((status + 65536) == 0x9000) {
                                                PublicPun.toast(mContext, "CwAccount Created");
                                                LogUtil.i("初始帳戶建立完成");
                                                LogUtil.i("初始新建地址");
                                                genChangeAddress(Constant.CwAddressKeyChainExternal);
                                                ACCOUNT_CNT++;
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    } else {
                        //thread 從這裡分出.
                        //舊用戶 or Recovery用戶
                        if (!cwAccountList.isEmpty()) {
                            cwAccountList.clear();
                        }

                        LogUtil.d("recovery:" + isFromRecovery + " 有" + ACCOUNT_CNT + "個帳戶" + "依序讀取帳戶訊息");
                        if (isFromRecovery) {
                            //do nothing
                            if (mProgress.isShowing()) {
                                mProgress.dismiss();
                            }
                            //Rrecovery進來後當次切換頁面不需再更新
                            for (int i = 0; i < PublicPun.accountRefresh.length; i++) {
                                PublicPun.accountRefresh[i] = true;
                            }
                            initTabFragment(mSavedInstanceState);
                        } else {
                            //一開始進入只需抓account0抓交易資料
                            final int refreshAccount = 0;
                            final RefreshBlockChainInfo refreshBlockChainInfo = new RefreshBlockChainInfo(mContext, refreshAccount);
                            refreshBlockChainInfo.FunQueryAccountInfo(cmdManager, new RefreshCallback() {
                                @Override
                                public void success() {

                                    refreshBlockChainInfo.callTxsRunnable(new RefreshCallback() {
                                        @Override
                                        public void success() {
                                            initTabFragment(mSavedInstanceState);
                                            if (mProgress.isShowing()) {
                                                mProgress.dismiss();
                                            }
                                            if (!PublicPun.accountRefresh[0]) {
                                                FunhdwSetAccInfo(refreshAccount);
                                                PublicPun.accountRefresh[0] = true;
                                            }
                                        }

                                        @Override
                                        public void fail(String msg) {
                                            PublicPun.showNoticeDialog(mContext, "Unstable internet connection", msg);
                                            initTabFragment(mSavedInstanceState);
                                            if (mProgress.isShowing()) {
                                                mProgress.dismiss();
                                            }
                                        }


                                    });
                                }

                                @Override
                                public void fail(String msg) {
                                    PublicPun.showNoticeDialog(mContext, "Unstable internet connection", msg);
                                    initTabFragment(mSavedInstanceState);
                                    if (mProgress.isShowing()) {
                                        mProgress.dismiss();
                                    }
                                }

                            });
                        }
                    }
                }
            }
        });
    }

    private void FunhdwSetAccInfo(int account) {
        msgSetAccInfoExcute = 0;
        LogUtil.e("這是Main FunhdwSetAccInfo=" + account + " ; 丟webSocket:" + PublicPun.accountSocketReg[account]);
        byte ByteAccId = (byte) account;
        //for card display
        cmdManager.McuSetAccountState(ByteAccId, new CmdResultCallback() {
                    @Override
                    public void onSuccess(int status, byte[] outputData) {
                        if ((status + 65536) == 0x9000) {
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
        listAddress = DatabaseHelper.queryAddress(mContext, account, -1);//ext+int


        for (int i = 0; i < listAddress.size(); i++) {
            if (!PublicPun.accountSocketReg[account]) {
                if (listAddress.get(i).getKcid() == 0) { //只需註冊external地址
                    try {
                        socketHandler.SendMessage(PublicPun.jSonGen(listAddress.get(i).getAddress()));
                    } catch (Exception e) {
                        e.getStackTrace();
                        LogUtil.e("socket send failed=" + e.getMessage());
                    }
                }
            }

            TotalBalance += listAddress.get(i).getBalance();
            if (listAddress.get(i).getKcid() == 0) {
                extKey++;
            }
            if (listAddress.get(i).getKcid() == 1) {
                intKey++;
            }
        }
        PublicPun.accountSocketReg[account] = true;
        LogUtil.i("after set socket account=" + PublicPun.accountSocketReg[account]);
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
                                    msgSetAccInfoExcute++;
                                    LogUtil.i("setAccountInfo account完成次數=" + msgSetAccInfoExcute + " ; ACCOUNT_CNT次數=" + ACCOUNT_CNT);
                                    //set完所有資料才開放

                                    if (msgSetAccInfoExcute == ACCOUNT_CNT) {//set accountInfo (account 1~account 5)
                                        cmdManager.McuSetAccountState((byte) 0, new CmdResultCallback() {
                                                    @Override
                                                    public void onSuccess(int status, byte[] outputData) {
                                                        if ((status + 65536) == 0x9000) {
                                                            LogUtil.i("McuSetAccountState !!!");
                                                        }
                                                    }
                                                }
                                        );
                                    }
                                }
                            } else {
                                LogUtil.i("setAccountInfo failed.");
                                PublicPun.showNoticeDialog(mContext, "Alert Message", "setAccountInfo failed!");
                                mProgress.dismiss();
                            }
                        }
                    }
            );
        }
    }

    public void genChangeAddress(final int keyChainId) {
        final int accountId = PublicPun.account.getId();
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

                        String keyStr = PublicPun.byte2HexString(keyIdBytes[0]);
                        int keyId = Integer.valueOf(keyStr, 16);
                        address.setKeyId(keyId);

                        byte[] addressBytes = new byte[25];
                        if (length >= 29) {
                            for (int i = 0; i < 25; i++) {
                                addressBytes[i] = outputData[i + 4];
                            }
                        }

                        byte[] addrBytes = Base58.encode(addressBytes);//34b
                        String addr = new String(addrBytes, Constant.UTF8);
                        address.setAddress(addr);

                        List<Address> intputAddressList = PublicPun.account.getInputAddressList();
                        if (intputAddressList == null) {
                            intputAddressList = new ArrayList<>();
                        }
                        intputAddressList.add(address);
                        PublicPun.account.setInputAddressList(intputAddressList);
                        LogUtil.i("初始帳戶地址:" + addr);

                        PublicPun.account.setInputIndex(PublicPun.account.getInputIndex() + 1);
                        //初始帳戶
//                        Context context, int accountID, String addr, int kcid,int kid, int n_tx, long balance
                        DatabaseHelper.insertAddress(mContext, 0, addr, 0, keyId, 0, 0);
                        //不能搬到FunQueryAccountInfo裡,因為另一段也會call到
                        mProgress.dismiss();
                        initTabFragment(mSavedInstanceState);
                    }
                }
            }
        });
    }

    private void getCurrRate() {
        DatabaseHelper.deleteTable(mContext, DbName.DB_TABLE_CURRENT);
        ContentValues cv = new ContentValues();
        new Thread(new MyRunnable(mHandler, cv, BtcUrl.URL_BLOCKCHAIN_EXCHANGE_RATE, 0, 60000 * 30, 0, cwBtcNetWork)).start();//1hr updated
        new Thread(new MyRunnable(mHandler, cv, BtcUrl.RECOMMENDED_TRANSACTION_FEES, 0, 60000 * 30, 0, cwBtcNetWork)).start();//1hr updated
    }

    private void setDrawerMenu() {
        // 定義新宣告的兩個物件：選項清單的 ListView 以及 Drawer內容的 LinearLayou
        mLsvDrawerMenu = (ListView) findViewById(R.id.lsv_drawer_menu);
        mLlvDrawerContent = (LinearLayout) findViewById(R.id.llv_left_drawer);

        // 當清單選項的子物件被點擊時要做的動作
        mLsvDrawerMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                selectMenuItem(position);
            }
        });

        // 設定清單的 Adapter，這裡直接使用 SimpleAdapter
        mAdapter = new SimpleAdapter(this,
                getData(),                             // 選單內容
                R.layout.drawer_menu_item,              // 選單物件的介面
                new String[]{"image", "title"},        // 選單物件的介面物件
                new int[]{R.id.img_menu, R.id.tv_menu}
        );
        mLsvDrawerMenu.setAdapter(mAdapter);
    }

    private void selectMenuItem(int position) {
        // 將選單的子物件設定為被選擇的狀態
        mLsvDrawerMenu.setItemChecked(position, true);
        LogUtil.i("點擊:" + MENU_ITEMS[position]);
        Intent intent;
        switch (position) {
            case 0:
                //host device
                intent = new Intent(getApplicationContext(), HostDeviceActivity.class);
                startActivityForResult(intent, 0);
                break;
            case 1:
                //CollWallet card
                intent = new Intent(getApplicationContext(), CoolWalletCardActivity.class);
                startActivityForResult(intent, 0);
                break;
            case 2:
                //Security
                intent = new Intent(getApplicationContext(), InitialSecuritySettingActivity.class);
                startActivityForResult(intent, 0);
                break;
            case 3:
                //Setting(Exchange Rate and Transaction Fees)
                intent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivityForResult(intent, 0);
                break;
//            case 4:
//                IssueFeedBack();
//                break;
//            case 5:
//                intent = new Intent(getApplicationContext(), LogOutActivity.class);
//                startActivityForResult(intent, 0);
//                break;
//            case 6:
//                //Share address service
//                intent = new Intent(getApplicationContext(), ShareAddress.class);
//                startActivityForResult(intent, 0);
//                break;

            //MARK XCHS
            case 4:
                intent = new Intent(getApplicationContext(), ExchangeLogin.class);
                startActivityForResult(intent, 0);
                break;

            case 5:
                int infoid = 0;
                cmdManager.XchsGetOtp(infoid, new CmdResultCallback() {
                    @Override
                    public void onSuccess(int status, byte[] outputData) {
                        if ((status + 65536) == 0x9000) {//-28672//36864
                            LogUtil.d("XchsGetOtp ok= " + outputData);
                        } else {
                            LogUtil.d("XchsGetOtp fail= " + outputData);

                        }
                    }
                });
                break;
            case 6:
                IssueFeedBack();
                break;
            case 7:
                intent = new Intent(getApplicationContext(), LogOutActivity.class);
                startActivityForResult(intent, 0);
                break;
            case 8:
                //Share address service
                intent = new Intent(getApplicationContext(), ShareAddress.class);
                startActivityForResult(intent, 0);
                break;


        }
        // 關掉 Drawer
        mDrawerLayout.closeDrawer(mLlvDrawerContent);
    }

    private void IssueFeedBack() {
        //show Notification
        issueCnt = 0;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View alert_view = inflater.inflate(R.layout.edit_dialog, null);//alert為另外做給alert用的layout
        final TextView mDialogTitle = (TextView) alert_view.findViewById(R.id.dialog_title);
        final TextView mDialogMessage = (TextView) alert_view.findViewById(R.id.dialog_message);
        mDialogTitle.setText(getString(R.string.issue_feedback_title));
        mDialogMessage.setText(getString(R.string.issue_feedback_message));
        //-----------產生輸入視窗--------

        new AlertDialog.Builder(mContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                .setView(alert_view)
                .setCancelable(false)
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LogUtil.d("feedBack Send");
                        mProgress.setMessage("Processing...");
                        mProgress.show();
                        forceCrash();
                    }
                })
                .setNegativeButton(">>POLICY", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        LayoutInflater inflater = LayoutInflater.from(mContext);
                        View alert_view = inflater.inflate(R.layout.edit_dialog, null);//alert為另外做給alert用的layout
                        final TextView mDialogTitle = (TextView) alert_view.findViewById(R.id.dialog_title);
                        final TextView mDialogMessage = (TextView) alert_view.findViewById(R.id.dialog_message);
                        mDialogTitle.setText(getString(R.string.issue_feedback_policy_title));
                        mDialogMessage.setText(getString(R.string.issue_feedback_policy));
                        //-----------產生輸入視窗--------
                        new AlertDialog.Builder(mContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                .setView(alert_view)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                }).show();
                    }
                }).show();

    }


    private void forceCrash() {
        try {
            ArrayList<CWAccountKeyInfo> cwList =
                    DatabaseHelper.queryAccountKeyInfo(mContext, -1);
            XchsSync mXchsSync = null;
            ArrayList<XchsSync> listXchsSync = new ArrayList<XchsSync>();
            for (CWAccountKeyInfo cw : cwList) {
                ArrayList<dbAddress> listAddress
                        = DatabaseHelper.queryAddress(mContext, cw.getAccId(), cw.getKcid());

                LogUtil.e("Loop=" + cw.getAccId());

                if (cw.getKcid() == 0) {
                    mXchsSync = new XchsSync();
                    mXchsSync.setAccID_ext(cw.getAccId());
                    mXchsSync.setKeyPointer_ext(cw.getKcid());
                    mXchsSync.setAccPub_ext(cw.getPublicKey());
                    mXchsSync.setAccChain_ext(cw.getChainCode());
                    mXchsSync.setAddNum_ext(listAddress.size());

                } else {
                    mXchsSync.setAccID_int(cw.getAccId());
                    mXchsSync.setKeyPointer_int(cw.getKcid());
                    mXchsSync.setAccPub_int(cw.getPublicKey());
                    mXchsSync.setAccChain_int(cw.getChainCode());
                    mXchsSync.setAddNum_int(listAddress.size());
                    listXchsSync.add(mXchsSync);
                }
            }

            String SyncData = createSyncJson(listXchsSync);
            LogUtil.d("sync jsonString=" + SyncData);
            Crashlytics.log(SyncData);
            throw new Exception("IssueFeedBack");
        } catch (Exception e) {
            mProgress.dismiss();
            LogUtil.d("sent issueFeedBack:");
            e.getStackTrace();
            Crashlytics.logException(e);
            Toast.makeText(mContext, "Feedback sent successfully.", Toast.LENGTH_SHORT).show();
        }
    }

    private String createSyncJson(ArrayList<XchsSync> listXchsSync) {

        JSONStringer jsonStringer = new JSONStringer();
        LogUtil.e("createSyncJson");
        try {
            jsonStringer.object();  //代表{
            jsonStringer.key("accounts");   //代表array key
            jsonStringer.array();    //代表[
            for (int x = 0; x < listXchsSync.size(); x++) {
                //Query accountKeyInfo's addr's Num & publicKey & chaincode
                jsonStringer.object();
                jsonStringer.key("id").value(listXchsSync.get(x).getAccID_ext());
                jsonStringer.key("extn");
                jsonStringer.object();
                jsonStringer.key("num").value(listXchsSync.get(x).getAddNum_ext());
                jsonStringer.key("pub").value(listXchsSync.get(x).getAccPub_ext());
                jsonStringer.key("chaincode").value(listXchsSync.get(x).getAccChain_ext());
                jsonStringer.endObject();
                jsonStringer.key("intn");
                jsonStringer.object();
                jsonStringer.key("num").value(listXchsSync.get(x).getAddNum_int());
                jsonStringer.key("pub").value(listXchsSync.get(x).getAccPub_int());
                jsonStringer.key("chaincode").value(listXchsSync.get(x).getAccChain_int());
                jsonStringer.endObject();
                jsonStringer.endObject();
            }
            jsonStringer.endArray();
            jsonStringer.endObject();


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jsonStringer.toString();
    }

    private List<HashMap<String, Object>> getData() {

        mHashMaps = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < MENU_ITEMS.length; i++) {
            map = new HashMap<String, Object>();
            map.put("image", MENU_ITEMS_PIC[i]);
            map.put("title", MENU_ITEMS[i]);
            mHashMaps.add(map);
        }
        return mHashMaps;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        LogUtil.e("FragMainActivtiy onActivityResult");
        scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (tabFragment == null) {
            tabFragment = new TabFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.content_fragment, tabFragment)
                    .commitAllowingStateLoss();
        }

        tabFragment.AccountRefresh(tabFragment.getAccoutId());
        mOnResumeFromBackCallBack.onRefresh();
    }

    @Override
    protected void onResume() {
        LogUtil.e("lifeCycle FragMainActivity onResume");
        super.onResume();
        if (cmdManager == null) {
            cmdManager = new CmdManager();
        }
        //註冊監聽
        registerBroadcast(this, cmdManager);

    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.e("lifeCycle FragMainActivity onStart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.e("lifeCycle FragMainActivity onPause");
        unRegisterBroadcast(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.e("lifeCycle FragMainActivity onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.e("lifeCycle FragMainActivity onResume");
        if (mProgress.isShowing()) {
            mProgress.dismiss();
        }
        if (socketHandler != null) {
            socketHandler.cancelALLTasks();
        }

    }



    public String getAccountFrag(int id) {
        return "Account" + id;
    }

    public interface OnResumeFromBackCallBack {
        void onRefresh();

    }

}
