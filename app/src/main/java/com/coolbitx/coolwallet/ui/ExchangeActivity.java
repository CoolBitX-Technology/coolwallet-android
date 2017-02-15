package com.coolbitx.coolwallet.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.adapter.PendingOrderAdapter;
import com.coolbitx.coolwallet.callback.APIResultCallback;
import com.coolbitx.coolwallet.entity.ExchangeOrder;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.util.ExchangeAPI;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.LogUtil;

import java.util.ArrayList;

/**
 * Created by ShihYi on 2015/12/25.
 */
public class ExchangeActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private CmdManager cmdManager;
    private Context mContext;
    private TextView sellVerification;
    private ListView gridViewSell;
    private ListView gridViewBuy;
    private ExchangeAPI mExchangeAPI;
    ArrayList<ExchangeOrder> listExchangeSellOrder;
    ArrayList<ExchangeOrder> listExchangeBuyOrder;
    private String orderId = "";
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

        mExchangeAPI.exchangeLogOut(new APIResultCallback() {
            @Override
            public void success(String[] msg) {
                LogUtil.e("Exchange Logout success");
                mExchangeAPI.exchangeLogin(new APIResultCallback() {
                    @Override
                    public void success(String[] msg) {
                        LogUtil.e("Exchange Login success");
                        GetPendingOrder();
                    }

                    @Override
                    public void fail(String msg) {
                        PublicPun.showNoticeDialog(mContext,"Notification","Exchange Login failed.");
                        ExchangeLogout();
                    }
                });
            }

            @Override
            public void fail(String msg) {
                PublicPun.showNoticeDialog(mContext,"Notification","Exchange Login failed.");
                ExchangeLogout();
            }
        });
    }

    private void GetPendingOrder() {
        listExchangeSellOrder = new ArrayList<>();
        listExchangeBuyOrder = new ArrayList<>();

        mExchangeAPI.getPendingOrder( new APIResultCallback() {
            @Override
            public void success(String[] msg) {
                LogUtil.d("GetPendingOrder ok " + msg[0]);
                String[] mString = new String[]{"sell", "buy"};

                for (int i = 0; i < mString.length; i++) {
                    if (i == 0) {
                        listExchangeSellOrder = PublicPun.jsonParserExchange(msg[0], mString[i]);
                        gridViewSell.setAdapter(new PendingOrderAdapter(mContext, listExchangeSellOrder));
                    } else {
                        listExchangeBuyOrder = PublicPun.jsonParserExchange(msg[0], mString[i]);
                        gridViewBuy.setAdapter(new PendingOrderAdapter(mContext, listExchangeBuyOrder));
                    }
                }
                LogUtil.d("listExchangeSellOrder.size=" + listExchangeSellOrder.size() + " ; listExchangeBuyOrder.size=" + listExchangeBuyOrder.size());
            }

            @Override
            public void fail(String msg) {
                LogUtil.e("getExchangeSync failed:" + msg);

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

        bundle.putSerializable("ExchangeOrder", exchngeOrder);
        intent.putExtras(bundle);
        startActivity(intent);

    }

    byte[] cancelBlkInfo = new byte[72];
    String orderID;
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        int clickId = parent.getId();
        ExchangeOrder exchngeOrder = new ExchangeOrder();
        orderID= null;
        switch (clickId) {
            case R.id.grid_sell:
                exchngeOrder = listExchangeSellOrder.get(position);
                break;
            case R.id.grid_buy:
                exchngeOrder = listExchangeBuyOrder.get(position);
                break;
        }

        orderID = exchngeOrder.getOrderId();
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

                mExchangeAPI.getExUnBlock(orderID, new APIResultCallback() {
                    @Override
                    public void success(String[] msg) {

//                        {"orderId":"15847930","okToken":"abc123","unblockTkn":"abc123",
//                                "mac":"dbe57d18f1c176606f40361a11c755ed655804a319d7b7120cdb1e729786d5dd"}
                        cancelBlkInfo = PublicPun.hexStringToByteArray(msg[0] + msg[1] + msg[2] + msg[3] + msg[4]);

                        cmdManager.XchsCancelBlock(cancelBlkInfo, new CmdResultCallback() {
                            @Override
                            public void onSuccess(int status, byte[] outputData) {

                                if ((status + 65536) == 0x9000) {//-28672//36864
                                    LogUtil.d("XchsCancelBlock  success = " + PublicPun.byte2HexString(outputData));
                                    cancelBlkInfo = outputData;

                                    mExchangeAPI.delExBlock(orderId, new APIResultCallback() {
                                        @Override
                                        public void success(String[] msg) {
                                            LogUtil.d("delExBlock  success = " + msg[0]);
                                            GetPendingOrder();
                                        }

                                        @Override
                                        public void fail(String msg) {
                                            LogUtil.d("delExBlock  failed = " + msg);
                                            ExchangeLogout();
                                        }
                                    });
                                } else {
                                    LogUtil.d("XchsCancelBlock fail");
                                    //for debug error code
                                    cmdManager.getError(new CmdResultCallback() {
                                        @Override
                                        public void onSuccess(int status, byte[] outputData) {
                                            LogUtil.d("Login failed = " + (status + 65536) + ";" + outputData);
                                        }
                                    });
                                }
                            }
                        });
                    }

                    @Override
                    public void fail(String msg) {
                        LogUtil.d("getExRequestOrderBlock failed:" + msg);
                        //exchangeSite Logout()
                        ExchangeLogout();
                    }
                });

            }
        });
        otp_dialog.show();
        return true;
    }

    private void ExchangeLogout(){
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

    }

    private void initValues() {

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Exchange");
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
        if (v == sellVerification) {
//            Intent intent;
//            intent = new Intent(getApplicationContext(), ExchangeVerificationActivity.class);
//            startActivityForResult(intent, 0);
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
//        GetPendingOrder();
    }
}
