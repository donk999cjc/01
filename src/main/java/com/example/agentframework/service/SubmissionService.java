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

    public Submission getSubmissionByStudentIdAndAssignmentId(String studentId, String assignmentId) {
        return submissionMapper.findByStudentIdAndAssignmentId(studentId, assignmentId);
    }

    public List<Submission> getAllSubmissions() {
        return submissionMapper.findAll();
    }

    public Submission updateSubmission(Long id, Submission submission) {
        Submission existingSubmission = submissionMapper.findById(id);
        if (existingSubmission != null) {
            existingSubmission.setContent(submission.getContent());
            existingSubmission.setScore(submission.getScore());
            existingSubmission.setFeedback(submission.getFeedback());
            existingSubmission.setStatus(submission.getStatus());
            submissionMapper.update(existingSubmission);
            return existingSubmission;
        }
        return null;
    }

    public void deleteSubmission(Long id) {
        submissionMapper.deleteById(id);
    }
}
