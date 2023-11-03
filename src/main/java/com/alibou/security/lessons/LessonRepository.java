package com.alibou.security.lessons;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    Lesson getLessonByLessonID(int id);
    List<Lesson> getDistinct9ByOrderByPopularityDesc();

    List<Lesson> getLessonByisLikedByStudent_id(int studentID, Pageable pageable);
    @Query("SELECT distinct l.subject from Lesson l order by l.subject ASC")
    List<String> getAllSubjects();
    @Query("SELECT distinct l.grade from Lesson l order by l.grade ASC")
    List<String> getAllGrades();

    @Query("SELECT MAX(price) from Lesson")
    int getMaxPrice();

    @Query("SELECT MIN(price) from Lesson")
    int getMinPrice();
}
