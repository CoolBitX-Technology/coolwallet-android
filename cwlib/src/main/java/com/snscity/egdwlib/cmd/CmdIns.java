package com.snscity.egdwlib.cmd;

public class CmdIns {
    //SE Commands
    public static final int GET_MODE_STATE = 0x10;
    public static final int GET_FW_VERSION = 0x11;
    public static final int GET_UID = 0x12;
    public static final int GET_ERROR = 0x13;

    //Init Commands
    public static final int INIT_SET_DATA = 0xA0;
    public static final int INIT_CONFIRM = 0xA2;
    public static final int INIT_BACK_INIT = 0xA4;
    public static final int INIT_VMK_CHLNG = 0xA3;

    //Authentication Commands
    public static final int PIN_CHLNG = 0x20;
    public static final int PIN_AUTH = 0x21;
    public static final int PIN_CHANGE = 0x22;
    public static final int PIN_LOGOUT = 0x23;

    //Binding Commands
    public static final int BIND_REG_INIT = 0xD0;
    public static final int BIND_REG_CHLNG = 0xD1;
    public static final int BIND_REG_FINISH = 0xD2;
    public static final int BIND_REG_INFO = 0xD3;
    public static final int BIND_REG_APPROVE = 0xD4;
    public static final int BIND_REG_REMOVE = 0xD5;
    public static final int BIND_LOGIN_CHLNG = 0xD6;
    public static final int BIND_LOGIN = 0xD7;
    public static final int BIND_LOGOUT = 0xD8;
    public static final int BIND_FIND_HOST_ID = 0xD9;
    public static final int BIND_BACK_NO_HOST = 0xDA;
    public static final int GEN_RESET_OTP = 0x63;
    public static final int VERIFY_RESET_OTP = 0x64;

    //Perso Commands
    public static final int PERSO_SET_DATA = 0x30;
    public static final int PERSO_CONFIRM = 0x32;
    public static final int PERSO_BACK_PERSO = 0x33;

    //CW Setting Commands
    public static final int SET_CURR_RATE = 0x40;
    public static final int GET_CURR_RATE = 0x41;
    public static final int GET_CARD_NAME = 0x42;
    public static final int SET_CARD_NAME = 0x43;
    public static final int GET_PERSO = 0x44;
    public static final int SET_PERSO = 0x45;
    public static final int GET_CARD_ID = 0x46;
    public static final int TURN_CURRENCY= 0x65;
    //HD Wallet Commands
    public static final int HDW_INIT_WALLET = 0xB0;
    public static final int HDW_INIT_WALLET_GEN = 0xB1;
    public static final int HDW_QUERY_WALLET_INFO = 0xB2;
    public static final int HDW_SET_WALLET_INFO = 0xB3;
    public static final int HDW_CREATE_ACCOUNT = 0xB4;
    public static final int HDW_QUERY_ACCOUNT_INFO = 0xB5;
    public static final int HDW_SET_ACCOUNT_INFO = 0xB6;
    public static final int HDW_GET_NEXT_ADDRESS = 0xB7;
    public static final int HDW_PREP_TRX_SIGN = 0xB8;
    public static final int HDW_INIT_WALLET_GEN_CONFIRM = 0xB9;
    public static final int HDW_QUERY_ACCOUNT_KEY_INFO = 0xBA;

    //Transaction Commands
    public static final int TRX_STATUS = 0x80;
    public static final int TRX_BEGIN = 0x72;
    public static final int TRX_VERIFY_OTP = 0x73;
    public static final int TRX_VERIFY_BT = 0x7A; // 認證按鈕（虛擬命令）
    public static final int TRX_SIGN = 0x74;
    public static final int TRX_FINISH = 0x76;
    public static final int TRX_GET_ADDR = 0x79;

    //Exchange Site Commands
    public static final int XCHS_REG_STATUS = 0xF0;
    public static final int XCHS_GET_OTP = 0xF4;
    public static final int XCHS_SESSION_INIT = 0xF5;
    public static final int XCHS_SESSION_ESTAB = 0xF6;
    public static final int XCHS_SESSION_LOGOUT = 0xF7;
    public static final int XCHS_BLOCK_INFO = 0xF8;
    public static final int XCHS_BLOCK_BTC = 0xF9;
    public static final int XCHS_BLOCK_CANCEL = 0xFA;
    public static final int XCHS_TRX_SIGN_LOGIN = 0xFB;
    public static final int XCHS_TRX_SIGN_PREPARE = 0xFC;
    public static final int XCHS_TRX_SIGN_LOGOUT = 0xFD;

    //FirmwareUpload Commmands
    public static final int BACK_TO_LOADER = 0x78;
    public static final int BACK_TO_SLE97_LOADER = 0x77;

    //MCU Commands
    public static final int MCU_RESET_SE = 0x60;
    public static final int MCU_QUERY_BAT_GAGUE = 0x61;
    public static final int MCU_SET_ACCOUNT = 0x62;

}
