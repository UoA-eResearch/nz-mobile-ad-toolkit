package com.adms.australianmobileadtoolkit.interpreter.platform;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.util.Objects;

public class S3Uploader {
    public static void uploadFileToS3(S3Client s3, String bucketName, String key, File file) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3.putObject(putRequest, file.toPath());
            System.out.println("File uploaded successfully: " + key);
        } catch (Exception e) {
            System.err.println("Error uploading file: " + e.getMessage());
        }
    }

    public static void uploadFolderToS3(S3Client s3, String bucketName, String folderPath, String s3FolderKey) {
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

