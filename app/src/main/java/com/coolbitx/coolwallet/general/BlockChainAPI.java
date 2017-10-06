package com.coolbitx.coolwallet.general;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import com.coolbitx.coolwallet.bean.UnSpentTxsBean;
import com.coolbitx.coolwallet.callback.UnSpentCallback;
import com.coolbitx.coolwallet.general.BtcUrl;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.httpRequest.CwBtcNetWork;
import java.util.List;

/**
 * Created by Dorac on 2017/9/19.
 */

public class BlockChainAPI {

    Context mContext;
    String CWID;

    public BlockChainAPI(Context context) {
        this.mContext = context;
        this.CWID = new String(PublicPun.hexStringToByteArray(PublicPun.card.getCardId()));
    }


    public void getUnspent( final String mAddrs,final UnSpentCallback unSpentCallback) {
        final ContentValues cv = new ContentValues();
        cv.put("addresses", mAddrs);

        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... param) {

                return new CwBtcNetWork().doGet(cv, BtcUrl.URL_BLOCKCHAIN_UNSPENT, null);
            }

            @Override
            protected void onPostExecute(String result) {

                if (result != null) {

                    if (result.equals("{\"errorCode\": 404}") || result.equals("{\"errorCode\": 400}") || result.equals("{\"errorCode\": 500}")) {
                        unSpentCallback.onException();
                    }
                    List<UnSpentTxsBean> unSpentTxsBeanList = PublicPun.jsonParseBlockChainInfoUnspent(result);

                    if (unSpentTxsBeanList != null) {
                        unSpentCallback.onSuccess(unSpentTxsBeanList);
                    } else {
                        unSpentCallback.onException();
                    }

                } else {
                    unSpentCallback.onException();
                }
            }
        }.execute();
    }
}
