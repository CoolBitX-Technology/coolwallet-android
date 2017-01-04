package com.coolbitx.coolwallet.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ShihYi on 2016/1/29.
 */
public class Txs {
    public int ver;
    public Inputs[] inputs;
    public int block_height;
    public String relayed_by;
    public Out[] out;
    public int lock_time;
    public long result;
    public int size;
    @SerializedName("balance")
    public long balance;
    public boolean double_spend;
    @SerializedName("time")
    public long time;
    public int tx_index;
    public int vin_sz;
    @SerializedName("hash")
    public String hash;
    public int vout_sz;

    public int getVer() {
        return ver;
    }

    public void setVer(int ver) {
        this.ver = ver;
    }

    public Inputs[] getInputs() {
        return inputs;
    }

    public void setInputs(Inputs[] inputs) {
        this.inputs = inputs;
    }

    public int getBlock_height() {
        return block_height;
    }

    public void setBlock_height(int block_height) {
        this.block_height = block_height;
    }

    public String getRelayed_by() {
        return relayed_by;
    }

    public void setRelayed_by(String relayed_by) {
        this.relayed_by = relayed_by;
    }

    public Out[] getOut() {
        return out;
    }

    public void setOut(Out[] out) {
        this.out = out;
    }

    public int getLock_time() {
        return lock_time;
    }

    public void setLock_time(int lock_time) {
        this.lock_time = lock_time;
    }

    public long getResult() {
        return result;
    }

    public void setResult(long result) {
        this.result = result;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public boolean isDouble_spend() {
        return double_spend;
    }

    public void setDouble_spend(boolean double_spend) {
        this.double_spend = double_spend;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getTx_index() {
        return tx_index;
    }

    public void setTx_index(int tx_index) {
        this.tx_index = tx_index;
    }

    public int getVin_sz() {
        return vin_sz;
    }

    public void setVin_sz(int vin_sz) {
        this.vin_sz = vin_sz;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getVout_sz() {
        return vout_sz;
    }

    public void setVout_sz(int vout_sz) {
        this.vout_sz = vout_sz;
    }

    public class Out {
        public boolean spent;
        public long tx_index;
        public int type;
        public String addr;
        public long value;
        public int n;
        public String script;

        public boolean isSpent() {
            return spent;
        }

        public void setSpent(boolean spent) {
            this.spent = spent;
        }

        public long getTx_index() {
            return tx_index;
        }

        public void setTx_index(long tx_index) {
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

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
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

    public class Inputs {
        public long sequence;
        public PrevOut prev_out;
        public String script;

        public long getSequence() {
            return sequence;
        }

        public void setSequence(long sequence) {
            this.sequence = sequence;
        }

        public PrevOut getPrev_out() {
            return prev_out;
        }

        public void setPrev_out(PrevOut prev_out) {
            this.prev_out = prev_out;
        }

        public String getScript() {
            return script;
        }

        public void setScript(String script) {
            this.script = script;
        }

        public class PrevOut {
            public boolean spent;
            public long tx_index;
            public int type;
            public String addr;
            public long value;
            public int n;
            public String script;

            public boolean isSpent() {
                return spent;
            }

            public void setSpent(boolean spent) {
                this.spent = spent;
            }

            public long getTx_index() {
                return tx_index;
            }

            public void setTx_index(long tx_index) {
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

            public long getValue() {
                return value;
            }

            public void setValue(long value) {
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
    }
}