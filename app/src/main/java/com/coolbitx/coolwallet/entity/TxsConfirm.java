package com.coolbitx.coolwallet.entity;

/**
 * Created by ShihYi on 2016/12/22.
 */
public class TxsConfirm {

    private String output_addrese;
    private long output_amount;
    private long fees;
    private long ouput_total;
    private int input_count;
    private long input_amount;
    private String change_address;
    private long change_amount;

    public TxsConfirm(String output_addrese, long output_amount, long fees, int input_count, long input_amount, String change_address, long change_amount) {
        this.output_addrese = output_addrese;
        this.output_amount = output_amount;
        this.fees = fees;
        this.ouput_total = fees + output_amount;
        this.input_count = input_count;
        this.input_amount = input_amount;
        this.change_address = change_address;
        this.change_amount = change_amount;
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

    public long getOuput_total() {
        return ouput_total;
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
