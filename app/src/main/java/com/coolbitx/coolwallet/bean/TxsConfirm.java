package com.coolbitx.coolwallet.bean;

/**
 * Created by ShihYi on 2016/12/22.
 */
public class TxsConfirm {

    private String output_addrese;
    private long output_amount;
    private long fees;
    private long output_total;
    private int input_count;
    private long input_amount;
    private String change_address;
    private long change_amount;
    private boolean isDust;

    public TxsConfirm(String output_addrese, long output_amount, long fees, int input_count,
                      long input_amount, String change_address, long change_amount,boolean isDust) {
        this.output_addrese = output_addrese;
        this.output_amount = output_amount;
        this.fees = fees;
        this.output_total = fees + output_amount;
        this.input_count = input_count;
        this.input_amount = input_amount;
        this.change_address = change_address;
        this.change_amount = change_amount;
        this.isDust = isDust;
    }

    public boolean isDust() {
        return isDust;
    }

    public String getOutput_addrese() {
        return output_addrese;
    }

    public long getOutput_amount() {
        return output_amount;
    }

    public long getFees() {
        return fees;
    }

    public long getOutput_total() {
        return output_total;
    }

    public int getInput_count() {
        return input_count;
    }

    public long getInput_amount() {
        return input_amount;
    }

    public String getChange_address() {
        return change_address;
    }

    public long getChange_amount() {
        return change_amount;
    }
}
