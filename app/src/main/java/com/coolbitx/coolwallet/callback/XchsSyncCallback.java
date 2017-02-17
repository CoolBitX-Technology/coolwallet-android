package com.coolbitx.coolwallet.callback;

import com.coolbitx.coolwallet.entity.XchsSync;

import java.util.ArrayList;

/**
 * Created by ShihYi on 2017/2/15.
 */

public interface XchsSyncCallback {
    void onSuccess(ArrayList<XchsSync> listXchsSync);

}
