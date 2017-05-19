package com.coolbitx.coolwallet.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
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
import com.coolbitx.coolwallet.adapter.PendingOrderAdapter;
import com.coolbitx.coolwallet.bean.ExchangeOrder;
import com.coolbitx.coolwallet.callback.APIResultCallback;
import com.coolbitx.coolwallet.general.BtcUrl;
import com.coolbitx.coolwallet.general.PublicPun;
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

    private void GetPendingOrder() {
        mProgress.show();
        listExchangeSellOrder = new ArrayList<>();
        listExchangeBuyOrder = new ArrayList<>();

        mExchangeAPI.getPendingOrder(new APIResultCallback() {
            @Override
            public void success(String[] msg) {
//                LogUtil.d("GetPendingOrder ok " + msg[0]);
                String[] mString = new String[]{"sell", "buy"};
                mProgress.dismiss();
                for (int i = 0; i < mString.length; i++) {
                    if (i == 0) {
                        listExchangeSellOrder = PublicPun.jsonParserExchange(msg[0], mString[i], true);
                        gridViewSell.setAdapter(new PendingOrderAdapter(mContext, listExchangeSellOrder));
                    } else {
                        listExchangeBuyOrder = PublicPun.jsonParserExchange(msg[0], mString[i], true);
                        gridViewBuy.setAdapter(new PendingOrderAdapter(mContext, listExchangeBuyOrder));
                    }
                }
//                LogUtil.d("listExchangeSellOrder.size=" + listExchangeSellOrder.size() + " ; listExchangeBuyOrder.size=" + listExchangeBuyOrder.size());

                if (listExchangeSellOrder.size() == 0 && listExchangeBuyOrder.size() == 0) {
                    isShowOrders(false);
                } else {
                    isShowOrders(true);
                }
            }

            @Override
            public void fail(String msg) {

                LogUtil.e("getExchangeSync failed:" + msg);
                mProgress.dismiss();
                PublicPun.showNoticeDialog(mContext, "Exchange Sync failed", "Please try again later.");
                //exchangeSite Logout()
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
        intent.setClass(mContext, ExchangeOrderActivity.class);
        Bundle bundle = new Bundle();
        LogUtil.i("exchangeOrder click" +
                exchngeOrder.getOrderId() + " , " + exchngeOrder.getAddr() + " , " +
                exchngeOrder.getAmount() + " , " + exchngeOrder.getAccount() + " , " +
                exchngeOrder.getPrice() + " , " + exchngeOrder.getExpiration());

        bundle.putSerializable("ExchangeOrder", exchngeOrder);
        intent.putExtras(bundle);
        startActivityForResult(intent, 0);

    }

    byte[] cancelBlkInfo = new byte[72];

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        int clickId = parent.getId();
        ExchangeOrder exchngeOrder = new ExchangeOrder();
        orderId = null;

        if(listExchangeSellOrder.size()==0){
            return false;
        }

        switch (clickId) {
            case R.id.grid_sell:
                exchngeOrder = listExchangeSellOrder.get(position);

                orderId = exchngeOrder.getOrderId();

//        final View item = LayoutInflater.from(mContext).inflate(R.layout.progress_dialog_layout, null);
                AlertDialog.Builder otp_dialog = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT);
//        otp_dialog.setView(item);
                otp_dialog.setCancelable(true);
                otp_dialog.setMessage("Are you sure you want to cancel the block order?");
                otp_dialog.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
//                cancelTrx(truP e);
                        //queryAccountInfo
                        mProgress.setMessage("Canceling the block order...");
                        mProgress.show();
                        mExchangeAPI.getExUnBlock(orderId, new APIResultCallback() {
                            @Override
                            public void success(String[] msg) {

//                        {"orderId":"15847930","okToken":"abc123","unblockTkn":"abc123",
//                                "mac":"dbe57d18f1c176606f40361a11c755ed655804a319d7b7120cdb1e729786d5dd"}

                                cancelBlkInfo = PublicPun.hexStringToByteArray(msg[0] + msg[1] + msg[2] + msg[3] + msg[4]);

                                LogUtil.e("cancelBlkInfo=trxID:" + msg[0] + ";OKTKN:" + msg[1] + ";UBLKTKN:" + msg[2] + ";MAC:" + msg[3] + ";NONCE:" + msg[4]);

                                cmdManager.XchsCancelBlock(cancelBlkInfo, new CmdResultCallback() {
                                    @Override
                                    public void onSuccess(int status, byte[] outputData) {

                                        if ((status + 65536) == 0x9000) {//-28672//36864
                                            LogUtil.d("cmd XchsCancelBlock  success = " + PublicPun.byte2HexString(outputData));
                                            cancelBlkInfo = outputData;

                                            mExchangeAPI.cancelOrder(orderId, new APIResultCallback() {
                                                @Override
                                                public void success(String[] msg) {
                                                    LogUtil.d("cancelOrder  success = " + msg[0]);
                                                    GetPendingOrder();
                                                    mProgress.dismiss();
                                                }

                                                @Override
                                                public void fail(String msg) {
                                                    LogUtil.d("cancelOrder  failed = " + msg);
                                                    mProgress.dismiss();
                                                    PublicPun.showNoticeDialog(mContext, "Unable to Cancel Block", "Error:" + msg);
                                                }
                                            });
                                        } else {
                                            mProgress.dismiss();
                                            LogUtil.d("XchsCancelBlock fail");
                                            //for debug error code
                                            PublicPun.showNoticeDialog(mContext, "Unable to Cancel Block", "Error:" + Integer.toHexString(status)
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
                                LogUtil.d("getExRequestOrderBlock failed:" + msg);
                                mProgress.dismiss();
                                PublicPun.showNoticeDialog(mContext, "Unable to Cancel Block", "Error:" + msg);
                            }
                        });

                    }
                });
                otp_dialog.show();




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
        toolbar.setTitle("Exchange");
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
                            LogUtil.d("XchsGetOtp ok= " + outputData);
                        } else {
                            LogUtil.d("XchsGetOtp fail= " + outputData);

                        }
                    }
                });
                break;

            default:
                break;
        }

        return true;
    }


//    @Override
//    public Intent getSupportParentActivityIntent() {
//        confirmExit();
//        return null;
//    }


    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        LogUtil.e("getSupportParentActivityIntent!!!!!");
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
            Toast.makeText(getBaseContext(), "Press once again to exit Exchange!", Toast.LENGTH_SHORT).show();
            // 如果超過兩秒則恢復預設值
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒鐘內沒有按下返回鍵，就啟動定時器取消剛才執行的任務
        } else {

            finish(); // 離開程式
//                System.exit(0);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == sellVerification) {
            Intent intent;
            intent = new Intent(getApplicationContext(), ExchangeVerificationActivity.class);
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

    @Override
    protected void onResume() {
        super.onResume();
        if (cmdManager == null) {
            cmdManager = new CmdManager();
        }
        //註冊監聽
        registerBroadcast(this, cmdManager);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unRegisterBroadcast(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ExchangeLogout();
    }
}
