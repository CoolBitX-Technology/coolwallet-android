package com.coolbitx.coolwallet.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.bean.Contents;
import com.coolbitx.coolwallet.bean.dbAddress;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.util.QRCodeEncoder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.snscity.egdwlib.utils.LogUtil;

import java.util.List;

/**
 * Created by Dora Chuang on 2015/8/26.
 *
 * @param -context
 * @param -ble      list
 * @param -listView
 */
public class ReceiveListViewAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    //    private ListView exvBleDevice = null;
    private Handler handler;
    //    private static OnRecvEditClickListener mOnRecvEditClickListener;
    private static OnRecvListClickListener mOnRecvListClickListener;
    private Context mContext;
    private ImageView imgQRcode;
    private List<dbAddress> AddressList;

    //    public ReceiveListViewAdapter(Context context, List<String> AddressList, ExpandableListView exvBleDevice, ImageView imgQRcode) {
    public ReceiveListViewAdapter(Context context, List<dbAddress> mAddressList, ImageView imgQRcode) {
       //android.content.Context.getSystemService(java.lang.String)' on a null
        if(context!=null){
            layoutInflater = LayoutInflater.from(context);
        }

//        this.exvBleDevice = exvBleDevice;
        this.mContext = context;
        this.imgQRcode = imgQRcode;
        this.AddressList = mAddressList;

        LogUtil.i("recv adapter in=" + mAddressList.size());
//        handler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                notifyDataSetChanged();
//                super.handleMessage(msg);
//            }
//        };
    }

    @Override
    public int getCount() {
        return AddressList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        String mAddr;
        final String mBCaddr;
        String mLabel;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.adapter_receive_item, null);
            holder = new ViewHolder();
            holder.item1 = (TextView) convertView.findViewById(R.id.receive_item_name);
            holder.item_lable = (TextView) convertView.findViewById(R.id.receive_item_label);
            holder.item_num = (TextView) convertView.findViewById(R.id.receive_item_num);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (AddressList != null && !AddressList.isEmpty()) {
            mAddr = AddressList.get(position).getAddress();
            mLabel = AddressList.get(position).getAddLabel();
            LogUtil.i("adapter mAddr:" + position + " = "+mLabel+ " ; " + mAddr);
            holder.item1.setText(mAddr);

            holder.item_lable.setText(mLabel);
            mBCaddr ="bitcoin:"+ mAddr + "?amount=0.0000";
            if(mLabel==null || mLabel.isEmpty()){
                holder.item_num.setText(String.valueOf(position+1));
            }else{
                holder.item_num.setText("");
            }
            if (AddressList.get(position).getN_tx() == 0) {
                holder.item_num.setTextColor(mContext.getResources().getColor(R.color.md_white_1000));
                holder.item1.setTextColor(mContext.getResources().getColor(R.color.md_white_1000));
                holder.item_lable.setTextColor(mContext.getResources().getColor(R.color.md_white_1000));
            }else{
                holder.item_num.setTextColor(mContext.getResources().getColor(R.color.dark_gray));
                holder.item1.setTextColor(mContext.getResources().getColor(R.color.dark_gray));
                holder.item_lable.setTextColor(mContext.getResources().getColor(R.color.dark_gray));
            }

            holder.item1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub

                    /**when ble be clicked , call interface and input parameter, BleActivity will process*/
                    mOnRecvListClickListener.onClick(v, position, mBCaddr);
                }
            });


        }
        return convertView;
    }

    public interface OnRecvListClickListener {
        /**
         * called when a view has been clicked
         *
         * @param v The view that was clicked.
         */
        void onClick(View v, int position, String mAddr);
    }

    //    public interface OnRecvEditClickListener {
//
//        void onClick(View v, int position);
//    }
//
//    public static void registerOnRecvEditClickListenerCallback(OnRecvEditClickListener cb) {
//        mOnRecvEditClickListener = cb;
//    }
//
    public static void registerOnRecvListClickListenerCallback(OnRecvListClickListener cb) {
        mOnRecvListClickListener = cb;
    }


    static class ViewHolder {
        TextView item1;
        TextView item_lable;
        TextView item_num;
    }

    public void EncoderQRcode(String qrInputText) {

        // Generate QR Code
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrInputText,
                null,
                Contents.Type.TEXT,
                BarcodeFormat.QR_CODE.toString(),
                PublicPun.FindScreenSize(mContext));
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            imgQRcode.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

}
