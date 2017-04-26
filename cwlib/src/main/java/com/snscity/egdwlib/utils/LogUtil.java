package com.snscity.egdwlib.utils;

import android.util.Log;

/**
 * @author canye
 */
public class LogUtil {
    private static boolean isShowLog = true;
    // 文件名
    private static String fileName;
    // 方法名
    private static String methodName;
    // 行号
    private static int lineNumber;

    public static void e(String message) {
        if (isShowLog) {
            getElementAttributes(new Throwable().getStackTrace());
            message = message.trim();
            int index = 0;
            int maxLength = 4000;
            String sub;
            while (index < message.length()) {
                // java的字符不允许指定超过总的长度end
                if (message.length() <= index + maxLength) {
                    sub = message.substring(index);
                } else {
                    sub = message.substring(index, maxLength);
                }
                index += maxLength;
                Log.e(fileName, createLog(sub.trim()));
            }

        }
    }

    public static void i(String message) {
        if (isShowLog) {
            getElementAttributes(new Throwable().getStackTrace());
            message = message.trim();
            int index = 0;
            int maxLength = 4000;
            String sub;
            while (index < message.length()) {
                // java的字符不允许指定超过总的长度end
                if (message.length() <= index + maxLength) {
                    sub = message.substring(index);
                } else {
                    sub = message.substring(index, maxLength);
                }
                index += maxLength;
                Log.i(fileName, createLog( sub.trim()));
            }
        }
    }

    public static void d(String message) {
        if (isShowLog) {
            getElementAttributes(new Throwable().getStackTrace());
            message = message.trim();
            int index = 0;
            int maxLength = 4000;
            String sub;
            while (index < message.length()) {
                // java的字符不允许指定超过总的长度end
                if (message.length() <= index + maxLength) {
                    sub = message.substring(index);
                } else {
                    sub = message.substring(index, maxLength);
                }
                index += maxLength;
                Log.d(fileName, createLog(sub.trim()));
            }
        }
    }

    public static void v(String message) {
        if (isShowLog) {
            getElementAttributes(new Throwable().getStackTrace());
            message = message.trim();
            int index = 0;
            int maxLength = 4000;
            String sub;
            while (index < message.length()) {
                // java的字符不允许指定超过总的长度end
                if (message.length() <= index + maxLength) {
                    sub = message.substring(index);
                } else {
                    sub = message.substring(index, maxLength);
                }
                index += maxLength;
                Log.v(fileName, createLog(sub.trim()));
            }
        }
    }

    public static void w(String message) {
        if (isShowLog) {
            getElementAttributes(new Throwable().getStackTrace());
            message = message.trim();
            int index = 0;
            int maxLength = 4000;
            String sub;
            while (index < message.length()) {
                // java的字符不允许指定超过总的长度end
                if (message.length() <= index + maxLength) {
                    sub = message.substring(index);
                } else {
                    sub = message.substring(index, maxLength);
                }
                index += maxLength;
                Log.w(fileName, createLog( sub.trim()));
            }
        }
    }


    private static void getElementAttributes(StackTraceElement[] sElements) {
        fileName = sElements[1].getFileName();
        methodName = sElements[1].getMethodName();
        lineNumber = sElements[1].getLineNumber();
    }

    private static String createLog(String log) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(lineNumber);
        builder.append("]");
        builder.append(methodName);
        builder.append(":  ");
        builder.append(log);
        return builder.toString();
    }

    public static String byte2HexString(byte [] bytes){
        if(bytes == null) return null;

        StringBuilder sb = new StringBuilder();
        for(byte b : bytes){
            sb.append(String.format("%02x ", b));
        }

        return sb.toString();
    }


}  
