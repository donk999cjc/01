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

    // 返回List（避免TooManyResultsException）
    @Select("SELECT * FROM submission WHERE student_id = #{studentId} AND assignment_id = #{assignmentId} ORDER BY submitted_at DESC")
    List<Submission> findListByStudentIdAndAssignmentId(@Param("studentId") String studentId, @Param("assignmentId") String assignmentId);

    @Select("SELECT * FROM submission")
    List<Submission> findAll();

    @Insert("<script>" +
            "INSERT INTO submission(submission_id, student_id, assignment_id, content, file_path, score, feedback, status, submitted_at, image_analysis, evaluation_report, learning_summary) " +
            "VALUES(#{submissionId}, #{studentId}, #{assignmentId}, #{content}, #{filePath}, #{score}, #{feedback}, #{status}, NOW(), #{imageAnalysis}, #{evaluationReport}, #{learningSummary})" +
            "</script>")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Submission submission);

    @Update("<script>" +
            "UPDATE submission SET " +
            "<if test='content != null'>content=#{content},</if>" +
            "<if test='filePath != null'>file_path=#{filePath},</if>" +
            "<if test='score != null'>score=#{score},</if>" +
            "<if test='feedback != null'>feedback=#{feedback},</if>" +
            "<if test='status != null'>status=#{status},</if>" +
            "<if test='imageAnalysis != null'>image_analysis=#{imageAnalysis},</if>" +
            "<if test='evaluationReport != null'>evaluation_report=#{evaluationReport},</if>" +
            "<if test='learningSummary != null'>learning_summary=#{learningSummary},</if>" +
            "graded_at=NOW() WHERE id=#{id}" +
            "</script>")
    int update(Submission submission);

    @Delete("DELETE FROM submission WHERE id = #{id}")
    int deleteById(Long id);
}
