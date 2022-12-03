package com.sunveee.framework.common.utils.bytes;

public class ByteArrayOperateUtils {

    public static byte[] unionByteArray(byte[] first, byte[] second) {
        byte[] result = new byte[4 + first.length + second.length];
        System.arraycopy(ByteArrayTransferUtils.fromInt(first.length), 0, result, 0, 4);
        System.arraycopy(first, 0, result, 4, first.length);
        System.arraycopy(second, 0, result, 4 + first.length, second.length);
        return result;
    }

    public static byte[][] splitUnionByteArray(byte[] unionByteArray) {
        // 解析长度
        byte[] head = new byte[4];
        System.arraycopy(unionByteArray, 0, head, 0, 4);
        int firstLength = ByteArrayTransferUtils.toInt(head);
        int secondLength = unionByteArray.length - 4 - firstLength;

        // arraycopy
        byte[] first = new byte[firstLength];
        byte[] second = new byte[secondLength];
        System.arraycopy(unionByteArray, 4, first, 0, firstLength);
        System.arraycopy(unionByteArray, 4 + firstLength, second, 0, secondLength);

        // 返回
        byte[][] result = new byte[2][];
        result[0] = first;
        result[1] = second;
        return result;
    }

}
