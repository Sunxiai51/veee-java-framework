package com.sunveee.framework.common.utils.test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.sunveee.framework.common.utils.compress.ArchiveUtils;

public class ArchiveUtilsTest {

    public static void main(String[] args) throws IOException {
        ArchiveUtils.tar(Arrays.asList(new File("D:\\Desktop")), "E:\\ddd.tar");
    }
}
