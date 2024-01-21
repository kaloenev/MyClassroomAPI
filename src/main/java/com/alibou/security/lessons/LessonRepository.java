package com.alibou.security.lessons;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    Lesson getLessonByLessonID(int id);
    //TODO find a way to remove drafts from results in next 2 queries
    List<Lesson> findTop12ByOrderByPopularityDesc();

    List<Lesson> findTop4BySubjectOrGradeOrderByPopularityDesc(String subject, String grade);

    @Query("select l from Lesson l inner join l.isLikedByStudent isLikedByStudent where isLikedByStudent.id = :studentID")
    Page<Lesson> getLessonByisLikedByStudent_id(@Param("studentID") int studentID, Pageable pageable);

    @Query("""
            select l from Lesson l inner join l.isLikedByStudent isLikedByStudent inner join l.termins termin
            where isLikedByStudent.id = :studentId
            order by termin.dateTime ASC""")
    Page<Lesson> getLessonByIsLikedByStudentOrderByDateTime(@Param("studentId") int studentId, Pageable pagedAndSorted);

    @Query("SELECT distinct l.subject from Lesson l order by l.subject ASC")
    List<String> getAllSubjects();
    @Query("SELECT distinct l.grade from Lesson l order by l.grade ASC")
    List<String> getAllGrades();

    @Query("SELECT MAX(price) from Lesson")
    int getMaxPrice();

    @Query("SELECT MIN(price) from Lesson")
    int getMinPrice();

    @Query("SELECT l.price from Lesson l where l.isPrivateLesson = false")
    List<Double> getCoursePrices();

    @Query("SELECT l.price from Lesson l where l.isPrivateLesson = true")
    List<Double> getPrivateLessonPrices();
}
