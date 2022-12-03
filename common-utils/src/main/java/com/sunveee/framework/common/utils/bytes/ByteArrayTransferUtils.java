package com.sunveee.framework.common.utils.bytes;

import java.nio.charset.Charset;
import java.util.Base64;

public class ByteArrayTransferUtils {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public static String toStr(byte[] bytes) {
        return new String(bytes, UTF_8);
    }

    public static byte[] fromStr(String str) {
        return str.getBytes(UTF_8);
    }

    public static String toBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] fromBase64(String str) {
        return Base64.getDecoder().decode(str);
    }

    public static byte[] fromInt(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    public static int toInt(byte[] bytes) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (3 - i) * 8;
            value += (bytes[i] & 0xFF) << shift;
        }
        return value;
    }

}
