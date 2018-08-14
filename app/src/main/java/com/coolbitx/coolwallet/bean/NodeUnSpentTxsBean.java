package com.coolbitx.coolwallet.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wmgs_01 on 15/10/7.
 */
public class NodeUnSpentTxsBean {

    String txid;
    long confirmations;
    List<vin> vinList ;
    List<vout> voutList;

    public List<vin> getVinList() {
        return vinList;
    }

    public List<vout> getVoutList() {
        return voutList;
    }

    public String getTxid() {
        return txid;
    }

    public long getConfirmations() {
        return confirmations;
    }

    public class vin {
        int n;
        String addr;
        double value;

        public int getN() {
            return n;
        }

        public String getAddr() {
            return addr;
        }

        public double getValue() {
            return value;
        }
    }

    public class vout {
        double value;
        int n;
        List<scriptPubKey> scriptPubKey = new ArrayList<>();

        public class scriptPubKey {
            String hex;
            String addresses;

            public String getHex() {
                return hex;
            }

            public String getAddresses() {
                return addresses;
            }
        }

        public double getValue() {
            return value;
        }

        public int getN() {
            return n;
        }
    }


}
