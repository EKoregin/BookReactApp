package ru.korevg.bookreactapp.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
     *
     * @param fileName file name
     * @param bytes file bytes
     * @param contentType content type
     * @return key
     */
    public Mono<String> uploadFile(String fileName, byte[] bytes, String contentType) {
        return Mono.fromCallable(() -> {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(contentType);
            objectMetadata.setContentLength(bytes.length);
            String key = String.format("%d-%s", System.currentTimeMillis(), fileName);
            PutObjectRequest putObjectRequest =
                    new PutObjectRequest(bucketName, key, new ByteArrayInputStream(bytes), objectMetadata);
            s3Client.putObject(putObjectRequest);
            return key;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Downloading file from Minio
     *
     * @param key fileName in Minio
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
     *
     * @param key fileName in Minio
     */
    public void deleteFile(final String key) {
        s3Client.deleteObject(bucketName, key);
    }
}
