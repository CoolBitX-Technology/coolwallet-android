package com.coolbitx.coolwallet.util;

import android.content.Context;

import com.coolbitx.coolwallet.bean.UnSpentTxsBean;
import com.coolbitx.coolwallet.general.AppPrefrence;
import com.coolbitx.coolwallet.general.PublicPun;
import com.snscity.egdwlib.utils.LogUtil;

import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.DERInteger;
import org.spongycastle.asn1.DLSequence;
import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.digests.RIPEMD160Digest;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.signers.ECDSASigner;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by wmgs_01 on 15/10/7.
 */
public class BTCUtils {

    public static final int MAX_TX_LEN_FOR_NO_FEE = 10000;
    public static final long MIN_PRIORITY_FOR_NO_FEE = 57600000;
    public static final long MIN_MIN_OUTPUT_VALUE_FOR_NO_FEE = 10000000L;
    public static final long MIN_FEE_PER_TX = 10000;
    //    public static final long MAX_ALLOWED_FEE = BTCUtils.parseValue("0.1");
    public static final long MAX_ALLOWED_FEE = BTCUtils.parseValue("0.0001");
    private static final ECDomainParameters EC_PARAMS;
    private static final char[] BASE58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private final static String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private static final int SATOSHIS_PER_COIN = 100000000;
    private static final int BASE58_CHUNK_DIGITS = 10;//how many base 58 digits fits in long
    private static final BigInteger BASE58_CHUNK_MOD = BigInteger.valueOf(0x5fa8624c7fba400L); //58^BASE58_CHUNK_DIGITS
    private static final byte[] BASE58_VALUES = new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -2, -2, -2, -2, -2, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, -1, -1, -1, -1, -1, -1,
            -1, 9, 10, 11, 12, 13, 14, 15, 16, -1, 17, 18, 19, 20, 21, -1,
            22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, -1, -1, -1, -1, -1,
            -1, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, -1, 44, 45, 46,
            47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

    private static int RECOMMENDED_FEE_PER_BYTE;

    static {
        X9ECParameters params = SECNamedCurves.getByName("secp256k1");
        EC_PARAMS = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
    }

    public static long parseValue(String valueStr) throws NumberFormatException {
        return (long) (Double.parseDouble(valueStr) * 1e8); //DTCO
//        return (long) (Double.parseDouble(valueStr));
    }

    public static FeeChangeAndSelectedOutputs calcFeeChangeAndSelectOutputsToSpend(Context mContext, List<UnSpentTxsBean> UnSpentTxsBeanList,
                                                                                   long amountToSend, long extraFee,
                                                                                   final boolean isPublicKeyCompressed) throws ValidationException {
        long fee = 0;//calculated below
        long change = 0;
        long valueOfUnspentOutputs = 0;
        //fees per byte *1000=1kb
        int txOneOutputLen = 0;
        long updatedFee = 0;
        boolean isDust = false;
        RECOMMENDED_FEE_PER_BYTE = AppPrefrence.getRecommendedFastestFee(mContext);
        ArrayList<UnSpentTxsBean> outputsToSpend = new ArrayList<>();
        if (amountToSend <= 0) {    //No this situation,already rule out by uone instruction.
            //transfer all funds from these addresses to outputAddress
            LogUtil.e("找到amountToSend<=0");
            change = 0;
            for (UnSpentTxsBean outputInfo : UnSpentTxsBeanList) {
                outputsToSpend.add(outputInfo);
                valueOfUnspentOutputs += (long) (outputInfo.getAmount() * SATOSHIS_PER_COIN); //Satoshi
            }

            final int txLen = BTCUtils.getMaximumTxSize(UnSpentTxsBeanList, 1, isPublicKeyCompressed);

            fee = BTCUtils.calcMinimumFee(txLen, UnSpentTxsBeanList, valueOfUnspentOutputs - MIN_FEE_PER_TX * (1 + txLen / 1000));
            amountToSend = valueOfUnspentOutputs - fee - extraFee;
        } else {

            for (int i = 0; i < UnSpentTxsBeanList.size(); i++) {
                UnSpentTxsBean outputInfo = UnSpentTxsBeanList.get(i);
                LogUtil.d("找到Unspent第" + i + "筆=" + String.valueOf(BTCUtils.BTCconvertToSatoshisValue(outputInfo.getAmount()))
                        + ";addr=" + outputInfo.getAddress());
                outputsToSpend.add(outputInfo);
                valueOfUnspentOutputs += BTCUtils.BTCconvertToSatoshisValue(outputInfo.getAmount());

                for (int j = 0; j < 2; j++) {
                    txOneOutputLen = BTCUtils.getMaximumTxSize(outputsToSpend, change > 0 ? 2 : 1, isPublicKeyCompressed);
                    updatedFee = calcRecommendedFee(txOneOutputLen);
                    AppPrefrence.saveRecommendedDefaultFee(mContext, updatedFee);

                    if (AppPrefrence.getAutoFeeCheckBox(mContext)) {
                        fee = updatedFee;
                    } else {
                        fee = BTCconvertToSatoshisValue(AppPrefrence.getManualFee(mContext));
                    }

                    change = valueOfUnspentOutputs - fee - amountToSend;
                }

                LogUtil.d("計算要用來寄出的第" + i + "筆資料:" + ";addr=" + outputInfo.getAddress() + ",餘額=" +
                        valueOfUnspentOutputs + ",amountToSend=" + amountToSend + ",fee=" + fee + ",change=" + change);

                if (valueOfUnspentOutputs >= amountToSend + fee) { //收集足夠金額的output
                    break;
                }
            }

            LogUtil.i("總計寄出資料=" + outputsToSpend.size() + "筆input,餘額=" +
                    valueOfUnspentOutputs + ",fee=" + fee + ",amountToSend=" + amountToSend + ",change=" + change);
        }

        //BitCoin dust
        if (change < 5460) {
//            amountToSend = amountToSend + change;
//            change = 0;
            isDust = true;
        }

        //ZERO Confirmation unspent?
        if (amountToSend + fee > valueOfUnspentOutputs) {
            LogUtil.e("Not enough funds " + (valueOfUnspentOutputs - fee));
            float output = (((float) valueOfUnspentOutputs - (float) fee - (float) amountToSend) / (float) SATOSHIS_PER_COIN);
            throw new ValidationException("Insufficient funds: " + new DecimalFormat("#.########").format(output) + " BTC\nFees:" + new DecimalFormat("#.########").format((float) fee / (float) SATOSHIS_PER_COIN) + " BTC");
        }
        if (outputsToSpend.isEmpty()) {
            LogUtil.e("No outputs to spend");
            throw new ValidationException("No outputs to spend.");
        }
//        if (fee + extraFee > MAX_ALLOWED_FEE) {
////            LogUtil.e("Fee is too big " + fee + ";MAX_ALLOWED_FEE=" + MAX_ALLOWED_FEE);
//            throw new ValidationException("Fee is too big " + fee + ";MAX_ALLOWED_FEE=" + MAX_ALLOWED_FEE);
//        }
        if (fee < 0 || extraFee < 0) {
            LogUtil.e("Incorrect fee " + fee);
            throw new ValidationException("Incorrect fee " + fee);
        }
        if (change < 0) {
            LogUtil.e("Incorrect change " + change);
            throw new ValidationException("Incorrect change " + change);
        }
        if (amountToSend < 0) {
            LogUtil.e("Incorrect amount to send " + amountToSend);
            throw new ValidationException("Incorrect amount to send " + amountToSend);
        }
        return new FeeChangeAndSelectedOutputs(fee + extraFee, change, amountToSend, outputsToSpend, valueOfUnspentOutputs, isDust);
    }

    /**
     * @param valueStr
     * @return
     * @throws NumberFormatException
     * Float formatting is different for countries
     */
    public static long convertToSatoshisValueForDIsplay(String valueStr) throws NumberFormatException {
        String satoshisStr = String.valueOf(SATOSHIS_PER_COIN);
        java.math.BigDecimal x = new java.math.BigDecimal(valueStr);
        java.math.BigDecimal y = new java.math.BigDecimal(satoshisStr);
        return x.multiply(y).longValue();
    }

    public static long BTCconvertToSatoshisValue(double value) throws NumberFormatException {
        double result = value * SATOSHIS_PER_COIN;

        return Math.round(result);
    }

//    public static long convertToBTC(String valueStr) throws NumberFormatException {
//        String satoshisStr = String.valueOf(SATOSHIS_PER_COIN);
//        java.math.BigDecimal x = new java.math.BigDecimal(valueStr);
//        java.math.BigDecimal y = new java.math.BigDecimal(satoshisStr);
//        return x.multiply(y).longValue();
//    }

    public static boolean isZeroFeeAllowed(int txLen, Collection<UnSpentTxsBean> unspentOutputInfos, long minOutput) {
        if (txLen < MAX_TX_LEN_FOR_NO_FEE && minOutput > MIN_MIN_OUTPUT_VALUE_FOR_NO_FEE) {
            long priority = 0;
            for (UnSpentTxsBean output : unspentOutputInfos) {
                if (output.getConfirmations() > 0) {
                    priority += output.getConfirmations() * output.getAmount();
                }
            }
            priority /= txLen;
            if (priority > MIN_PRIORITY_FOR_NO_FEE) {
                return true;
            }
        }
        return false;
    }

    private static long calcRecommendedFee(int txLen) {
        LogUtil.e("資料長度=" + txLen + ";fee=" + txLen * RECOMMENDED_FEE_PER_BYTE);//);
        return txLen * RECOMMENDED_FEE_PER_BYTE;//;
    }

    public static long calcMinimumFee(int txLen, Collection<UnSpentTxsBean> unspentOutputInfos, long minOutput) {
        if (isZeroFeeAllowed(txLen, unspentOutputInfos, minOutput)) {
            return 0;
        }
        return MIN_FEE_PER_TX * (1 + txLen / 1000);
    }

    public static int getMaximumTxSize(Collection<UnSpentTxsBean> unspentOutputInfos, int outputsCount, boolean compressedPublicKey) throws ValidationException {
        if (unspentOutputInfos == null || unspentOutputInfos.isEmpty()) {
            throw new ValidationException("No information about tx inputs provided");
        }
        //scriptSig contains the signature along with the public key(106)
        int maxInputScriptLen = 73 + (compressedPublicKey ? 33 : 65);
//        return 9 + unspentOutputInfos.size() * (41 + maxInputScriptLen) + outputsCount * 33;
        LogUtil.e("getMaximumTxSize 計算:INPUT=" + unspentOutputInfos.size() +
                ";maxInputScriptLen=" + (41 + maxInputScriptLen) + ";OUTPUT=" + outputsCount);
        int maxSize = 10 + unspentOutputInfos.size() * (41 + maxInputScriptLen) + outputsCount * 34;

        LogUtil.e("getMaximumTxSize=" + maxSize);
        return maxSize;
    }

    private static byte[] Sha256(byte[] data, int start, int len, int recursion) {
        if (recursion == 0) return data;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Arrays.copyOfRange(data, start, start + len));
            return Sha256(md.digest(), 0, 32, recursion - 1);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static boolean ValidateBitcoinAddress(String addr) {
        if (addr.length() < 26 || addr.length() > 35) {
            LogUtil.d("ValidateBitcoinAddress--addr.length()");
            return false;
        }
//        byte[] decoded = DecodeBase58(addr, 58, 25);
        byte[] decoded = decodeBase58(addr);

        if (decoded == null) {
            LogUtil.d("ValidateBitcoinAddress--DecodeBase58");
            return false;
        }
        byte[] hash = Sha256(decoded, 0, 21, 2);
        LogUtil.d("ValidateBitcoinAddress--hash pass");

        LogUtil.e(LogUtil.byte2HexString(Arrays.copyOfRange(hash, 0, 4)) + " 比對 " + LogUtil.byte2HexString(Arrays.copyOfRange(decoded, 21, 25)));

        return Arrays.equals(Arrays.copyOfRange(hash, 0, 4), Arrays.copyOfRange(decoded, 21, 25));
    }

    private static byte[] DecodeBase58(String input, int base, int len) {
        byte[] output = new byte[len];
        for (int i = 0; i < input.length(); i++) {
            char t = input.charAt(i);
            LogUtil.d("跳出字元=" + t);
            int p = ALPHABET.indexOf(t);
            if (p == -1) return null;

            for (int j = len - 1; j > 0; j--, p /= 256) {
                p += base * (output[j] & 0xFF);
                output[j] = (byte) (p % 256);
                LogUtil.d("跳出字元j=" + j + ";p=" + p);
            }

            if (p != 0) return null;
        }

        return output;
    }

    public static byte[] decodeBase58(String input) {
        if (input == null) {
            return null;
        }
        input = input.trim();
        if (input.length() == 0) {
            return new byte[0];
        }
        BigInteger resultNum = BigInteger.ZERO;
        int nLeadingZeros = 0;
        while (nLeadingZeros < input.length() && input.charAt(nLeadingZeros) == BASE58[0]) {
            nLeadingZeros++;
        }
        long acc = 0;
        int nDigits = 0;
        int p = nLeadingZeros;
        while (p < input.length()) {
            int v = BASE58_VALUES[input.charAt(p) & 0xff];
            if (v >= 0) {
                acc *= 58;
                acc += v;
                nDigits++;
                if (nDigits == BASE58_CHUNK_DIGITS) {
                    resultNum = resultNum.multiply(BASE58_CHUNK_MOD).add(BigInteger.valueOf(acc));
                    acc = 0;
                    nDigits = 0;
                }
                p++;
            } else {
                break;
            }
        }
        if (nDigits > 0) {
            long mul = 58;
            while (--nDigits > 0) {
                mul *= 58;
            }
            resultNum = resultNum.multiply(BigInteger.valueOf(mul)).add(BigInteger.valueOf(acc));
        }
        final int BASE58_SPACE = -2;
        while (p < input.length() && BASE58_VALUES[input.charAt(p) & 0xff] == BASE58_SPACE) {
            p++;
        }
        if (p < input.length()) {
            return null;
        }
        byte[] plainNumber = resultNum.toByteArray();
        int plainNumbersOffs = plainNumber[0] == 0 ? 1 : 0;
        byte[] result = new byte[nLeadingZeros + plainNumber.length - plainNumbersOffs];
        System.arraycopy(plainNumber, plainNumbersOffs, result, nLeadingZeros, plainNumber.length - plainNumbersOffs);
        return result;
    }

    public static String toHex(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] doubleSha256(byte[] bytes) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            LogUtil.i("doubleSha256 unSign hex=" + PublicPun.byte2HexStringNoBlank(sha256.digest(sha256.digest(bytes))));
            return sha256.digest(sha256.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] reverse(byte[] bytes) {
        byte[] result = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            result[i] = bytes[bytes.length - i - 1];
        }
        return result;
    }

    /**
     * 格式化字符串为指定长度，不足长度前面补arg0
     *
     * @param s:要格式化的字符串
     * @param arg0:补充的字符
     * @param lenth:将字符串格式化成指定长度
     * @return 格式化后的字符串
     */
    public static String formatStr(String s, String arg0, int lenth) {
        StringBuffer str = new StringBuffer(s);
        while (str.toString().getBytes().length < lenth) {
            str.insert(0, arg0);
        }
        return str.toString();
    }

    public static byte[] sha256ripemd160(byte[] publicKey) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            //https://en.bitcoin.it/wiki/Technical_background_of_Bitcoin_addresses
            //1 - Take the corresponding public key generated with it (65 bytes, 1 byte 0x04, 32 bytes corresponding to X coordinate, 32 bytes corresponding to Y coordinate)
            //2 - Perform SHA-256 hashing on the public key
            byte[] sha256hash = sha256.digest(publicKey);
            //3 - Perform RIPEMD-160 hashing on the result of SHA-256
            RIPEMD160Digest ripemd160Digest = new RIPEMD160Digest();
            ripemd160Digest.update(sha256hash, 0, sha256hash.length);
            byte[] hashedPublicKey = new byte[20];
            ripemd160Digest.doFinal(hashedPublicKey, 0);
            return hashedPublicKey;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verify(byte[] publicKey, byte[] signature, byte[] msg) {
        synchronized (EC_PARAMS) {
            boolean valid;
            ECDSASigner signerVer = new ECDSASigner();
            try {
                ECPublicKeyParameters pubKey = new ECPublicKeyParameters(EC_PARAMS.getCurve().decodePoint(publicKey), EC_PARAMS);
                signerVer.init(false, pubKey);
                ASN1InputStream derSigStream = new ASN1InputStream(signature);
                DLSequence seq = (DLSequence) derSigStream.readObject();
                BigInteger r = ((DERInteger) seq.getObjectAt(0)).getPositiveValue();
                BigInteger s = ((DERInteger) seq.getObjectAt(1)).getPositiveValue();
                derSigStream.close();
                valid = signerVer.verifySignature(msg, r, s);
            } catch (IOException e) {
                throw new RuntimeException();
            }
            return valid;
        }
    }

    public static class FeeChangeAndSelectedOutputs {
        public final long amountForRecipient, change, fee, valueOfUnspentOutputs;
        public final ArrayList<UnSpentTxsBean> outputsToSpend;
        public final boolean isDust;

        public FeeChangeAndSelectedOutputs(long fee, long change, long amountForRecipient, ArrayList<UnSpentTxsBean> outputsToSpend, long ValueOfUnspentOutputs, boolean isDust) {
            this.fee = fee;
            this.change = change;
            this.amountForRecipient = amountForRecipient;
            this.outputsToSpend = outputsToSpend;
            this.valueOfUnspentOutputs = ValueOfUnspentOutputs;
            this.isDust = isDust;
            LogUtil.e("fee=" + fee + ";change=" + change + ";amountForRecipient=" + amountForRecipient + ";valueOfUnspentOutputs=" + valueOfUnspentOutputs);
        }
    }
}
