package com.alibou.security.lessons;

import com.alibou.security.token.Token;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    Lesson getLessonByLessonID(int id);
    List<Lesson> findFirst9ByOrderByPopularityDesc();

    List<Lesson> getLessonByisLikedByStudent_id(int studentID, Pageable pageable);
}
