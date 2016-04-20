package com.snscity.egdwlib.cmd;


public interface CmdResultCallback {

    void onSuccess(int status, byte[] outputData);
}
