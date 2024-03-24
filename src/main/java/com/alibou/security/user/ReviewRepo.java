package com.alibou.security.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepo extends JpaRepository<Review, Integer> {
    List<Review> getDistinct3ByOrderByRatingDescMessageDesc();

    Page<Review> getByLesson_lessonID(int id, Pageable pageable);

    //TODO Change queries to be like that instead by Id and looping through all of them (solution names example)
    @Query("select r from Review r where r.studentName = ?1 and r.studentSurname = ?2 and r.lesson.lessonID = ?3")
    Review getReviewByStudentName(String name, String surname, int lessonId);
}
