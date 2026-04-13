package com.example.agentframework.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取 uploads 目录的绝对路径
        String uploadPath = System.getProperty("user.dir") + File.separator + "uploads";
        File uploadDir = new File(uploadPath);

        // 确保目录存在
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 映射 /files/** 路径到 uploads 目录（使用 file: 协议）
        String resourceLocation = "file:" + uploadPath + File.separator;

        System.out.println("🌐 静态资源映射: /files/** → " + uploadDir.getAbsolutePath());

        registry.addResourceHandler("/files/**")
                .addResourceLocations(resourceLocation);
    }
}
