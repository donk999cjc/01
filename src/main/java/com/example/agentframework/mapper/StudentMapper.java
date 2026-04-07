package com.example.agentframework.mapper;

import com.example.agentframework.entity.Student;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface StudentMapper {
    
    @Select("SELECT * FROM student WHERE id = #{id}")
    Student findById(Long id);
    
    @Select("SELECT * FROM student WHERE student_id = #{studentId}")
    Student findByStudentId(String studentId);
    
    @Select("SELECT * FROM student")
    List<Student> findAll();
    
    @Select("SELECT * FROM student WHERE courses LIKE CONCAT('%', #{courseId}, '%')")
    List<Student> findByCourseId(String courseId);
    
    @Insert("INSERT INTO student(student_id, name, courses, created_at) VALUES(#{studentId}, #{name}, #{courses}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Student student);
    
    @Update("UPDATE student SET student_id=#{studentId}, name=#{name}, courses=#{courses} WHERE id=#{id}")
    int update(Student student);
    
    @Delete("DELETE FROM student WHERE id = #{id}")
    int deleteById(Long id);
}
