package com.example.agentframework.service;

import com.example.agentframework.entity.Submission;
import com.example.agentframework.mapper.SubmissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubmissionService {

    @Autowired
    private SubmissionMapper submissionMapper;

    public Submission createSubmission(Submission submission) {
        submissionMapper.insert(submission);
        return submission;
    }

    public Optional<Submission> getSubmissionById(Long id) {
        Submission submission = submissionMapper.findById(id);
        return Optional.ofNullable(submission);
    }

    public Submission getSubmissionBySubmissionId(String submissionId) {
        return submissionMapper.findBySubmissionId(submissionId);
    }

    public List<Submission> getSubmissionsByStudentId(String studentId) {
        return submissionMapper.findByStudentId(studentId);
    }

    public List<Submission> getSubmissionsByAssignmentId(String assignmentId) {
        return submissionMapper.findByAssignmentId(assignmentId);
    }

    // 返回最新的提交记录（避免TooManyResultsException）
    public Submission getSubmissionByStudentIdAndAssignmentId(String studentId, String assignmentId) {
        List<Submission> submissions = submissionMapper.findListByStudentIdAndAssignmentId(studentId, assignmentId);
        if (submissions != null && !submissions.isEmpty()) {
            // 返回最新的一条（已按submitted_at DESC排序）
            return submissions.get(0);
        }
        return null;
    }

    // 获取所有提交（用于查看历史）
    public List<Submission> getSubmissionsListByStudentIdAndAssignmentId(String studentId, String assignmentId) {
        return submissionMapper.findListByStudentIdAndAssignmentId(studentId, assignmentId);
    }

    public List<Submission> getAllSubmissions() {
        return submissionMapper.findAll();
    }

    public Submission updateSubmission(Long id, Submission submission) {
        Submission existingSubmission = submissionMapper.findById(id);
        if (existingSubmission != null) {
            if (submission.getContent() != null) {
                existingSubmission.setContent(submission.getContent());
            }
            if (submission.getScore() != null) {
                existingSubmission.setScore(submission.getScore());
            }
            if (submission.getFeedback() != null) {
                existingSubmission.setFeedback(submission.getFeedback());
            }
            if (submission.getStatus() != null) {
                existingSubmission.setStatus(submission.getStatus());
            }
            if (submission.getImageAnalysis() != null) {
                existingSubmission.setImageAnalysis(submission.getImageAnalysis());
            }
            submissionMapper.update(existingSubmission);
            return existingSubmission;
        }
        return null;
    }

    public void deleteSubmission(Long id) {
        submissionMapper.deleteById(id);
    }
}
