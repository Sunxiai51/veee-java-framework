/**
 * Project: baas-api-gateway
 * <p>
 * File Created at 2019-10-22 20:27
 * <p>
 * Copyright 2020 qianhai HYLY Corporation Limited.
 * All rights reserved.
 */

package com.sunveee.framework.common.utils.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;

public class FileUtils {

    /**
     * 目录或文件是否存在
     * @param parh
     * @return
     */
    public static boolean exist(String parh) {
        return Files.exists(Paths.get(parh));
    }

    /**
     * 创建目录
     * @param parh
     * @return
     */
    public static Path createDirectory(String parh) throws IOException {
        Path p = Paths.get(parh);
        // 创建路径，能创建不存在的中间部件
        Files.createDirectories(p);
        return p;
    }

    /**
     * 创建文件
     * @param fileParh
     * @return
     */
    public static Path createFileIfNotExist(String fileParh) throws IOException {
        Path p = Paths.get(fileParh);
        createDirectory(p.getParent().toString());
        if(!Files.exists(p)){
            // 创建文件
            Files.createFile(p);
        }
        return p;
    }

    /**
     * 删除文件
     * @param absPath
     * @return
     */
    public static void deleteFileOrDirectory(String absPath) throws IOException {
        File localDir = new File(absPath);
        // 确保存在空的project 文件夹
        if (localDir.exists()) {
            // 清空文件夹
            // Files.walk - return all files/directories below rootPath including
            // .sorted - sort the list in reverse order, so the directory itself comes after the including
            // subdirectories and files
            // .map - map the Path to File
            // .peek - is there only to show which entry is processed
            // .forEach - calls the .delete() method on every File object
            Files.walk(Paths.get(absPath)).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    /**
     * 移动文件(覆盖目标文件)
     * @param fromPath
     * @param toFilePath
     * @return
     */
    public static Path moveFile(String fromPath, String toFilePath) throws IOException {
        Path pSrc = Paths.get(fromPath);
        Path pDest = Paths.get(toFilePath);
        createDirectory(pDest.getParent().toString());
        return Files.move(pSrc, pDest, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 复制文件(覆盖目标文件)
     * @param fromPath
     * @param toFilePath
     * @return
     */
    public static Path copyFile(String fromPath, String toFilePath) throws IOException {
        Path pSrc = Paths.get(fromPath);
        Path pDest = Paths.get(toFilePath);
        createDirectory(pDest.getParent().toString());
        return Files.copy(pSrc, pDest, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 从文件读取数据
     * @param path
     * @return
     */
    public static byte[] readFromFile(String path) throws IOException {
        Path p = Paths.get(path);
        return Files.readAllBytes(p);
    }

    /**
     * 向文件写入数据,根据StandardOpenOption决定打开文件策略
     * @param filePath
     * @param content
     * @param options
     * @return
     */
    public static Path write2File(String filePath, String content, StandardOpenOption... options) throws IOException {
        // 获得路径
        Path p = createFileIfNotExist(filePath);
        // 向文件中写入信息
        return Files.write(p, content.getBytes("utf8"), options);
    }

    /**
     * 复制文件(覆盖目标文件)
     * @param filePath
     * @return
     */
    public static String hash256FileToHex(String filePath) throws IOException {
        byte[] byteHash = hash256File(filePath);
        return hexEncode(byteHash);
    }

    /**
     * 大文件计算方法
     * @param filePath
     * @return
     * @throws IOException
     */
    public static byte [] hash256File(String filePath) throws IOException {
        File file = new File(filePath);
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ignored) {
        }
        int bufferSize = 16384;
        byte [] buffer = new byte[bufferSize];
        int sizeRead;
        while ((sizeRead = in.read(buffer)) != -1) {
            digest.update(buffer, 0, sizeRead);
        }
        in.close();
        return digest.digest();
    }

    private static String hexEncode(byte buf[]) {
        StringBuilder sb = new StringBuilder();
        for (byte b : buf) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) hex = '0' + hex;
            sb.append(hex.toLowerCase());
        }
        return sb.toString();
    }
}