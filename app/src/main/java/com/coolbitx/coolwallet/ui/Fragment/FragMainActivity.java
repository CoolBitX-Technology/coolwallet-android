package com.coolbitx.coolwallet.ui.Fragment;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.NetworkOnMainThreadException;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.coolbitx.coolwallet.DataBase.DatabaseHelper;
import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.Service.BlockSocketHandler;
import com.coolbitx.coolwallet.Service.SocketHandler;
import com.coolbitx.coolwallet.Service.socketService;
import com.coolbitx.coolwallet.callback.RefreshCallback;
import com.coolbitx.coolwallet.entity.Account;
import com.coolbitx.coolwallet.entity.Address;
import com.coolbitx.coolwallet.entity.Constant;
import com.coolbitx.coolwallet.entity.Host;
import com.coolbitx.coolwallet.entity.dbAddress;
import com.coolbitx.coolwallet.general.AppPrefrence;
import com.coolbitx.coolwallet.general.BtcUrl;
import com.coolbitx.coolwallet.general.CSVReadWrite;
import com.coolbitx.coolwallet.general.DbName;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.general.RefreshBlockChainInfo;
import com.coolbitx.coolwallet.ui.BaseActivity;
import com.coolbitx.coolwallet.ui.BleActivity;
import com.coolbitx.coolwallet.ui.CoolWalletCardActivity;
import com.coolbitx.coolwallet.ui.ExchangeRateActivity;
import com.coolbitx.coolwallet.ui.HostDeviceActivity;
import com.coolbitx.coolwallet.ui.InitialCreateWalletIIActivity;
import com.coolbitx.coolwallet.ui.InitialSecuritySettingActivity;
import com.coolbitx.coolwallet.ui.LogOutActivity;
import com.coolbitx.coolwallet.ui.RecovWalletActivity;
import com.coolbitx.coolwallet.util.Base58;
import com.coolbitx.coolwallet.util.CwBtcNetWork;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.ByteUtil;
import com.snscity.egdwlib.utils.LogUtil;

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

    private SwipeRefreshLayout swipeRefreshLayout;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private LinearLayout mLlvDrawerContent;
    private ListView mLsvDrawerMenu;
    private List<HashMap<String, Object>> mHashMaps;
    private HashMap<String, Object> map;
    public static int ACCOUNT_CNT = 0;//這裡要改為抓qryWalletInfo的
    int addrAuccess = 0;
    private RadioButton rbReceive;
    private RadioButton rbSend;
    private RadioButton rbHome;
    Bundle mSavedInstanceState;
    private List<Account> cwAccountList = new ArrayList<>();
    private int getWallteStatus = 0x03;
    private Address address;

    private SimpleAdapter mAdapter;
    // 左側選單圖片
    private static final int[] MENU_ITEMS_PIC = new int[]
            {R.mipmap.host, R.mipmap.cwcard, R.mipmap.security, R.mipmap.settings, R.mipmap.logout};
    // 左側選單文字項目
    private static final String[] MENU_ITEMS = new String[]{
            "Host devices", "CoolWallet card", "Security", "Settings", "Logout"
    };
    //    public static CmdManager cmdManager;
    private CSVReadWrite mLoginCsv;
    private static OnResumeFromBackCallBack mOnResumeFromBackCallBack = null;
    CwBtcNetWork cwBtcNetWork;
    private ProgressDialog mProgress;
    Context mContext;
    StringBuilder sbAdd = new StringBuilder();
    boolean isFromRecovery;
    boolean isNewUser = false;
    private String mParentActivityName = null;

    private socketService mSocketService = null;
    private Timer mTimer;

    //    public static socketNotificationReceiver socketSNR;
    public static SocketHandler socketHandler;
    public static BlockSocketHandler test_socketHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frag_main);

        Intent intent = getIntent();
        mSavedInstanceState = savedInstanceState;
        if (intent != null) {
            mParentActivityName = intent.getStringExtra("Parent");
            LogUtil.i("getSimpleName=" + RecovWalletActivity.class.getSimpleName());
            if (mParentActivityName.equals(RecovWalletActivity.class.getSimpleName())) {
                isFromRecovery = true;
            } else {
                isFromRecovery = false;
            }
        }

        //LogUtil.e("class name = " + mParentActivityName);

        mContext = FragMainActivity.this;
        cwBtcNetWork = new CwBtcNetWork();
        cmdManager = new CmdManager();
        address = new Address();
        mLoginCsv = new CSVReadWrite(mContext);

        initToolbar();
        initView();

        mProgress.setMessage("Syncing...");
        mProgress.show();

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mProgress.isShowing()) {
                    mProgress.dismiss();
                }
                mTimer.cancel();
            }
        }, 180000);//3mins沒成功就自動cacel

        LogUtil.i("FragMainActivity onCreate");

        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                socketHandler = new SocketHandler(FragMainActivity.this);

            }
        };
        thread.start();


        try {
            getModeState();
            getHosts();//獲取設備資訊
            getCardId();//獲取卡片id
            getCardName();//獲取卡片名稱
            getCurrRate();
            queryWallteInfo(getWallteStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //啟動服務
//        LogUtil.i("starService!!");
//        Intent serviceIntent = new Intent(mContext, socketService.class);
//        startService(serviceIntent);
//        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }

    /**
     * ServiceConnection，获取service实例;  Defines callbacks for service binding, passed to bindService()
     */
    boolean bindFlag;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            bindFlag = false;
            mSocketService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bindFlag = true;
            LogUtil.i("onServiceConnected name=" + name);

        }
    };

    private void getModeState() {
        cmdManager.getModeState(new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {//-28672//36864
                    PublicPun.card.setMode(PublicPun.selectMode(PublicPun.byte2HexString(outputData[0])));
                    PublicPun.card.setState(String.valueOf(outputData[1]));

                    LogUtil.i("Main getModeState:" + " \nMode=" + PublicPun.card.getMode() + "\nState=" + PublicPun.card.getState());
                }
            }
        });
    }


    private void initView() {

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.cw_refresh);

        rbReceive = (RadioButton) findViewById(R.id.rb_receive);
        rbSend = (RadioButton) findViewById(R.id.rb_send);
        rbHome = (RadioButton) findViewById(R.id.rb_home);

        //can't use mContext,but Activity.this.
        mProgress = new ProgressDialog(FragMainActivity.this);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
    }

    TabFragment tabFragment;

    private void initTabFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (tabFragment == null) {
                tabFragment = new TabFragment();

                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.content_fragment, tabFragment)
                        .commit();
            }
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

    private static Boolean isExit = false;
    private static Boolean hasTask = false;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Timer tExit = null;
        LogUtil.i("CLICK BACK");
        // 判斷是否按下Back
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 是否要退出
            if (isExit == false) {
                isExit = true; //記錄下一次要退出
                Toast.makeText(getBaseContext(), "Press once again to exit!", Toast.LENGTH_SHORT).show();
                // 如果超過兩秒則恢復預設值
                tExit = new Timer();
                tExit.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        isExit = false; // 取消退出
                        hasTask = true;
                    }
                }, 2000); // 如果2秒鐘內沒有按下返回鍵，就啟動定時器取消剛才執行的任務
            } else {

                //回到藍芽列表
                Intent intent = new Intent();
                intent.setClass(FragMainActivity.this, BleActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//关掉所要到的界面中间的activity
                startActivity(intent);
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

                        LogUtil.i("recovery:" + isFromRecovery + " 有" + ACCOUNT_CNT + "個帳戶" + "依序讀取帳戶訊息");
                        if (isFromRecovery) {
                            //do nothing
                            if (mProgress.isShowing()) {
                                mProgress.dismiss();
                            }
                            initTabFragment(mSavedInstanceState);
                        } else {

                            final int refreshAccount = 0;
                            final RefreshBlockChainInfo refreshBlockChainInfo = new RefreshBlockChainInfo(mContext, refreshAccount);
                            refreshBlockChainInfo.FunQueryAccountInfo(new RefreshCallback() {

                                @Override
                                public void success() {

                                    refreshBlockChainInfo.callTxsRunnable(new RefreshCallback() {
                                        @Override
                                        public void success() {

                                            FunhdwSetAccInfo(refreshAccount);
                                            if (!PublicPun.accountRefresh[0]) {
                                                PublicPun.accountRefresh[0] = true;
                                            }
                                        }

                                        @Override
                                        public void fail(String msg) {
                                            PublicPun.ClickFunction(mContext, "Unstable internet connection", msg);
                                            initTabFragment(mSavedInstanceState);
                                            if (mProgress.isShowing()) {
                                                mProgress.dismiss();
                                            }
                                        }

                                        @Override
                                        public void exception(String msg) {

                                        }
                                    });
                                }

                                @Override
                                public void fail(String msg) {
                                    PublicPun.ClickFunction(mContext, "Unstable internet connection", msg);
                                    initTabFragment(mSavedInstanceState);
                                    if (mProgress.isShowing()) {
                                        mProgress.dismiss();
                                    }
                                }

                                @Override
                                public void exception(String msg) {

                                }
                            });
                        }
                    }
                }
            }
        });
    }

    private void getCurrRate() {
        DatabaseHelper.deleteTable(mContext, DbName.DATA_BASE_CURRENT);
        ContentValues cv = new ContentValues();
        new Thread(new MyRunnable(mHandler, cv, BtcUrl.URL_BLOCKR_EXCHANGE_RATE, 0, 60000 * 60, 0)).start();//1hr

    }

    public class MyRunnable implements Runnable {
        ContentValues cv;
        int what;
        Handler handler;
        int interval;
        int identify;
        String extralUrl;

        public MyRunnable(Handler handler, ContentValues cv, String extralUrl, int what, int interval, int identify) {
            this.cv = cv;
            this.what = what;
            this.handler = handler;
            this.interval = interval;
            this.identify = identify;
            this.extralUrl = extralUrl;
        }

        @Override
        public void run() {
            String result = "";
            boolean ex = true;
            try {
                result = cwBtcNetWork.doGet(cv, extralUrl, null);
                if (result != null) {
                    PublicPun.jsonParserRate(mContext, result);
                }
            } catch (NetworkOnMainThreadException e) {
                LogUtil.i("doGet 錯誤:" + e.toString());
                ex = false;
//                new Thread(new MyRunnable(handler, cv, mAddr, what, 0, 0)).start();
            }

            if (ex) {
                Message msg = new Message();
                msg.what = what;
                Bundle data = new Bundle();
                data.putString("identify", extralUrl);
                data.putString("result", result);
                msg.setData(data);
                handler.sendMessage(msg);
            }
            if (interval > 0) {
                handler.postDelayed(this, interval);
            }
        }
    }


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String identify = data.getString("identify");
            String val = data.getString("result");
            try {

                if (val != null) {
//                    使用 Float.floatToRawIntBits 先转成与 int 值相同位结构的 int 类型值，再根据 Big-Endian 或者是 Little-Endian 转成 byte 数组。
                    int currRate = Float.floatToIntBits(AppPrefrence.getCurrentRate(mContext));
                    byte[] BigcurrData = ByteUtil.intToByteBig(currRate, 4);
                    byte[] currData = new byte[5];
                    currData[0] = 0;
                    for (int i = 0; i < BigcurrData.length; i++) {
                        currData[i + 1] = BigcurrData[i];
                    }
                    cmdManager.SetCurrencyRate(currData, new CmdResultCallback() {
                                @Override
                                public void onSuccess(int status, byte[] outputData) {
                                    if ((status + 65536) == 0x9000) {
                                        LogUtil.i("SetCurrencyRate !!!");
                                    }
                                }
                            }
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    private void FunhdwSetAccInfo(int account) {

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
                                    //for test
                                    initTabFragment(mSavedInstanceState);
                                    mProgress.dismiss();
                                }
                            } else {
                                LogUtil.i("setAccountInfo failed.");
                                PublicPun.ClickFunction(mContext, "Alert Message", "Set account information failed!");
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
//                        FunhdwSetAccInfo(0);
//                        FunQueryAccountInfo(0);
                        //不能搬到FunQueryAccountInfo裡,因為另一段也會call到
                        mProgress.dismiss();
                        initTabFragment(mSavedInstanceState);
                    }
                }
            }
        });
    }

    private void addHostList(byte[] outputData, byte hostId) {
        Host bean = new Host();
        byte bindStatus = outputData[0];
        int length = outputData.length - 1;
        byte[] desc = new byte[length];
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                desc[i] = outputData[i + 1];
            }
        }

        bean.setBindStatus(bindStatus);
        bean.setId(hostId);
        bean.setDesc(new String(desc, Constant.UTF8).toString().trim());
        LogUtil.i("addhostId:" + hostId + "; status=" + bindStatus + "; desc=" + new String(desc, Constant.UTF8));


        //會發生0x01先跑完,IndexOutOfBoundsException, remove ind
//            if (hostId == 0x00) {
//                PublicPun.hostList.add(0, bean);
//            } else if (hostId == 0x01) {
//                PublicPun.hostList.add(1, bean);
//            } else if (hostId == 0x02) {
//                PublicPun.hostList.add(2, bean);
//            }
        PublicPun.hostList.add(bean);

    }

    private void getHosts() {
        if (!PublicPun.hostList.isEmpty()) {
            PublicPun.hostList.clear();
        }
        final byte first = 0x00;
        final byte second = 0x01;
        final byte third = 0x02;
        cmdManager.bindRegInfo(first, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    if (outputData != null && outputData.length > 0) {
                        addHostList(outputData, first);
                    }
                }
            }
        });
        cmdManager.bindRegInfo(second, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    if (outputData != null && outputData.length > 0) {
                        addHostList(outputData, second);
                    }
                }
            }
        });
        cmdManager.bindRegInfo(third, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    if (outputData != null && outputData.length > 0) {
                        addHostList(outputData, third);
                    }
                }
            }
        });
    }

    private void getCardId() {
        cmdManager.getCardId(new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {//8byte cardId
                    if (outputData != null) {
                        PublicPun.card.setCardId(PublicPun.byte2HexString(outputData).replace(" ", ""));
                        LogUtil.i("卡片id:" + PublicPun.card.getCardId());
                    }
                }
            }
        });
    }

    private void getCardName() {
        cmdManager.getCardName(new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {//32byte cardName
                    if (outputData != null) {
                        String cardName = new String(outputData, Constant.UTF8).trim();
                        if (cardName.isEmpty()) {
                            PublicPun.card.setCardName(PublicPun.card.getDeviceName());
                        } else {
                            PublicPun.card.setCardName(cardName);
                        }
                        LogUtil.i("卡片name:" + PublicPun.card.getCardName());
                        AppPrefrence.saveCardName(mContext, new String(outputData, Constant.UTF8).trim());
                    }
                }
            }
        });
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
//                startActivity(intent);
                startActivityForResult(intent, 0);
                break;
            case 1:
                intent = new Intent(getApplicationContext(), CoolWalletCardActivity.class);
//                startActivity(intent);
                startActivityForResult(intent, 0);
                break;
            case 2:
                intent = new Intent(getApplicationContext(), InitialSecuritySettingActivity.class);
//                startActivity(intent);
                startActivityForResult(intent, 0);
                break;
            case 3:
                intent = new Intent(getApplicationContext(), ExchangeRateActivity.class);
//                startActivity(intent);
                startActivityForResult(intent, 0);
                break;
            case 4:
                intent = new Intent(getApplicationContext(), LogOutActivity.class);
//                startActivity(intent);
                startActivityForResult(intent, 0);
                break;
        }
        // 關掉 Drawer
        mDrawerLayout.closeDrawer(mLlvDrawerContent);
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

    public static IntentResult scanningResult;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        LogUtil.e("FragMainActivtiy onActivityResult");
        scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        tabFragment.AccountRefresh(tabFragment.getAccoutId());
        mOnResumeFromBackCallBack.onRefresh();
    }

    public interface OnResumeFromBackCallBack {
        void onRefresh();

    }

    @Override
    protected void onResume() {
        super.onResume();
        cmdManager = new CmdManager();
//        //註冊監聽
//        socketSNR = new socketNotificationReceiver();
//        //註冊廣播
//        registerReceiver(socketSNR, new IntentFilter(BTConfig.SOCKET_ADDRESS_MSG));
//        registerReceiver(socketSNR, new IntentFilter(BTConfig.DISCONN_NOTIFICATION));
    }

    public static void registerOnResumeFromBackCallBack(OnResumeFromBackCallBack cb) {
        mOnResumeFromBackCallBack = cb;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.e("FragMainActivity - onDestroy");
//        if (mLoginCsv != null) {
//            mLoginCsv.closeSaveFile();
//        }
//        unregisterReceiver(socketSNR);
        if (socketHandler != null) {
            socketHandler.cancelALLTasks();
        }
        //停止服務
//        if (bindFlag) {
//            LogUtil.i("stopService!!bindFlag=" + bindFlag);
//            unbindService(mConnection);
//            Intent intent = new Intent(mContext, socketService.class);
//            stopService(intent);
//        }
    }


    public String getAccountFrag(int id) {
        return "Account" + id;
    }

}
