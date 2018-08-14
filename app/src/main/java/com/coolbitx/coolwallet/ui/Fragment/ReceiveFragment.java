package com.coolbitx.coolwallet.ui.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolbitx.coolwallet.DataBase.DatabaseHelper;
import com.coolbitx.coolwallet.R;
import com.coolbitx.coolwallet.adapter.ReceiveListViewAdapter;
import com.coolbitx.coolwallet.bean.Account;
import com.coolbitx.coolwallet.bean.Address;
import com.coolbitx.coolwallet.bean.Constant;
import com.coolbitx.coolwallet.bean.Contents;
import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.util.Base58;
import com.coolbitx.coolwallet.util.QRCodeEncoder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.snscity.egdwlib.CmdManager;
import com.snscity.egdwlib.cmd.CmdResultCallback;
import com.snscity.egdwlib.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by ShihYi on 2015/12/7.
 */
public class ReceiveFragment extends BaseFragment implements View.OnClickListener {

    private static final String DATA_NAME = "name";
    private static final String DATA_ID = "id";
    public static byte[] hdwAccountPointer;
    private static int genKid = 0;
    // -----類別變數-----
    private static boolean chinese = isChinese();
    private ListView listView;
    private ReceiveListViewAdapter adapter;
    private Button btnGenAddress;
    private ImageView imgQRcode;
    private CmdManager cmdManager;
    private List<Address> intputAddressList;
    private int trxStatus;
    private int getWalltePointer = 0x02;
    private int accountId = -1;
    private List<Account> cwAccountList = new ArrayList<>();
    private Button item_btn_edit;
    private Button item_btn_request;
    private Button item_btn_copy;
    private int mPosition = -1;
    private TextView tvQrCodeLable;
    private TextView tvQrCodeAddr;
    private ReceiveListViewAdapter.OnRecvListClickListener mOnRecvListClickListener = null;
    //    private Context mContext;
    //for Bundle
    private String value = "";
    private String title = "";
    private int id;
    private ProgressDialog mProgress;

    public static ReceiveFragment newInstance(String title, int indicatorColor, int dividerColor, int iconResId, int accountId) {
        ReceiveFragment f = new ReceiveFragment();
        f.setTitle(title);
        f.setIndicatorColor(indicatorColor);
        f.setDividerColor(dividerColor);
        f.setIconResId(iconResId);

        //pass data
        Bundle args = new Bundle();
        args.putString(DATA_NAME, title);

        //modify
        args.putInt(DATA_ID, accountId);
        f.setArguments(args);

        return f;
    }

    // -----類別方法-----
    private static boolean isChinese() {
        final Locale locale = Locale.getDefault();
        return locale.equals(Locale.CHINESE) || locale.equals(Locale.SIMPLIFIED_CHINESE) || locale.equals(Locale.TRADITIONAL_CHINESE);
    }

    private static String getString(final String[] string) {
        int index = chinese ? 1 : 0;
        if (index >= string.length) {
            index = 0;
        }
        return string[index];
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //already do it on BaseFragment
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        title = getArguments().getString(DATA_NAME);
        id = getArguments().getInt(DATA_ID);//account id
        value = ((FragMainActivity) mContext).getAccountFrag(id);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_receive_bc_address, container, false);

        LogUtil.i("Receive onCreateView");
        initView(view);
        cmdManager = new CmdManager();
        intputAddressList = new ArrayList<>();

        refresh(false);

        return view;
    }

    private void syncFromCard() {

        //infoId 1B (=00 status, 01 name, 02 accountPointer)
//        queryWallteInfo(getWalltePointer);
    }

    private void ClickFunction(String mTitle, final int position, String mMessage, View v) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View alert_view = inflater.inflate(R.layout.edit_dialog, null);//alert為另外做給alert用的layout
        final EditText mEditText = (EditText) alert_view.findViewById(R.id.etInputLabel);
        mEditText.setVisibility(View.VISIBLE);
        final TextView mDialogTitle = (TextView) alert_view.findViewById(R.id.dialog_title);
        final TextView mDialogMessage = (TextView) alert_view.findViewById(R.id.dialog_message);
        final int id = v.getId();
        mDialogMessage.setText(mMessage);
        switch (id) {
            case R.id.btn_edit_label:
                mEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                mDialogMessage.setVisibility(View.GONE);
                LogUtil.i("按edit");
                break;
            case R.id.btn_request:
                mEditText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                mDialogMessage.setVisibility(View.GONE);
                LogUtil.i("按request");
                break;
            case R.id.btn_copy:
                mEditText.setVisibility(View.GONE);
                LogUtil.i("按copy");
                ClipboardManager clipboardManager
                        = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setText(TabFragment.lisCwBtcAdd.get(position).getAddress());
                break;
        }
        //-----------產生輸入視窗--------
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        mDialogTitle.setText(mTitle);
        builder.setView(alert_view);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                switch (id) {
                    case R.id.btn_edit_label:
                        LogUtil.i("lebel str=" + mEditText.getText().toString().trim());
                        String mLable = mEditText.getText().toString().trim();
                        if (DatabaseHelper.updateAddress(mContext, TabFragment.lisCwBtcAdd.get(position).getAddress(), mLable)) {
                            TabFragment.lisCwBtcAdd.get(mPosition).setAddLabel(mLable);
                        }

                        Refresh(false, null);
                        break;
                    case R.id.btn_request:
                        String amount;
                        if (mEditText.getText().toString().isEmpty()) {
                            amount = "0.000";
                        } else {
                            amount = mEditText.getText().toString().trim();
                        }
                        String mBCaddress;
                        mBCaddress = "bitcoin:" + TabFragment.lisCwBtcAdd.get(position).getAddress()
                                + "?amount="
                                + amount;
                        TabFragment.lisCwBtcAdd.get(position).setBCAddress(mBCaddress);
                        LogUtil.i(mBCaddress);
//                        Refresh(false, null);
                        EncoderQRcode(mBCaddress);
                }

            }
        });
        builder.show();
    }

    private void initView(View view) {
        btnGenAddress = (Button) view.findViewById(R.id.btn_gen_address);
        imgQRcode = (ImageView) view.findViewById(R.id.img_qrcode);
        item_btn_edit = (Button) view.findViewById(R.id.btn_edit_label);
        item_btn_request = (Button) view.findViewById(R.id.btn_request);
        item_btn_copy = (Button) view.findViewById(R.id.btn_copy);
        tvQrCodeAddr = (TextView) view.findViewById(R.id.receive_click_label);
        tvQrCodeLable = (TextView) view.findViewById(R.id.receive_click_label_2);
        listView = (ListView) view.findViewById(R.id.lvAddress);

        item_btn_edit.setOnClickListener(this);
        item_btn_request.setOnClickListener(this);
        item_btn_copy.setOnClickListener(this);
        btnGenAddress.setOnClickListener(this);

        mProgress = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_DARK);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
    }

    public void EncoderQRcode(String qrInputText) {

        LogUtil.i("EncoderQRcode=" + qrInputText);
        // Generate QR Code
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrInputText,
                null,
                Contents.Type.TEXT,
                BarcodeFormat.QR_CODE.toString(),
                PublicPun.FindScreenSize(mContext));
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            LogUtil.i("EncoderQRcode bitmap");
            imgQRcode.setImageBitmap(bitmap);

        } catch (WriterException e) {
            LogUtil.i("EncoderQRcode error=" + e.getMessage());
            Toast.makeText(mContext, "QR code write error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        String mTitle;
        String mMessage;
        if (mPosition == -1) {
            PublicPun.toast(mContext, "No Address found!");
        } else {
            switch (id) {
                case R.id.btn_gen_address:
                    genChangeAddress(Constant.CwAddressKeyChainExternal);
                    break;

                case R.id.btn_edit_label:
                    mTitle = getString(R.string.edit_label);
                    mMessage = "";
                    //-----------取得alert Layout reference--------
                    ClickFunction(mTitle, mPosition, mMessage, v);
                    break;
                case R.id.btn_request:
                    mTitle = getString(R.string.request_payment);
                    mMessage = "";
                    //-----------取得alert Layout reference--------
                    ClickFunction(mTitle, mPosition, mMessage, v);
                    break;
                case R.id.btn_copy:
                    mTitle =  getString(R.string.copied);
                    mMessage = TabFragment.lisCwBtcAdd.get(mPosition).getAddress();
                    //-----------取得alert Layout reference--------
                    ClickFunction(mTitle, mPosition, mMessage, v);
                    break;
            }
        }
    }

    public void genChangeAddress(final int keyChainId) {
//        accountID = id - 1;
        int NotrsAddress = 0;
        for (int i = 0; i < TabFragment.lisCwBtcAdd.size(); i++) {
            if (TabFragment.lisCwBtcAdd.get(i).getN_tx() == 0) {
                NotrsAddress++;
            }
        }
        if (NotrsAddress >= 5) {
            PublicPun.showNoticeDialog(mContext, getString(R.string.unable_create_new_address), getString(R.string.maximum_unused_address));
        } else {
            mProgress.setMessage(getString(R.string.generating_new_address)+"...");
            mProgress.show();
            cmdManager.hdwGetNextAddress(keyChainId, id - 1, new CmdResultCallback() {
                @Override
                public void onSuccess(int status, byte[] outputData) {
                    if ((status + 65536) == 0x9000) {
                        if (outputData != null) {
                            Address address = new Address();
                            address.setAccountId(id);
                            address.setKeyChainId(keyChainId);

                            int length = outputData.length;
                            byte[] keyIdBytes = new byte[4];
                            if (length >= 4) {
                                for (int i = 0; i < 4; i++) {
                                    keyIdBytes[i] = outputData[i];
                                }
                            }

                            String keyStr = PublicPun.byte2HexString(keyIdBytes[0]);
                            int keyId = Integer.valueOf(keyStr, 16);
                            address.setKeyId(keyId);

                            byte[] addressBytes = new byte[25];
                            if (length >= 29) {
                                for (int i = 0; i < 25; i++) {
                                    addressBytes[i] = outputData[i + 4];
                                }
                            }

                            byte[] addrBytes = Base58.encode(addressBytes);//34b

                            String addr = new String(addrBytes, Constant.UTF8);
                            address.setAddress(addr);
                            address.setBCAddress(addr + "?amount=0");

//                        intputAddressList.add(address);
//                        PublicPun.account.setInputAddressList(intputAddressList);
                            DatabaseHelper.insertAddress(mContext, id - 1, addr, 0, keyId, 0, 0);
                            Refresh(true, address);
                            mProgress.dismiss();
                            PublicPun.account.setInputIndex(PublicPun.account.getInputIndex() + 1);
                        }
                    }
                }
            });
        }
    }

    private void Refresh(boolean isAddData, Address address) {

        if (isAddData && address != null) {
            System.out.print("add address");
            intputAddressList.add(address);
            PublicPun.account.setInputAddressList(intputAddressList);
            TabFragment.lisCwBtcAdd = DatabaseHelper.queryAddress(mContext, id - 1, 0);
        }
        intputAddressList = PublicPun.account.getInputAddressList();
        refresh(isAddData);
    }

    public void refresh(boolean isGenAddress) {

        mOnRecvListClickListener = new ReceiveListViewAdapter.OnRecvListClickListener() {
            @Override
            public void onClick(View v, int position, String mBCaddr) {

                LogUtil.i("position=" + position + " ; addr click=" + mBCaddr);
                EncoderQRcode(mBCaddr);
                item_btn_edit.setVisibility(View.VISIBLE);
                item_btn_request.setVisibility(View.VISIBLE);
                item_btn_copy.setVisibility(View.VISIBLE);
                mPosition = position;
                tvQrCodeAddr.setText(TabFragment.lisCwBtcAdd.get(position).getAddress().toString());
//                tvQrCodeLable.setText(TabFragment.lisCwBtcAdd.get(position).getAddLabel() == null ?
//                        String.valueOf(position): TabFragment.lisCwBtcAdd.get(position).getAddLabel().toString());
            }
        };
        ReceiveListViewAdapter.registerOnRecvListClickListenerCallback(mOnRecvListClickListener);

        adapter = new ReceiveListViewAdapter(mContext, TabFragment.lisCwBtcAdd, imgQRcode);
        listView.setAdapter(adapter);

        LogUtil.i("isGenAddress=" + isGenAddress + " ;" + "lisCwBtcAdd.size=" + TabFragment.lisCwBtcAdd.size());

        if (TabFragment.lisCwBtcAdd.size() > 0) {
            if (isGenAddress) {
                int clickPosition = TabFragment.lisCwBtcAdd.size() - 1;
                mOnRecvListClickListener.onClick(null, clickPosition, TabFragment.lisCwBtcAdd.get(clickPosition).getAddress() + "?amount=0.000");
                item_btn_edit.callOnClick();
            } else {
                mOnRecvListClickListener.onClick(null, 0, TabFragment.lisCwBtcAdd.get(0).getAddress() + "?amount=0.000");
            }
        }
    }

}

