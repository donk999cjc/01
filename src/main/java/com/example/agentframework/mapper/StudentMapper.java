package com.example.agentframework.mapper;

import com.example.agentframework.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StudentMapper {

    List<Student> findAll();

    Student findById(@Param("id") Long id);

    Student findByStudentId(@Param("studentId") String studentId);

    List<Student> findByClass(@Param("classes") String classes);

    int insert(Student student);

    int update(Student student);

    int deleteById(@Param("id") Long id);
}
