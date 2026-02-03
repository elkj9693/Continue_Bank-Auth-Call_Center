package com.callcenter.callcenterwas.infrastructure.s3;

import com.callcenter.callcenterwas.domain.log.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;
    private final LogService logService;

    @Value("${cloud.aws.s3.bucket:continue-bank-recordings}")
    private String bucketName;

    /**
     * Upload call recording to S3
     * 
     * @return S3 Key (path)
     */
    public String uploadRecording(String customerRef, String campaignId, byte[] audioData) {
        String key = String.format("recordings/%s/%s/%s.mp3",
                campaignId,
                customerRef,
                UUID.randomUUID().toString());

        try {
            PutObjectRequest putOb = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("audio/mpeg")
                    .build();

            s3Client.putObject(putOb, RequestBody.fromBytes(audioData));

            log.info("[S3] Uploaded recording: {}", key);
            logService.logIntegration("N/A", "S3 Upload", "PUT", 200, "Uploaded recording: " + key);

            return key;
        } catch (Exception e) {
            log.error("[S3] Upload failed", e);
            String errorMsg = "Upload failed: " + e.getMessage();
            if (errorMsg.length() > 200)
                errorMsg = errorMsg.substring(0, 200) + "...";

            logService.logIntegration("N/A", "S3 Upload", "PUT", 500, errorMsg);
            throw new RuntimeException("S3 Upload Failed", e);
        }
    }
}
