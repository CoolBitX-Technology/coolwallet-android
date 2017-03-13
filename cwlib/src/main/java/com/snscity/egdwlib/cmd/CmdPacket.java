package com.snscity.egdwlib.cmd;


import android.os.Handler;

import com.snscity.egdwlib.utils.ByteUtil;

import java.util.Arrays;
import java.util.List;

public class CmdPacket {
    private CmdPriority priority;
    private int cla;
    private int ins;
    private int pram1;
    private int pram2;

    private byte[] inputData;

    private int currentPid;

    private int status;

    private byte[] outputData;

    private CmdResultCallback cmdResultCallback;

    private boolean trxBtnFlag=false;

    private static final Handler handler = new Handler();

    private CmdPacket(Builder builder) {
        this.priority = builder.priority;
        this.cla = builder.cla;
        this.ins = builder.ins;
        this.pram1 = builder.pram1;
        this.pram2 = builder.pram2;
        this.inputData = builder.inputData;
    }

    public static class Builder {
        private CmdPriority priority;

        private int cla;
        private int ins;
        private int pram1;
        private int pram2;

        private byte[] inputData;

        public Builder() {
        }

        public Builder setPriority(CmdPriority priority) {
            this.priority = priority;
            return this;
        }

        public Builder setCla(int cla) {
            this.cla = cla;
            return this;
        }

        public Builder setIns(int ins) {
            this.ins = ins;
            return this;
        }

        public Builder setPram1(int pram1) {
            this.pram1 = pram1;
            return this;
        }

        public Builder setPram2(int pram2) {
            this.pram2 = pram2;
            return this;
        }

        public Builder setInputData(byte[] inputData) {
            this.inputData = inputData;
            return this;
        }

        public CmdPacket build() {
            initDefaultValues();
            return new CmdPacket(this);
        }

        private void initDefaultValues() {
            if (priority == null) {
                priority = CmdPriority.NONE;
            }
        }
    }

    public int getIns() {
        return ins;
    }

    public CmdPriority getPriority() {
        return priority;
    }

    public byte[] getOutputData() {
        return outputData;
    }

    public byte[] getInputCmdPacket() {
        currentPid = 0;
        byte[] pid = ByteUtil.getBytesPositive(currentPid, 1);
        byte[] cla = ByteUtil.getBytesPositive(this.cla, 1);
        byte[] ins = ByteUtil.getBytesPositive(this.ins, 1);
        byte[] pram1 = ByteUtil.getBytesPositive(this.pram1, 1);
        byte[] pram2 = ByteUtil.getBytesPositive(this.pram2, 1);
        byte[] lsb, msb, dpc;
        if (inputData == null) {
            lsb = ByteUtil.getBytesNegative(0, 2);
            msb = ByteUtil.getBytesPositive(0, 2);
            dpc = ByteUtil.getBytesPositive(0, 1);
        } else {
            lsb = ByteUtil.getBytesNegative(inputData.length, 2);
            msb = ByteUtil.getBytesPositive(inputData.length, 2);
            int dataPacketCounts = (int) Math.ceil(inputData.length / 16.0);
            dpc = ByteUtil.getBytesPositive(dataPacketCounts, 1);
        }
        int cmdLength = cla.length + ins.length + pram1.length + pram2.length + lsb.length + msb.length + dpc.length;
        byte[] cl = ByteUtil.getBytesPositive(cmdLength, 1);
        currentPid++;
        return ByteUtil.concatAll(pid, cl, cla, ins, pram1, pram2, lsb, msb, dpc);
    }

    public byte[] getInputDataPacket() {
        int dataPacketCounts = (int) Math.ceil(inputData.length / 16.0);
        if (currentPid <= dataPacketCounts) {
            byte[] pid = ByteUtil.getBytesPositive(currentPid, 1);
            byte[] dataLen;
            if ((inputData.length - (currentPid - 1) * 16) >= 16) {
                dataLen = ByteUtil.getBytesPositive(16, 1);
            } else {
                dataLen = ByteUtil.getBytesPositive(inputData.length - (currentPid - 1) * 16, 1);
            }
            byte[] data = Arrays.copyOfRange(inputData, (currentPid - 1) * 16, (currentPid - 1) * 16 + dataLen[0]);
            currentPid++;
            return ByteUtil.concatAll(pid, dataLen, data);
        }
        return null;
    }

    public void parseOutputDataPacket(List<byte[]> outputDataPackets) {
        currentPid = 1;
        byte[] bufferOutputData = null;
        if (outputDataPackets == null || outputDataPackets.size() == 0) {
            return;
        }
        for (byte[] outputDataPacket : outputDataPackets) {
            if (outputDataPacket[0] != currentPid) {
                continue;
            }
            if (outputDataPacket[1] > 16) {
                return;
            }
            byte[] segmentOutputData = Arrays.copyOfRange(outputDataPacket, 2, outputDataPacket.length);
            if (bufferOutputData == null) {
                bufferOutputData = segmentOutputData;
            } else {
                bufferOutputData = ByteUtil.concatAll(bufferOutputData, segmentOutputData);
            }
            currentPid++;
        }
        if (bufferOutputData != null) {
            status = bufferOutputData[bufferOutputData.length - 2] << 8 | bufferOutputData[bufferOutputData.length - 1];
//            Log.e(TAG, "length:"+ String.valueOf(bufferOutputData[bufferOutputData.length - 2] )+"cmd packet result:" + status);
            outputData = Arrays.copyOfRange(bufferOutputData, 0, bufferOutputData.length - 2);
//            Log.e(TAG,"output:" + Arrays.toString(outputData));
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (cmdResultCallback != null) {
                        cmdResultCallback.onSuccess(status, outputData);
                    }
                }
            });
        }
    }

    public void setCmdResultListener(CmdResultCallback cmdResultCallback) {
        this.cmdResultCallback = cmdResultCallback;
    }

    public void setTrxBtnFlag(boolean trxBtnFlag) {
        this.trxBtnFlag = trxBtnFlag;
    }
}
