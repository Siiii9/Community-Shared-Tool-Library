package com.example.Community_Shared_Tool_java.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class ImageUploadController {
    
    // 图片存储目录
    private static final String UPLOAD_DIR = "uploads/images/";
    
    // 允许的图片类型
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};
    
    // 最大文件大小（5MB）
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("image") MultipartFile file) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                response.put("error", "文件不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 检查文件大小
            if (file.getSize() > MAX_FILE_SIZE) {
                response.put("error", "文件大小不能超过5MB");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 检查文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !isValidImageFile(originalFilename)) {
                response.put("error", "不支持的文件类型，请上传图片文件");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 创建上传目录
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // 生成唯一文件名
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;
            
            // 保存文件
            Path filePath = Paths.get(UPLOAD_DIR + uniqueFileName);
            Files.copy(file.getInputStream(), filePath);
            
            // 返回文件访问URL
            String imageUrl = "/uploads/images/" + uniqueFileName;
            response.put("imageUrl", imageUrl);
            response.put("message", "图片上传成功");
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            response.put("error", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 检查是否为有效的图片文件
     */
    private boolean isValidImageFile(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equals(extension)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }
    
    /**
     * 删除图片文件
     */
    @DeleteMapping("/image")
    public ResponseEntity<Map<String, String>> deleteImage(@RequestParam String imageUrl) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // 从URL中提取文件名
            String filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            Path filePath = Paths.get(UPLOAD_DIR + filename);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                response.put("message", "图片删除成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "文件不存在");
                return ResponseEntity.notFound().build();
            }
            
        } catch (IOException e) {
            response.put("error", "文件删除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}