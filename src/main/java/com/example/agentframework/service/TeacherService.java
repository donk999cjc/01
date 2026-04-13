package com.example.agentframework.service;

import com.example.agentframework.entity.Teacher;
import com.example.agentframework.mapper.TeacherMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherService {

    @Autowired
    private TeacherMapper teacherMapper;

    public List<Teacher> getAllTeachers() {
        return teacherMapper.findAll();
    }

    public Teacher getTeacherById(Long id) {
        return teacherMapper.findById(id);
    }

    public Teacher getTeacherByTeacherId(String teacherId) {
        return teacherMapper.findByTeacherId(teacherId);
    }

    public Teacher findByTeacherId(String teacherId) {
        return teacherMapper.findByTeacherId(teacherId);
    }

    public List<Teacher> getTeachersByDepartment(String department) {
        return teacherMapper.findByDepartment(department);
    }

    public Teacher createTeacher(Teacher teacher) {
        teacherMapper.insert(teacher);
        return teacher;
    }

    public Teacher updateTeacher(Long id, Teacher teacher) {
        Teacher existingTeacher = teacherMapper.findById(id);
        if (existingTeacher != null) {
            existingTeacher.setRealName(teacher.getRealName());
            existingTeacher.setEmail(teacher.getEmail());
            existingTeacher.setPhone(teacher.getPhone());
            existingTeacher.setDepartment(teacher.getDepartment());
            teacherMapper.update(existingTeacher);
            return existingTeacher;
        }
        return null;
    }

    public void deleteTeacher(Long id) {
        teacherMapper.deleteById(id);
    }
}
