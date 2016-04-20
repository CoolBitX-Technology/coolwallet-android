package com.coolbitx.coolwallet.ui.Fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.coolbitx.coolwallet.DataBase.DatabaseHelper;
import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.adapter.AddressInfoAdapter;
import com.coolbitx.coolwallet.callback.RefreshCallback;
import com.coolbitx.coolwallet.entity.CwBtcTxs;
import com.coolbitx.coolwallet.entity.dbAddress;
import com.coolbitx.coolwallet.general.AppPrefrence;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.general.RefreshBlockChainInfo;
import com.coolbitx.coolwallet.ui.TxsActivity;
import com.coolbitx.coolwallet.util.CwBtcNetWork;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.ByteUtil;
import com.snscity.egdwlib.utils.LogUtil;

import java.util.ArrayList;

/**
 * Created by ShihYi on 2015/12/14.
 */
public class HomeFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private String value = "";
    private String title = "";
    private static final String DATA_NAME = "name";
    //    private Context mContext;
    TextView txtTitle;
    TextView txtSubTiltle;
    TextView txtSubTitl_country;
    ListView lsvInfo;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CmdManager cmdManager;
    CwBtcNetWork cwBtcNetWork;
    private ProgressDialog mProgress;
    //modify by kunming
    //move the json function to TabFragment
//    public static ArrayList<CwBtcTxs> lisCwBtcTxs = new ArrayList<CwBtcTxs>();
//    public static ArrayList<ExchangeRate> lisExchangeRate = new ArrayList<ExchangeRate>();
//    public static ArrayList<ParsingAddress> lisCwBtcAdd = new ArrayList<ParsingAddress>();
    public static double CURRENT_RATE = 1;
    //modify
    private int id;
    private static final String DATA_ID = "id";

    public static HomeFragment newInstance(String title, int indicatorColor, int dividerColor, int iconResId, int accountId) {
        LogUtil.i("FragAccount" + accountId + " - newInstance accountId :" + accountId);
        HomeFragment f = new HomeFragment();
        f.setTitle(title);
        f.setIndicatorColor(indicatorColor);
        f.setDividerColor(dividerColor);
        f.setIconResId(iconResId);


        //pass data
        Bundle args = new Bundle();
        args.putString(DATA_NAME, title);

        //modify
        args.putInt(DATA_ID, accountId);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        FragMainActivity mainActivity = (FragMainActivity) activity;
//        value = mainActivity.getAccount1();
        //modify

//        mContext = mainActivity;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cmdManager = new CmdManager();
        //get data
        title = getArguments().getString(DATA_NAME);

        //modify
        id = getArguments().getInt(DATA_ID);
        value = ((FragMainActivity) mContext).getAccountFrag(id);

        LogUtil.e("FragAccount" + id + " - onCreate");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_frag, container, false);
        LogUtil.e("FragAccount" + id + " - onCreateView");
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.cw_home_refresh);
        txtTitle = (TextView) view.findViewById(R.id.tv_frag1_title);
        txtSubTiltle = (TextView) view.findViewById(R.id.tv_frag1_subtitle);
        txtSubTitl_country = (TextView) view.findViewById(R.id.tv_subtitle_country);
        lsvInfo = (ListView) view.findViewById(R.id.lsv_frag1_info);
        lsvInfo.setOnItemClickListener(this);
        cwBtcNetWork = new CwBtcNetWork();
        //can't use mContext,but Activity.this.
        mProgress = new ProgressDialog(getActivity());
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
//        refresh();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                final int mAccount = id - 1;
                mProgress.setMessage("Refreshing transaction data...");
                mProgress.show();

                RefreshBlockChainInfo refreshBlockChainInfo = new RefreshBlockChainInfo(mContext, mAccount);
                refreshBlockChainInfo.callTxsRunnable(new RefreshCallback() {
                    @Override
                    public void success() {
                        FunhdwSetAccInfo(mAccount);
                    }

                    @Override
                    public void fail(String msg) {
                        LogUtil.i("rollback failed");
                        PublicPun.ClickFunction(mContext, "Unstable internet connection", msg);
                        mProgress.dismiss();
                        TabFragment.lisCwBtcTxs = DatabaseHelper.queryTxs(getActivity(), mAccount);
                        TabFragment.lisCwBtcAdd = DatabaseHelper.queryAddress(getActivity(), mAccount, -1);
                        TabFragment.ExchangeRate = DatabaseHelper.queryCurrent(getActivity(), AppPrefrence.getCurrentCountry(getActivity()));
                        refresh();
                    }

                    @Override
                    public void exception(String msg) {

                    }
                });
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LogUtil.e("FragAccount" + id + " - onViewCreated");


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogUtil.e("FragAccount" + id + " - onActivityCreated");
    }

    @Override
    public void onDestroy() {
        LogUtil.e("FragAccount" + id + " - onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        LogUtil.e("FragAccount" + id + " - onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        CwBtcTxs objTxs = lisCwBtcTxs.get(position);
        CwBtcTxs objTxs = TabFragment.lisCwBtcTxs.get(position);
        Intent intent = new Intent();
        intent.setClass(mContext, TxsActivity.class);
        Bundle bundle = new Bundle();

        bundle.putString("addr", objTxs.getTxs_Address());
        bundle.putDouble("btc", objTxs.getTxs_Result() * PublicPun.SATOSHI_RATE);
//        bundle.putDouble("usd", objTxs.getTxs_Result() * CURRENT_RATE);
//        bundle.putDouble("usd",AppPrefrence.getCurrentRate(mContext));
        bundle.putString("date", objTxs.getTxs_Date());
        bundle.putString("confirmation", String.valueOf(objTxs.getTxs_Confirmation()));
        bundle.putString("tid", objTxs.getTxs_TransationID());

        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        LogUtil.e("FragAccount" + id + " - onAttach");
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void FunhdwSetAccInfo(int account) {
        LogUtil.e("這是home FunhdwSetAccInfo=" + account);
        byte ByteAccId = (byte) account;
        //for card display
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
        final byte cwHdwAccountInfoBalance = 0x01;
        byte[] accountInfo = new byte[32];
        int dbTotalBalance = 0;
        ArrayList<dbAddress> listAddress = new ArrayList<dbAddress>();
        listAddress = DatabaseHelper.queryAddress(mContext, account, -1);
        for (int i = 0; i < listAddress.size(); i++) {
            dbTotalBalance += listAddress.get(i).getBalance();
        }
        accountInfo = new byte[8];
        //204E000000000000
        byte[] newBalanceBytes = ByteUtil.intToByteLittle(dbTotalBalance, 8);
        accountInfo = newBalanceBytes;
        final int accountID = account;
        cmdManager.hdwSetAccInfo(PublicPun.user.getMacKey(), cwHdwAccountInfoBalance, account, accountInfo,
                new CmdResultCallback() {
                    @Override
                    public void onSuccess(int status, byte[] outputData) {
                        if ((status + 65536) == 0x9000) {
                            mProgress.dismiss();
                            TabFragment.lisCwBtcTxs = DatabaseHelper.queryTxs(getActivity(), accountID);
                            TabFragment.lisCwBtcAdd = DatabaseHelper.queryAddress(getActivity(), accountID, -1);
                            TabFragment.ExchangeRate = DatabaseHelper.queryCurrent(getActivity(), AppPrefrence.getCurrentCountry(getActivity()));
                            refresh();
                        } else {
                            LogUtil.i("setAccountInfo failed.");
                            PublicPun.toast(mContext, "setAccountInfo failed!");
                            mProgress.dismiss();
                        }
                    }
                }
        );
    }

    public void refresh() {

        int final_balance = 0;

        LogUtil.i("TabFragment.lisCwBtcTxs.size()=" + String.valueOf(TabFragment.lisCwBtcTxs.size()));

//        if (TabFragment.lisCwBtcTxs.size()!=0) {
        for (int i = 0; i < TabFragment.lisCwBtcAdd.size(); i++) {
            final_balance += TabFragment.lisCwBtcAdd.get(i).getBalance();
        }
        final double btcAmt = final_balance * PublicPun.SATOSHI_RATE;
        try {
            double currRate = btcAmt * AppPrefrence.getCurrentRate(mContext);
            LogUtil.i("btcAmt=" + TabFragment.BtcFormatter.format(btcAmt) + " ;currRate=" + currRate + " ;rate=" + AppPrefrence.getCurrentRate(mContext));

            txtTitle.setText(TabFragment.BtcFormatter.format(btcAmt));
            txtSubTiltle.setText(TabFragment.currentFormatter.format(currRate));
            txtSubTitl_country.setText(AppPrefrence.getCurrentCountry(mContext));
            lsvInfo.setAdapter(new AddressInfoAdapter(mContext, TabFragment.lisCwBtcTxs));
        } catch (Exception e) {

        }
    }
}
