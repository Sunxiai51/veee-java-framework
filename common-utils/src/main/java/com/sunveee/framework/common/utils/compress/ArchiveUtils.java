package com.sunveee.framework.common.utils.compress;

import java.io.*;
import java.util.List;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

/**
 * 
 * ArchiveUtils.java
 *
 * @author  SunVeee
 * @version 2021-10-14 16:23:12
 */
public class ArchiveUtils {

    /**
     * tar归档
     * 
     * @param  srcFiles       源路径（可以是文件或目录）
     * @param  targetFilePath 目标文件路径（必须是文件）
     * @throws IOException
     */
    public static void tar(List<File> srcFiles, String targetFilePath) throws IOException {
        File targetFile = new File(targetFilePath);
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(new FileOutputStream(targetFile))) {
            for (File src : srcFiles) {
                tarRecursive(tos, src, "");
            }
        }
    }

    /**
     * 递归归档
     * 
     * @param  tos
     * @param  srcFile
     * @param  basePath
     * @throws IOException
     */
    private static void tarRecursive(TarArchiveOutputStream tos, File srcFile, String basePath) throws IOException {
        if (srcFile.isDirectory()) {
            File[] files = srcFile.listFiles();
            String nextBasePath = basePath + srcFile.getName() + File.separator;
            if (ArrayUtils.isEmpty(files)) {
                // 空目录
                TarArchiveEntry entry = new TarArchiveEntry(srcFile, nextBasePath);
                tos.putArchiveEntry(entry);
                tos.closeArchiveEntry();
            } else {
                for (File file : files) {
                    tarRecursive(tos, file, nextBasePath);
                }
            }
        } else {
            TarArchiveEntry entry = new TarArchiveEntry(srcFile, basePath + srcFile.getName());
            entry.setSize(srcFile.length());
            tos.putArchiveEntry(entry);
            FileUtils.copyFile(srcFile, tos);
            tos.closeArchiveEntry();
        }
    }

}
