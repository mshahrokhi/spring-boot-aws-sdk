package com.shahrokhi.springbootawssdk.controller;

import com.shahrokhi.springbootawssdk.model.BaseResponse;
import com.shahrokhi.springbootawssdk.model.StringResponse;
import com.shahrokhi.springbootawssdk.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class FileController {
    
    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<StringResponse> upload(@RequestPart(value = "file") MultipartFile file) {
        return updateStatusCode(fileService.uploadFile(file));
    }

    @GetMapping("/download/{key}")
    public ResponseEntity<Resource> download(@PathVariable String key) {
        return fileService.downloadFile(key);
    }


    @GetMapping("/link/{key}")
    public ResponseEntity<StringResponse> getUrl(@PathVariable String key) {
        return updateStatusCode(fileService.getUrl(key));
    }

    @GetMapping("/delete/{key}")
    public ResponseEntity<BaseResponse> delete(@PathVariable String key) {
        return updateStatusCode(fileService.deleteFile(key));
    }

    private <T extends BaseResponse> ResponseEntity<T> updateStatusCode(T response) {
        HttpStatus httpStatus = HttpStatus.valueOf(response.getStatus());
        return new ResponseEntity<>(response, httpStatus);
    }

}
