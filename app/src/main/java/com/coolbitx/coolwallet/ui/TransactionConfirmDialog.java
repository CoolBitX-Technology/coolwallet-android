package com.coolbitx.coolwallet.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.callback.TransactionConfirmCallback;
import com.coolbitx.coolwallet.bean.TxsConfirm;
import com.coolbitx.coolwallet.general.AppPrefrence;
import com.coolbitx.coolwallet.general.PublicPun;
import com.snscity.egdwlib.utils.LogUtil;

import java.text.DecimalFormat;

/**
 * Created by ShihYi on 2016/12/20.
 */
public class TransactionConfirmDialog extends AlertDialog implements View.OnClickListener {

    private TransactionConfirmCallback mConfirmListener;
    //use the parent context
    private Button btnConfrim, btnCancel;  //確定取消按鈕
    private TextView tvAddress, tvAddressLower, tvSendAmount, tvSendForeignAmount, tvFeesAmount, tvFeesForeignAmount,
            tvTotalAmount, tvTotalForeignAmount, tvInputStr, tvInputAmount, tvChangeAddr, tvChangeAmount,tv_dust;//
    private TxsConfirm mTxsConfirm;
    private Context mContext;
    private AppCompatActivity activity;
    private ImageView imgAlert;
    private TextView tvFeeAlert;
    private boolean isClick = true;
    public TransactionConfirmDialog(Context context, TxsConfirm mTxsConfirm, TransactionConfirmCallback mConfirmListener) {
        super(context, android.R.style.Theme);
        setOwnerActivity((Activity) context);

        this.mContext = context;
        this.mConfirmListener = mConfirmListener;
        this.mTxsConfirm = mTxsConfirm;
        this.activity = (AppCompatActivity) context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tx_confirm_dialog_layout);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setDialogView();
        findViews();
        initToolbar();
        DiasplayValue();


        if(!AppPrefrence.getAutoFeeCheckBox(mContext)){
            if (mTxsConfirm.getFees() < AppPrefrence.getRecommendedDefaultFee(mContext)) {
                imgAlert.setVisibility(View.VISIBLE);
            } else {
                imgAlert.setVisibility(View.GONE);
            }
        }
    }

    private void setDialogView() {
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        WindowManager.LayoutParams lay = getWindow().getAttributes();
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        Rect rect = new Rect();
        View view = getWindow().getDecorView();//decorView是window中的最顶层view，可以从window中获取到decorView
        view.getWindowVisibleDisplayFrame(rect);
        lay.height = dm.heightPixels - rect.top;
        lay.width = dm.widthPixels;
        this.getWindow().setAttributes(lay);
    }

    private void DiasplayValue() {

        float UseExchangeRate = AppPrefrence.getCurrentRate(mContext);

        tvAddress.setText(mTxsConfirm.getOutput_addrese());
        tvAddressLower.setText(mTxsConfirm.getOutput_addrese());
        tvSendAmount.setText("฿" + String.valueOf(new DecimalFormat("#.########").format(
                mTxsConfirm.getOutput_amount() * PublicPun.SATOSHI_RATE)));
        tvSendForeignAmount.setText("$" + String.valueOf(new DecimalFormat("#.##").format(
                mTxsConfirm.getOutput_amount() * PublicPun.SATOSHI_RATE * UseExchangeRate)));
        tvFeesAmount.setText("฿" + String.valueOf(new DecimalFormat("#.########").format(
                mTxsConfirm.getFees() * PublicPun.SATOSHI_RATE)));
        tvFeesForeignAmount.setText("$" + String.valueOf(new DecimalFormat("#.##").format(
                mTxsConfirm.getFees() * PublicPun.SATOSHI_RATE * UseExchangeRate)));
        tvTotalAmount.setText("฿" + String.valueOf(new DecimalFormat("#.########").format(
                mTxsConfirm.getOutput_total() * PublicPun.SATOSHI_RATE)));
        tvTotalForeignAmount.setText("$" + String.valueOf(new DecimalFormat("#.##").format(
                mTxsConfirm.getOutput_total() * PublicPun.SATOSHI_RATE * UseExchangeRate)));
        tvInputStr.setText(String.valueOf(mTxsConfirm.getInput_count()) + " Inputs");
        tvInputAmount.setText("฿" + String.valueOf(new DecimalFormat("#.########").format(
                mTxsConfirm.getInput_amount() * PublicPun.SATOSHI_RATE)));
        tvChangeAddr.setText(mTxsConfirm.getChange_address());
        tvChangeAmount.setText("฿" + String.valueOf(new DecimalFormat("#.########").format(
                mTxsConfirm.getChange_amount() * PublicPun.SATOSHI_RATE)));

        if(mTxsConfirm.isDust()){
            tv_dust.setVisibility(View.VISIBLE);
            tv_dust.setText( String.format(mContext.getString(R.string.note_dust),
                    mTxsConfirm.getChange_amount() * PublicPun.SATOSHI_RATE));
        }else{
            tv_dust.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id) {
            case R.id.btn_confirm:
                //confirm to send
                if (mConfirmListener != null) {
                    mConfirmListener.TransactionConfirm(mTxsConfirm.getOutput_addrese(),
                            mTxsConfirm.getChange_address(),mTxsConfirm.getOutput_amount());
                }
                dismiss();
                break;
            case R.id.btn_cancel:
                //transaction cancel
                mConfirmListener.TransactionCancel();
                dismiss();
                break;
            case R.id.img_alert:
                LogUtil.e("click alert!");
                if(isClick){
                    tvFeeAlert.setVisibility(View.VISIBLE);
                }else{
                    tvFeeAlert.setVisibility(View.GONE);
                }
                isClick=!isClick;

                break;
        }
    }



    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        toolbar.setLogo(mContext.getResources().getDrawable(R.mipmap.logo2));
        activity.setSupportActionBar(toolbar);
//        toolbar.setNavigationIcon(R.mipmap.menu_3x);




        // Navigation Icon設定在 setSupoortActionBar後才有作用,否則會出現 back button_up
//        ActionBar actionBar = activity.getSupportActionBar();
        // 打開  "<"
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setHomeButtonEnabled(true);
    }

    private void findViews() {
        tvAddress = (TextView) findViewById(R.id.tvAddress);
        tvAddressLower = (TextView) findViewById(R.id.tvAddressLower);
        tvSendAmount = (TextView) findViewById(R.id.tvSendAmount);
        tvSendForeignAmount = (TextView) findViewById(R.id.tvSendForeignAmount);
        tvFeesAmount = (TextView) findViewById(R.id.tvFeesAmount);
        tvFeesForeignAmount = (TextView) findViewById(R.id.tvFeesForeignAmount);
        tvTotalAmount = (TextView) findViewById(R.id.tvTotalAmount);
        tvTotalForeignAmount = (TextView) findViewById(R.id.tvTotalForeignAmount);
        tvInputStr = (TextView) findViewById(R.id.tvInputStr);
        tvInputAmount = (TextView) findViewById(R.id.tvInputAmount);
        tvChangeAddr = (TextView) findViewById(R.id.tvChangeAddr);
        tvChangeAmount = (TextView) findViewById(R.id.tvChangeAmount);
        btnConfrim = (Button) findViewById(R.id.btn_confirm);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        imgAlert = (ImageView) findViewById(R.id.img_alert);
        tvFeeAlert = (TextView)findViewById(R.id.notice_fee_alert);
        tv_dust = (TextView)findViewById(R.id.tv_dust);
        btnConfrim.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        imgAlert.setOnClickListener(this);
    }
}
