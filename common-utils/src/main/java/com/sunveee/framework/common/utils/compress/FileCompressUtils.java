package com.sunveee.framework.common.utils.compress;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.Deflater;

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.apache.commons.compress.utils.IOUtils;

/**
 * 
 * FileCompressUtils.java
 *
 * @author  SunVeee
 * @version 2021-10-14 16:09:46
 */
public class FileCompressUtils {

    private static final LocalDateTime DEFAULT_GZIP_MODIFICATION_TIME = LocalDateTime.parse("2021-12-02 15:28:28", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    /**
     * gzip压缩
     * 
     * @param  srcFile        源文件（必须是文件）
     * @param  targetFilePath 目标文件路径（必须是文件，且不存在于文件系统）
     * @throws IOException
     */
    public static void gzip(File srcFile, String targetFilePath) throws IOException {
        if (!srcFile.isFile()) {
            throw new IllegalArgumentException("srcFile must be a normal file");
        }

        File targetFile = new File(targetFilePath);
        targetFile.createNewFile();
        if (!targetFile.isFile()) {
            throw new IllegalArgumentException("targetFilePath must be a normal file");
        }

        GzipParameters parameters = new GzipParameters();
        parameters.setCompressionLevel(Deflater.DEFAULT_COMPRESSION);
        // 为了确保文件内容一致时压缩文件hash值一致，指定固定的时间作为文件修改时间
        parameters.setModificationTime(DEFAULT_GZIP_MODIFICATION_TIME.toEpochSecond(ZoneOffset.UTC) * 1000);

        try (FileOutputStream fos = new FileOutputStream(targetFile);
                GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(fos, parameters);
                InputStream is = new FileInputStream(srcFile)) {
            IOUtils.copy(is, gzos);
        }
    }

    public static void tarAndGzip(List<File> srcFiles, String targetFilePath) throws IOException {
        final String tempTarFilePath = srcFiles.get(0).getParent() + "tar_" + System.currentTimeMillis();
        File tempTarFile = new File(tempTarFilePath);
        ArchiveUtils.tar(srcFiles, tempTarFilePath);
        gzip(tempTarFile, targetFilePath);
        tempTarFile.delete();
    }
}
