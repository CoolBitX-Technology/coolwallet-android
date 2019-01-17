package com.coolbitx.coolwallet.httpRequest;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.coolbitx.coolwallet.DataBase.DatabaseHelper;
import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.bean.UnSpentTxsBean;
import com.coolbitx.coolwallet.bean.dbAddress;
import com.coolbitx.coolwallet.callback.APIPostCallback;
import com.coolbitx.coolwallet.callback.APIResultCallback;
import com.coolbitx.coolwallet.callback.RefreshCallback;
import com.coolbitx.coolwallet.callback.UnSpentCallback;
import com.coolbitx.coolwallet.exception.ValidationException;
import com.coolbitx.coolwallet.general.BtcUrl;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.util.ExtendedKey;
import com.coolbitx.coolwallet.util.HttpUtils;
import com.snscity.egdwlib.utils.LogUtil;

import org.json.JSONException;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dorac on 2017/9/19.
 */

public class BlockChainAPI {

    Context mContext;

    public BlockChainAPI(Context context) {
        this.mContext = context;
    }

    public int updateBalance(final ContentValues cv) {
        int errCnt = 0;
        String result = "";
        String[] listAddrs = cv.getAsString("addresses").split("\\|");
        for (String s : listAddrs) {
            cv.put("addresses", s);
            result = new CwBtcNetWork().doGet(cv, BtcUrl.URL_SERVER_BC_CBX_IO + BtcUrl.URL_PATH_ADDR, "");
            if (TextUtils.isEmpty(result) || result.contains("errorCode")) {
                errCnt++;
            } else {
                try {
                    PublicPun.jsonParseAddressinfo(mContext, result, cv);
                } catch (JSONException e) {
                    e.printStackTrace();
                    errCnt++;
                }
            }
        }
        return errCnt == 0 ? 1 : -1;
    }

    @SuppressLint("StaticFieldLeak")
    public void broacastTx(final String rawTx, final APIPostCallback apiPostCallback) {

        new AsyncTask<String, Integer, Integer>() {

            @Override
            protected Integer doInBackground(String... strings) {
                int code = new CwBtcNetWork().doPost(BtcUrl.URL_BLOCKCHAIN_SERVER_SITE + BtcUrl.URL_BLICKCHAIN_PUSH, rawTx, false);
                if (code != HttpURLConnection.HTTP_OK) {
                    code = new CwBtcNetWork().doPost(BtcUrl.URL_SERVER_BC_CBX_IO + BtcUrl.URL_PATH_PUSH, rawTx, true);
                }
                return code;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    apiPostCallback.onSuccess();
                } else {
                    apiPostCallback.onFailure(responseCode);
                }

            }
        }.execute(rawTx);

    }


    @SuppressLint("StaticFieldLeak")
    public void getUnspent(String postData, final UnSpentCallback unSpentCallback) {

        new AsyncTask<String, Integer, List<UnSpentTxsBean>>() {
            @Override
            protected List<UnSpentTxsBean> doInBackground(String... strings) {
                List<UnSpentTxsBean> unSpentTxsBeanList = null;
                try {
                    unSpentTxsBeanList = performRequest(strings[0]);
                } catch (JSONException e) {
                    e.printStackTrace();
                    unSpentCallback.onException(e.getMessage());
                }
                return unSpentTxsBeanList;
            }

            @Override
            protected void onPostExecute(List<UnSpentTxsBean> UnSpentTxsBeans) {
                if (UnSpentTxsBeans == null) {
                    unSpentCallback.onException(mContext.getString(R.string.note_unspent));
                } else {
                    LogUtil.d("UnSpentTxsBeans 取得完成有 " + UnSpentTxsBeans.size() + " 筆");
                    unSpentCallback.onSuccess(UnSpentTxsBeans);
                }
            }
        }.execute(postData);

    }

    private ArrayList<UnSpentTxsBean> performRequest(String mAddr) throws JSONException {
        boolean isNode;

        String result;
        result = callAPI(BtcUrl.URL_BLOCKCHAIN_SERVER_SITE + BtcUrl.URL_BLOCKCHAIN_UNSPENT, mAddr, "");
        isNode = false;
//        result = "{\"errorCode\": " + "404" + "}"; //for test node API
        if (TextUtils.isEmpty(result) || result.contains("errorCode")) {
            mAddr = mAddr.replace("|", ",");//different separator.
            result = callAPI(BtcUrl.URL_SERVER_BC_CBX_IO + BtcUrl.URL_PATH_ADDRS, mAddr, BtcUrl.URL_PATH_UTXO);
            isNode = true;
            if (TextUtils.isEmpty(result) || result.contains("errorCode")) {
                return null;
            }
        }

        return PublicPun.jsonParseBlockChainInfoUnspent(result, isNode);
    }

    private String callAPI(String url, String param, String option) {
        ContentValues cv = new ContentValues();
        cv.put("addresses", param);
        return new CwBtcNetWork().doGet(cv, url, option);
    }

}

