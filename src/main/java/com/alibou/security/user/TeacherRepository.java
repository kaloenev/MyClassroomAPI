package com.alibou.security.user;

import com.alibou.security.lessons.Lesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeacherRepository extends JpaRepository<Teacher, Integer> {
    Teacher findTeacherById(Integer id);

    Teacher findTeacherByTokens_token(String token);

    @Query("""
            select l from Teacher l inner join l.isLikedByStudent isLikedByStudent
            where isLikedByStudent.id = :studentId""")
    List<Teacher> getTeacherByIsLikedByStudent(@Param("studentId") int studentId);
}
