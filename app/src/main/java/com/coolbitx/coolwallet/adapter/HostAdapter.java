package com.coolbitx.coolwallet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.bean.Host;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ShihYi on 2016/1/12.
 */
public class HostAdapter   extends BaseAdapter {

    private LayoutInflater mInflater;
    private boolean [] checked;
    private List<Host> data = new ArrayList<>();
    private Context mContext;

    public HostAdapter( List<Host> hostData , Context context) {

        this.mContext = context;
        this.data = hostData;
        mInflater = LayoutInflater.from(mContext);
        int size = data.size();
        checked = new boolean[size];

        for (int i = 0; i < size; i++) {
            byte bindState = data.get(i).getBindStatus();
            if(bindState == 0x02){
                checked[i] = true;
            }
        }
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
        if(view == null){
            view = mInflater.inflate(R.layout.host_list_item, null);
            holder = new ViewHolder();
//            holder.id = (TextView) view.findViewById(R.id.host_list_item_id);
            holder.desc = (TextView) view.findViewById(R.id.host_list_item_desc);
            holder.status = (TextView) view.findViewById(R.id.host_list_item_status);
//            holder.rb = (RadioButton) view.findViewById(R.id.host_list_item_rb);
            holder.hostChk= (TextView) view.findViewById(R.id.host_list_item_chk);
            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }

        Host bean = data.get(i);
        if(bean != null){
//            holder.id.setText(String.valueOf(bean.getId()));
            holder.desc.setText(bean.getDesc());
            holder.status.setText(switchStatus(bean.getBindStatus()));
        }

        if(checked[i]){
//            holder.rb.setChecked(true);
            holder.hostChk.setText("✓");
        }else {
//            holder.rb.setChecked(false);
            holder.hostChk.setText("");
        }



        return view;
    }

    private String switchStatus(byte bindStatus){
        String status = "";
        switch (bindStatus){
            case 0x00:
                status = "Empty";
                break;

            case 0x01:
                status = "Registered";
                break;

            case 0x02:
                status = "Confirmed";
                break;
        }

        return status;
    }

    private class ViewHolder{
//        TextView id;
        TextView desc;
        TextView status;
//        RadioButton rb;
        TextView hostChk;
    }
}