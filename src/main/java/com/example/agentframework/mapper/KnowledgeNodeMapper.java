package com.example.agentframework.mapper;

import com.example.agentframework.entity.KnowledgeNode;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface KnowledgeNodeMapper {
    
    @Select("SELECT * FROM knowledge_node WHERE id = #{id}")
    KnowledgeNode findById(Long id);
    
    @Select("SELECT * FROM knowledge_node WHERE node_id = #{nodeId}")
    KnowledgeNode findByNodeId(String nodeId);
    
    @Select("SELECT * FROM knowledge_node WHERE course_id = #{courseId}")
    List<KnowledgeNode> findByCourseId(String courseId);
    
    @Select("SELECT * FROM knowledge_node WHERE parent_id = #{parentId}")
    List<KnowledgeNode> findByParentId(String parentId);
    
    @Select("SELECT * FROM knowledge_node")
    List<KnowledgeNode> findAll();
    
    @Insert("INSERT INTO knowledge_node(node_id, name, course_id, parent_id, description, difficulty, created_at) " +
            "VALUES(#{nodeId}, #{name}, #{courseId}, #{parentId}, #{description}, #{difficulty}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(KnowledgeNode node);
    
    @Update("UPDATE knowledge_node SET name=#{name}, course_id=#{courseId}, parent_id=#{parentId}, description=#{description}, difficulty=#{difficulty} WHERE id=#{id}")
    int update(KnowledgeNode node);
    
    @Delete("DELETE FROM knowledge_node WHERE id = #{id}")
    int deleteById(Long id);
}
