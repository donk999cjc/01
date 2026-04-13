package com.example.agentframework.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    // 使用项目根目录下的 uploads 文件夹
    private final String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
    private final String submissionDir = "submissions";

    public FileStorageService() {
        // 确保目录存在
        createDirectoryIfNotExists(uploadDir);
        createDirectoryIfNotExists(uploadDir + File.separator + submissionDir);

        // 打印实际路径，方便调试
        System.out.println("📁 文件存储根目录: " + new File(uploadDir).getAbsolutePath());
    }

    private void createDirectoryIfNotExists(String dirPath) {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                System.out.println("✅ 创建目录: " + path.toAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException("无法创建目录: " + dirPath, e);
            }
        }
    }

    /**
     * 保存学生提交的作业图片
     */
    public String saveSubmissionImage(MultipartFile file, String studentId, String assignmentId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 生成唯一文件名：studentId_assignmentId_uuid.原扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = studentId + "_" + assignmentId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

        // 构建完整路径（使用绝对路径）
        String relativePath = submissionDir + File.separator + fileName;
        String fullPath = uploadDir + File.separator + relativePath;

        // 保存文件
        Path destination = Paths.get(fullPath);
        file.transferTo(destination.toFile());

        System.out.println("💾 文件已保存: " + destination.toAbsolutePath());
        System.out.println("   文件大小: " + (file.getSize() / 1024) + " KB");

        // 返回相对路径（用于数据库存储）
        return relativePath.replace(File.separator, "/");
    }

    /**
     * 获取文件的完整URL路径（供前端使用）
     */
    public String getFileUrl(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        return "/files/" + filePath;
    }

    /**
     * 获取文件的绝对路径（供后端处理）
     */
    public String getAbsolutePath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        // 确保使用正确的分隔符
        String normalizedPath = filePath.replace("/", File.separator);
        return uploadDir + File.separator + normalizedPath;
    }

    /**
     * 检查文件是否存在
     */
    public boolean fileExists(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        Path path = Paths.get(getAbsolutePath(filePath));
        boolean exists = Files.exists(path);
        System.out.println("🔍 检查文件存在: " + path.toAbsolutePath() + " → " + exists);
        return exists;
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(getAbsolutePath(filePath));
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                System.out.println("🗑️ 文件已删除: " + path.toAbsolutePath());
            }
            return deleted;
        } catch (IOException e) {
            System.err.println("删除文件失败: " + e.getMessage());
            return false;
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg"; // 默认扩展名
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    /**
     * 获取上传根目录的绝对路径
     */
    public String getUploadDir() {
        return uploadDir;
    }

    /**
     * 获取上传根目录的File对象
     */
    public File getUploadDirFile() {
        return new File(uploadDir);
    }
}
