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

    @Insert("INSERT INTO assignment(assignment_id, course_id, title, content, description, total_score, status, due_date, deadline, created_at) VALUES(#{assignmentId}, #{courseId}, #{title}, #{content}, #{description}, #{totalScore}, #{status}, #{dueDate}, #{deadline}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Assignment assignment);

    @Update("<script>" +
            "UPDATE assignment " +
            "<set>" +
            "<if test='assignmentId != null'>assignment_id=#{assignmentId},</if>" +
            "<if test='courseId != null'>course_id=#{courseId},</if>" +
            "<if test='title != null'>title=#{title},</if>" +
            "<if test='content != null'>content=#{content},</if>" +
            "<if test='description != null'>description=#{description},</if>" +
            "<if test='totalScore != null'>total_score=#{totalScore},</if>" +
            "<if test='status != null'>status=#{status},</if>" +
            "<if test='dueDate != null'>due_date=#{dueDate},</if>" +
            "<if test='deadline != null'>deadline=#{deadline},</if>" +
            "</set>" +
            "WHERE id=#{id}" +
            "</script>")
    int update(Assignment assignment);

    @Delete("DELETE FROM assignment WHERE id = #{id}")
    int deleteById(Long id);
}
