package com.coolbitx.coolwallet.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.adapter.HostAdapter;
import com.coolbitx.coolwallet.entity.Constant;
import com.coolbitx.coolwallet.entity.Host;
import com.coolbitx.coolwallet.general.PublicPun;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ShihYi on 2015/12/25.
 */
public class HostDeviceActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private Context context;
    private LayoutInflater layoutInflater;

    private ListView hostList;
    private List<Host> data = new ArrayList<>();
    private HostAdapter adapter;
    private CmdManager cmdManager;
    private ProgressDialog mProgress;

    //    @Nullable
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_device);

        context = this;
        layoutInflater = getLayoutInflater();
        initToolbar();

        mProgress = new ProgressDialog(HostDeviceActivity.this);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        cmdManager = new CmdManager();
        hostList = (ListView) findViewById(R.id.lv_host);
        hostList.setOnItemClickListener(this);
        hostList.requestFocus();
        data = PublicPun.hostList;

        Collections.sort(data, new Comparator<Host>() {
            @Override
            public int compare(Host arg0, Host arg1) {
                return String.valueOf(arg0.getId()).compareTo(String.valueOf(arg1.getId()));
            }
        });

        updataViews();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        hostList.requestFocus();
        if (data != null && !data.isEmpty()) {
            Host bean = data.get(position);
            if (bean != null) {

                final byte hostId = (byte) bean.getId();
                byte bindStatus = bean.getBindStatus();
                //bindStatus
                //0x00 = Empty
                //0x01 = Registered
                //0x02 = Confirmed
//                    LogUtil.i("host:"+bean.getDesc()+";"+Build.DEVICE);
                if (bean.getDesc().equals(Build.DEVICE)) {

                } else {
                    switch (bindStatus) {

                        case 0x00:

                            break;

                        case 0x01:
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage("Approve this device");
                            builder.setCancelable(true);
                            //建立按下按鈕
                            DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //如果不做任何事情 就會直接關閉 對話方塊

                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE: //aprroved

                                            if (PublicPun.card.getMode().equals("PERSO") ||
                                                    PublicPun.card.getMode().equals("NORMAL") ||
                                                    PublicPun.card.getMode().equals("AUTH")) {
                                                cmdManager.bindRegApprove(hostId, new CmdResultCallback() {
                                                    @Override
                                                    public void onSuccess(int status, byte[] outputData) {
                                                        if ((status + 65536) == 0x9000) {
                                                            PublicPun.ClickFunction(context, "Host Approved", "");
                                                            updataViews();
                                                            getHosts();
                                                        } else {
                                                            PublicPun.ClickFunction(context, "Host Approved", "Approved failed:" + Integer.toHexString(status));
                                                        }
                                                    }
                                                });
                                            }
                                            break;

                                        case DialogInterface.BUTTON_NEGATIVE: //removed

                                            cmdManager.bindRegRemove(hostId, new CmdResultCallback() {
                                                @Override
                                                public void onSuccess(int status, byte[] outputData) {
                                                    if ((status + 65536) == 0x9000) {
                                                        PublicPun.ClickFunction(context, "Host Removed", "");
                                                        getHosts();
                                                    } else {
                                                        PublicPun.ClickFunction(context, "Host Removed", "Removed failed:" + Integer.toHexString(status));
                                                    }
                                                }
                                            });
                                            break;
                                    }
                                }
                            };
                            builder.setNegativeButton("Remove", OkClick);
                            builder.setPositiveButton("Approve", OkClick);
                            builder.show();
                            break;

                        case 0x02:
                            AlertDialog.Builder host_dialog = new AlertDialog.Builder(HostDeviceActivity.this);
                            host_dialog.setCancelable(true)
                                    .setMessage("Remove this device")
                                    .setNeutralButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //不做任何事情 直接關閉對話方塊
                                                    cmdManager.bindRegRemove(hostId, new CmdResultCallback() {
                                                        @Override
                                                        public void onSuccess(int status, byte[] outputData) {
                                                            if ((status + 65536) == 0x9000) {
                                                                PublicPun.ClickFunction(context, "Host Removed!", "");
                                                                getHosts();
                                                            } else {
                                                                PublicPun.ClickFunction(context, "Host Removed", "Removed failed:" + Integer.toHexString(status));
                                                            }
                                                        }
                                                    });
                                                }
                                            }).setNegativeButton(R.string.strCancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            host_dialog.show();
                            break;
                    }
                }
            }
        }
    }


    private void updataViews() {
        LogUtil.i("updataViews!!");
        if (adapter == null) {
            adapter = new HostAdapter(data, context);
        } else {
            adapter.notifyDataSetChanged();
        }
        hostList.setAdapter(adapter);
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        finish();
        return null;
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(getResources().getDrawable(R.mipmap.host));
        toolbar.setTitle("Host devices");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.menu_3x);
        ActionBar actionBar = getSupportActionBar();
        // 打開 up button_up
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }


    private void getHosts() {
        LogUtil.i("getHosts!!");
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
                    if (outputData != null) {
                        addHostList(outputData, second);
                    }
                }
            }
        });
        cmdManager.bindRegInfo(third, new CmdResultCallback() {
            @Override
            public void onSuccess(int status, byte[] outputData) {
                if ((status + 65536) == 0x9000) {
                    if (outputData != null) {
                        addHostList(outputData, third);
                        updataViews();
                    }
                }
            }
        });
    }

    private void addHostList(byte[] outputData, byte hostId) {
        try {
            Host bean = new Host();
            byte bindStatus = outputData[0];
            int length = outputData.length - 1;
            byte[] desc = new byte[length];

            if (length > 0) {
                for (int i = 0; i < length; i++) {
                    desc[i] = outputData[i + 1];
                }
            }
            if (length > 1) {
                bean.setBindStatus(bindStatus);
                bean.setId(hostId);
                bean.setDesc(new String(desc, Constant.UTF8).toString().trim());
                LogUtil.i("卡片描述 id=" + hostId + ";status" + bindStatus + ";" + ";desc=" + new String(desc, Constant.UTF8).toString().trim());
                //會發生0x01先跑完,IndexOutOfBoundsException, remove ind
//        if (hostId == 0x00) {
//            PublicPun.hostList.add(0, bean);
//        } else if (hostId == 0x01) {
//            PublicPun.hostList.add(1, bean);
//        } else if (hostId == 0x02) {
//            PublicPun.hostList.add(2, bean);
//        }
                PublicPun.hostList.add(bean);
            }
        } catch (Exception e) {
            LogUtil.e("addHostList error=" + e.getMessage());
        }
    }
}
