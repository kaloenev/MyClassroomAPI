package com.alibou.security.lessons;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    Lesson getLessonByLessonID(int id);
    List<Lesson> findTop12ByIsDraftFalseAndIsPrivateLessonTrueOrderByPopularityDesc();

    List<Lesson> findTop12ByIsDraftFalseAndIsPrivateLessonFalseOrderByPopularityDesc();

    @Query("select l from Lesson l where l.isDraft = false and (l.subject = :subject or l.grade = :grade) order by l.popularity DESC")
    List<Lesson> getSimilarLessons(@Param("subject") String subject, @Param("grade") String grade);

    @Query("select l from Lesson l inner join l.isLikedByStudent isLikedByStudent where isLikedByStudent.id = :studentID")
    Page<Lesson> getLessonByisLikedByStudent_id(@Param("studentID") int studentID, Pageable pageable);

    @Query("""
            select l from Lesson l inner join l.isLikedByStudent isLikedByStudent inner join l.termins termin
            where isLikedByStudent.id = :studentId
            order by termin.dateTime ASC""")
    Page<Lesson> getLessonByIsLikedByStudentOrderByDateTime(@Param("studentId") int studentId, Pageable pagedAndSorted);

    //TODO Add ? instead of params to avoid sql injection
    @Query("""
            select l from Lesson l inner join l.termins isLikedByStudent inner join l.termins termin
            where termin.terminID = :terminId""")
    Lesson getLessonByTerminId(@Param("terminId") int terminId);

    @Query("""
            select l from Lesson l inner join l.isLikedByStudent isLikedByStudent
            where isLikedByStudent.id = :studentId""")
    List<Lesson> getLessonByIsLikedByStudent(@Param("studentId") int studentId);


    @Query("SELECT distinct l.subject from Lesson l order by l.subject ASC")
    List<String> getAllSubjects();

    @Query("SELECT distinct l.grade from Lesson l order by l.grade ASC")
    List<String> getAllGrades();

    @Query("SELECT MAX(price) from Lesson")
    int getMaxPrice();

    @Query("SELECT MIN(price) from Lesson")
    int getMinPrice();

    @Query("select l.imageLocation from Lesson l where l.lessonID = ?1")
    String getImageLocationByLessonID(int id);

    @Query("SELECT l.price from Lesson l where l.isPrivateLesson = false")
    List<Double> getCoursePrices();

    @Query("SELECT l.price from Lesson l where l.isPrivateLesson = true")
    List<Double> getPrivateLessonPrices();
}
