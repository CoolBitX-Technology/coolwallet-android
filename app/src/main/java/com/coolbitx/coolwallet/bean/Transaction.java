package com.coolbitx.coolwallet.bean;

import com.coolbitx.coolwallet.general.PublicPun;
import com.coolbitx.coolwallet.util.BTCUtils;
import com.coolbitx.coolwallet.util.BitcoinOutputStream;
import com.coolbitx.coolwallet.util.ByteUtils;
import com.coolbitx.coolwallet.util.ECKey;
import com.coolbitx.coolwallet.util.ValidationException;
import com.snscity.egdwlib.utils.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Stack;

/**
 * Created by wmgs_01 on 15/10/6.
 */
public class Transaction {

    public static byte[] ScriptAdd;
    public final Input[] inputs;
    public final Output[] outputs;
    public final int lockTime;

    public Transaction(Input[] inputs, Output[] outputs, int lockTime) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.lockTime = lockTime;
    }

    public  static byte[] getSignature(byte[] signedData) {

        byte[] derX = new byte[32];
        byte[] derY = new byte[32];

        //x data
        System.arraycopy(signedData, 0, derX, 0, derX.length);
        System.arraycopy(signedData, 32, derY, 0, derY.length);
        ECKey.ECDSASignature ecSig = new ECKey.ECDSASignature(new BigInteger(1, derX), new BigInteger(1, derY));
        byte[] signature = ecSig.encodeToDER();


        return signature;
    }

    public  byte[] getScriptSig(int ind) throws IOException {

        BitcoinOutputStream bos = new BitcoinOutputStream();
        int scriptLen = inputs[ind].signature == null ? 0 : inputs[ind].signature.length;
        bos.writeVarInt(scriptLen + 1);//SIGHASH_ALL 1B
        if (scriptLen > 0) {
            bos.write(inputs[ind].signature);
        }
        bos.writeVarInt(Transaction.Script.SIGHASH_ALL);
        int pushPublicKeyLen = inputs[ind].publicKey.length;
        bos.writeVarInt(pushPublicKeyLen);
        bos.write(inputs[ind].publicKey);

        return bos.toByteArray();
    }

    public byte[] getBitcoinTransaction() {
        BitcoinOutputStream bos = new BitcoinOutputStream();
        try {
            bos.writeInt32(1);
            bos.writeVarInt(inputs.length);
            for (Input input : inputs) {
                bos.write(BTCUtils.reverse(input.outPoint.hash));
                bos.writeInt32(input.outPoint.index);
                byte[] scriptSig = getScriptSig(inputs.length - 1);
                bos.writeVarInt(scriptSig.length);
                bos.write(scriptSig);
                bos.writeInt32(input.sequence);
            }
            bos.writeVarInt(outputs.length);
            for (Output output : outputs) {
                bos.writeInt64(output.value);
                int scriptLen = output.script == null ? 0 : output.script.bytes.length;
                bos.writeVarInt(scriptLen);
                if (scriptLen > 0) {
                    bos.write(output.script.bytes);
                }
            }
            bos.writeInt32(lockTime);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bos.toByteArray();
    }


    public byte[] getBytes() {
        BitcoinOutputStream bos = new BitcoinOutputStream();
        try {
            bos.writeInt32(1);//4B
            bos.writeVarInt(inputs.length);//1B
            for (Input input : inputs) {
                bos.write(BTCUtils.reverse(input.outPoint.hash));
                bos.writeInt32(input.outPoint.index);//4B
                int scriptLen = input.script == null ? 0 : input.script.bytes.length;
                bos.writeVarInt(scriptLen);
                if (scriptLen > 0) {
                    bos.write(input.script.bytes);
                }
                bos.writeInt32(input.sequence);
            }
            bos.writeVarInt(outputs.length);
            for (Output output : outputs) {
                bos.writeInt64(output.value);
                int scriptLen = output.script == null ? 0 : output.script.bytes.length;
                bos.writeVarInt(scriptLen);
                if (scriptLen > 0) {
                    bos.write(output.script.bytes);
                }
            }
            bos.writeInt32(lockTime);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bos.toByteArray();

    }


    public static class Input {
        public final OutPoint outPoint;
        public final Script script;
        public final int sequence;
        public byte[] signature;
        public byte[] publicKey;

        public Input(OutPoint outPoint, Script script, int sequence) {
            this.outPoint = outPoint;
            this.script = script;
            this.sequence = sequence;
        }

        public void setSignature(byte[] signature) {
            this.signature = signature;
        }

        public void setPublicKey(byte[] publicKey) {
            this.publicKey = publicKey;
        }


    }

    public static class Output {
        public final long value;
        public final Script script;

        public Output(long value, Script script) {
            LogUtil.e("output data:value=" + String.valueOf(value));
            this.value = value;
            this.script = script;
        }
    }

    public static class OutPoint {
        public final byte[] hash;
        public final int index;

        public OutPoint(byte[] hash, int index) {
            this.hash = hash;
            this.index = index;

//            LogUtil.i("第二次印出=" + hash.length + ";\nHexString=" + PublicPun.byte2HexString(hash));
        }
    }


    public static final class Script {

        public static final byte OP_FALSE = 0;
        public static final byte OP_TRUE = 0x51;
        public static final byte OP_PUSHDATA1 = 0x4c;
        public static final byte OP_PUSHDATA2 = 0x4d;
        public static final byte OP_PUSHDATA4 = 0x4e;
        public static final byte OP_DUP = 0x76;//Duplicates the top stack item.
        public static final byte OP_DROP = 0x75;
        public static final byte OP_HASH160 = (byte) 0xA9;//The input is hashed twice: first with SHA-256 and then with RIPEMD-160.
        public static final byte OP_VERIFY = 0x69;//Marks transaction as invalid if top stack value is not true. True is removed, but false is not.
        public static final byte OP_EQUAL = (byte) 0x87;//Returns 1 if the inputs are exactly equal, 0 otherwise.
        public static final byte OP_EQUALVERIFY = (byte) 0x88;//Same as OP_EQUAL, but runs OP_VERIFY afterward.
        public static final byte OP_CHECKSIG = (byte) 0xAC;//The entire transaction's outputs,
        // inputs, and script (from the most recently-executed OP_CODESEPARATOR to the end) are hashed.
        // The signature used by OP_CHECKSIG must be a valid signature for this hash and public key.
        // If it is, 1 is returned, 0 otherwise.
        public static final byte OP_CHECKSIGVERIFY = (byte) 0xAD;
        public static final byte OP_NOP = 0x61;
        public static final byte SIGHASH_ALL = 1;
        public final byte[] bytes;

        public Script(byte[] rawBytes) {
            bytes = rawBytes;
        }

        public Script(byte[] data1, byte[] data2) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(data1.length + data2.length + 2);
            try {
                writeBytes(data1, baos);
                writeBytes(data2, baos);
                baos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            bytes = baos.toByteArray();
        }

        private static void writeBytes(byte[] data, ByteArrayOutputStream baos) throws IOException {
            if (data.length < OP_PUSHDATA1) {
                baos.write(data.length);
            } else if (data.length < 0xff) {
                baos.write(OP_PUSHDATA1);
                baos.write(data.length);
            } else if (data.length < 0xffff) {
                baos.write(OP_PUSHDATA2);
                baos.write(data.length & 0xff);
                baos.write((data.length >> 8) & 0xff);
            } else {
                baos.write(OP_PUSHDATA4);
                baos.write(data.length & 0xff);
                baos.write((data.length >> 8) & 0xff);
                baos.write((data.length >> 16) & 0xff);
                baos.write((data.length >>> 24) & 0xff);
            }
            baos.write(data);
        }

        public static byte[] hashTransaction(int inputIndex, byte[] subscript, Transaction tx) {
            Input[] unsignedInputs = new Input[tx.inputs.length];
            for (int i = 0; i < tx.inputs.length; i++) {
                Input txInput = tx.inputs[i];
                if (i == inputIndex) {
                    unsignedInputs[i] = new Input(txInput.outPoint, new Script(subscript), txInput.sequence);
                } else {
                    unsignedInputs[i] = new Input(txInput.outPoint, new Script(new byte[0]), txInput.sequence);
                }
            }
            Transaction unsignedTransaction = new Transaction(unsignedInputs, tx.outputs, tx.lockTime);
            byte[] result = null;
            try {
                result = hashTransactionForSigning(unsignedTransaction);
            } catch (ValidationException e) {
                e.printStackTrace();
                LogUtil.e("hashTransaction error=" + e.getMessage());
            }
            return result;
        }

        public static byte[] rawTransactionForpublishing(Transaction unsignedTransaction) throws ValidationException {
            byte[] txUnsignedBytes = unsignedTransaction.getBitcoinTransaction();
            LogUtil.d("rawTx hex=" + ByteUtils.bytesToHex(txUnsignedBytes));
            BitcoinOutputStream baos = new BitcoinOutputStream();
            try {
                baos.write(txUnsignedBytes);
                baos.close();
            } catch (Exception e) {
                throw new ValidationException(e);
            }

            return baos.toByteArray();
        }

        public static byte[]
        hashTransactionForSigning(Transaction unsignedTransaction) throws ValidationException {
//            byte[] txUnsignedBytes = unsignedTransaction.getBitcoinOutputStreamBytes(); //
            byte[] txUnsignedBytes = unsignedTransaction.getBytes();
            LogUtil.d("hashTransactionForSigning hex=" + PublicPun.byte2HexStringNoBlank(txUnsignedBytes));
            BitcoinOutputStream baos = new BitcoinOutputStream();
            try {
                baos.write(txUnsignedBytes);
                baos.writeInt32(Transaction.Script.SIGHASH_ALL);
                baos.close();
            } catch (Exception e) {
                throw new ValidationException(e);
            }
            return BTCUtils.doubleSha256(baos.toByteArray());
        }

        public static boolean verifyFails(Stack<byte[]> stack) {
            byte[] input;
            boolean valid;
            input = stack.pop();
            if (input.length == 0 || (input.length == 1 && input[0] == OP_FALSE)) {
                //false
                stack.push(new byte[]{OP_FALSE});
                valid = false;
            } else {
                //true
                valid = true;
            }
            return !valid;
        }

        public static Script buildOutput(String address) throws ValidationException, NoSuchAlgorithmException, IOException {
            //noinspection TryWithIdenticalCatches
//            try {
            LogUtil.e("out address=" + address);
            byte[] addressWithCheckSumAndNetworkCode = BTCUtils.decodeBase58(address);
            LogUtil.e("addressWithCheckSumAndNetworkCode=" + PublicPun.byte2HexStringNoBlank(addressWithCheckSumAndNetworkCode));
//                if (addressWithCheckSumAndNetworkCode[0] != 0 ||  addressWithCheckSumAndNetworkCode[0]!=5) {
            if (addressWithCheckSumAndNetworkCode[0] != 0 && addressWithCheckSumAndNetworkCode[0] != 5) {
                LogUtil.e("Unknown address type: " + addressWithCheckSumAndNetworkCode[0] + ";addr=" + address);
                throw new ValidationException("Unknown address type " + address);
//                    return null;
            }

            if (!BTCUtils.ValidateBitcoinAddress(address)) {
                LogUtil.e("Unknown address type " + address);
                throw new ValidationException("Unknown address type " + address);
//                    return null;
            }
            byte[] bareAddress = new byte[20];
            System.arraycopy(addressWithCheckSumAndNetworkCode, 1, bareAddress, 0, bareAddress.length);
            LogUtil.d("addressWithCheckSumAndNetworkCode=" + PublicPun.byte2HexString(addressWithCheckSumAndNetworkCode) + "\n" + "bareAddress=" + PublicPun.byte2HexStringNoBlank(bareAddress));
            MessageDigest digestSha = MessageDigest.getInstance("SHA-256");
            digestSha.update(addressWithCheckSumAndNetworkCode, 0, addressWithCheckSumAndNetworkCode.length - 4);
            byte[] calculatedDigest = digestSha.digest(digestSha.digest());
//            LogUtil.d("calculatedDigest=" + PublicPun.byte2HexString(calculatedDigest));
            for (int i = 0; i < 4; i++) {
                if (calculatedDigest[i] != addressWithCheckSumAndNetworkCode[addressWithCheckSumAndNetworkCode.length - 4 + i]) {
//                        LogUtil.e("Bad address " + address);
                    throw new ValidationException("Bad address " + address);
                }
            }
            ByteArrayOutputStream buf;
            if (addressWithCheckSumAndNetworkCode[0] == 0) {//single address
                buf = new ByteArrayOutputStream(25);
                buf.write(OP_DUP);
                buf.write(OP_HASH160);
                writeBytes(bareAddress, buf);
                buf.write(OP_EQUALVERIFY);
                buf.write(OP_CHECKSIG);
                ScriptAdd = buf.toByteArray();
            } else {
                buf = new ByteArrayOutputStream(23);
//                    buf.write(OP_DUP);
                buf.write(OP_HASH160);
                writeBytes(bareAddress, buf);
                buf.write(OP_EQUAL);
//                    buf.write(OP_CHECKSIG);
                ScriptAdd = buf.toByteArray();
            }
            LogUtil.d("產生script的output地址=" + LogUtil.byte2HexString(ScriptAdd));
//            LogUtil.e("toBase58="+getAddressFromScript());
            return new Script(buf.toByteArray());
        }

        public void run(Stack<byte[]> stack) throws ScriptInvalidException {
            LogUtil.e("Script run");
            run(0, null, stack);
        }

        public void run(int inputIndex, Transaction tx, Stack<byte[]> stack) throws ScriptInvalidException {
            for (int pos = 0; pos < bytes.length; pos++) {
                LogUtil.e("trancsation run:tx=" + tx + " ; bytes[pos]=" + bytes[pos]);
                switch (bytes[pos]) {
                    case OP_NOP:
                        break;
                    case OP_DROP:
                        if (stack.isEmpty()) {
                            throw new IllegalArgumentException("stack empty on OP_DROP");
                        }
                        stack.pop();
                        break;
                    case OP_DUP:
                        if (stack.isEmpty()) {
                            throw new IllegalArgumentException("stack empty on OP_DUP");
                        }
                        stack.push(stack.peek());
                        break;
                    case OP_HASH160:
                        if (stack.isEmpty()) {
                            throw new IllegalArgumentException("stack empty on OP_HASH160");
                        }
                        stack.push(BTCUtils.sha256ripemd160(stack.pop()));
                        break;
                    case OP_EQUAL:
                    case OP_EQUALVERIFY:
                        if (stack.size() < 2) {
                            throw new IllegalArgumentException("not enough elements to perform OP_EQUAL");
                        }
                        stack.push(new byte[]{(byte) (Arrays.equals(stack.pop(), stack.pop()) ? 1 : 0)});
                        if (bytes[pos] == OP_EQUALVERIFY) {
                            if (verifyFails(stack)) {
                                throw new ScriptInvalidException("wrong address");
                            }
                        }
                        break;
                    case OP_VERIFY:
                        if (verifyFails(stack)) {
                            throw new ScriptInvalidException("wrong address");
                        }
                        break;
                    case OP_CHECKSIG:
                    case OP_CHECKSIGVERIFY:
                        byte[] publicKey = stack.pop();
                        byte[] signatureAndHashType = stack.pop();
                        if (signatureAndHashType[signatureAndHashType.length - 1] != SIGHASH_ALL) {
                            throw new IllegalArgumentException("I cannot check this sig type: " + signatureAndHashType[signatureAndHashType.length - 1]);
                        }
                        byte[] signature = new byte[signatureAndHashType.length - 1];
                        System.arraycopy(signatureAndHashType, 0, signature, 0, signature.length);
                        byte[] hash = hashTransaction(inputIndex, bytes, tx);
                        boolean valid = BTCUtils.verify(publicKey, signature, hash);
                        if (bytes[pos] == OP_CHECKSIG) {
                            stack.push(new byte[]{(byte) (valid ? 1 : 0)});
                        } else {
                            if (verifyFails(stack)) {
                                throw new ScriptInvalidException("bad signature");
                            }
                        }
                        break;
                    case OP_FALSE:
                        stack.push(new byte[]{0});
                        break;
                    case OP_TRUE:
                        stack.push(new byte[]{1});
                        break;
                    default:
                        if (bytes[pos] < OP_PUSHDATA1) {
                            byte[] data = new byte[bytes[pos]];
                            System.arraycopy(bytes, pos + 1, data, 0, bytes[pos]);
                            stack.push(data);
                            pos += data.length;
                        } else if (bytes[pos] == OP_PUSHDATA1) {
                            int len = bytes[pos + 1] & 0xff;
                            byte[] data = new byte[len];
                            System.arraycopy(bytes, pos + 1, data, 0, len);
                            stack.push(data);
                            pos += 1 + data.length;
                        } else {
                            throw new IllegalArgumentException("I cannot read this data: " + Integer.toHexString(bytes[pos]));
                        }
                        break;
                }
            }
        }

        @Override
        public String toString() {
            return BTCUtils.toHex(bytes);
        }

        @Override
        public boolean equals(Object o) {
            return this == o || !(o == null || getClass() != o.getClass()) && Arrays.equals(bytes, ((Script) o).bytes);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(bytes);
        }


        public static class ScriptInvalidException extends Exception {
            public ScriptInvalidException() {

            }

            public ScriptInvalidException(String s) {
                super(s);
            }
        }


    }
}
