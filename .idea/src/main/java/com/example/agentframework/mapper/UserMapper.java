package com.example.agentframework.mapper;

import com.example.agentframework.entity.User;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserMapper {
    
    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    User findById(Long id);
    
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User findByUsername(String username);
    
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND password = #{password}")
    User findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
    
    @Select("SELECT * FROM sys_user WHERE role = #{role}")
    List<User> findByRole(String role);
    
    @Select("SELECT * FROM sys_user")
    List<User> findAll();
    
    @Insert("INSERT INTO sys_user(username, password, real_name, email, phone, avatar, role, status, student_id, teacher_id, department, created_at, updated_at) " +
            "VALUES(#{username}, #{password}, #{realName}, #{email}, #{phone}, #{avatar}, #{role}, #{status}, #{studentId}, #{teacherId}, #{department}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
    
    @Update("UPDATE sys_user SET username=#{username}, real_name=#{realName}, email=#{email}, phone=#{phone}, " +
            "avatar=#{avatar}, role=#{role}, status=#{status}, student_id=#{studentId}, teacher_id=#{teacherId}, " +
            "department=#{department}, updated_at=NOW() WHERE id=#{id}")
    int update(User user);
    
    @Update("UPDATE sys_user SET password=#{password}, updated_at=NOW() WHERE id=#{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);
    
    @Update("UPDATE sys_user SET last_login_time=NOW() WHERE id=#{id}")
    int updateLoginTime(Long id);
    
    @Delete("DELETE FROM sys_user WHERE id = #{id}")
    int deleteById(Long id);
}
