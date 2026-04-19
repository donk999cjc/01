package com.example.agentframework.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    @Value("${file.upload.path:D:/uploads/}")
    private String uploadPath;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(MultipartHttpServletRequest request) {
        MultipartFile file = request.getFileMap().values().stream()
                .findFirst()
                .orElse(null);

        Map<String, String> result = new HashMap<>();
        if (file == null || file.isEmpty()) {
            result.put("error", "文件不能为空");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            // 关键：强制创建目录，校验是否成功
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    throw new IOException("目录创建失败：" + uploadPath);
                }
            }

            String suffix = "";
            if (file.getOriginalFilename().contains(".")) {
                suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            }
            String fileName = UUID.randomUUID() + suffix;
            File dest = new File(uploadPath + fileName);
            file.transferTo(dest);

            result.put("url", "/files/" + fileName);
            result.put("name", file.getOriginalFilename());
            return ResponseEntity.ok(result);

        } catch (IOException e) {
            e.printStackTrace();
            result.put("error", "文件保存失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}