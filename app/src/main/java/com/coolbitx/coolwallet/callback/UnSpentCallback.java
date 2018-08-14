package com.coolbitx.coolwallet.callback;

import com.coolbitx.coolwallet.bean.UnSpentTxsBean;

import java.util.List;

/**
 * Created by Dorac on 2017/9/19.
 */

public interface UnSpentCallback {
    void onSuccess(List<UnSpentTxsBean> UnSpentTxsBeanList);
    void onException(String msg);
}
