package com.example.demo.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class FileService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.url-expiry}")
    private int urlExpiry;

    /**
     * 上传文件
     * @param file 要上传的文件
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 生成唯一的文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectName = UUID.randomUUID().toString() + extension;

        try (InputStream inputStream = file.getInputStream()) {
            // 上传文件到MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 返回文件访问URL
            return getFileUrl(objectName);
        }
    }

    /**
     * 获取文件访问URL（带权限参数的预签名URL）
     * @param objectName 对象名称
     * @return 文件访问URL
     */
    public String getFileUrl(String objectName) {
        try {
            return getPresignedUrl(objectName, urlExpiry);
        } catch (Exception e) {
            // 如果生成预签名URL失败，记录错误并返回直接访问URL作为备选
            System.err.println("生成预签名URL失败：" + e.getMessage());
            return endpoint + "/" + bucketName + "/" + objectName;
        }
    }

    /**
     * 获取文件预签名URL（用于临时访问）
     * @param objectName 对象名称
     * @param expiry 过期时间（单位：小时）
     * @return 预签名URL
     */
    public String getPresignedUrl(String objectName, int expiry) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(expiry, TimeUnit.HOURS)
                        .build()
        );
    }

    /**
     * 删除文件
     * @param objectName 对象名称
     */
    public void deleteFile(String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    /**
     * 检查文件是否存在
     * @param objectName 对象名称
     * @return 是否存在
     */
    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取文件流
     * @param objectName 对象名称
     * @return 文件输入流
     */
    public InputStream getFileStream(String objectName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }
}
