package com.adms.australianmobileadtoolkit.interpreter.platform;

import java.io.File;
import java.util.Objects;

public class S3Uploader {
    // Placeholder implementation - methods disabled to fix compilation
    public static void uploadFileToS3(Object s3, String bucketName, String key, File file) {
        System.out.println("S3 upload placeholder - file: " + key);
        // TODO: Implement proper S3 upload when AWS dependencies are properly configured
    }

    public static void uploadFolderToS3(Object s3, String bucketName, String folderPath, String s3FolderKey) {
    public static void uploadFolderToS3(Object s3, String bucketName, String folderPath, String s3FolderKey) {
        File folder = new File(folderPath);

        // Validate the folder path
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Invalid folder path: " + folderPath);
            return;
        }

        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isFile()) {
                String key = s3FolderKey + "/" + file.getName();
                uploadFileToS3(s3, bucketName, key, file);
            }
        }
    }
}

