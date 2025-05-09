package com.example.gradlecoding.domain.nft.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;


@RequiredArgsConstructor
@Service
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile multipartFile, String folder) throws IOException {
        String fileName = folder + "/" + UUID.randomUUID() + "-" + multipartFile.getOriginalFilename();
        long contentLength = multipartFile.getSize();
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .contentType(multipartFile.getContentType())
            .build();


        s3Client.putObject(
            putObjectRequest,

            RequestBody.fromInputStream(
                multipartFile.getInputStream(),
                multipartFile.getSize()
            )
        );

        return "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
    }

    public String uploadJson(Map<String, Object> data, String folder) throws IOException {
        String fileName = folder + "/" + UUID.randomUUID() + ".json";
        String json = new ObjectMapper().writeValueAsString(data);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .contentType("application/json")
            .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromString(json));

        return "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
    }
}

