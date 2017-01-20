package com.coolbitx.coolwallet.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.general.AppPrefrence;
import com.coolbitx.coolwallet.general.PublicPun;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;

/**
 * Created by ShihYi on 2015/12/25.
 */
public class CoolWalletCardActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private CmdManager cmdManager;
    private Context mContext;
    private EditText edtCardName;
    private Button btnUpdateCardName;
    private Switch swtTurCurrency;
    private Boolean isturnCurrency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cool_wallet_card);
        mContext = this;
        isturnCurrency = AppPrefrence.getCurrency(mContext);

        initViews();
        initValues();
        initToolbar();
        cmdManager = new CmdManager();
    }

    private void initViews() {
        edtCardName = (EditText) findViewById(R.id.edt_card_name);
        btnUpdateCardName = (Button) findViewById(R.id.btn_card_update);
        swtTurCurrency = (Switch) findViewById(R.id.switch_currency);

        btnUpdateCardName.setOnClickListener(this);
        swtTurCurrency.setOnCheckedChangeListener(this);
    }

    private void initValues() {

        edtCardName.setText(AppPrefrence.getCardName(mContext));
        swtTurCurrency.setChecked(AppPrefrence.getCurrency(mContext));
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(getResources().getDrawable(R.mipmap.cw_card));
        toolbar.setTitle("CoolWallet card");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        // 打開 up button_up
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

    }

    @Override
    public void onClick(View v) {
        if (v == btnUpdateCardName) {

            final String cardNameStr = edtCardName.getText().toString().trim();
            if (!TextUtils.isEmpty(cardNameStr)) {
                cmdManager.setCardName(cardNameStr, new CmdResultCallback() {
                    @Override
                    public void onSuccess(int status, byte[] outputData) {
                        if ((status + 65536) == 0x9000) {
                            PublicPun.card.setCardName(cardNameStr);
                            AppPrefrence.saveCardName(mContext, cardNameStr);
                            PublicPun.showNoticeDialog(mContext, "Updated!", "");

                            cmdManager.turnCurrency(isturnCurrency, new CmdResultCallback() {
                                @Override
                                public void onSuccess(int status, byte[] outputData) {
                                    if ((status + 65536) == 0x9000) {
                                        AppPrefrence.saveCurrency(mContext, isturnCurrency);
//                                        PublicPun.toast(mContext, "fiat value updated!");
                                        finish();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == swtTurCurrency) {
            isturnCurrency = isChecked;
        }
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        finish();
        return null;
    }
}
