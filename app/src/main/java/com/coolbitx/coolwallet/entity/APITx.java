package com.coolbitx.coolwallet.entity;

/**
 * Created by ShihYi on 2015/12/30.
 */
import java.util.ArrayList;

public class APITx {

    public  static class Txin {

        String tx;
        int index;
        int scriptPubLen;
        int scriptSigLen;
        byte[]  scriptPub;
        byte[] scriptSig;


        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public int getScriptPubLen() {
            return scriptPubLen;
        }

        public void setScriptPubLen(int scriptPubLen) {
            this.scriptPubLen = scriptPubLen;
        }

        public int getScriptSigLen() {
            return scriptSigLen;
        }

        public void setScriptSigLen(int scriptSigLen) {
            this.scriptSigLen = scriptSigLen;
        }


        public byte[] getScriptSig() {
            return scriptSig;
        }

        public void setScriptSig(byte[] scriptSig) {
            this.scriptSig = scriptSig;
        }

        public String getTx() {
            return tx;
        }

        public void setTx(String tx) {
            this.tx = tx;
        }

        public byte[] getScriptPub() {
            return scriptPub;
        }

        public void setScriptPub(byte[] scriptPub) {
            this.scriptPub = scriptPub;
        }
    }


    public static class Txout {
        String add;
        long value;

        public String getAdd() {
            return add;
        }

        public void setAdd(String add) {
            this.add = add;
        }

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }

    public static class Tx {
        int version;
        int txinCnt;
        int txoutCnt;
        ArrayList<Txin> txinList = new ArrayList<Txin>();
        ArrayList<Txout> txoutList = new ArrayList<Txout>();
        int lock;

        public ArrayList<Txin> getTxinList() {
            return txinList;
        }

        public void setTxinList(ArrayList<Txin> txinList) {
            this.txinList = txinList;
        }

        public ArrayList<Txout> getTxoutList() {
            return txoutList;
        }

        public void setTxoutList(ArrayList<Txout> txoutList) {
            this.txoutList = txoutList;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public int getTxinCnt() {
            return txinCnt;
        }

        public void setTxinCnt(int txinCnt) {
            this.txinCnt = txinCnt;
        }

        public int getTxoutCnt() {
            return txoutCnt;
        }

        public void setTxoutCnt(int txoutCnt) {
            this.txoutCnt = txoutCnt;
        }

        public int getLock() {
            return lock;
        }

        public void setLock(int lock) {
            this.lock = lock;
        }
    }


}