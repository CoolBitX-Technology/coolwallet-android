package com.coolbitx.coolwallet.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.general.AppPrefrence;
import com.coolbitx.coolwallet.general.PublicPun;
import com.snscity.egdwlib.utils.LogUtil;

import java.text.DecimalFormat;
import java.text.ParseException;

import static com.coolbitx.coolwallet.general.PublicPun.SATOSHI_RATE;

/**
 * Created by ShihYi on 2016/2/2.
 */
public class TransactionFeeActivity extends BaseActivity implements CheckBox.OnCheckedChangeListener {//SeekBar.OnSeekBarChangeListener,

    private Context context;
    //    private SeekBar seekBarFee;
//    private TextView tv_recommendedFeeLength;
    private TextView tv_recommendedFeeDesc;
    private CheckBox chkAutoFee;
    private LinearLayout autoFeeLayout;
    private EditText edtFee;
    private TextView autoFeeEstimated;
    double estimatedFeeBTC;
    private Boolean isExit = false;

    @Nullable
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_fee);

        context = this;
        initView();
        initToolbar();

    }

    @Override
    protected void onResume() {

        super.onResume();
        //註冊監聽
        registerBroadcast(this, cmdManager);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unRegisterBroadcast(this);
    }


    private void initView() {

//        seekBarFee = (SeekBar) findViewById(R.id.see);
//        tv_recommendedFeeLength = (TextView) findViewById(R.id.tv_recommended_fee_length);
//        tv_recommendedFeeDesc = (TextView) findViewById(R.id.tv_recommended_fee_desc);
//        seekBarFee.setOnSeekBarChangeListener(this);

        chkAutoFee = (CheckBox) findViewById(R.id.ChkAutoFee);
        autoFeeLayout = (LinearLayout) findViewById(R.id.autoFeeLayout);
        edtFee = (EditText) findViewById(R.id.et_manual_fee);
        chkAutoFee.setOnCheckedChangeListener(this);
        autoFeeEstimated = (TextView) findViewById(R.id.autoFeeEstimated);

        edtFee.setText(String.valueOf(new DecimalFormat("#.########").format(AppPrefrence.getManualFee(this))));

        int recommendedFee = AppPrefrence.getRecommendedFastestFee(context);
        int estimatedFeeSatoshi = recommendedFee * 226;//median transaction bytes
        estimatedFeeBTC = estimatedFeeSatoshi * SATOSHI_RATE;
        String estimatedText =
                String.format(getResources().getString(R.string.str_estimated_fee_content),
                        recommendedFee, estimatedFeeSatoshi, estimatedFeeBTC);

        autoFeeEstimated.setText(estimatedText);

        chkAutoFee.setChecked(AppPrefrence.getAutoFeeCheckBox(this));

        if (chkAutoFee.isChecked()) {
            autoFeeEstimated.setVisibility(View.VISIBLE);
            edtFee.setBackgroundResource(R.drawable.edit_format_unabled);
            edtFee.setEnabled(false);
        } else {
            autoFeeEstimated.setVisibility(View.INVISIBLE);
            edtFee.setBackgroundResource(R.drawable.edit_format);
            edtFee.setEnabled(true);
        }
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Transaction Fee");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.menu_3x);
        ActionBar actionBar = getSupportActionBar();
        // 打開 up button_up
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        checkFees();
        return null;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            autoFeeEstimated.setVisibility(View.VISIBLE);
            edtFee.setBackgroundResource(R.drawable.edit_format_unabled);
            edtFee.setEnabled(false);
        } else {
            autoFeeEstimated.setVisibility(View.INVISIBLE);
            edtFee.setBackgroundResource(R.drawable.edit_format);
            edtFee.setEnabled(true);
        }
        AppPrefrence.saveAutoFeeCheckBox(TransactionFeeActivity.this, isChecked);
    }

    private void checkFees() {
        if (!chkAutoFee.isChecked()) {
            if (edtFee.getText().toString().isEmpty()) {
                PublicPun.showNoticeDialog(context, "Notification", "Please enter the transaction fee.");
                return;
            } else {
//                if (Float.valueOf(edtFee.getText().toString()) < estimatedFeeBTC) {
                try {
                    if (PublicPun.parseStringToFloatInternational(edtFee.getText().toString()) < estimatedFeeBTC) {
                        showNoticeDialogToFinish(context, "Notification", "Your fee is below average and may take longer than 30 minutes to confirm.");
                    }

                    if (PublicPun.parseStringToFloatInternational(edtFee.getText().toString()) > 1) {
                        if (Float.valueOf(edtFee.getText().toString()) > 1) {
                            showNoticeDialogToFinish(context, "Notification", "Your fee is higher than 1 BTC.");
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    showNoticeDialogToFinish(context, "Notification", "Fees format is wrong.");
                }
            }
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        AppPrefrence.saveManual(context, Float.valueOf(edtFee.getText().toString()));

        LogUtil.e("Exit: manual fee=" + edtFee.getText().toString()
                + ";" + String.valueOf(new DecimalFormat("#.########").format(AppPrefrence.getManualFee(context))));
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 判斷是否按下Back
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 是否要退出
            if (isExit == false) {
                isExit = true; //記錄下一次要退出
                checkFees();
            } else {
                finish(); // 離開程式
            }
        }
        return false;
    }

    private void showNoticeDialogToFinish(final Context mContext, String mTitle, String mMessage) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View alert_view = inflater.inflate(R.layout.edit_dialog, null);//alert為另外做給alert用的layout
        final EditText mEditText = (EditText) alert_view.findViewById(R.id.etInputLabel);
        final TextView mDialogTitle = (TextView) alert_view.findViewById(R.id.dialog_title);
        final TextView mDialogMessage = (TextView) alert_view.findViewById(R.id.dialog_message);
//        mEditText.setVisibility(View.INVISIBLE);
        mDialogTitle.setText(mTitle);
        mDialogMessage.setText(mMessage);
        //-----------產生輸入視窗--------ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT
        new AlertDialog.Builder(mContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                .setCancelable(false)
                .setView(alert_view)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish(); // 離開程式
//                System.exit(0);
                    }
                })
                .show();
    }

    //    @Override
//    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//        if (progress > newProgress + 10 || progress < newProgress - 10) {
//            newProgress = lastProgress;
//            seekBarFee.setProgress(lastProgress);
//            return;
//        }
//        newProgress = progress;
//    }

//    @Override
//    public void onStartTrackingTouch(SeekBar seekBar) {
//
//    }
//
//    @Override
//    public void onStopTrackingTouch(SeekBar seekBar) {
//
//        int seekProgress = seekBarFee.getProgress();
//        if (seekProgress < 30) {
//            seekBarFee.setProgress(0);
//            tv_recommendedFeeLength.setText(String.valueOf(AppPrefrence.getRecommendedHourFee(this)));
//            tv_recommendedFeeDesc.setText("hourFee");
//
//        } else if (seekProgress > 70) {
//
//            seekBarFee.setProgress(100);
//            tv_recommendedFeeLength.setText(String.valueOf(AppPrefrence.getRecommendedFastestFee(this)));
//            tv_recommendedFeeDesc.setText("fastestFee");
//
//        } else {
//
//            seekBarFee.setProgress(50);
//            tv_recommendedFeeLength.setText(String.valueOf(AppPrefrence.getRecommendedHalfHourFees(this)));
//            tv_recommendedFeeDesc.setText("halfHourFee");
//        }
//        AppPrefrence.saveRecommendedDefaultFee(this, Integer.valueOf(tv_recommendedFeeLength.getText().toString()));
//        LogUtil.d("停止拖拉進度條:" + seekProgress + ";" + tv_recommendedFeeLength.getText().toString());
//    }
//

}
