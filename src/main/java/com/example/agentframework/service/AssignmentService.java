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
            existingAssignment.setAssignmentId(assignment.getAssignmentId());
            existingAssignment.setCourseId(assignment.getCourseId());
            existingAssignment.setTitle(assignment.getTitle());
            existingAssignment.setContent(assignment.getContent());
            existingAssignment.setDeadline(assignment.getDeadline());
            assignmentMapper.update(existingAssignment);
            return existingAssignment;
        }
        return null;
    }

    public void deleteAssignment(Long id) {
        assignmentMapper.deleteById(id);
    }
}
