package com.coolbitx.coolwallet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.bean.CwBtcTxs;
import com.coolbitx.coolwallet.general.PublicPun;
import com.snscity.egdwlib.utils.LogUtil;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by ShihYi on 2015/9/10.
 */
public class AddressInfoAdapter extends BaseAdapter {


    private LayoutInflater li;
    private ArrayList<CwBtcTxs> data;
    Context mContext;
    Calendar calendar;
    //設定日期格式
    SimpleDateFormat parse_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy h:mm a");

    public AddressInfoAdapter(Context context, ArrayList<CwBtcTxs> data) {
        this.data = data;
        this.li = LayoutInflater.from(context);
        this.mContext = context;
        LogUtil.e("*** AddressInfoAdapter ***");

        //系統預設語系
        Locale defLocale = Locale.getDefault(); //繁體中文
        //系統預設時區
        TimeZone defZone = TimeZone.getDefault();  //台灣標準時間
        LogUtil.e("預設語系: " + defLocale + " 預設時區： " + defZone.getID());
        calendar = Calendar.getInstance(defZone);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public CwBtcTxs getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        TextView tvDate;
        TextView tvAddress;
        TextView tvBTC;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String strDate;
        String strAddress;
        double formateDoubleBTC;
        String strDouBtc;
        final ViewHolder holder;

        if (convertView == null) {
            convertView = li.inflate(R.layout.adapter_address_info, parent, false);
            holder = new ViewHolder();
            holder.tvDate = (TextView) convertView.findViewById(R.id.tv_date);
            holder.tvAddress = (TextView) convertView.findViewById(R.id.tv_address);
            holder.tvBTC = (TextView) convertView.findViewById(R.id.tv_btc);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //建構時決定資料輸出格式
        //#字號當為小數後面為0時會自動去除
        DecimalFormat formatter = new DecimalFormat("#.########");

        CwBtcTxs txs = getItem(position);
        strDate = txs.getTxs_Date();
        strAddress = txs.getTxs_Address();
        formateDoubleBTC =(txs.getTxs_Result() * PublicPun.SATOSHI_RATE);
        strDouBtc = formatter.format(formateDoubleBTC);

        LogUtil.i("list " + position + "=" + strDate + " ; " + strAddress + " ; " + strDouBtc);

        holder.tvDate.setText(strDate);
        holder.tvAddress.setText(strAddress);
        //String.format("%.8f", douubleBTC)
        if (formateDoubleBTC >= 0) {
            holder.tvBTC.setText("+" + strDouBtc);
            holder.tvBTC.setTextColor(mContext.getResources().getColor(R.color.md_light_green_A700));
        } else if (formateDoubleBTC < 0) {
            holder.tvBTC.setText(strDouBtc);
            holder.tvBTC.setTextColor(mContext.getResources().getColor(R.color.md_red_A700));
        }

        isEnabled(position);
        return convertView;
    }

    /**
     * Author : Dora
     * ZoneConverter
     * Date : 2015/12/12
     *
     * @param strDate
     */
    private String ZoneConverter(String strDate) {
        String covertStr = null;
        try {
            Date dt = parse_sdf.parse(strDate);
            LogUtil.e("dt:" + dt);
            //傳入date,指定時間
            calendar.setTime(dt);
            //取得Date形別
            Date parseDate = calendar.getTime();
            //進行轉換
            covertStr = sdf.format(parseDate);
        } catch (Exception e) {
            LogUtil.e("日期轉換錯誤:" + e.toString());
        }
        return covertStr;
    }
}