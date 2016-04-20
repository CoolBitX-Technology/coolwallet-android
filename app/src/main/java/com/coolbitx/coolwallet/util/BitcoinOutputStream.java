package com.coolbitx.coolwallet.util;

import java.io.ByteArrayOutputStream;

/**
 * Created by wmgs_01 on 15/10/7.
 */
public class BitcoinOutputStream extends ByteArrayOutputStream {

    public void writeInt16(int value) {
        write(value & 0xff);
        write((value >> 8) & 0xff);
    }

    public void writeInt32(int value) {
        write(value & 0xff);
        write((value >> 8) & 0xff);
        write((value >> 16) & 0xff);
        write((value >>> 24) & 0xff);
    }

    public void writeInt64(long value) {
        writeInt32((int) (value & 0xFFFFFFFFL));
        writeInt32((int) ((value >>> 32) & 0xFFFFFFFFL));
    }

    public void writeVarInt(long value) {
        if (value < 0xfd) {
            write((int) (value & 0xff));
        } else if (value < 0xffff) {
            write(0xfd);
            writeInt16((int) value);
        } else if (value < 0xffffffffL) {
            write(0xfe);
            writeInt32((int) value);
        } else {
            write(0xff);
            writeInt64(value);
        }
    }
}
