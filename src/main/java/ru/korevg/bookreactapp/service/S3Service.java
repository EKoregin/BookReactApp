package ru.korevg.bookreactapp.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * Uploading file to Minio
     * @param fileName
     * @param file
     * @return key
     */
    public String uploadFile(String fileName, MultipartFile file) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType());
        objectMetadata.setContentLength(file.getSize());
        String key = String.format("%d-%s", System.currentTimeMillis(), fileName);
        try {
            PutObjectRequest putObjectRequest =
                    new PutObjectRequest(bucketName, key, new ByteArrayInputStream(file.getBytes()), objectMetadata);
            s3Client.putObject(putObjectRequest);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return key;
    }

    /**
     * Downloading file from Minio
     * @param key
     * @return byte[]
     */
    public byte[] downloadFile(String key) {
        log.info("Downloading file {} from Minio", key);
        GetObjectRequest request = new GetObjectRequest(bucketName, key);
        try {
            return s3Client.getObject(request).getObjectContent().readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("File not found in MinIO: " + key, e);
        }

    }

    /**
     * Deleting file in Minio
     * @param key
     */
    public void deleteFile(final String key) {
        s3Client.deleteObject(bucketName, key);
    }
}
