package com.coolbitx.coolwallet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import com.snscity.egdwlib.utils.LogUtil;

import java.util.List;

/**
 * Created by ShihYi on 2016/6/29.
 */
public abstract class CommonAdapter<T> extends BaseAdapter {

    protected LayoutInflater mInflater;
    protected Context mContext;
    protected List<T> mDatas;

    public CommonAdapter(Context context, List<T> mDatas) {
        mInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mDatas = mDatas;
        LogUtil.d("call CommonAdapter");
    }

    @Override
    public int getCount() {
//        LogUtil.d(" mDatas.size=" + String.valueOf(mDatas.size()));
        return mDatas.size();

    }

    @Override
    public Object getItem(int position) {
//        LogUtil.d("getItem=" + String.valueOf(mDatas.get(position)));
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
//        LogUtil.d("getItemId=" + String.valueOf(position));
        return position;
    }
}
