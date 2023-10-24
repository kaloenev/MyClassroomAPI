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
            select distinct l.lesson from LessonTermin l
            where (l.lesson.title = :searchTerm or l.lesson.teacher.firstname = :searchTerm2
            or l.lesson.teacher.lastname = :searchTerm3) and l.lesson.subject = :subject and l.lesson.isDraft = :isDraft
            and l.lesson.grade = :grade and l.lesson.price between :priceLowerBound and :priceUpperBound and l.lessonHours
            between :hoursLowerBound and :hoursUpperBound and l.dateTime between :lowerBound and :upperBound and l.isFull = :isFull""")
    Page<Lesson> getFilteredLessonTermins(@Param("searchTerm") String searchTerm, @Param("searchTerm2") String searchTerm2,
                                          @Param("searchTerm3") String searchTerm3, @Param("subject") String subject,
                                          @Param("isDraft") boolean isDraft, @Param("grade") String grade,
                                          @Param("priceLowerBound") double priceLowerBound,
                                          @Param("priceUpperBound") double priceUpperBound,
                                          @Param("hoursLowerBound") int hoursLowerBound,
                                          @Param("hoursUpperBound") int hoursUpperBound, @Param("lowerBound") Timestamp lowerBound,
                                          @Param("upperBound") Timestamp upperBound, @Param("isFull") boolean isFull,
                                          Pageable pageable);

}
