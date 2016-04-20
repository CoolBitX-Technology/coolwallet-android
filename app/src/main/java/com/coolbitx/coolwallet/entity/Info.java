package com.coolbitx.coolwallet.entity;

/**
 * Created by ShihYi on 2016/1/29.
 */
public class Info {
    public double conversion;
    public int nconnected;
    public SymbolLocal symbol_local;
    public SymbolBtc symbol_btc;
    public LatestBlock latest_block;

    public double getConversion() {
        return conversion;
    }

    public void setConversion(double conversion) {
        this.conversion = conversion;
    }

    public int getNconnected() {
        return nconnected;
    }

    public void setNconnected(int nconnected) {
        this.nconnected = nconnected;
    }

    public SymbolLocal getSymbol_local() {
        return symbol_local;
    }

    public void setSymbol_local(SymbolLocal symbol_local) {
        this.symbol_local = symbol_local;
    }

    public SymbolBtc getSymbol_btc() {
        return symbol_btc;
    }

    public void setSymbol_btc(SymbolBtc symbol_btc) {
        this.symbol_btc = symbol_btc;
    }

    public LatestBlock getLatest_block() {
        return latest_block;
    }

    public void setLatest_block(LatestBlock latest_block) {
        this.latest_block = latest_block;
    }
}
