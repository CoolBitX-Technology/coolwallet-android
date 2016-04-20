package com.snscity.egdwlib.utils;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;

public class ByteUtil {
    private static final char[] b58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();

    public static byte[] getBytesPositive(int srcValue, int desLength) {
        byte[] bytes = new byte[desLength];
        for (int i = 0; i < desLength; i++) {
            bytes[i] = (byte) ((srcValue >> (desLength - i - 1) * 8) & 0xFF);
        }
        return bytes;
    }

    public static byte[] getBytesNegative(int srcValue, int desLength) {
        byte[] bytes = new byte[desLength];
        for (int i = 0; i < desLength; i++) {
            bytes[i] = (byte) ((srcValue >> i * 8) & 0xFF);
        }
        return bytes;
    }

    public static byte[] concatAll(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static byte[] getBytes(String srcValue, int desLength, boolean isZeroize) {
        byte[] bytes = srcValue.getBytes(Charset.defaultCharset());
        byte[] bufferBytes;
        if (bytes.length < desLength) {
            if (isZeroize) {
                bufferBytes = new byte[desLength];
            } else {
                bufferBytes = new byte[bytes.length];
            }
            System.arraycopy(bytes, 0, bufferBytes, 0, bytes.length);
        } else {
            bufferBytes = new byte[desLength];
            System.arraycopy(bytes, 0, bufferBytes, 0, desLength);
        }
        return bufferBytes;
    }

    public static String getString(byte[] bytes) {
        int length = 0;
        for (byte aByte : bytes) {
            if (aByte == 0) {
                break;
            }
            length++;
        }
        byte[] bufferBytes = new byte[length];
        System.arraycopy(bytes, 0, bufferBytes, 0, length);
        return new String(bufferBytes);
    }

    public static String toBase58(byte[] b) {
        if (b.length == 0) {
            return "";
        }

        int lz = 0;
        while (lz < b.length && b[lz] == 0) {
            ++lz;
        }

        StringBuffer s = new StringBuffer();
        BigInteger n = new BigInteger(1, b);
        while (n.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] r = n.divideAndRemainder(BigInteger.valueOf(58));
            n = r[0];
            char digit = b58[r[1].intValue()];
            s.append(digit);
        }
        while (lz > 0) {
            --lz;
            s.append("1");
        }
        return s.reverse().toString();
    }

    /**
     * float转换byte
     *
     * @param bb
     * @param x
     * @param index
     */
    public static void floatToByte(byte[] bb, float x, int index) {
        // byte[] b = new byte[4];
        int l = Float.floatToIntBits(x);
        for (int i = 0; i < 4; i++) {
            bb[index + i] = new Integer(l).byteValue();
            l = l >> 8;
        }
    }

    /**
     * int to byte[] 支持 1或者 4 个字节 little-endian
     *
     * @param i
     * @param len
     * @return
     */
    public static byte[] intToByteLittle(int i, int len) {
        byte[] abyte = null;
        if (len == 1) {
            abyte = new byte[len];
            abyte[0] = (byte) (0xff & i);
        } else {
            abyte = new byte[len];
            abyte[0] = (byte) (0xff & i);
            abyte[1] = (byte) ((0xff00 & i) >> 8);
            abyte[2] = (byte) ((0xff0000 & i) >> 16);
            abyte[3] = (byte) ((0xff000000 & i) >> 24);
        }
        return abyte;
    }

    public static int bytesToIntLittle(byte[] bytes) {
        int addr = 0;
        if (bytes.length == 1) {
            addr = bytes[0] & 0xFF;
        } else {
            addr = bytes[0] & 0xFF;
            addr |= ((bytes[1] << 8) & 0xFF00);
            addr |= ((bytes[2] << 16) & 0xFF0000);
            addr |= ((bytes[3] << 24) & 0xFF000000);
        }
        return addr;
    }

    /**
     * int to byte[] 支持 1或者 4 个字节 big-endian
     *
     * @param i
     * @param len
     * @return
     */
    public static byte[] intToByteBig(int i, int len) {
        int intLen = Integer.SIZE / Byte.SIZE;
        byte[] abyte = new byte[len];
        if (len == 1) {
            abyte[0] = (byte) (0xff & i);
        } else {

            //int=4B;但卡片要求8B;
//            原本是00002710000000,不符格式 (多餘的byte會右移補0)
//            正確要求=0000000000002710
            for (int j = intLen; j < len; j++) {
                abyte[j] = (byte) ((i >>> (intLen - 1 - j) * 8) & 0xff);
                LogUtil.i("new intToByteBig abyte[" + (j) + "]=" + String.format("%02x ", abyte[j]));
            }
        }
        return abyte;
    }

    public static int bytesToIntBig(byte[] bytes) {
        int addr = 0;
        if (bytes.length == 1) {
            addr = bytes[0] & 0xFF;
        } else {
            addr = bytes[0] & 0xFF;
            addr = (addr << 8) | (bytes[1] & 0xff);
            addr = (addr << 8) | (bytes[2] & 0xff);
            addr = (addr << 8) | (bytes[3] & 0xff);
        }
        return addr;
    }


}  
