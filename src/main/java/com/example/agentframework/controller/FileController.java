package com.example.agentframework.controller;

import com.example.agentframework.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
@CrossOrigin
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 通用文件访问接口（支持图片、PDF、文档等）
     * 用途：学生端查看教师上传的作业附件
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String filename,
            HttpServletRequest request) {
        try {
            // 构建文件路径
            Path filePath = Paths.get(fileStorageService.getUploadDir()).resolve(filename).normalize();

            // 安全检查：防止路径遍历攻击
            if (!filePath.startsWith(Paths.get(fileStorageService.getUploadDir()))) {
                return ResponseEntity.badRequest().build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // 确定内容类型
                String contentType = null;
                try {
                    contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
                } catch (IOException ex) {
                    // 使用默认类型
                }

                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                System.err.println("❌ 文件不存在或无法读取: " + filePath);
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 文件下载接口（强制下载，不在线预览）
     */
    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> forceDownloadFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(fileStorageService.getUploadDir()).resolve(filename).normalize();

            if (!filePath.startsWith(Paths.get(fileStorageService.getUploadDir()))) {
                return ResponseEntity.badRequest().build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 检查文件是否存在
     */
    @GetMapping("/exists/{filename:.+}")
    public ResponseEntity<Boolean> checkFileExists(@PathVariable String filename) {
        boolean exists = fileStorageService.fileExists(filename);
        return ResponseEntity.ok(exists);
    }

    /**
     * 获取文件信息（大小、类型等）
     */
    @GetMapping("/info/{filename:.+}")
    public ResponseEntity<?> getFileInfo(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(fileStorageService.getUploadDir())
                    .resolve("submissions")
                    .resolve(filename)
                    .normalize();

            java.io.File file = filePath.toFile();
            if (file.exists()) {
                java.util.Map<String, Object> info = new java.util.HashMap<>();
                info.put("name", file.getName());
                info.put("size", file.length());
                info.put("sizeFormatted", formatFileSize(file.length()));
                info.put("lastModified", new java.util.Date(file.lastModified()));
                info.put("exists", true);
                return ResponseEntity.ok(info);
            } else {
                // 尝试根目录
                filePath = Paths.get(fileStorageService.getUploadDir()).resolve(filename).normalize();
                file = filePath.toFile();
                if (file.exists()) {
                    java.util.Map<String, Object> info = new java.util.HashMap<>();
                    info.put("name", file.getName());
                    info.put("size", file.length());
                    info.put("sizeFormatted", formatFileSize(file.length()));
                    info.put("lastModified", new java.util.Date(file.lastModified()));
                    info.put("exists", true);
                    return ResponseEntity.ok(info);
                }
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        return String.format("%.1f MB", size / (1024.0 * 1024));
    }
}
