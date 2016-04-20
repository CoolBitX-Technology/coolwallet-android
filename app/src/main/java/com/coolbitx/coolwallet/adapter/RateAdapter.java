package com.coolbitx.coolwallet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.general.AppPrefrence;
import com.snscity.egdwlib.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ShihYi on 2016/1/12.
 */
public class RateAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private boolean[] checked;
    private List<String> data = new ArrayList<>();
    private Context mContext;

    public RateAdapter(List<String> rateData, Context context) {

        this.mContext = context;
        this.data = rateData;
        mInflater = LayoutInflater.from(mContext);
        int size = data.size();
        checked = new boolean[size];
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.rate_list_item, null);
            holder = new ViewHolder();
            holder.contry = (TextView) view.findViewById(R.id.rate_list_item);
            holder.hostChk = (TextView) view.findViewById(R.id.host_list_item_chk);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.contry.setText(data.get(i));
        LogUtil.i(data.get(i)+" ;country "+AppPrefrence.getCurrentCountry(mContext));
        if (data.get(i).equals(AppPrefrence.getCurrentCountry(mContext))) {
            holder.hostChk.setText("✓");
        } else {
            holder.hostChk.setText("");
        }

        return view;
    }

    private class ViewHolder {
        TextView contry;
        TextView hostChk;
    }
}