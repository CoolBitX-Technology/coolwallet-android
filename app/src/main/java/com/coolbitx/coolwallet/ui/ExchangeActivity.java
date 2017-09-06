package com.coolbitx.coolwallet.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.Service.BTConfig;
import com.coolbitx.coolwallet.adapter.PendingOrderAdapter;
import com.coolbitx.coolwallet.bean.ExchangeOrder;
import com.coolbitx.coolwallet.callback.APIResultCallback;
import com.coolbitx.coolwallet.general.BtcUrl;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.ui.Fragment.BSConfig;
import com.coolbitx.coolwallet.util.ExchangeAPI;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.LogUtil;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ShihYi on 2015/12/25.
 */
public class ExchangeActivity extends BaseActivity implements
        View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private CmdManager cmdManager;
    private Context mContext;
    private TextView sellVerification, tv_noMatchedOrders;
    private Button btn_place_order;
    private ListView gridViewSell;
    private ListView gridViewBuy;
    private ExchangeAPI mExchangeAPI;
    ArrayList<ExchangeOrder> listExchangeSellOrder;
    ArrayList<ExchangeOrder> listExchangeBuyOrder;
    private String orderId;
    private ProgressDialog mProgress;
    private LinearLayout lin_orders;
    XchsMsgListener xchsMsgListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange);
        mContext = this;

        initViews();
        initValues();
        initToolbar();
        cmdManager = new CmdManager();
        mExchangeAPI = new ExchangeAPI(mContext, cmdManager);


        GetPendingOrder();

    }

    private class XchsMsgListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.e("Receive Message");
            String action = intent.getAction();

            if (action.equals(BTConfig.XCHS_NOTIFICATION)) {
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putString("handlerMessage", intent.getExtras().getString("ExchangeMessage"));
                msg.setData(data);
                msg.what = BSConfig.HANDLER_XCHS;
                brocastMsgHandler.sendMessage(msg);
            }
        }
    }

    private Handler brocastMsgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case BSConfig.HANDLER_XCHS:
                    LogUtil.d("go Handler");
                    GetPendingOrder();
            }
            super.handleMessage(msg);
        }
    };

    private void GetPendingOrder() {

//        mProgress.show();
        listExchangeSellOrder = new ArrayList<>();
        listExchangeBuyOrder = new ArrayList<>();

        mExchangeAPI.getPendingTrx(new APIResultCallback() {
            @Override
            public void success(String[] msg) {
                String[] mString = new String[]{"sell", "buy"};
//                mProgress.dismiss();
                for (int i = 0; i < mString.length; i++) {
                    if (i == 0) {
                        listExchangeSellOrder = PublicPun.jsonParserExchange(msg[0], mString[i], true);
                        gridViewSell.setAdapter(new PendingOrderAdapter(mContext, listExchangeSellOrder));
                    } else {
                        listExchangeBuyOrder = PublicPun.jsonParserExchange(msg[0], mString[i], true);
                        gridViewBuy.setAdapter(new PendingOrderAdapter(mContext, listExchangeBuyOrder));
                    }
                }

                if (listExchangeSellOrder.size() == 0 && listExchangeBuyOrder.size() == 0) {
                    isShowOrders(false);
                } else {
                    isShowOrders(true);
                }
            }

            @Override
            public void fail(String msg) {
                mProgress.dismiss();
                PublicPun.showNoticeDialog(mContext, getString(R.string.sync_failed), getString(R.string.plz_try_again));
            }
        });
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //go to  Exchange Order details

        int clickId = parent.getId();
        ExchangeOrder exchngeOrder = new ExchangeOrder();

        switch (clickId) {
            case R.id.grid_sell:
                exchngeOrder = listExchangeSellOrder.get(position);
                break;
            case R.id.grid_buy:
                exchngeOrder = listExchangeBuyOrder.get(position);

                break;
        }

        Intent intent = new Intent();
        intent.setClass(mContext, ExchangeCompleteOrderActivity.class);
        Bundle bundle = new Bundle();
        LogUtil.i("exchangeOrder click" +
                exchngeOrder.getOrderId() + " , " + exchngeOrder.getCworderId() +" , " + exchngeOrder.getAddr() + " , " +
                exchngeOrder.getAmount() + " , " + exchngeOrder.getAccount() + " , " +
                exchngeOrder.getPrice() + " , " + exchngeOrder.getExpiration());

        bundle.putSerializable("ExchangeOrder", exchngeOrder);
        intent.putExtras(bundle);
        startActivityForResult(intent, 0);

    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        int clickId = parent.getId();
        ExchangeOrder exchngeOrder = new ExchangeOrder();
        orderId = null;

        if (listExchangeSellOrder.size() == 0) {
            return false;
        }

        switch (clickId) {
            case R.id.grid_sell:
                exchngeOrder = listExchangeSellOrder.get(position);
                orderId = exchngeOrder.getOrderId();
                AlertDialog.Builder mBuilder =
                        PublicPun.CustomNoticeDialog(mContext, getString(R.string.btn_cancel_order_str), getString(R.string.str_cancel_order_msg));
                mBuilder.setPositiveButton(getString(R.string.str_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // cancel trx

                        mExchangeAPI.cancelTrx(orderId, new APIResultCallback() {
                            @Override
                            public void success(String[] msg) {
                                GetPendingOrder();
                            }

                            @Override
                            public void fail(String msg) {
                                PublicPun.showNoticeDialog(mContext, getString(R.string.str_unable_cancel), getString(R.string.reason) + msg);
                            }
                        });
                    }
                }).setNegativeButton(getString(R.string.str_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                        .show();

                break;
            case R.id.grid_buy:
                exchngeOrder = listExchangeBuyOrder.get(position);
                break;
        }


        return true;
    }


    private void ExchangeLogout() {
        cmdManager.XchsSessionLogout(new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                LogUtil.e("XchsSessionLogout success");
            }
        });
        mExchangeAPI.exchangeLogOut(new APIResultCallback() {
            @Override
            public void success(String[] msg) {
                LogUtil.d("Logout success.");
            }

            @Override
            public void fail(String msg) {

            }
        });
    }

    private void initViews() {
        sellVerification = (TextView) findViewById(R.id.tv_order_verification);
        sellVerification.setOnClickListener(this);
        gridViewSell = (ListView) findViewById(R.id.grid_sell);
        gridViewBuy = (ListView) findViewById(R.id.grid_buy);
        gridViewSell.setOnItemClickListener(this);
        gridViewBuy.setOnItemClickListener(this);
        gridViewSell.setOnItemLongClickListener(this);
        gridViewBuy.setOnItemLongClickListener(this);
        lin_orders = (LinearLayout) findViewById(R.id.layout_matched_orders);
        tv_noMatchedOrders = (TextView) findViewById(R.id.tv_no_matched_orders);
        btn_place_order = (Button) findViewById(R.id.btn_place_order);
        btn_place_order.setOnClickListener(this);
        isShowOrders(true);

    }


    private void isShowOrders(boolean isShow) {
        if (isShow) {
            lin_orders.setVisibility(View.VISIBLE);
            tv_noMatchedOrders.setVisibility(View.GONE);
            btn_place_order.setVisibility(View.GONE);
        } else {
            lin_orders.setVisibility(View.GONE);
            tv_noMatchedOrders.setVisibility(View.VISIBLE);
            btn_place_order.setVisibility(View.VISIBLE);
        }
    }

    private void initValues() {
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(getResources().getDrawable(R.drawable.exchange));
        toolbar.setTitle(getString(R.string.exchange));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        // 打開 up button_up
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                int infoid = 0;
                cmdManager.XchsGetOtp(infoid, new CmdResultCallback() {
                    @Override
                    public void onSuccess(int status, byte[] outputData) {
                        if ((status + 65536) == 0x9000) {//-28672//36864
                        } else {

                        }
                    }
                });

                break;

            default:
                break;
        }

        return true;
    }


    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        confirmExit();
        return null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            confirmExit();
        }
        return false;
    }

    private static Boolean isExit = false;

    private void confirmExit() {
        // 判斷是否按下Back

        // 是否c要退出
        if (isExit == false) {
            isExit = true; //記錄下一次要退出
            Toast.makeText(getBaseContext(), getString(R.string.press_exit), Toast.LENGTH_SHORT).show();
            // 如果超過兩秒則恢復預設值
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒鐘內沒有按下返回鍵，就啟動定時器取消剛才執行的任務
        } else {

            finish(); // 離開程式
        }
    }

    @Override
    public void onClick(View v) {
        if (v == sellVerification) {
            Intent intent;
            intent = new Intent(getApplicationContext(), ExchangeOpenOrderActivity.class);
            startActivityForResult(intent, 0);
        } else if (v == btn_place_order) {
            Intent ie = new Intent(Intent.ACTION_VIEW, Uri.parse(BtcUrl.CW_XHCS_SITE));
            startActivity(ie);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        GetPendingOrder();

    }

    public void registerBroadcast(Context context) {
        LogUtil.e("註冊");
        xchsMsgListener = new XchsMsgListener();
        //註冊監聽廣播
        LocalBroadcastManager.getInstance(context).registerReceiver(xchsMsgListener, new IntentFilter(BTConfig.XCHS_NOTIFICATION));
    }

    public void unRegisterLocalBroadcast(Context context) {
        LogUtil.e("取消註冊");
        try {
            if (brocastNR != null) {
                LocalBroadcastManager.getInstance(context).unregisterReceiver(xchsMsgListener);
                brocastNR = null;
            }

        } catch (Exception e) {
            brocastNR = null;

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //註冊監聽(from BaseActivity)
        registerBroadcast(this, cmdManager);
        //註冊local
        registerBroadcast(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unRegisterBroadcast(this);
        unRegisterLocalBroadcast(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cmdManager == null) {
            cmdManager = new CmdManager();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ExchangeLogout();
    }
}
