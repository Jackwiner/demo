package com.example.demo.controller;

import com.example.demo.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * 上传文件
     * @param file 要上传的文件
     * @return 上传结果和文件访问URL
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "文件不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            String fileUrl = fileService.uploadFile(file);
            
            result.put("success", true);
            result.put("message", "文件上传成功");
            result.put("url", fileUrl);
            result.put("filename", file.getOriginalFilename());
            result.put("size", file.getSize());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "文件上传失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 获取文件预签名URL（用于临时访问）
     * @param objectName 对象名称
     * @param expiry 过期时间（小时，默认1小时）
     * @return 预签名URL
     */
    @GetMapping("/presigned-url")
    public ResponseEntity<Map<String, Object>> getPresignedUrl(
            @RequestParam("objectName") String objectName,
            @RequestParam(value = "expiry", defaultValue = "1") int expiry) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (!fileService.fileExists(objectName)) {
                result.put("success", false);
                result.put("message", "文件不存在");
                return ResponseEntity.status(404).body(result);
            }

            String presignedUrl = fileService.getPresignedUrl(objectName, expiry);
            
            result.put("success", true);
            result.put("presignedUrl", presignedUrl);
            result.put("expiry", expiry + "小时");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取预签名URL失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 下载文件
     * @param objectName 对象名称
     * @return 文件流
     */
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam("objectName") String objectName) {
        try {
            if (!fileService.fileExists(objectName)) {
                return ResponseEntity.notFound().build();
            }

            InputStream inputStream = fileService.getFileStream(objectName);
            InputStreamResource resource = new InputStreamResource(inputStream);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + objectName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 删除文件
     * @param objectName 对象名称
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteFile(@RequestParam("objectName") String objectName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (!fileService.fileExists(objectName)) {
                result.put("success", false);
                result.put("message", "文件不存在");
                return ResponseEntity.status(404).body(result);
            }

            fileService.deleteFile(objectName);
            
            result.put("success", true);
            result.put("message", "文件删除成功");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "文件删除失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 检查文件是否存在
     * @param objectName 对象名称
     * @return 文件存在状态
     */
    @GetMapping("/exists")
    public ResponseEntity<Map<String, Object>> fileExists(@RequestParam("objectName") String objectName) {
        Map<String, Object> result = new HashMap<>();
        
        boolean exists = fileService.fileExists(objectName);
        
        result.put("exists", exists);
        result.put("objectName", objectName);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取文件直接访问URL
     * @param objectName 对象名称
     * @return 文件URL
     */
    @GetMapping("/url")
    public ResponseEntity<Map<String, Object>> getFileUrl(@RequestParam("objectName") String objectName) {
        Map<String, Object> result = new HashMap<>();
        
        if (!fileService.fileExists(objectName)) {
            result.put("success", false);
            result.put("message", "文件不存在");
            return ResponseEntity.status(404).body(result);
        }

        String fileUrl = fileService.getFileUrl(objectName);
        
        result.put("success", true);
        result.put("url", fileUrl);
        result.put("objectName", objectName);
        
        return ResponseEntity.ok(result);
    }
}
