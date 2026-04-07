package com.example.agentframework.mapper;

import com.example.agentframework.entity.Assignment;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AssignmentMapper {
    
    @Select("SELECT * FROM assignment WHERE id = #{id}")
    Assignment findById(Long id);
    
    @Select("SELECT * FROM assignment WHERE assignment_id = #{assignmentId}")
    Assignment findByAssignmentId(String assignmentId);
    
    @Select("SELECT * FROM assignment WHERE course_id = #{courseId}")
    List<Assignment> findByCourseId(String courseId);
    
    @Select("SELECT * FROM assignment")
    List<Assignment> findAll();
    
    @Insert("INSERT INTO assignment(assignment_id, course_id, title, content, deadline, created_at) VALUES(#{assignmentId}, #{courseId}, #{title}, #{content}, #{deadline}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Assignment assignment);
    
    @Update("UPDATE assignment SET assignment_id=#{assignmentId}, course_id=#{courseId}, title=#{title}, content=#{content}, deadline=#{deadline} WHERE id=#{id}")
    int update(Assignment assignment);
    
    @Delete("DELETE FROM assignment WHERE id = #{id}")
    int deleteById(Long id);
}
