package com.shahrokhi.springbootawssdk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class AwsConfig {

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.endpoint}")
    private String s3Endpoint;

    @Value("${aws.accessKey}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .endpointOverride(URI.create(s3Endpoint))
                .build();
    }
}
