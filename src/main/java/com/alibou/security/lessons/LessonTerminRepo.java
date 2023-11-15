package com.alibou.security.lessons;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface LessonTerminRepo extends JpaRepository<LessonTermin, Integer> {
    LessonTermin getLessonTerminByTerminID(int id);

    @Query("select l from LessonTermin l where l.lesson.lessonID = :lessonId")
    List<LessonTermin> getLessonTerminsByLessonID(@Param("lessonId") int lessonId);

    @Query("select distinct l.lesson from LessonTermin l where l.lesson.teacher.id = :teacherID")
    List<Lesson> getPrivateLessonsByTeacherID(@Param("teacherID") int teacherID);

    @Query("""
            select distinct c.lesson from LessonTermin c
                where ((:searchTerm is null or c.lesson.title = :searchTerm)
                or (:searchTerm2 is null or c.lesson.teacher.firstname = :searchTerm2)
                or (:searchTerm3 is null or c.lesson.teacher.lastname = :searchTerm3))
                and (:subject is null or c.lesson.subject = :subject)
                and c.lesson.isDraft = :isDraft
                and (:grade is null or c.lesson.grade = :grade)
                and (c.lesson.price between :priceLowerBound and :priceUpperBound)
                and (c.lessonHours between :hoursLowerBound and :hoursUpperBound)
                and (c.dateTime between :lowerBound and :upperBound)
                and c.isFull = :isFull and c.lesson.isPrivateLesson = :isPrivateLesson""")
    Page<Lesson> getFilteredLessonTermins(@Param("searchTerm") String searchTerm, @Param("searchTerm2") String searchTerm2,
                                          @Param("searchTerm3") String searchTerm3, @Param("subject") String subject,
                                          @Param("isDraft") boolean isDraft, @Param("grade") String grade,
                                          @Param("priceLowerBound") double priceLowerBound,
                                          @Param("priceUpperBound") double priceUpperBound,
                                          @Param("hoursLowerBound") int hoursLowerBound,
                                          @Param("hoursUpperBound") int hoursUpperBound, @Param("lowerBound") Timestamp lowerBound,
                                          @Param("upperBound") Timestamp upperBound, @Param("isFull") boolean isFull,
                                          @Param("isPrivateLesson") boolean isPrivateLesson, Pageable pageable);

}
