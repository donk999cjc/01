package com.example.agentframework.mapper;

import com.example.agentframework.entity.Agent;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AgentMapper {
    
    @Select("SELECT * FROM agent WHERE id = #{id}")
    Agent findById(Long id);
    
    @Select("SELECT * FROM agent WHERE course_id = #{courseId}")
    List<Agent> findByCourseId(String courseId);
    
    @Select("SELECT * FROM agent")
    List<Agent> findAll();
    
    @Insert("INSERT INTO agent(name, course_id, description, config, avatar, created_at) VALUES(#{name}, #{courseId}, #{description}, #{config}, #{avatar}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Agent agent);
    
    @Update("UPDATE agent SET name=#{name}, course_id=#{courseId}, description=#{description}, config=#{config}, avatar=#{avatar} WHERE id=#{id}")
    int update(Agent agent);
    
    @Delete("DELETE FROM agent WHERE id = #{id}")
    int deleteById(Long id);

}
