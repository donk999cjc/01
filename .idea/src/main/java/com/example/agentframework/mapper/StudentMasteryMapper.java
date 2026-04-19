package com.example.agentframework.mapper;

import com.example.agentframework.entity.StudentMastery;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface StudentMasteryMapper {

    @Select("SELECT * FROM student_mastery WHERE id = #{id}")
    StudentMastery findById(Long id);

    @Select("SELECT * FROM student_mastery WHERE student_id = #{studentId} AND knowledge_id = #{knowledgeId}")
    StudentMastery findByStudentAndKnowledge(@Param("studentId") String studentId, @Param("knowledgeId") String knowledgeId);

    @Select("SELECT * FROM student_mastery WHERE student_id = #{studentId}")
    List<StudentMastery> findByStudentId(String studentId);

    @Select("SELECT * FROM student_mastery WHERE student_id = #{studentId} AND course_id = #{courseId}")
    List<StudentMastery> findByStudentAndCourse(@Param("studentId") String studentId, @Param("courseId") String courseId);

    @Select("SELECT * FROM student_mastery WHERE knowledge_id = #{knowledgeId}")
    List<StudentMastery> findByKnowledgeId(String knowledgeId);

    @Select("SELECT * FROM student_mastery WHERE course_id = #{courseId}")
    List<StudentMastery> findByCourseId(String courseId);

    @Insert("INSERT INTO student_mastery(student_id, knowledge_id, course_id, p_l, p_l0, p_t, p_g, p_s, " +
            "total_attempts, correct_attempts, last_attempt_at, created_at, updated_at) " +
            "VALUES(#{studentId}, #{knowledgeId}, #{courseId}, #{pL}, #{pL0}, #{pT}, #{pG}, #{pS}, " +
            "#{totalAttempts}, #{correctAttempts}, #{lastAttemptAt}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(StudentMastery mastery);

    @Update("UPDATE student_mastery SET p_l=#{pL}, p_l0=#{pL0}, p_t=#{pT}, p_g=#{pG}, p_s=#{pS}, " +
            "total_attempts=#{totalAttempts}, correct_attempts=#{correctAttempts}, " +
            "last_attempt_at=#{lastAttemptAt}, updated_at=NOW() WHERE id=#{id}")
    int update(StudentMastery mastery);

    @Delete("DELETE FROM student_mastery WHERE id = #{id}")
    int deleteById(Long id);
}
