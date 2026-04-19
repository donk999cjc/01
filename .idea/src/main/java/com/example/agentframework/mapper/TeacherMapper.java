package com.example.agentframework.mapper;

import com.example.agentframework.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TeacherMapper {

    List<Teacher> findAll();

    Teacher findById(@Param("id") Long id);

    Teacher findByTeacherId(@Param("teacherId") String teacherId);

    List<Teacher> findByDepartment(@Param("department") String department);

    int insert(Teacher teacher);

    int update(Teacher teacher);

    int deleteById(@Param("id") Long id);
}
