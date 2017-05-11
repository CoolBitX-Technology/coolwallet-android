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
    // 行號
    private static int lineNumber;


    private static void l(int p, String message) {
        if (isShowLog) {
            getElementAttributes(new Throwable().getStackTrace());
            message = message.trim();
            int index = 0;
            int maxLength = 4000;
            String sub;
            while (index < message.length()) {

                if (message.length() <= index + maxLength) {
                    sub = message.substring(index);
                } else {
                    sub = message.substring(index, index + maxLength);
                }
                index += maxLength;
                Log.println(p, fileName, createLog(sub.trim()));//4*1024
            }

        }
    }

    public static void e(String message) {
        l(Log.ERROR, message);
    }

    public static void i(String message) {
        l(Log.INFO, message);
    }

    public static void d(String message) {
        l(Log.DEBUG, message);
    }

    public static void v(String message) {
        l(Log.VERBOSE, message);
    }

    public static void w(String message) {
        l(Log.WARN, message);
    }


    private static void getElementAttributes(StackTraceElement[] sElements) {
        fileName = sElements[2].getFileName(); //sElements[1]=LogUtil.java
        methodName = sElements[2].getMethodName();//sElements[1]=i,d,v,w
        lineNumber = sElements[2].getLineNumber();////sElements[1]=LogUtil's lineNumber
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

    public static String byte2HexString(byte[] bytes) {
        if (bytes == null) return null;

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x ", b));
        }

        return sb.toString();
    }
//    public static void e(String message) {
//        if (isShowLog) {
//            getElementAttributes(new Throwable().getStackTrace());
//            message = message.trim();
//            int index = 0;
//            int maxLength = 4000;
//            String sub;
//            while (index < message.length()) {
//
//                if (message.length() <= index + maxLength) {
//                    sub = message.substring(index);
//                } else {
//                    sub = message.substring(index, index+maxLength);
//                }
//                index += maxLength;
//                Log.e(fileName, createLog(sub.trim()));//4*1024
//            }
//
//        }
//    }

//    public static void i(String message) {
//        if (isShowLog) {
//            getElementAttributes(new Throwable().getStackTrace());
//            message = message.trim();
//            int index = 0;
//            int maxLength = 4000;
//            String sub;
//            while (index < message.length()) {
//                if (message.length() <= index + maxLength) {
//                    sub = message.substring(index);
//                } else {
//                    sub = message.substring(index, index+maxLength);
//                }
//                index += maxLength;
//                Log.i(fileName, createLog( sub.trim()));
//            }
//        }
//    }

//    public static void d(String message) {
//        if (isShowLog) {
//            getElementAttributes(new Throwable().getStackTrace());
//            message = message.trim();
//            int index = 0;
//            int maxLength = 4000;
//            String sub;
//            while (index < message.length()) {
//                if (message.length() <= index + maxLength) {
//                    sub = message.substring(index);
//                } else {
//                    sub = message.substring(index, index+maxLength);
//                }
//                index += maxLength;
//                Log.d(fileName, createLog(sub.trim()));
//            }
//        }
//    }

//    public static void v(String message) {
//        if (isShowLog) {
//            getElementAttributes(new Throwable().getStackTrace());
//            message = message.trim();
//            int index = 0;
//            int maxLength = 4000;
//            String sub;
//            while (index < message.length()) {
//                if (message.length() <= index + maxLength) {
//                    sub = message.substring(index);
//                } else {
//                    sub = message.substring(index, index+maxLength);
//                }
//                index += maxLength;
//                Log.v(fileName, createLog(sub.trim()));
//            }
//        }
//    }

//    public static void w(String message) {
//        if (isShowLog) {
//            getElementAttributes(new Throwable().getStackTrace());
//            message = message.trim();
//            int index = 0;
//            int maxLength = 4000;
//            String sub;
//            while (index < message.length()) {
//                if (message.length() <= index + maxLength) {
//                    sub = message.substring(index);
//                } else {
//                    sub = message.substring(index, index+maxLength);
//                }
//                index += maxLength;
//                Log.w(fileName, createLog( sub.trim()));
//            }
//        }
//    }

}
