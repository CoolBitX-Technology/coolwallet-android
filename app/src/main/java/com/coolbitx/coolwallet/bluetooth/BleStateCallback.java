package com.coolbitx.coolwallet.bluetooth;

import android.content.Context;

import com.coolbitx.coolwallet.general.PublicPun;
import com.snscity.egdwlib.BleManager;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;

/**
 * Created by ShihYi on 2015/10/2.
 */
public class BleStateCallback {

    private Context mContext;
    private BleManager mleManager;
    private CmdManager cmdManager;

    public BleStateCallback(Context context,BleManager blemanager){
        this.mContext = context;
        this.mleManager=blemanager;
    }
        public void onBleConnected() {
            cmdManager.getModeState(new CmdResultCallback() {
                @Override
                public void onSuccess(int status, byte[] outputData) {
                    if ((status + 65536) == 0x9000) {//-28672//36864
                        PublicPun.modeState = PublicPun.selectMode(PublicPun.byte2HexString(outputData[0]));
//                        PublicPun.toast(mContext, "Connected../nMode=" + PublicPun.modeState+"/nState=" + outputData[1]);
//                        modeStatusTv.setText("Mode=" + PublicPun.modeState);
//                        modeStatusCodeTv.setText("State=" + outputData[1]);
                    }
                }
            });
            cmdManager.getFwVersion(new CmdResultCallback() {
                @Override
                public void onSuccess(int status, byte[] outputData) {
                    if ((status + 65536) == 0x9000) {
                        PublicPun.fwVersion = PublicPun.byte2HexString(outputData).replace(" ", "");
//                        fwVersion.setText("FwVersion=" + PublicPun.fwVersion);
                    }
                }
            });
            cmdManager.getUniqueId(new CmdResultCallback() {
                @Override
                public void onSuccess(int status, byte[] outputData) {
                    if ((status + 65536) == 0x9000) {
                        PublicPun.uid = PublicPun.byte2HexString(outputData).replace(" ", "");
//                        uid.setText("UniqueId=" + PublicPun.uid);
                    }
                }
            });

            //睡300毫秒
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

//            if(!currentUuid.equals("") && !currentOptCode.equals("")){
//                //获取当前手机设备uuid对应的hostid
//                cmdManager.bindFindHstid(currentUuid, new CmdResultCallback() {
//                    @Override
//                    public void onSuccess(int status, byte[] outputData) {
//                        if ((status + 65536) == 0x9000) {
//                            LogUtil.e("当前uuid对应" + PublicPun.byte2HexString(outputData));
//                            if(outputData != null && outputData.length == 2){
//                                hostID = outputData[0];
//                            }
//                        }
//                    }
//                });
//
//                reg1.setEnabled(false);
//                reg2.setEnabled(false);
//                login1.setEnabled(true);
//                erasureInfo.setEnabled(true);
//            }else {
//                reg1.setEnabled(true);
//                reg2.setEnabled(true);
//                login1.setEnabled(false);
//                erasureInfo.setEnabled(true);
//            }

        }

        public void onBleDisConnected() {
            PublicPun.toast(mContext, "Disconnect-_-");
        }
}
