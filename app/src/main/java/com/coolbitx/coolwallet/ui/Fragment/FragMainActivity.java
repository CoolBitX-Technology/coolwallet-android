package com.coolbitx.coolwallet.ui.Fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.coolbitx.coolwallet.DataBase.DatabaseHelper;
import com.coolbitx.coolwallet.DataBase.DbName;
import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.Service.BTConfig;
import com.coolbitx.coolwallet.Service.BlockSocketHandler;
import com.coolbitx.coolwallet.Service.socketService;
import com.coolbitx.coolwallet.callback.RefreshCallback;
import com.coolbitx.coolwallet.entity.Account;
import com.coolbitx.coolwallet.entity.Address;
import com.coolbitx.coolwallet.entity.Constant;
import com.coolbitx.coolwallet.entity.dbAddress;
import com.coolbitx.coolwallet.entity.socketByAddress;
import com.coolbitx.coolwallet.general.AppPrefrence;
import com.coolbitx.coolwallet.general.BtcUrl;
import com.coolbitx.coolwallet.general.CSVReadWrite;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.general.RefreshBlockChainInfo;
import com.coolbitx.coolwallet.httpRequest.CwBtcNetWork;
import com.coolbitx.coolwallet.ui.BaseActivity;
import com.coolbitx.coolwallet.ui.CoolWalletCardActivity;
import com.coolbitx.coolwallet.ui.ExchangeRateActivity;
import com.coolbitx.coolwallet.ui.HostDeviceActivity;
import com.coolbitx.coolwallet.ui.InitialCreateWalletIIActivity;
import com.coolbitx.coolwallet.ui.InitialSecuritySettingActivity;
import com.coolbitx.coolwallet.ui.LogOutActivity;
import com.coolbitx.coolwallet.ui.RecovWalletActivity;
import com.coolbitx.coolwallet.ui.ShareAddress;
import com.coolbitx.coolwallet.util.Base58;
import com.crashlytics.android.Crashlytics;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.ByteUtil;
import com.snscity.egdwlib.utils.LogUtil;

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

    // 左側選單圖片
    private static final int[] MENU_ITEMS_PIC = new int[]
            {R.mipmap.host, R.mipmap.cwcard, R.mipmap.security, R.mipmap.settings, R.mipmap.ic_feedback_white_24dp, R.mipmap.logout, R.mipmap.ic_share_white_24dp};
    // 左側選單文字項目
    private static final String[] MENU_ITEMS = new String[]{
            "Host devices", "CoolWallet card", "Security", "Settings", "Issue Feedback", "Logout", "Share address\n(beta)"
    };
    public static int ACCOUNT_CNT = 0;//這裡要改為抓qryWalletInfo的
    public static boolean refreshFlag = false;
    public static CmdManager cmdManager;
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
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String identify = data.getString("identify");
            String val = data.getString("result");
            try {
                if (val != null) {
                    if (identify.equals(BtcUrl.URL_BLICKCHAIN_EXCHANGE_RATE)) {
                        //使用 Float.floatToRawIntBits 先转成与 int 值相同位结构的 int 类型值，再根据 Big-Endian 或者是 Little-Endian 转成 byte 数组。
//                        int currRate = Float.floatToIntBits(AppPrefrence.getCurrentRate(mContext));
//                        float currRate = AppPrefrence.getCurrentRate(mContext)*100;
                        // byte[] BigcurrData = ByteUtil.intToByteBig(currRate, 4);

//                        int currRate = (int) (AppPrefrence.getCurrentRate(mContext) * 100);
//                        byte[] BigcurrData = ByteBuffer.allocate(4).putInt(currRate).order(ByteOrder.BIG_ENDIAN).array();
//
//                        byte[] currData = new byte[5];
//                        currData[0] = 0;
//                        for (int i = 0; i < BigcurrData.length; i++) {
//                            currData[i + 1] = BigcurrData[i];
//                        }
//                        cmdManager.SetCurrencyRate(currData, new CmdResultCallback() {
//                                    @Override
//                                    public void onSuccess(int status, byte[] outputData) {
//                                        if ((status + 65536) == 0x9000) {
//                                            LogUtil.i("SetCurrencyRate !!!");
//                                        }
//                                    }
//                                }
//                        );
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
    private RadioButton rbReceive;
    private RadioButton rbSend;
    private RadioButton rbHome;
    private int getWallteName = 0x01;
    private int getWalltePointer = 0x02;
    private List<Account> cwAccountList = new ArrayList<>();
    private int getWallteStatus = 0x03;
    private Address address;
    private SimpleAdapter mAdapter;
    private CSVReadWrite mLoginCsv;
    private ProgressDialog mProgress;
    private String mParentActivityName = null;
    private socketService mSocketService = null;
    private Timer mTimer;
    private socketNotificationReceiver socketSNR;
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
                        PublicPun.showNoticeDialog(mContext, socketTitle, socketMsg);
                    }
                    break;
                case BSConfig.HANDLER_DISCONN:
                    String title = "CoolWallet Disconnected";
                    String noteMsg = PublicPun.card.getCardName() + " Disconnected";
                    PublicPun.showNoticeDialogToFinish(mContext, title, noteMsg);
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

        LogUtil.d("FragMainActivity onCreate");

        Intent intent = getIntent();
        mSavedInstanceState = savedInstanceState;
        if (intent != null) {
            mParentActivityName = intent.getStringExtra("Parent");
            LogUtil.i("getSimpleName=" + RecovWalletActivity.class.getSimpleName());
            isFromRecovery = mParentActivityName.equals(RecovWalletActivity.class.getSimpleName());
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
        }, 15000);////15s沒成功就自動cacel

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
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.log("FragMainActivity onCreate failed=" + e.getMessage());
        }
    }

    private void initView() {

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.cw_refresh);

        //can't use mContext,but Activity.this.
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
        toolbar.setLogo(getResources().getDrawable(R.mipmap.logo2));
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
                        DatabaseHelper.deleteTable(mContext, DbName.DATA_BASE_TXS);
                        DatabaseHelper.deleteTable(mContext, DbName.DATA_BASE_ADDR);
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
                            //一開始進入只需抓account1 txs
                            //抓交易資料
                            final int refreshAccount = 0;
                            final RefreshBlockChainInfo refreshBlockChainInfo = new RefreshBlockChainInfo(mContext, refreshAccount);
                            refreshBlockChainInfo.FunQueryAccountInfo(new RefreshCallback() {
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
        int TotalBalance = 0;
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
                    byte[] newBalanceBytes = ByteUtil.intToByteLittle(TotalBalance, 8);
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
                                    //原本的寫法是set完所有資料才開放

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
        DatabaseHelper.deleteTable(mContext, DbName.DATA_BASE_CURRENT);
        ContentValues cv = new ContentValues();
        new Thread(new MyRunnable(mHandler, cv, BtcUrl.URL_BLICKCHAIN_EXCHANGE_RATE, 0, 60000 * 30, 0, cwBtcNetWork)).start();//1hr updated
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
                intent = new Intent(getApplicationContext(), HostDeviceActivity.class);
                startActivityForResult(intent, 0);
                break;
            case 1:
                intent = new Intent(getApplicationContext(), CoolWalletCardActivity.class);
                startActivityForResult(intent, 0);
                break;
            case 2:
                intent = new Intent(getApplicationContext(), InitialSecuritySettingActivity.class);
                startActivityForResult(intent, 0);
                break;
            case 3:
                intent = new Intent(getApplicationContext(), ExchangeRateActivity.class);
                startActivityForResult(intent, 0);
                break;
            case 4:
                IssueFeedBack();
                break;
            case 5:
                intent = new Intent(getApplicationContext(), LogOutActivity.class);
                startActivityForResult(intent, 0);
                break;
            case 6:
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
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        getAccounts();
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })
                .show();
    }

    private void getAccounts() {

        for (int i = 0; i < ACCOUNT_CNT; i++) {
            final int accountId = i;
            for (int j = 0; j < 2; j++) {
                final byte kcid = (byte) j;
                cmdManager.hdwQueryAccountInfo(kcid, accountId, new CmdResultCallback() {
                    @Override
                    public void onSuccess(int status, byte[] outputData) {
                        if ((status + 65536) == 0x9000) {
                            if (outputData != null) {
                                int Key = Integer.valueOf(PublicPun.byte2HexString(outputData[0]), 16);
                                getAccountKeyInfo(Constant.CwHdwAccountKeyInfoPubKeyAndChainCd, kcid, accountId, Key);
                            }
                        }
                    }
                });
            }
        }
    }

    private void getAccountKeyInfo(final int kinfoid, final int kcId, final int accountId, final int kid) {
        FragMainActivity.cmdManager.hdwQueryAccountKeyInfo(kinfoid,
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
                                    int mFirstKey = Integer.parseInt(PublicPun.byte2HexString(publicKeyBytes[63]), 16);
//                                    LogUtil.i("public key的最後字元=" + PublicPun.byte2HexString(publicKeyBytes[63]) +
//                                            ";轉int=" + mFirstKey);

                                    //format last charactors
                                    if (mFirstKey % 2 == 0) {
                                        extendPub[0] = 02;
                                    } else {
                                        extendPub[0] = 03;
                                    }
                                    for (int a = 0; a < 32; a++) {
                                        extendPub[a + 1] = publicKeyBytes[a];
                                    }
                                    //最後兩個字元一起
                                    LogUtil.d("account=" + accountId + " ;kcid=" + kcId + " ;kid=" + kid + " ;建地址的public key=" + LogUtil.byte2HexString(extendPub) + ";chainCodeBytes=" + LogUtil.byte2HexString(chainCodeBytes));
                                    Crashlytics.log("account=" + accountId + " ; kcid=" + kcId
                                            + " ; public key=" + LogUtil.byte2HexStringNoBlank(extendPub) + " ; ChainCodeBytes=" + LogUtil.byte2HexStringNoBlank(chainCodeBytes));

                                    if (issueCnt == ACCOUNT_CNT * 2) {//external/internal
                                        forceCrash();
                                    }
                                }
                            }
                        }
                    }
                });
    }

    private void forceCrash() {
        try {
            throw new Exception("IssueFeedBack");
        } catch (Exception e) {
            LogUtil.d("寄issueFeedBack");
            e.getStackTrace();
            Crashlytics.logException(e);
        }
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
        LogUtil.e("onResume");
        super.onResume();
        if (cmdManager == null) {
            cmdManager = new CmdManager();
        }
        //註冊監聽
        if (socketSNR == null) {
            socketSNR = new socketNotificationReceiver();
        }
        registerReceiver(socketSNR, new IntentFilter(BTConfig.SOCKET_ADDRESS_MSG));
        registerReceiver(socketSNR, new IntentFilter(BTConfig.DISCONN_NOTIFICATION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.e("onDestroy");
        if (mProgress.isShowing()) {
            mProgress.dismiss();
        }
        if (socketHandler != null) {
            socketHandler.cancelALLTasks();
        }

        try {
            if (socketSNR != null) {
                unregisterReceiver(socketSNR);
                socketSNR = null;
            }
        } catch (Exception e) {
            e.getStackTrace();
            Crashlytics.logException(e);
        }
    }

    public String getAccountFrag(int id) {
        return "Account" + id;
    }

    public interface OnResumeFromBackCallBack {
        void onRefresh();

    }

    //建立廣播接收socket訊息
    public class socketNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            LogUtil.d("webSocket broadcast recv!");
            if (action.equals(BTConfig.SOCKET_ADDRESS_MSG)) {
                socketMsgHandler.sendMessage(socketMsgHandler.obtainMessage(BSConfig.HANDLER_SOCKET,
                        intent.getExtras().getSerializable("socketAddrMsg")));
            } else if (action.equals(BTConfig.DISCONN_NOTIFICATION)) {
                socketMsgHandler.sendMessage(socketMsgHandler.obtainMessage(BSConfig.HANDLER_DISCONN));
            }
        }
    }
}
