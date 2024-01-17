package com.alibou.security.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Integer> {
    Student findStudentByTokens_token(String token);

    Student findStudentById(int id);
}
