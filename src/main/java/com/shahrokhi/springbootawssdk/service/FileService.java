package com.shahrokhi.springbootawssdk.service;

import com.shahrokhi.springbootawssdk.model.BaseResponse;
import com.shahrokhi.springbootawssdk.model.StringResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;

@Service
public class FileService {
    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.endpoint}")
    private String s3Endpoint;

    private final S3Client s3Client;

    @Autowired
    public FileService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public StringResponse uploadFile(MultipartFile file) {
        StringResponse output = new StringResponse();
        String key = generateKey(file.getOriginalFilename());
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try {
            PutObjectResponse response = s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            if (response != null && response.sdkHttpResponse().isSuccessful()) {
                output.setResult(key);
                output.commonSetSuccess();
            } else {
                output.commonSetInternalError("Failed to upload file", "فایل آپلود نشد");
            }
        } catch (Exception e) {
            e.printStackTrace();
            output.commonSetInternalError("Failed to upload file", "فایل آپلود نشد");
        }
        return output;
    }

    public ResponseEntity<Resource> downloadFile(String key) {
        if(fileExist(key)) {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            try (ResponseInputStream<GetObjectResponse> responseInputStream = s3Client.getObject(getObjectRequest)) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = responseInputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }

                return ResponseEntity.ok()
                        .headers(getHeaderFile(key))
                        .contentLength(responseInputStream.response().contentLength())
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(new ByteArrayResource(byteArrayOutputStream.toByteArray()));
            } catch (Exception e) {
                System.out.println("Can't get the file content.");
                return null;
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public BaseResponse deleteFile(String key) {
        BaseResponse output = new BaseResponse();
        if(fileExist(key)) {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            try {
                DeleteObjectResponse response = s3Client.deleteObject(deleteObjectRequest);
                if(response != null && response.sdkHttpResponse().isSuccessful()) {
                    output.commonSetSuccess();
                }
            } catch (Exception e) {
                e.printStackTrace();
                output.commonSetInternalError("Failed to delete file", "فایل حذف نشد");
            }
        } else {
            output.commonSetNotFound("The file doesn't exist", "این فایل وجود ندارد");
        }
        return output;
    }

    public StringResponse getUrl(String key) {
        StringResponse output = new StringResponse();
        if(fileExist(key)) {
            output.setResult(s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(key)).toString());
            output.commonSetSuccess();
        } else {
            output.commonSetNotFound("The file doesn't exist", "این فایل وجود ندارد");
        }
        return output;
    }

    private HttpHeaders getHeaderFile(String key) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" +
                encodeFileName(decodeFileName(key)));
        return headers;
    }

    private String generateKey(String fileName) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return timestamp + "_" + fileName;
    }

    private String decodeFileName(String key) {
        try {
            return URLDecoder.decode(key, "UTF-8");
        } catch (Exception e) {
            System.out.println("Can't decode file name");
            return "undefined";
        }
    }

    private String encodeFileName(String fileName) {
        try {
            return URLEncoder.encode(fileName, "UTF-8");
        } catch (Exception e) {
            System.out.println("Can't encode file name");
            return "undefined";
        }
    }

    private boolean fileExist(String key) {
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try {
            HeadObjectResponse headObjectResponse = s3Client.headObject(headObjectRequest);
            return headObjectResponse != null;
        } catch (Exception e) {
            return false;
        }
    }
}
