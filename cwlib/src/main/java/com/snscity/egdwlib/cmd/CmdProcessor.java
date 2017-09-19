package com.snscity.egdwlib.cmd;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.snscity.egdwlib.ble.BleGattCallback;
import com.snscity.egdwlib.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class CmdProcessor implements BleGattCallback {
    private static CmdProcessor cmdProcessor;

    private BluetoothGatt bluetoothGatt;
    private CmdPacket currentCmdPacket;
    private List<CmdPacket> cmdPackets;
    private List<BluetoothGattCharacteristic> gattCharacteristics;

    private boolean isBusy;
    private boolean isReady;

    private CmdProcessor() {
    }

    public static CmdProcessor getInstance() {
        if (cmdProcessor == null) {
            synchronized (CmdProcessor.class) {
                if (cmdProcessor == null) {
                    cmdProcessor = new CmdProcessor();
                }
            }
        }
        return cmdProcessor;
    }

    public void init(BluetoothGatt bluetoothGatt, List<BluetoothGattCharacteristic> gattCharacteristics) {
        cmdPackets = new ArrayList<>();
        this.bluetoothGatt = bluetoothGatt;
        this.gattCharacteristics = gattCharacteristics;
    }

    public void addCmd(CmdPacket cmdPacket) {
        cmdPackets.add(cmdPacket);
        getCmd();
    }

    public void getCmd() {
        if (isBusy) {
            return;
        }
        if (cmdPackets.size() == 0) {
            return;
        }
        for (int i = 0; i < cmdPackets.size(); i++) {
            if (cmdPackets.get(i).getPriority() == CmdPriority.EXCLUSIVE) {
                currentCmdPacket = cmdPackets.get(i);
                isBusy = true;
                removeCmdBeforeIndex(i);
                break;
            }
        }
        if (!isBusy) {
            for (int i = 0; i < cmdPackets.size(); i++) {
                if (cmdPackets.get(i).getPriority() == CmdPriority.TOP) {
                    currentCmdPacket = cmdPackets.get(i);
                    isBusy = true;
                    removeCmdAtIndex(i);
                    break;
                }
            }
        }
        if (!isBusy) {
            currentCmdPacket = cmdPackets.get(0);
            isBusy = true;
            removeCmdAtIndex(0);
        }
        executeCmd();
    }

    private void removeCmdBeforeIndex(int index) {
        for (int i = index; i >= 0; i--) {
            cmdPackets.remove(i);
        }
    }

    private void removeCmdAtIndex(int index) {
        cmdPackets.remove(index);
    }

        private void executeCmd() {
        byte[] bytes = currentCmdPacket.getInputCmdPacket();
//        LogUtil.i("executecmd=" + LogUtil.byte2HexString((bytes)));
        if (gattCharacteristics != null) {
            final BluetoothGattCharacteristic characteristic = gattCharacteristics.get(1);
            characteristic.setValue(bytes);
            if (bluetoothGatt == null) {
                return;
            }
            bluetoothGatt.writeCharacteristic(characteristic);
        }
    }
//    private void executeCmd() {
//        LogUtil.e("executeCmd="+Integer.toHexString(currentCmdPacket.getIns()));
//
//        if (currentCmdPacket.getIns() == CmdIns.TRX_VERIFY_BT) {
//
//            if (gattCharacteristics != null) {
//                final BluetoothGattCharacteristic characteristic = gattCharacteristics.get(0);
//                if (bluetoothGatt == null) {
//                    return;
//                }
//                bluetoothGatt.writeCharacteristic(characteristic);
//            }
//        } else {
//            byte[] bytes = currentCmdPacket.getInputCmdPacket();
//            if (gattCharacteristics != null) {
//                final BluetoothGattCharacteristic characteristic = gattCharacteristics.get(1);
//                characteristic.setValue(bytes);
//                if (bluetoothGatt == null) {
//                    return;
//                }
//                bluetoothGatt.writeCharacteristic(characteristic);
//            }
//        }
//    }

    static boolean isBeginFlag = false;
    static boolean isBtnFlag = false;

    @Override
    public void onWriteToA007(byte[] data) {


        if ((data[3] == 0x72 || data[3] == 0x73) && isBtnFlag) {
            isBeginFlag = true;
            isBtnFlag = false;
        } else {
            isBeginFlag = false;
        }
        LogUtil.e("send command is:" + LogUtil.byte2HexString(data) + " ; isBtnFlag=" + isBtnFlag + " ; isBeginFlag=" + isBeginFlag);

        if (gattCharacteristics != null) {
            if (data != null && data.length > 0 && data[data.length - 1] != (byte) 0x00) {
                final BluetoothGattCharacteristic characteristic = gattCharacteristics.get(2);
//                LogUtil.e("准备请求uuid：" + characteristic.getUuid());
                byte[] bytes = currentCmdPacket.getInputDataPacket();
                if (bytes != null) {
                    characteristic.setValue(bytes);
                }
                bluetoothGatt.writeCharacteristic(characteristic);
            } else {
                final BluetoothGattCharacteristic characteristic = gattCharacteristics.get(0);
//                LogUtil.e("准备请求uuid：" + characteristic.getUuid());
                bluetoothGatt.readCharacteristic(characteristic);
            }
        }
    }

    @Override
    public void onWriteToA008(byte[] data) {
        LogUtil.e("inputData is:" + LogUtil.byte2HexString(data));
        if (gattCharacteristics != null) {
            byte[] bytes = currentCmdPacket.getInputDataPacket();
            if (bytes != null) {
                final BluetoothGattCharacteristic characteristic = gattCharacteristics.get(2);
//                LogUtil.e("准备请求uuid：" + characteristic.getUuid());
                characteristic.setValue(bytes);
                bluetoothGatt.writeCharacteristic(characteristic);
            } else {
                final BluetoothGattCharacteristic characteristic = gattCharacteristics.get(0);
//                LogUtil.e("准备请求uuid：" + characteristic.getUuid());
                bluetoothGatt.readCharacteristic(characteristic);
            }
        }
    }

    @Override
    public void onReadFromA006(byte[] data) {
        LogUtil.e("A006:" + LogUtil.byte2HexString(data));
        if (gattCharacteristics != null) {

            // for didGetButton
            if (isBeginFlag == true) {
//                LogUtil.i("isBtnFlag=true && data=" + LogUtil.byte2HexString(data));
                if (data[data.length - 1] == (byte) 0x80) {
                    //button pressed
                    if (isReady) {
                        isBusy = false;
                        if (!outputDatas.isEmpty()) {
                            outputDatas.clear();
                        }

                        getCmd();
                        isReady = false;

                    } else {
                        final BluetoothGattCharacteristic characteristic = gattCharacteristics.get(3);
//                LogUtil.e("准备请求uuid：" + characteristic.getUuid());
                        bluetoothGatt.readCharacteristic(characteristic);
                    }
                } else {
                    // No button pressed yet
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } finally {
                                final BluetoothGattCharacteristic characteristic = gattCharacteristics.get(0);
//                            LogUtil.e("准备请求uuid：" + characteristic.getUuid());
                                bluetoothGatt.readCharacteristic(characteristic);
                            }
                        }
                    }.start();
                }

            } else {

                //一般cmd,ff跟81都是忙碌;00 is ready;81 is button presses;
                if (data[data.length - 1] == (byte) 0xFF || data[data.length - 1] == (byte) 0x81) {
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } finally {
                                final BluetoothGattCharacteristic characteristic = gattCharacteristics.get(0);
//                            LogUtil.e("准备请求uuid：" + characteristic.getUuid());
                                bluetoothGatt.readCharacteristic(characteristic);
                            }
                        }
                    }.start();
                } else {


                    // modified for ble , 20170222
//                    if (isReady) {
//                        isBusy = false;
//
//                        if (!outputDatas.isEmpty()) {
//                            outputDatas.clear();
//                        }
//
//                        getCmd();
//                        isReady = false;
//                    } else {
//                        final BluetoothGattCharacteristic characteristic = gattCharacteristics.get(3);
//                        bluetoothGatt.readCharacteristic(characteristic);
//                    }

                    final BluetoothGattCharacteristic characteristic = gattCharacteristics.get(3);
//                    LogUtil.e("準備請求uuid：" + characteristic.getUuid());
                    bluetoothGatt.readCharacteristic(characteristic);
                }
            }
        }
    }

//    @Override
//    public void onReadFromA006(byte[] data) {
//        LogUtil.e("state:" + LogUtil.byte2HexString(data)+";CmdIns="+Integer.toHexString(currentCmdPacket.getIns()));
//        if (currentCmdPacket.getIns() == CmdIns.TRX_VERIFY_OTP || currentCmdPacket.getIns() == CmdIns.TRX_VERIFY_BT) {
//            if (gattCharacteristics != null) {
//                if (data[data.length - 1] == (byte) 0x00) {
//                    new Thread() {
//                        @Override
//                        public void run() {
//                            try {
//                                Thread.sleep(100);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            } finally {
//                                final BluetoothGattCharacteristic characteristic = gattCharacteristics.get(0);
//                                bluetoothGatt.readCharacteristic(characteristic);
//                            }
//                        }
//                    }.start();
//                } else if (data[data.length - 1] == (byte) 0x80) {
//                    final BluetoothGattCharacteristic characteristic = gattCharacteristics.get(3);
//                    bluetoothGatt.readCharacteristic(characteristic);
//                }
//            }
//        } else {
//            if (gattCharacteristics != null) {
//                if (data[data.length - 1] == (byte) 0xFF || data[data.length - 1] == (byte) 0x81) {
//                    new Thread() {
//                        @Override
//                        public void run() {
//                            try {
//                                Thread.sleep(100);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            } finally {
//                                final BluetoothGattCharacteristic characteristic = gattCharacteristics.get(0);
//                                bluetoothGatt.readCharacteristic(characteristic);
//                            }
//                        }
//                    }.start();
//                } else if (data[data.length - 1] == (byte) 0x00) {
//                    final BluetoothGattCharacteristic characteristic = gattCharacteristics.get(3);
//                    bluetoothGatt.readCharacteristic(characteristic);
//                }
//            }
//        }
//    }


    public void setButtonA006(boolean flag) {
        isBtnFlag = flag;
    }

    private List<byte[]> outputDatas = new ArrayList<>();

    @Override
    public void onReadFromA009(byte[] data) {
        LogUtil.e("card response is:" + LogUtil.byte2HexString(data));
        if (gattCharacteristics != null) {
            if (data.length == 1 && data[0] == (byte) 0xFC) {
                //讀取完畢,最後返回FC
                currentCmdPacket.parseOutputDataPacket(outputDatas);

                isBusy = false;

                if (!outputDatas.isEmpty()) {
                    outputDatas.clear();
                }

                getCmd();

                //modified for ble
//                isReady = false;

            } else if (data.length == 3 && data[0] == 0x00) {
                //回覆錯誤碼
                //no data return, sw return only
                //add this case for speed up, skip read FC
                byte[] swOnly = {0x01, 0x02, data[1], data[2]};
                outputDatas.add(swOnly);
                currentCmdPacket.parseOutputDataPacket(outputDatas);

                isBusy = false;

                // modified for ble
//                isReady = true;
//                final BluetoothGattCharacteristic characteristic = gattCharacteristics.get(0);
//                bluetoothGatt.readCharacteristic(characteristic);

            } else {
                //继续读取其他数据
                outputDatas.add(data);
                final BluetoothGattCharacteristic characteristic = gattCharacteristics.get(3);
//                LogUtil.e("准备请求uuid：" + characteristic.getUuid());
                bluetoothGatt.readCharacteristic(characteristic);
            }
        }
    }
}
