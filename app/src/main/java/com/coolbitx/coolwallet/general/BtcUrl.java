package com.coolbitx.coolwallet.general;

/**
 * Created by ShihYi on 2016/2/3.
 */
public class BtcUrl {

//https://blockchain.info/tx/6fce53ced4045b0aed05e7e691a7e52ac37ae960af73f0ae98b552d597e9de10?show_adv=false&format=json

//  Exchange Rates API : https://blockchain.info/ticker


    public static final String URL_BLOCKCHAIN_RAW_ADDRESS ="https://blockchain.info/rawaddr/";
    public static final String URL_BLOCKCHAIN_SERVER_SITE = "https://blockchain.info/";
    public static final String URL_BLOCKCHAIN_UNSPENT = "unspent?active=";
    public static final String URL_BLICKCHAIN_TXS_MULTIADDR ="multiaddr?active=";

//    public static final String URL_BLICKCHAIN_PUSH ="pushtx";
public static final String URL_BLICKCHAIN_PUSH ="pushtx?cors=true";
    public static final String URL_BLICKCHAIN_DECODE ="decode-tx";





    public static final String URL_BLOCKR_SERVER_SITE = "http://btc.blockr.io/api/v1/";
    public static final String URL_BLOCKR_UNSPENT = "address/unspent/";
//    public static final String URL_BLOCKR_UNCONFIRMED = "unconfirmed/address";
//    public static final String URL_BLOCKR_INFO = "info/address";
//    public static final String URL_BLOCKR_EXCHANGE_RATE = "exchangerate/current";
    public static final String URL_BLOCKCHAIN_EXCHANGE_RATE = "ticker";

    public static final String URL_BLOCKR_DECODE ="tx/decode";
    public static final String URL_BLOCKR_PUSH ="tx/push";

    public static final String RECOMMENDED_TRANSACTION_FEES = "https://bitcoinfees.21.co/api/v1/fees/recommended";

    public static final String SOCKET_BLOCK_IO="wss://n.block.io:443/socket";

    public static final String CW_XHCS_SITE="http://xsm.coolbitx.com:8080/logout";



}
