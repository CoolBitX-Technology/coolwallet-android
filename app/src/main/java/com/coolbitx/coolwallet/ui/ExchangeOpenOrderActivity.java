package com.coolbitx.coolwallet.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.adapter.UnclarifyOrderAdapter;
import com.coolbitx.coolwallet.bean.ExchangeOrder;
import com.coolbitx.coolwallet.callback.APIResultCallback;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.general.ExchangeAPI;
import com.snscity.egdwlib.CmdManager;

import java.util.ArrayList;

/**
 * Created by ShihYi on 2015/12/25.
 */
public class ExchangeOpenOrderActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private CmdManager cmdManager;
    private Context mContext;
    private ExchangeAPI mExchangeAPI;
    private ListView listViewVerification;
    ArrayList<ExchangeOrder> listExchangeOrder;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_open_order);
        mContext = this;

        initViews();
        initValues();
        initToolbar();
        cmdManager = new CmdManager();
        mExchangeAPI = new ExchangeAPI(mContext,
                cmdManager);
        GetOpenOrder();
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
    protected void onStart() {
        super.onStart();
        //註冊監聽
        registerBroadcast(this, cmdManager);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unRegisterBroadcast(this);
    }

    private void GetOpenOrder() {
        mProgress.show();
        mExchangeAPI.getOrderInfo(new APIResultCallback() {
            @Override
            public void success(String[] msg) {
                mProgress.dismiss();

                listExchangeOrder = new ArrayList<>();
                listExchangeOrder = PublicPun.jsonParserExchange(msg[0], "response", false);
                if (listExchangeOrder.size() != 0) {
                    listViewVerification.setAdapter(new UnclarifyOrderAdapter(mContext, listExchangeOrder));
                } else {
                    clickToFinish(getString(R.string.open_orders), getString(R.string.no_open_orders_found));
                }
            }

            @Override
            public void fail(String msg) {
                mProgress.dismiss();
                clickToFinish(getString(R.string.unable_to_get_open_orders), getString(R.string.error)+":" + msg);
            }
        });
    }


    private void clickToFinish(String title, String msg) {
        AlertDialog.Builder mBuilder =
                PublicPun.CustomNoticeDialog(mContext, title, msg);
        mBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        }).show();
    }

    private void initViews() {
        listViewVerification = (ListView) findViewById(R.id.grid_verification);
        listViewVerification.setOnItemClickListener(this);
    }

    private void initValues() {
        mProgress = new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
        mProgress.setMessage(getString(R.string.synchronizing_data)+"...");
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.open_orders));
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
//        if (v == sellVerification) {
//            PublicPun.toast(mContext,"press sell verification");
//        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final int itemPos = position;

        final String orderID = listExchangeOrder.get(position).getOrderId();

        AlertDialog.Builder mBuilder =
                PublicPun.CustomNoticeDialog(mContext, getString(R.string.btn_cancel_order_str), getString(R.string.str_cancel_order_msg));
        mBuilder.setPositiveButton(getString(R.string.str_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // cancel order

                mExchangeAPI.cancelOrder(orderID, new APIResultCallback() {
                    @Override
                    public void success(String[] msg) {

                        GetOpenOrder();

                    }
                    @Override
                    public void fail(String msg) {
                        PublicPun.showNoticeDialog(mContext, getString(R.string.unable_to_cancel_order), getString(R.string.reason) + msg);
                    }
                });

            }
        }).setNegativeButton(getString(R.string.str_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        })
         .show();


    }

}
