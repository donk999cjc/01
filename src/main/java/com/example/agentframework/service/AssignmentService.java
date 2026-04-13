package com.example.agentframework.service;

import com.example.agentframework.entity.Assignment;
import com.example.agentframework.mapper.AssignmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AssignmentService {

    @Autowired
    private AssignmentMapper assignmentMapper;

    public Assignment createAssignment(Assignment assignment) {
        // 自动生成assignmentId（如果为空）
        if (assignment.getAssignmentId() == null || assignment.getAssignmentId().isEmpty()) {
            String assignmentId = "A" + System.currentTimeMillis();
            assignment.setAssignmentId(assignmentId);
        }
        assignmentMapper.insert(assignment);
        return assignment;
    }

    public Optional<Assignment> getAssignmentById(Long id) {
        Assignment assignment = assignmentMapper.findById(id);
        return Optional.ofNullable(assignment);
    }

    public Assignment getAssignmentByAssignmentId(String assignmentId) {
        return assignmentMapper.findByAssignmentId(assignmentId);
    }

    public List<Assignment> getAssignmentsByCourseId(String courseId) {
        return assignmentMapper.findByCourseId(courseId);
    }

    public List<Assignment> getAllAssignments() {
        return assignmentMapper.findAll();
    }

    public Assignment updateAssignment(Long id, Assignment assignment) {
        Assignment existingAssignment = assignmentMapper.findById(id);
        if (existingAssignment != null) {
            // 保留原有的assignment_id（除非明确提供新值）
            if (assignment.getAssignmentId() == null || assignment.getAssignmentId().isEmpty()) {
                // 不设置，让update SQL跳过此字段
            }
            if (assignment.getCourseId() != null) {
                existingAssignment.setCourseId(assignment.getCourseId());
            }
            if (assignment.getTitle() != null) {
                existingAssignment.setTitle(assignment.getTitle());
            }
            if (assignment.getContent() != null) {
                existingAssignment.setContent(assignment.getContent());
            }
            if (assignment.getDeadline() != null) {
                existingAssignment.setDeadline(assignment.getDeadline());
            }
            // 使用existingAssignment更新，保留原有assignmentId
            assignmentMapper.update(existingAssignment);
            return existingAssignment;
        }
        return null;
    }

    public void deleteAssignment(Long id) {
        assignmentMapper.deleteById(id);
    }
}
