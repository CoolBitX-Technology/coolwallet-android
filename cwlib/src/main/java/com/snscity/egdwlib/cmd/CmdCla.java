package com.snscity.egdwlib.cmd;

public class CmdCla {
    private class ClaType {
        /**
         * 80: Can’t Power off SE
         * 81: Ask MCU reserve Memory Credential, Then Power off SE
         * 82: Ask MCU reserve Flash Credential, Then power off SE
         * 83: No need to reserve anything, Power off SE after command.
         */
        private static final int KEEP_POWER = 0x80;
        private static final int KEEP_MEMORY = 0x81;
        private static final int KEEP_FLASH = 0x82;
        private static final int KEEP_NONE = 0x83;
    }

    //SE Commands
    /**EGD ver.*/
//    public static final int GET_MODE_STATE = ClaType.KEEP_MEMORY;
    public static final int GET_MODE_STATE = ClaType.KEEP_MEMORY;
    public static final int GET_FW_VERSION = ClaType.KEEP_NONE;
    public static final int GET_UID = ClaType.KEEP_NONE;
    public static final int GET_ERROR = ClaType.KEEP_NONE;//?

    //Init Commands
    public static final int INIT_SET_DATA = ClaType.KEEP_POWER;
    public static final int INIT_CONFIRM = ClaType.KEEP_NONE;
    public static final int INIT_VMK_CHLNG = ClaType.KEEP_POWER;
    public static final int INIT_BACK_INIT = ClaType.KEEP_POWER;

    //Authentication Commands
    public static final int PIN_CHLNG = ClaType.KEEP_POWER;
    public static final int PIN_AUTH = ClaType.KEEP_MEMORY;
    public static final int PIN_CHANGE = ClaType.KEEP_MEMORY;
    public static final int PIN_LOGOUT = ClaType.KEEP_MEMORY;

    //Binding Commands
    public static final int BIND_REG_INIT = ClaType.KEEP_POWER; //?
    public static final int BIND_REG_CHLNG = ClaType.KEEP_POWER;
    public static final int BIND_REG_FINISH = ClaType.KEEP_MEMORY;
    public static final int BIND_REG_INFO = ClaType.KEEP_NONE; //for registered host: 83, for non-registered hot: 80
    public static final int BIND_REG_APPROVE = ClaType.KEEP_MEMORY;
    public static final int BIND_REG_REMOVE = ClaType.KEEP_MEMORY;
    public static final int BIND_LOGIN_CHLNG = ClaType.KEEP_MEMORY;  //0x80
    public static final int BIND_LOGIN = ClaType.KEEP_MEMORY;//0x80
    public static final int BIND_LOGOUT = ClaType.KEEP_MEMORY; //?CwCmdClaKeepNone
    public static final int BIND_FIND_HOST_ID = ClaType.KEEP_NONE;
    public static final int BIND_BACK_NO_HOST = ClaType.KEEP_MEMORY;

    public static final int GEN_RESET_OTP = ClaType.KEEP_POWER;
    public static final int VERIFY_RESET_OTP = ClaType.KEEP_POWER;

    //Perso Commands
    public static final int PERSO_SET_DATA = ClaType.KEEP_MEMORY;
    public static final int PERSO_CONFIRM = ClaType.KEEP_MEMORY;
    public static final int PERSO_BACK_PERSO = ClaType.KEEP_MEMORY;

    //CW Setting Commands
    public static final int SET_CURR_RATE = ClaType.KEEP_NONE;
    public static final int GET_CURR_RATE = ClaType.KEEP_NONE;
    public static final int GET_CARD_NAME = ClaType.KEEP_NONE;
    public static final int SET_CARD_NAME = ClaType.KEEP_NONE;
    public static final int GET_PERSO = ClaType.KEEP_NONE;
    public static final int SET_PERSO = ClaType.KEEP_NONE;
    public static final int GET_CARD_ID = ClaType.KEEP_NONE;
    public static final int TURN_CURRENCY = ClaType.KEEP_NONE;

    //HD Wallet Commands
    public static final int HDW_INIT_WALLET = ClaType.KEEP_MEMORY;
    public static final int HDW_INIT_WALLET_GEN = ClaType.KEEP_MEMORY;
    public static final int HDW_QUERY_WALLET_INFO = ClaType.KEEP_NONE;
    public static final int HDW_SET_WALLET_INFO = ClaType.KEEP_MEMORY;
    public static final int HDW_CREATE_ACCOUNT = ClaType.KEEP_MEMORY;
    public static final int HDW_QUERY_ACCOUNT_INFO = ClaType.KEEP_NONE;
    public static final int HDW_SET_ACCOUNT_INFO = ClaType.KEEP_MEMORY;
    public static final int HDW_GET_NEXT_ADDRESS = ClaType.KEEP_MEMORY;
    public static final int HDW_PREP_TRX_SIGN = ClaType.KEEP_MEMORY;
    public static final int HDW_INIT_WALLET_GEN_CONFIRM = ClaType.KEEP_MEMORY;
    public static final int HDW_QUERY_ACCOUNT_KEY_INFO = ClaType.KEEP_MEMORY;

    //Transaction Commands
    public static final int TRX_STATUS = ClaType.KEEP_MEMORY;
    public static final int TRX_BEGIN = ClaType.KEEP_MEMORY;
    public static final int TRX_VERIFY_OTP = ClaType.KEEP_MEMORY;
    public static final int TRX_SIGN = ClaType.KEEP_MEMORY;
    public static final int TRX_FINISH = ClaType.KEEP_MEMORY;
    public static final int TRX_GET_ADDR = ClaType.KEEP_MEMORY; //?

    //Exchange Site Commands
    public static final int EX_REG_STATUS = ClaType.KEEP_MEMORY;
    public static final int EX_GET_OTP = ClaType.KEEP_MEMORY;
    public static final int EX_SESSION_INIT = ClaType.KEEP_FLASH; //?
    public static final int EX_SESSION_ESTAB = ClaType.KEEP_FLASH; //?
    public static final int EX_SESSION_LOGOUT = ClaType.KEEP_FLASH; //?
    public static final int EX_BLOCK_INFO = ClaType.KEEP_FLASH; //?
    public static final int EX_BLOCK_BTC = ClaType.KEEP_FLASH; //?
    public static final int EX_BLOCK_CANCEL = ClaType.KEEP_FLASH; //?
    public static final int EX_TRX_SIGN_LOGIN = ClaType.KEEP_FLASH; //?
    public static final int EX_TRX_SIGN_PREPARE = ClaType.KEEP_FLASH; //?
    public static final int EX_TRX_SIGN_LOGOUT = ClaType.KEEP_FLASH; //?

    //FirmwareUpload Commmands
    public static final int BACK_TO_LOADER = ClaType.KEEP_NONE; //?

    public static final int BACK_TO_SLE97_LOADER = ClaType.KEEP_NONE; //?

    //MCU Commands
    public static final int MCU_RESET_SE = ClaType.KEEP_NONE; //?
    public static final int MCU_QUERY_BAT_GAGUE = ClaType.KEEP_NONE; //?
    public static final int MCU_SET_ACCOUNT = ClaType.KEEP_NONE;


}
