package com.coolbitx.coolwallet.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.entity.MyDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dora Chuang on 2015/8/26.
 *
 * @param -context
 * @param -ble      list
 * @param -listView
 */
public class ListViewAdapter extends BaseExpandableListAdapter {

    private LayoutInflater layoutInflater;
    private List<MyDevice> AddressList = new ArrayList<>();
    private String TAG = "coolwallet";
    private int lastExpandedGroupPosition;
    private ExpandableListView exvBleDevice = null;
    private Handler handler;
    private static OnBleConnClickListener mOnBleConnClickListener;
    private static OnCWResetClickListener mOnCWResetClickListener;

    public ListViewAdapter(Context context, List<MyDevice> AddressList, ExpandableListView exvBleDevice) {
        layoutInflater = LayoutInflater.from(context);
        this.AddressList = AddressList;
        this.exvBleDevice = exvBleDevice;

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                notifyDataSetChanged();
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public int getGroupCount() {
        return AddressList.size();
    }

    //notifyDataSetChanged
    public void refresh() {
        handler.sendMessage(new Message());
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1; //only button_up obj.
    }

    @Override
    public Object getGroup(int groupPosition) {
        return AddressList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    /* when listView Expanded triggered */
    @Override
    public void onGroupExpanded(int groupPosition) {
        //collapse the old expanded group, if not the same
        //as new group to expand
        if (groupPosition != lastExpandedGroupPosition) {
            exvBleDevice.collapseGroup(lastExpandedGroupPosition);
        }
        super.onGroupExpanded(groupPosition);
        lastExpandedGroupPosition = groupPosition;
    }

    /* 標題View */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.cwlist_item, parent, false);
            holder = new ViewHolder();
            holder.item1 = (TextView) convertView.findViewById(R.id.cwlist_item_name);
//            holder.item2 = (TextView) convertView.findViewById(R.id.cwlist_item_address);
//            holder.item3 = (TextView) convertView.findViewById(R.id.cwlist_item_rssi);
            convertView.setTag(holder);
//            Log.i(TAG, "groupPosition:" + groupPosition + "/lastExpandedGroupPosition:" + lastExpandedGroupPosition);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
//        exvBleDevice.expandGroup(lastExpandedGroupPosition);
        if (AddressList != null && !AddressList.isEmpty()) {
            MyDevice device = AddressList.get(groupPosition);
            holder.item1.setText(device.getName());
//            holder.item2.setText(device.getAddress());
//            holder.item3.setText(device.getRssi());
//            Log.e(TAG, "adapter_address=" + device.getAddress());
        }

        return convertView;
    }

    /* 內容View */
    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder holder;
        final int mchildPosition = childPosition;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.cwlist_item_child, parent, false);
            holder = new ViewHolder();
            holder.item_btn_connect = (Button) convertView.findViewById(R.id.btn_connect);
            holder.item_btn_reset = (Button) convertView.findViewById(R.id.btn_reset);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.item_btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                /**when ble be clicked , call interface and input parameter, BleActivity will process*/
                mOnBleConnClickListener.onClick(v, groupPosition);
            }
        });

        holder.item_btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //Intent i=new Intent(context,Assign_Task.class);
                //StartElementListener()
                Log.e(TAG, "RESET!!!");
                mOnCWResetClickListener.onClick(v, groupPosition);
            }
        });

        return convertView;
    }

    public interface OnBleConnClickListener {
        /**
         * called when a view has been clicked
         *
         * @param v The view that was clicked.
         */
        void onClick(View v, int position);
    }

    public static void registerOnBleConnClickListenerCallback(OnBleConnClickListener cb) {
        mOnBleConnClickListener = cb;
    }

    public interface OnCWResetClickListener {
        /**
         * called when a view has been clicked
         *
         * @param v The view that was clicked.
         */
        void onClick(View v, int position);
    }

    public static void registerOnCWResetClickListenerCallback(OnCWResetClickListener cb) {
        mOnCWResetClickListener = cb;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    static class ViewHolder {
        TextView item1;
        Button item_btn_connect;
        Button item_btn_reset;
    }
}
