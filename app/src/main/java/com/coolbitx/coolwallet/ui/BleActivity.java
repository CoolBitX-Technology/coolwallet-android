package com.coolbitx.coolwallet.ui;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.coolbitx.coolwallet.DataBase.DatabaseHelper;
import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.Service.BTConfig;
import com.coolbitx.coolwallet.adapter.ListViewAdapter;
import com.coolbitx.coolwallet.entity.Constant;
import com.coolbitx.coolwallet.entity.MyDevice;
import com.coolbitx.coolwallet.general.CSVReadWrite;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.ui.Fragment.FragMainActivity;
import com.crashlytics.android.Crashlytics;
import com.snscity.egdwlib.BleManager;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.ble.BleScanCallback;
import com.snscity.egdwlib.ble.BleStateCallback;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.LogUtil;
import com.snscity.egdwlib.utils.UUIDGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;

public class BleActivity extends BaseActivity {
    public static BleManager bleManager;
    boolean isConnected = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ExpandableListView listView;
    private ListViewAdapter adapter;
    private List<MyDevice> myDeviceList;
    private BluetoothStateListener listener;
    private boolean isScanning;
    private ImageView imgSearch;
    private TextView tvPullMsg;
    private TextView txtSearch;
    private TextView txtSearchDetail;
    private CmdManager cmdManager;
    private ProgressDialog mProgress;
    private Context mContext = this;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Boolean isReset = false;
    private String currentUuid = "";
    private byte hostId = -1;//主机id
    private byte hostStatus = -1;
    private byte[] loginChallenge;//登录的特征值
    private String currentOptCode = "";
    private String address;
    private CSVReadWrite mLoginCsv;
    private boolean isNewUser = false;
    private Timer mTimer;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
//            progressDialog.dismiss();
        }
    };
    private Handler Disconnhandler = new Handler();
    private Runnable Disconnrunnable = new Runnable() {
        @Override
        public void run() {
            LogUtil.i("xxx Disconnrunnable=" + getPackageName());
            isConnected = false;
            disconnBroadCast();
        }
    };
    private BleScanCallback bleScanCallback = new BleScanCallback() {

        @Override
        public void onBleDeviceDiscovered(BluetoothDevice device, int rssi, byte[] scanRecord) {
            String address = device.getAddress();
//            Log.e(TAG, "BleScanCallback address:" + address);
            if (!TextUtils.isEmpty(address)) {
                if (!contains(address)) {
                    imgSearch.setVisibility(View.INVISIBLE);
                    txtSearch.setVisibility(View.INVISIBLE);
                    txtSearchDetail.setVisibility(View.INVISIBLE);

                    String name = device.getName();
                    if (name == null || !name.startsWith("CoolWallet")) {
                        return;
                    }

                    //列表中没有该蓝牙设备，添加设备到列表中
                    MyDevice myDevice = new MyDevice();
                    myDevice.setName(name == null ? "unknown device" : name);
                    myDevice.setAddress(address == null ? "unknown address" : address);
                    myDevice.setRssi(String.valueOf(rssi));
                    myDeviceList.add(myDevice);
                    adapter.refresh();
                    try {
                        listView.expandGroup(adapter.getGroupCount() - 1);
                    } catch (Exception e) {

                    }
                }
            } else {
                imgSearch.setVisibility(View.VISIBLE);
                txtSearch.setVisibility(View.VISIBLE);
                txtSearchDetail.setVisibility(View.VISIBLE);
            }
        }
    };
    private BleStateCallback bleStateCallback = new BleStateCallback() {
        @Override
        public void onBleConnected() {

            LogUtil.i("連線address:" + address);
            PublicPun.user.settMacID(address);
            isConnected = true;
            mProgress.dismiss();

            mProgress.setMessage("Login Host...");
            mProgress.show();

            getFwVersion();
            getUniqueId();

            cmdManager.getCardId(new CmdResultCallback() {
                @Override
                public void onSuccess(int status, byte[] outputData) {
                    if ((status + 65536) == 0x9000) {//8byte cardId
                        if (outputData != null) {
                            PublicPun.card.setCardId(PublicPun.byte2HexString(outputData).replace(" ", ""));
                            Crashlytics.setUserIdentifier(new String(PublicPun.hexStringToByteArray(PublicPun.card.getCardId())));
                        }
                    } else {
                        PublicPun.toast(mContext, "Get Card ID failed,please connect again!");
                        BleActivity.bleManager.disConnectBle();
                    }
                }
            });

            cmdManager.getModeState(new CmdResultCallback() {
                @Override
                public void onSuccess(int status, byte[] outputData) {
                    if ((status + 65536) == 0x9000) {//-28672//36864
                        PublicPun.card.setMode(PublicPun.selectMode(PublicPun.byte2HexString(outputData[0])));
                        PublicPun.card.setState(String.valueOf(outputData[1]));
                        PublicPun.modeState = PublicPun.selectMode(PublicPun.byte2HexString(outputData[0]));
                        LogUtil.d("getModeState:" + "PAIRED \nMode=" + PublicPun.modeState + "\nState=" + outputData[1]);
                        LogUtil.d("Reset:" + isReset);
                        if (isReset) {
                            if (PublicPun.card.getMode().equals("NOHOST")) {
                                mProgress.dismiss();
                                PublicPun.toast(mContext, "Initial Success");
                                isShowDisconnAlert = false;
                                BleActivity.bleManager.disConnectBle();
                                bleManager.startScanBle(bleScanCallback);
                                isScanning = true;

                            } else {
                                Intent intent = new Intent(mContext, EraseActivity.class);
                                startActivityForResult(intent, 1);
                            }
                        } else {
                            try {
//                                if (mLoginCsv.setReadFileName(PublicPun.csvFilename)) {
//                                    LogUtil.i("ReadFileName has been created before");
//                                    isNewLgoin = mLoginCsv.readFromCSV(address);
                                ArrayList<String> arrayLoginList = new ArrayList<String>();
                                arrayLoginList = DatabaseHelper.queryLogin(mContext);
                                isNewUser = arrayLoginList.size() <= 0;

                                if (isNewUser) {
                                    //generate uuid
                                    String uuid = UUIDGenerator.getUUID();//手機唯一標識
                                    currentUuid = uuid;
                                    editor.putString("uuid", currentUuid);
                                    editor.commit();
                                } else {
                                    //get uuid form db
//                                    currentOptCode = sharedPreferences.getString("optCode", "");
                                    currentUuid = arrayLoginList.get(1);
                                    currentOptCode = arrayLoginList.get(2);
                                    editor.putString("uuid", currentUuid);
                                    editor.putString("optCode", currentOptCode);
                                    editor.commit();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            LogUtil.i("isRegistered:" + isNewUser + "; uuid=" + currentUuid + ",optCode:" + currentOptCode);
                            if (PublicPun.modeState.equals("NOHOST")) {
                                //find no host,this is unpaired card.
                                bleManager.stopScanBle();
                                Intent intent = new Intent(getApplicationContext(), ConfirmOtpActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putBoolean("isRegistered", false);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            } else {
                                // 丟uuid進去判斷有無註冊過,如果沒有的話hostId return-1
                                cmdManager.bindFindHstid(currentUuid, new CmdResultCallback() {
                                    @Override
                                    public void onSuccess(int status, byte[] outputData) {
                                        if ((status + 65536) == 0x9000) {
                                            LogUtil.i("bindFindHstid  ok");
                                            if (outputData != null && outputData.length == 2) {
                                                hostId = outputData[0];
                                                hostStatus = outputData[1];
                                                LogUtil.i("(bindFindHstid)獲取當前手機設備uuid對應的hostid:" + hostId + ",status:" + hostStatus);
                                                if ((int) hostId >= 0) {
                                                    //registered and has been confirmed.
                                                    if ((int) hostStatus == 0) {
                                                        BindLogin();
                                                    } else {
                                                        //registered and has not been confirmed.
                                                        bleManager.stopScanBle();
                                                        Intent intent = new Intent(getApplicationContext(), ConfirmOtpActivity.class);
                                                        Bundle bundle = new Bundle();
                                                        bundle.putBoolean("isRegistered", true);
                                                        intent.putExtras(bundle);
                                                        startActivity(intent);
                                                    }
                                                } else {
                                                    //no registered
                                                    bleManager.stopScanBle();
                                                    Intent intent = new Intent(getApplicationContext(), ConfirmOtpActivity.class);
                                                    Bundle bundle = new Bundle();
                                                    bundle.putBoolean("isRegistered", false);
                                                    intent.putExtras(bundle);
                                                    startActivity(intent);
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    } else {
//                        mProgress.dismiss();
                    }
                }
            });
        }

        public void onBleDisConnected() {
            LogUtil.i("onBleDisConnected");
            Crashlytics.log("BleDisConnected");
            Disconnhandler.post(Disconnrunnable);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_ble);
        LogUtil.d("BleActivity onCreate");
        bleManager = new BleManager(this);
        cmdManager = new CmdManager();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        listener = new BluetoothStateListener();
        registerReceiver(listener, filter);
        sharedPreferences = getSharedPreferences("card", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        initView();
    }

    private void initView() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.cw_refresh);
        listView = (ExpandableListView) findViewById(R.id.cw_list);
        imgSearch = (ImageView) findViewById(R.id.imgsearch);
        txtSearch = (TextView) findViewById(R.id.txtsearch);
        txtSearchDetail = (TextView) findViewById(R.id.txtsearchDetail);
        tvPullMsg = (TextView) findViewById(R.id.tvPullMsg);
//        pbarBleConnecting=(ProgressBar) findViewById(R.id.pBarConnecting);
              /* Initialize the progress dialog */
        mProgress = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_DARK);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LogUtil.i("swipeRefresh");
                bleManager.stopScanBle();
                isScanning = false;
                swipeRefreshLayout.setRefreshing(false);
                if (!isScanning) {
                    bleManager.openBluetooth();
                    isScanning = bleManager.startScanBle(bleScanCallback);
                    isScanning = true;
                }
                myDeviceList.clear();
                adapter.refresh();
            }
        });
        myDeviceList = new ArrayList<>();
        adapter = new ListViewAdapter(getApplicationContext(), myDeviceList, listView);
        listView.setAdapter(adapter);


        ListViewAdapter.OnBleConnClickListener mOnBleConnClickListener = new ListViewAdapter.OnBleConnClickListener() {
            @Override
            public void onClick(View v, int position) {
//                int id = v.getId();
//                switch (id) {
//                    case R.id.btn_connect:
                isReset = false;
                bleManager.stopScanBle();
                isScanning = false;
                MyDevice device = myDeviceList.get(position);
                address = device.getAddress();
                if (!address.isEmpty()) {
                    LogUtil.i("卡片連線=" + address + "---" + device.getName());
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            super.run();
                                       /* Show the progress. */
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    mProgress.setMessage("Connecting...");
                                    mProgress.show();
                                }
                            });

                            //處理程式寫在此
                            final boolean isConnectBle = bleManager.connectBle(address, bleStateCallback);
                            LogUtil.i("Connect 正在連接:" + address + "; is connectBle=" + isConnectBle);

                            mTimer = new Timer();
                            mTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    if (!isConnected) {
                                        mProgress.dismiss();
                                    }
                                    mTimer.cancel();
                                }
                            }, 5000);

                            handler.post(runnable);
                        }
                    };
                    thread.start();
                }
            }
        };
        ListViewAdapter.registerOnBleConnClickListenerCallback(mOnBleConnClickListener);

        ListViewAdapter.OnCWResetClickListener mCWResetClickListener = new ListViewAdapter.OnCWResetClickListener() {
            @Override
            public void onClick(View v, int position) {
                isReset = true;
                bleManager.stopScanBle();
                isScanning = false;
                MyDevice device = myDeviceList.get(position);
                address = device.getAddress();

                if (!address.isEmpty()) {
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            super.run();
                                       /* Show the progress. */
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    mProgress.setMessage("Connecting...");
                                    mProgress.show();
                                }
                            });
                            //處理程式寫在此
                            bleManager.connectBle(address, bleStateCallback);
//                            handler.post(runnable);
                        }
                    };
                    thread.start();

                    LogUtil.i("Reset 正在連接:" + address);

                }
            }
        };
        ListViewAdapter.registerOnCWResetClickListenerCallback(mCWResetClickListener);
    }

    private void BindLogin() {
        cmdManager.bindLoginChlng(hostId, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    if (outputData != null) {
                        loginChallenge = outputData;//16

                        LogUtil.i("loginChallenge=" + LogUtil.byte2HexString(loginChallenge) + "\n" + "currentUuid=" + currentUuid + "\n" + "otp=" + currentOptCode);

                        cmdManager.bindLogin(currentUuid, currentOptCode, loginChallenge, hostId, new CmdResultCallback() {
                            @Override
                            public void onSuccess(int status, byte[] outputData) {
                                if ((status + 65536) == 0x9000) {
                                    //计算enckey和mackey
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(currentUuid);//32
                                    sb.append(currentOptCode);//6
                                    String info = sb.toString();
                                    byte[] devKey = PublicPun.encryptSHA256(info.getBytes(Constant.UTF8));
                                    LogUtil.i("devKey=" + LogUtil.byte2HexString(devKey));
                                    byte[] encKey = PublicPun.encryptSHA256(PublicPun.calcKey(devKey, "ENC", loginChallenge));
                                    LogUtil.i("encKey=" + LogUtil.byte2HexString(encKey));
                                    byte[] macKey = PublicPun.encryptSHA256(PublicPun.calcKey(devKey, "MAC", loginChallenge));
                                    LogUtil.i("macKey=" + LogUtil.byte2HexString(macKey));

                                    PublicPun.user.setUuid(currentUuid);
                                    PublicPun.user.setOtpCode(currentOptCode);
                                    PublicPun.user.setEncKey(encKey);
                                    PublicPun.user.setMacKey(macKey);

//                                    try {
//                                        mLoginCsv.setSaveFileName(PublicPun.csvFilename, true);
//                                        mLoginCsv.saveLoginToCSV(PublicPun.user);
//                                    DatabaseHelper.insertLogin(mContext, currentUuid, currentOptCode);
//                                    } catch (IOException e) {
//                                        LogUtil.i("saveLoginToCSV錯誤:" + e.getMessage());
//                                        e.printStackTrace();
//                                    }

//                                    PublicPun.toast(BleActivity.this, "Login successful");

                                    cmdManager.getModeState(new CmdResultCallback() {
                                        @Override
                                        public void onSuccess(int status, byte[] outputData) {
                                            if ((status + 65536) == 0x9000) {//-28672//36864

                                                PublicPun.card.setMode(PublicPun.selectMode(PublicPun.byte2HexString(outputData[0])));
                                                PublicPun.card.setState(String.valueOf(outputData[1]));
                                                PublicPun.modeState = PublicPun.selectMode(PublicPun.byte2HexString(outputData[0]));
//                                                PublicPun.toast(getApplicationContext(), "Connected..\nMode=" + PublicPun.modeState + "\nState=" + outputData[1]);
                                                LogUtil.i("BLE Login  ModeState:" + "\nMode=" + PublicPun.modeState + "\nState=" + outputData[1]);
//                                                getRegInfo();
//                                                getHosts();
                                                if (PublicPun.modeState.equals("PERSO")) {
//                                                    Intent intent = new Intent(getApplicationContext(), InitialSecuritySettingActivity.class);
//                                                    startActivity(intent);
                                                    setPersoSecurity(false, true, false, false);
                                                } else {
//                                                    Intent intent = new Intent(getApplicationContext(), FragMainActivity.class);
//                                                                    startActivity(intent);
                                                    byte infoId = 0x03;
                                                    cmdManager.hdwQryWaInfo(infoId, new CmdResultCallback() {
                                                        @Override
                                                        public void onSuccess(int status, byte[] outputData) {
                                                            if ((status + 65536) == 0x9000) {
                                                                LogUtil.e("hdwQryWaInfo=" + PublicPun.byte2HexString(outputData));

                                                                //00 is INACTIVE ->進CREATE WALLET
                                                                //01 is WAITACTV ->進CREATE WALLET
                                                                //02 IS ACTIVE   ->進MAIN PAGE
//                                                                if(PublicPun.byte2HexString(outputData).equals("02")){

                                                                if (outputData[0] == 0x02) {
                                                                    bleManager.stopScanBle();
//                                                                    Intent intent = new Intent(getApplicationContext(), RecovWalletActivity.class);
                                                                    Intent intent = new Intent(getApplicationContext(), FragMainActivity.class);
                                                                    intent.putExtra("Parent", BleActivity.class.getSimpleName());

                                                                    startActivity(intent);
                                                                } else {
                                                                    bleManager.stopScanBle();
                                                                    Intent intent = new Intent(getApplicationContext(), InitialCreateWalletActivity.class);
                                                                    startActivity(intent);
                                                                }
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.i("onDestroy");
        mProgress.dismiss();
        bleManager.disConnectBle();
        bleManager.closeBluetooth();
        unregisterReceiver(listener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

//    private void getRegInfo() {
//
//        final byte first = 0x00;
//        final byte second = 0x01;
//        final byte third = 0x02;
//
//        cmdManager.bindRegInfo(first, new CmdResultCallback() {
//            @Override
//            public void onSuccess(int status, byte[] outputData) {
//                if ((status + 65536) == 0x9000) {
//                    if (outputData != null) {
//                        LogUtil.i("bindRegInfo 第一個:" + dealRegInfo(outputData));
//                        addHostList(outputData, first);
//                    }
//                }
//            }
//        });
//        cmdManager.bindRegInfo(second, new CmdResultCallback() {
//            @Override
//            public void onSuccess(int status, byte[] outputData) {
//                if ((status + 65536) == 0x9000) {
//                    if (outputData != null) {
//                        LogUtil.i("bindRegInfo 第二個:" + dealRegInfo(outputData));
//                        addHostList(outputData, second);
//                    }
//                }
//            }
//        });
//        cmdManager.bindRegInfo(third, new CmdResultCallback() {
//            @Override
//            public void onSuccess(int status, byte[] outputData) {
//                if ((status + 65536) == 0x9000) {
//                    if (outputData != null) {
//                        LogUtil.i("bindRegInfo 第三個:" + dealRegInfo(outputData));
//                        addHostList(outputData, third);
//                    }
//                }
//            }
//        });
//    }
//
//    private String dealRegInfo(byte[] outputData) {
//
//        if (outputData == null) return "";
//        StringBuilder sb = new StringBuilder();
//        int length = outputData.length;
//        if (length > 0) {
//            sb.append("注册状态是:");
//            byte bindState = outputData[0];
//            if (bindState == 0x00) {
//                sb.append("未注册");
//            } else if (bindState == 0x01) {
//                sb.append("已注册");
//            } else if (bindState == 0x02) {
//                sb.append("已确认");
//            }
//        } else {
//            return "";
//        }
//
//        if (length > 1) {
//            byte[] desc = new byte[length - 1];
//            int descLen = desc.length;
//            for (int i = 0; i < descLen; i++) {
//                desc[i] = outputData[i + 1];
//            }
//
//            String s = new String(desc, Constant.UTF8).toString().trim();
//            sb.append(" 卡片描述是:");
//            if (s.equals("")) {
//                sb.append("空");
//            } else {
//                sb.append(s);
//            }
//        }
//
//        return sb.toString();
//    }

    private boolean contains(String address) {
        boolean flag = false;
        if (myDeviceList != null) {
            for (MyDevice d : myDeviceList) {
                if (d != null) {
                    String address1 = d.getAddress();
                    if (!TextUtils.isEmpty(address1) && !TextUtils.isEmpty(address)) {
                        if (address1.equals(address)) {
                            flag = true;
                        }
                    }
                }
            }
        }
        return flag;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d("onResume");
        if (bleManager == null) {
            bleManager = new BleManager(this);
        }
        if (bleManager.isOpen()) {
            bleManager.startScanBle(bleScanCallback);
            isScanning = true;
        } else {
            bleManager.openBluetooth();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        LogUtil.i("onActivityResult");
        //不要出diconn alert
        isShowDisconnAlert = false;
        if (bleManager == null) {
            bleManager = new BleManager(this);
        }
        bleManager.disConnectBle();

        if (!bleManager.isOpen()) {
            bleManager.openBluetooth();
        } else {
            bleManager.startScanBle(bleScanCallback);
            isScanning = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mProgress.dismiss();
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    /**
     * 發送斷線廣播訊息
     */
    private void disconnBroadCast() {
        Intent SocketIntent = new Intent(BTConfig.DISCONN_NOTIFICATION);
        LogUtil.i("DISCONN_NOTIFICATION");
        mContext.sendBroadcast(SocketIntent);
    }

    private class BluetoothStateListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            LogUtil.i("BluetoothStateListener=" + String.valueOf(state));
            switch (state) {
                case BluetoothAdapter.STATE_TURNING_ON:
                    break;
                case BluetoothAdapter.STATE_ON:
                    bleManager.startScanBle(bleScanCallback);
                    isScanning = true;
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    break;
                case BluetoothAdapter.STATE_OFF:
                    bleManager.openBluetooth();
                    bleManager.startScanBle(bleScanCallback);
                    isScanning = true;
                    break;
            }
        }
    }

}
