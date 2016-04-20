package com.coolbitx.coolwallet.entity;

/**
 * Created by ShihYi on 2016/1/28.
 */
public class SymbolLocal
{
    public String symbol ;
    public String code ;
    public boolean symbolAppearsAfter ;
    public String name ;
    public boolean local ;
    public double conversion ;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isSymbolAppearsAfter() {
        return symbolAppearsAfter;
    }

    public void setSymbolAppearsAfter(boolean symbolAppearsAfter) {
        this.symbolAppearsAfter = symbolAppearsAfter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public double getConversion() {
        return conversion;
    }

    public void setConversion(double conversion) {
        this.conversion = conversion;
    }
}
