package com.coolbitx.coolwallet.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.bean.ExchangeOrder;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by ShihYi on 2016/6/29.
 */
public class PendingOrderAdapter<T> extends CommonAdapter<T> {

    public PendingOrderAdapter(Context context, List<T> mDatas) {
        super(context, mDatas);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = ViewHolder.get(mContext, convertView, parent,
                R.layout.adapter_grid_sell_detail, position);

        ExchangeOrder exchangeOrder = (ExchangeOrder) mDatas.get(position);

//        LogUtil.d(String.valueOf(exchangeOrder.getAmount())+";"+String.valueOf(exchangeOrder.getPrice())+";"+exchangeOrder.getExpiration());

        TextView mAmount = viewHolder.getView(R.id.tv_grid_sell_amount);
//        mAmount.setText(String.valueOf(exchangeOrder.getAmount()));
        mAmount.setText(new DecimalFormat("#.########").format(exchangeOrder.getAmount()));

        TextView mPrice = viewHolder.getView(R.id.tv_grid_sell_price);
        mPrice.setText(String.valueOf(exchangeOrder.getPrice()));

        TextView mExpiration = viewHolder.getView(R.id.tv_grid_sell_expiration);
        mExpiration.setText(exchangeOrder.getExpiration());

        return viewHolder.getConvertView();
    }
}
