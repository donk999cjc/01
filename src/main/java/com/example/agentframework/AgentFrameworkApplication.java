package com.example.agentframework;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.agentframework.mapper")
public class AgentFrameworkApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgentFrameworkApplication.class, args);
    }
}
