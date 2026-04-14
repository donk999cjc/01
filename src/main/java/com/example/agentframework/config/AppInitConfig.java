package com.example.agentframework.config;

import com.example.agentframework.workflow.WorkflowEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppInitConfig {

    @Autowired
    private WorkflowEngine workflowEngine;

    @Bean
    public CommandLineRunner initWorkflowEngine() {
        return args -> {
            workflowEngine.init();
            System.out.println("Workflow engine initialized with edu-workflow, grading-workflow, analysis-workflow");
        };
    }
}
