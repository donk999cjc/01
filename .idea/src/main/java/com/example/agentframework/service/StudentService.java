package com.example.agentframework.service;

import com.example.agentframework.entity.Student;
import com.example.agentframework.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    @Autowired
    private StudentMapper studentMapper;

    public List<Student> getAllStudents() {
        return studentMapper.findAll();
    }

    public Student getStudentById(Long id) {
        return studentMapper.findById(id);
    }

    public Student getStudentByStudentId(String studentId) {
        return studentMapper.findByStudentId(studentId);
    }

    public List<Student> getStudentsByClass(String classes) {
        return studentMapper.findByClass(classes);
    }

    public List<Student> getStudentsByCourseId(String courseId) {
        // 根据课程 ID 查询学生，暂时返回所有学生
        // TODO: 实现课程与学生的关联查询
        return studentMapper.findAll();
    }

    public Student createStudent(Student student) {
        studentMapper.insert(student);
        return student;
    }

    public Student updateStudent(Long id, Student student) {
        Student existingStudent = studentMapper.findById(id);
        if (existingStudent != null) {
            existingStudent.setRealName(student.getRealName());
            existingStudent.setEmail(student.getEmail());
            existingStudent.setPhone(student.getPhone());
            existingStudent.setClasses(student.getClasses());
            studentMapper.update(existingStudent);
            return existingStudent;
        }
        return null;
    }

    public void deleteStudent(Long id) {
        studentMapper.deleteById(id);
    }
}
