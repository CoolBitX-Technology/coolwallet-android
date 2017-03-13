package com.coolbitx.coolwallet.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.entity.ExchangeOrder;
import com.snscity.egdwlib.utils.LogUtil;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by ShihYi on 2016/6/29.
 */
public class UnclarifyOrderAdapter<T> extends CommonAdapter<T> {

    public UnclarifyOrderAdapter(Context context, List<T> mDatas) {
        super(context, mDatas);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = ViewHolder.get(mContext, convertView, parent,
                R.layout.adapter_grid_unclarify_detail, position);

        ExchangeOrder exchangeOrder = (ExchangeOrder) mDatas.get(position);


        LogUtil.d(String.valueOf(exchangeOrder.getOrderId())+";"+exchangeOrder.getAmount()+";"+String.valueOf(exchangeOrder.getPrice()));

        TextView mOrderId = viewHolder.getView(R.id.tv_grid_orderId);
        mOrderId.setText(String.valueOf(exchangeOrder.getOrderId()));

        TextView mAmount = viewHolder.getView(R.id.tv_grid_amount);
        mAmount.setText(new DecimalFormat("#.########").format(exchangeOrder.getAmount()));


        TextView mPrice = viewHolder.getView(R.id.tv_grid_price);
        mPrice.setText(String.valueOf(exchangeOrder.getPrice()));

        return viewHolder.getConvertView();
    }
}
