package com.sunveee.framework.common.utils.test;

import java.io.IOException;

import com.sunveee.framework.common.utils.bytes.ByteArrayTransferUtils;

public class ByteArrayTransferUtilsTest {

    public static void main(String[] args) throws IOException {
        byte[] b = ByteArrayTransferUtils.fromStr("abc");
        String base64 = ByteArrayTransferUtils.toBase64(b);
        System.out.println("base64=" + base64);
        System.out.println(ByteArrayTransferUtils.toStr(ByteArrayTransferUtils.fromBase64(base64)));
    }
}
