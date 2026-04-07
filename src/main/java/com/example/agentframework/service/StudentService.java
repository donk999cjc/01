package com.example.agentframework.service;

import com.example.agentframework.entity.Student;
import com.example.agentframework.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    @Autowired
    private StudentMapper studentMapper;

    public Student createStudent(Student student) {
        studentMapper.insert(student);
        return student;
    }

    public Optional<Student> getStudentById(Long id) {
        Student student = studentMapper.findById(id);
        return Optional.ofNullable(student);
    }

    public Student getStudentByStudentId(String studentId) {
        return studentMapper.findByStudentId(studentId);
    }

    public List<Student> getAllStudents() {
        return studentMapper.findAll();
    }

    public List<Student> getStudentsByCourseId(String courseId) {
        return studentMapper.findByCourseId(courseId);
    }

    public Student updateStudent(Long id, Student student) {
        Student existingStudent = studentMapper.findById(id);
        if (existingStudent != null) {
            existingStudent.setStudentId(student.getStudentId());
            existingStudent.setName(student.getName());
            existingStudent.setCourses(student.getCourses());
            studentMapper.update(existingStudent);
            return existingStudent;
        }
        return null;
    }

    public void deleteStudent(Long id) {
        studentMapper.deleteById(id);
    }
}
