package com.coolbitx.coolwallet.ui.Fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.coolbitx.coolwallet.DataBase.DatabaseHelper;
import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.adapter.TabFragmentPagerAdapter;
import com.coolbitx.coolwallet.bean.Address;
import com.coolbitx.coolwallet.bean.Constant;
import com.coolbitx.coolwallet.bean.CwBtcTxs;
import com.coolbitx.coolwallet.bean.dbAddress;
import com.coolbitx.coolwallet.callback.RefreshCallback;
import com.coolbitx.coolwallet.general.AppPrefrence;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.general.RefreshBlockChainInfo;
import com.coolbitx.coolwallet.util.Base58;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.ByteUtil;
import com.snscity.egdwlib.utils.LogUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by ShihYi on 2015/12/15.
 */
public class TabFragment extends Fragment {

    public static ArrayList<CwBtcTxs> lisCwBtcTxs = new ArrayList<CwBtcTxs>();
    //    public static ArrayList<ExchangeRate> lisExchangeRate = new ArrayList<ExchangeRate>();
    public static double ExchangeRate = 0.00;
    public static ArrayList<dbAddress> lisCwBtcAdd = new ArrayList<dbAddress>();
    public static DecimalFormat currentFormatter = new DecimalFormat("#.##");
    public static DecimalFormat BtcFormatter = new DecimalFormat("#.########");
    public final int HOME_PAGE = 0;
    public final int SEND_PAGE = 1;
    public final int RECEIVE_PAGE = 2;
    private SlidingTabLayout tabs;
    private ViewPager pager;
    private TabFragmentPagerAdapter adapter;
    private LinkedList<BaseFragment> fragments = null;
    private int indicatorColor = Color.BLACK;
    private int dividerColor = Color.TRANSPARENT;
    private int id = 0;
    private FragMainActivity.OnResumeFromBackCallBack mOnResumeFromBackCallBack = null;
    private MyPageChangeListener myPageChangeListener = null;
    private int mPageCurItem = -1;
    private RadioButton rbReceive;
    private RadioButton rbSend;
    private RadioButton rbHome;
    private MyCheckedChanged myCheckedChanged = null;
    private int mPageType = 0;
    private Boolean isAddFrag = false;
    private ProgressDialog mProgress;
    private Timer mTimer;
    CmdManager cmdManager;


    public static Fragment newInstance() {
        TabFragment f = new TabFragment();

        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        FragMainActivity activity = (FragMainActivity) getActivity();
        cmdManager = activity.getCmdManager();

        return inflater.inflate(R.layout.frag_tab, container, false);

    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {


        //adapter
        //fragments = getFragments();
        mProgress = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_DARK);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        isAddFrag = false;
        mPageType = HOME_PAGE;


        rbReceive = (RadioButton) getActivity().findViewById(R.id.rb_receive);
        rbSend = (RadioButton) getActivity().findViewById(R.id.rb_send);
        rbHome = (RadioButton) getActivity().findViewById(R.id.rb_home);

        //預設為home
        rbReceive.setChecked(false);
        rbSend.setChecked(false);
        rbHome.setChecked(true);

        rbHome.setBackgroundResource(R.drawable.home);
        rbSend.setBackgroundResource(R.drawable.send_gray);
        rbReceive.setBackgroundResource(R.drawable.receive_gray);

        fragments = new LinkedList<BaseFragment>();
        getFragments(mPageType);
        adapter = new TabFragmentPagerAdapter(getFragmentManager(), fragments);

        //pager
        pager = (ViewPager) view.findViewById(R.id.pager);
        //取消滑動
//        pager.beginFakeDrag();
        pager.setAdapter(adapter);
        //頁簽tabs
        tabs = (SlidingTabLayout) view.findViewById(R.id.tabs);
        tabs.setSelectedIndicatorColors(Color.DKGRAY);//Color.DKGRAY
        //dora
//        tabs.setDividerColors(R.color.md_white_1000);

        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {

            @Override
            public int getIndicatorColor(int position) {
                return fragments.get(position).getIndicatorColor();
            }

            @Override
            public int getDividerColor(int position) {
                return fragments.get(position).getDividerColor();
            }
        });


        tabs.setBackgroundResource(R.color.md_grey_900);
        tabs.setViewPager(pager);

        myPageChangeListener = new MyPageChangeListener();
        tabs.setOnPageChangeListener(myPageChangeListener);

        myCheckedChanged = new MyCheckedChanged();
        rbReceive.setOnCheckedChangeListener(myCheckedChanged);
        rbSend.setOnCheckedChangeListener(myCheckedChanged);
        rbHome.setOnCheckedChangeListener(myCheckedChanged);

        //for Drawer listItem back to HomeFragment,should refresh data.
        mOnResumeFromBackCallBack = new FragMainActivity.OnResumeFromBackCallBack() {
            @Override
            public void onRefresh() {
                AccountRefresh(pager.getCurrentItem());
            }
        };
        FragMainActivity.registerOnResumeFromBackCallBack(mOnResumeFromBackCallBack);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //更新畫面
        tabs.post(new Runnable() {
            @Override
            public void run() {
                AccountRefresh(0);
            }
        });

    }

    public int getPageType(){
        return mPageType;
    }
    /**
     * 記錄被選擇的選單(accountID)指標用
     */

    public int getAccoutId() {
        int currPage;
        try {
            currPage = pager.getCurrentItem();
        }catch(Exception e){
            currPage = 0;
        }

        return currPage;
    }

    /**
     * 根據不同的page，來產生Fragment的list
     *
     * @param type -- home, send , receive
     */
    private void getFragments(int type) {

        if (fragments == null) return;
        if (fragments.size() > 0) {
            adapter.clearFragment(fragments);
            fragments.clear();
        }

        for (int i = 1; i <= FragMainActivity.ACCOUNT_CNT; i++) {
            fragments.add(getFragmentByType(type, String.valueOf(i), i));
        }
        //當 < 5個時，後面要補一個 +
        if (FragMainActivity.ACCOUNT_CNT < 5) {
            fragments.add(getFragmentByType(type, "+", FragMainActivity.ACCOUNT_CNT));
        }
    }

    /**
     * 點了＋的動作會有兩個
     * 1.產生新的Fragment
     * 2.將+轉成title
     *
     * @param type -- home , send, receive
     */
    public void addFragmentToList(int type) {
        //LogUtil.i("addFragmentToList - addFragmentToList");
//        fragments.add(HomeFragment.newInstance(String.valueOf(fragments.size()+1), Color.DKGRAY, dividerColor, R.mipmap.logo2,fragments.size()+1));
//
         /*
            如果現在是2個ID，所以會是1,2,+
            當新增了一筆後，要變成是1,2,3,+
            因此要先把＋轉成3，再加上一個＋
           如果現在是4個ID,所以會是1,2,3,4,+
           所以只要把＋轉成5
             */

//        type = "home";
//        rbReceive.setChecked(false);
//        rbSend.setChecked(false);
//        rbHome.setChecked(true);
//        getFragments(type);

        LogUtil.i("pager.getCurrentItem()=" + pager.getCurrentItem());
        tabs.post(new Runnable() {
            @Override
            public void run() {
                AccountRefresh(pager.getCurrentItem());
            }
        });

        isAddFrag = true;

        rbReceive.setChecked(false);
        rbSend.setChecked(false);
        rbHome.setChecked(true);

        rbHome.setBackgroundResource(R.drawable.home);
        rbSend.setBackgroundResource(R.drawable.send_gray);
        rbReceive.setBackgroundResource(R.drawable.receive_gray);


//        FragMainActivity.ACCOUNT_CNT++;
        mPageType = HOME_PAGE;

        getFragments(mPageType);
        tabs.setOnPageChangeListener(null);
        adapter = new TabFragmentPagerAdapter(getFragmentManager(), fragments);
        pager.setAdapter(adapter);
        tabs.setViewPager(pager);


        if (fragments.size() == 5) {
            //LogUtil.e("fragment = 5 , setCurrentItem : "+(fragments.size()-2));
            if (fragments.get(fragments.size() - 1).getTitle().equals("+")) {
                pager.setCurrentItem(fragments.size() - 2);

            } else {
                pager.setCurrentItem(fragments.size() - 1);
            }
        } else {
            pager.setCurrentItem(fragments.size() - 2);
        }
        tabs.setOnPageChangeListener(myPageChangeListener);

        isAddFrag = false;

    }

    private void FunhdwSetAccInfo(final int account) {

        LogUtil.e("這是Main FunhdwSetAccInfo=" + account);
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
        listAddress = DatabaseHelper.queryAddress(getActivity(), account, -1);//ext+int

        LogUtil.i("before set webSocket account=" + PublicPun.accountSocketReg[account]+" ; send size="+listAddress.size());

        for (int i = 0; i < listAddress.size(); i++) {
            if (!PublicPun.accountSocketReg[account]) {
                if (listAddress.get(i).getKcid() == 0) { //只需註冊external地址
                    try {
                        FragMainActivity.socketHandler.SendMessage(PublicPun.jSonGen(listAddress.get(i).getAddress()));
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
                                    mProgress.dismiss();
                                    cmdManager.McuSetAccountState((byte) account, new CmdResultCallback() {
                                                @Override
                                                public void onSuccess(int status, byte[] outputData) {

                                                    if ((status + 65536) == 0x9000) {
                                                        LogUtil.i("McuSetAccountState !!!");
                                                    }
                                                    AccountRefresh(account);
                                                }
                                            }
                                    );
                                }
                            } else {
                                LogUtil.i("setAccountInfo failed.");
                                PublicPun.showNoticeDialog(getActivity(), getString(R.string.error_msg), getString(R.string.error) + ":" + Integer.toHexString(status));
                                mProgress.dismiss();
                            }
                        }
                    }
            );
        }
    }

    /**
     * 根據不同的page，產生不同的Fragment Instance
     *
     * @param type  -- home, send, receive
     * @param title -- 1,2,3,4,5,+
     * @param index -- 1,2,3,4,5
     * @return
     */
    private BaseFragment getFragmentByType(int type, String title, int index) {
        BaseFragment f = null;
        if (type == HOME_PAGE) {
            f = HomeFragment.newInstance(title, Color.DKGRAY, dividerColor, R.mipmap.logo2, index);//Color.DKGRAY
        } else if (type == SEND_PAGE) {
            f = SendFragment.newInstance(title, Color.DKGRAY, dividerColor, R.mipmap.logo2, index);
        } else {
            f = ReceiveFragment.newInstance(title, Color.DKGRAY, dividerColor, R.mipmap.logo2, index);
        }

        return f;
    }

    private void CreateNewAccount(final int accountId) {
        String accName = "";
        cmdManager.hdwCreateAccount(accountId, accName, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    byte[] balance = new byte[8];
                    cmdManager.hdwSetAccInfo(PublicPun.user.getMacKey(), (byte) 0x01, accountId, balance, new CmdResultCallback() {
                        @Override
                        public void onSuccess(int status, byte[] outputData) {
                            if ((status + 65536) == 0x9000) {
                                LogUtil.i("CwAccount " + accountId + " created!");
                                FragMainActivity.ACCOUNT_CNT++;
                                addFragmentToList(mPageType);
                                genChangeAddress(Constant.CwAddressKeyChainExternal, accountId);

                            }
                        }
                    });
                } else {
                    mProgress.dismiss();
                    PublicPun.showNoticeDialog(getActivity(), getString(R.string.error_msg), getString(R.string.error) + ":" + Integer.toHexString(status));
                }
            }
        });
    }

    public void genChangeAddress(final int keyChainId, int account) {
        final int accountId = account;
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
//                        LogUtil.i("初始帳戶地址:" + addr + " ;ACCOUNT=" + accountId);

                        PublicPun.account.setInputIndex(PublicPun.account.getInputIndex() + 1);
                        //初始帳戶
                        DatabaseHelper.insertAddress(getActivity(), accountId, addr, 0, keyId, 0, 0);
                        try {
                            FragMainActivity.socketHandler.SendMessage(PublicPun.jSonGen(addr));
                        } catch (Exception e) {
                            LogUtil.i("socket snedMsg error=" + e.getMessage());
                        }
                        mProgress.dismiss();
                    }
                }
            }
        });
    }

    /**
     * 先判斷是何種Fragment，轉型後在呼叫該Fragment的refresh
     *
     * @param position -- 目前所挑選的位置
     */
    public void AccountRefresh(int position) {
        LogUtil.i("TabFragment AccountRefresh=" + position);
        //dora for json function

        if (fragments != null) {
            if (fragments.size() > 0) {
                if (fragments.get(position) instanceof HomeFragment) {
                    lisCwBtcTxs = DatabaseHelper.queryTxs(getActivity(), position);
                    lisCwBtcAdd = DatabaseHelper.queryAddress(getActivity(), position, -1); // -1 = all addresses
                    ExchangeRate = DatabaseHelper.queryCurrent(getActivity(), AppPrefrence.getCurrentCountry(getActivity()));
                    AppPrefrence.saveCurrentRate(getActivity(), (float) ExchangeRate);
                    ((HomeFragment) fragments.get(position)).refresh();

                } else if (fragments.get(position) instanceof SendFragment) {
                    lisCwBtcAdd = DatabaseHelper.queryAddress(getActivity(), position, -1);// 0 = display ext addr only
                    ExchangeRate = DatabaseHelper.queryCurrent(getActivity(), AppPrefrence.getCurrentCountry(getActivity()));
                    AppPrefrence.saveCurrentRate(getActivity(), (float) ExchangeRate);
                    ((SendFragment) fragments.get(position)).refresh(position, FragMainActivity.scanningResult);

                } else {
                    lisCwBtcAdd = DatabaseHelper.queryAddress(getActivity(), position, 0);// 0 = display ext addr only
                    ((ReceiveFragment) fragments.get(position)).refresh(false);
                }
            }
        }
    }

    class MyPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            //如果所點的位置是最後一個時，且又是＋。則
            // 1.New Account
            // 2.進行增加list的動作

            final int accountID = position;
            LogUtil.i("切換標籤account=" + accountID);
            if (fragments.get(position).getTitle().equals("+")) {
                mProgress.setMessage(getString(R.string.create_account) + "...");
                mProgress.show();
                CreateNewAccount(accountID);
                addFragmentToList(mPageType);
            } else {
                //當切換位置
                if (!PublicPun.accountRefresh[accountID]) {
                    mProgress.setMessage(getString(R.string.synchronizing_data) + "...");
                    mProgress.show();
                    mTimer = new Timer();
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            getActivity().runOnUiThread(new Runnable() {
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
                    final RefreshBlockChainInfo refreshBlockChainInfo = new RefreshBlockChainInfo(getActivity(), accountID);
                    refreshBlockChainInfo.FunQueryAccountInfo(cmdManager, new RefreshCallback() {
                        @Override
                        public void onSuccess() {
                            refreshBlockChainInfo.callTxsRunnable(new RefreshCallback() {
                                @Override
                                public void onSuccess() {

                                    FunhdwSetAccInfo(accountID);
                                }
                                @Override
                                public void onFailure(String msg) {
                                    PublicPun.showNoticeDialog(getActivity(), getString(R.string.unable_connect_internet), msg);
                                    mProgress.dismiss();
                                }
                            });
                        }
                        @Override
                        public void onFailure(String msg) {
                            PublicPun.showNoticeDialog(getActivity(), getString(R.string.error_msg), msg);
                            mProgress.dismiss();
                        }
                    });
                    PublicPun.accountRefresh[accountID] = true;
                }
                //1.進行refresh
                AccountRefresh(accountID);
                //2.change card display
                byte ByteAccId = (byte) accountID;
                cmdManager.McuSetAccountState(ByteAccId, new CmdResultCallback() {
                            @Override
                            public void onSuccess(int status, byte[] outputData) {
                                if ((status + 65536) == 0x9000) {
                                    LogUtil.i("McuSetAccountState to: account" + accountID);
                                }
                            }
                        }
                );
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }

    class MyCheckedChanged implements RadioButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            try {

                if (!isAddFrag) {
                    if (buttonView == rbSend) {
                        if (isChecked) {
                            rbSend.setBackgroundResource(R.drawable.send);
                            rbHome.setBackgroundResource(R.drawable.home_gray);
                            rbReceive.setBackgroundResource(R.drawable.receive_gray);

                            mPageType = SEND_PAGE;
                        }
                    } else if (buttonView == rbHome) {
                        if (isChecked) {

                            rbHome.setBackgroundResource(R.drawable.home);
                            rbSend.setBackgroundResource(R.drawable.send_gray);
                            rbReceive.setBackgroundResource(R.drawable.receive_gray);
                            mPageType = HOME_PAGE;
                        }
                    } else if (buttonView == rbReceive) {
                        if (isChecked) {
                            rbHome.setBackgroundResource(R.drawable.home_gray);
                            rbSend.setBackgroundResource(R.drawable.send_gray);
                            rbReceive.setBackgroundResource(R.drawable.receive);
                            mPageType = RECEIVE_PAGE;

                        }
                    }

                    if (isChecked) {
                        mPageCurItem = pager.getCurrentItem();
                        getFragments(mPageType);
                        tabs.setOnPageChangeListener(null);
                        adapter = new TabFragmentPagerAdapter(getFragmentManager(), fragments);
                        pager.setAdapter(adapter);
                        tabs.setViewPager(pager);
                        pager.setCurrentItem(mPageCurItem);
                        tabs.setOnPageChangeListener(myPageChangeListener);
                        tabs.post(new Runnable() {
                            @Override
                            public void run() {
                                AccountRefresh(pager.getCurrentItem());
                            }
                        });
                    }
                }

            } catch (Exception e) {
                LogUtil.i("切換頁面錯誤=" + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}