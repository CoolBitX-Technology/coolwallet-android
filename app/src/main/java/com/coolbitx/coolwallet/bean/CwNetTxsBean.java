package com.coolbitx.coolwallet.bean;


import com.google.gson.annotations.SerializedName;

/**
 * Created by ShihYi on 2016/1/28.
 */
public class CwNetTxsBean {
    @SerializedName("recommend_include_fee")
    public boolean recommend_include_fee;

    @SerializedName("sharedcoin_endpoint")
    public String sharedcoin_endpoint;

    @SerializedName("info")
    public Info info;

    @SerializedName("wallet")
    public Wallet wallet;

    @SerializedName("addresses")
    public Addresses[] addresses;

    @SerializedName("txs")
    public Txs[] txs;

    public boolean isRecommend_include_fee() {
        return recommend_include_fee;
    }

    public void setRecommend_include_fee(boolean recommend_include_fee) {
        this.recommend_include_fee = recommend_include_fee;
    }

    public String getSharedcoin_endpoint() {
        return sharedcoin_endpoint;
    }

    public void setSharedcoin_endpoint(String sharedcoin_endpoint) {
        this.sharedcoin_endpoint = sharedcoin_endpoint;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public Addresses[] getAddresses() {
        return addresses;
    }

    public void setAddresses(Addresses[] addresses) {
        this.addresses = addresses;
    }
//
    public Txs[] getTxs() {
        return txs;
    }

    public void setTxs(Txs[] txs) {
        this.txs = txs;
    }

    public class Wallet {
        public int n_tx;
        public int n_tx_filtered;
        public int total_received;
        public int total_sent;
        @SerializedName("final_balance")
        public int final_balance;

        public int getN_tx() {
            return n_tx;
        }

        public void setN_tx(int n_tx) {
            this.n_tx = n_tx;
        }

        public int getN_tx_filtered() {
            return n_tx_filtered;
        }

        public void setN_tx_filtered(int n_tx_filtered) {
            this.n_tx_filtered = n_tx_filtered;
        }

        public int getTotal_received() {
            return total_received;
        }

        public void setTotal_received(int total_received) {
            this.total_received = total_received;
        }

        public int getTotal_sent() {
            return total_sent;
        }

        public void setTotal_sent(int total_sent) {
            this.total_sent = total_sent;
        }

        public int getFinal_balance() {
            return final_balance;
        }

        public void setFinal_balance(int final_balance) {
            this.final_balance = final_balance;
        }
    }

    public class Addresses {
        @SerializedName("address")
        public String address;
        @SerializedName("n_tx")
        public int n_tx;
        @SerializedName("total_received")
        public int total_received;
        @SerializedName("total_sent")
        public int total_sent;
        @SerializedName("final_balance")
        public int final_balance;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getN_tx() {
            return n_tx;
        }

        public void setN_tx(int n_tx) {
            this.n_tx = n_tx;
        }

        public int getTotal_received() {
            return total_received;
        }

        public void setTotal_received(int total_received) {
            this.total_received = total_received;
        }

        public int getTotal_sent() {
            return total_sent;
        }

        public void setTotal_sent(int total_sent) {
            this.total_sent = total_sent;
        }

        public int getFinal_balance() {
            return final_balance;
        }

        public void setFinal_balance(int final_balance) {
            this.final_balance = final_balance;
        }
    }

    public class PrevOut {
        public boolean spent;
        public int tx_index;
        public int type;
        public String addr;
        public int value;
        public int n;
        public String script;

        public boolean isSpent() {
            return spent;
        }

        public void setSpent(boolean spent) {
            this.spent = spent;
        }

        public int getTx_index() {
            return tx_index;
        }

        public void setTx_index(int tx_index) {
            this.tx_index = tx_index;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getAddr() {
            return addr;
        }

        public void setAddr(String addr) {
            this.addr = addr;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public int getN() {
            return n;
        }

        public void setN(int n) {
            this.n = n;
        }

        public String getScript() {
            return script;
        }

        public void setScript(String script) {
            this.script = script;
        }
    }

//    public class Input {
//        public long sequence;
//        public PrevOut prev_out;
//        public String script;
//
//        public long getSequence() {
//            return sequence;
//        }
//
//        public void setSequence(long sequence) {
//            this.sequence = sequence;
//        }
//
//        public PrevOut getPrev_out() {
//            return prev_out;
//        }
//
//        public void setPrev_out(PrevOut prev_out) {
//            this.prev_out = prev_out;
//        }
//
//        public String getScript() {
//            return script;
//        }
//
//        public void setScript(String script) {
//            this.script = script;
//        }
//    }

//    public class Out {
//        public boolean spent;
//        public int tx_index;
//        public int type;
//        public String addr;
//        public int value;
//        public int n;
//        public String script;
//
//        public boolean isSpent() {
//            return spent;
//        }
//
//        public void setSpent(boolean spent) {
//            this.spent = spent;
//        }
//
//        public int getTx_index() {
//            return tx_index;
//        }
//
//        public void setTx_index(int tx_index) {
//            this.tx_index = tx_index;
//        }
//
//        public int getType() {
//            return type;
//        }
//
//        public void setType(int type) {
//            this.type = type;
//        }
//
//        public String getAddr() {
//            return addr;
//        }
//
//        public void setAddr(String addr) {
//            this.addr = addr;
//        }
//
//        public int getValue() {
//            return value;
//        }
//
//        public void setValue(int value) {
//            this.value = value;
//        }
//
//        public int getN() {
//            return n;
//        }
//
//        public void setN(int n) {
//            this.n = n;
//        }
//
//        public String getScript() {
//            return script;
//        }
//
//        public void setScript(String script) {
//            this.script = script;
//        }
//    }

//    public class Txs {
//        public int ver;
//        public Input[] inputs;
//        public int block_height;
//        public String relayed_by;
//        public Out[] out;
//        public int lock_time;
//        public int result;
//        public int size;
//        @SerializedName("balance")
//        public int balance;
//        public boolean double_spend;
//        @SerializedName("time")
//        public int time;
//        public int tx_index;
//        public int vin_sz;
//        @SerializedName("hash")
//        public String hash;
//        public int vout_sz;
//
//        public int getVer() {
//            return ver;
//        }
//
//        public void setVer(int ver) {
//            this.ver = ver;
//        }
//
//        public Input[] getInputs() {
//            return inputs;
//        }
//
//        public void setInputs(Input[] inputs) {
//            this.inputs = inputs;
//        }
//
//        public int getBlock_height() {
//            return block_height;
//        }
//
//        public void setBlock_height(int block_height) {
//            this.block_height = block_height;
//        }
//
//        public String getRelayed_by() {
//            return relayed_by;
//        }
//
//        public void setRelayed_by(String relayed_by) {
//            this.relayed_by = relayed_by;
//        }
//
//        public Out[] getOut() {
//            return out;
//        }
//
//        public void setOut(Out[] out) {
//            this.out = out;
//        }
//
//        public int getLock_time() {
//            return lock_time;
//        }
//
//        public void setLock_time(int lock_time) {
//            this.lock_time = lock_time;
//        }
//
//        public int getResult() {
//            return result;
//        }
//
//        public void setResult(int result) {
//            this.result = result;
//        }
//
//        public int getSize() {
//            return size;
//        }
//
//        public void setSize(int size) {
//            this.size = size;
//        }
//
//        public int getBalance() {
//            return balance;
//        }
//
//        public void setBalance(int balance) {
//            this.balance = balance;
//        }
//
//        public boolean isDouble_spend() {
//            return double_spend;
//        }
//
//        public void setDouble_spend(boolean double_spend) {
//            this.double_spend = double_spend;
//        }
//
//        public int getTime() {
//            return time;
//        }
//
//        public void setTime(int time) {
//            this.time = time;
//        }
//
//        public int getTx_index() {
//            return tx_index;
//        }
//
//        public void setTx_index(int tx_index) {
//            this.tx_index = tx_index;
//        }
//
//        public int getVin_sz() {
//            return vin_sz;
//        }
//
//        public void setVin_sz(int vin_sz) {
//            this.vin_sz = vin_sz;
//        }
//
//        public String getHash() {
//            return hash;
//        }
//
//        public void setHash(String hash) {
//            this.hash = hash;
//        }
//
//        public int getVout_sz() {
//            return vout_sz;
//        }
//
//        public void setVout_sz(int vout_sz) {
//            this.vout_sz = vout_sz;
//        }
//    }
}

