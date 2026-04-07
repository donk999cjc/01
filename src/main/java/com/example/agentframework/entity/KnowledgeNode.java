package com.example.agentframework.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeNode {
    private Long id;
    private String nodeId;
    private String name;
    private String courseId;
    private String parentId;
    private String description;
    private Integer difficulty;
    private Date createdAt;
}
