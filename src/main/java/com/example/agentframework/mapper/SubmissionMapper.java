package com.example.agentframework.mapper;

import com.example.agentframework.entity.Submission;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface SubmissionMapper {
    
    @Select("SELECT * FROM submission WHERE id = #{id}")
    Submission findById(Long id);
    
    @Select("SELECT * FROM submission WHERE submission_id = #{submissionId}")
    Submission findBySubmissionId(String submissionId);
    
    @Select("SELECT * FROM submission WHERE assignment_id = #{assignmentId}")
    List<Submission> findByAssignmentId(String assignmentId);
    
    @Select("SELECT * FROM submission WHERE student_id = #{studentId}")
    List<Submission> findByStudentId(String studentId);
    
    @Select("SELECT * FROM submission WHERE student_id = #{studentId} AND assignment_id = #{assignmentId}")
    Submission findByStudentIdAndAssignmentId(@Param("studentId") String studentId, @Param("assignmentId") String assignmentId);
    
    @Select("SELECT * FROM submission")
    List<Submission> findAll();
    
    @Insert("INSERT INTO submission(submission_id, student_id, assignment_id, content, score, feedback, status, submitted_at) " +
            "VALUES(#{submissionId}, #{studentId}, #{assignmentId}, #{content}, #{score}, #{feedback}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Submission submission);
    
    @Update("UPDATE submission SET content=#{content}, score=#{score}, feedback=#{feedback}, status=#{status}, graded_at=NOW() WHERE id=#{id}")
    int update(Submission submission);
    
    @Delete("DELETE FROM submission WHERE id = #{id}")
    int deleteById(Long id);
}
