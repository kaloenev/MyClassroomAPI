package com.alibou.security.coursesServiceController;

import com.alibou.security.lessons.CourseTermin;
import com.alibou.security.lessons.LessonStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseTerminRequestResponse {
    private String startDate;
    private String endDate;
    private int weekLength;
    private String courseDays;

    private int[] courseDaysNumbers;
    private String courseHours;
    private int studentsUpperBound;
    private int numberOfStudents;

    private String lessonStatus;

    public CourseTerminRequestResponse(CourseTermin courseTermin) {
        startDate = courseTermin.getDate();
        weekLength = courseTermin.getWeekLength();
        courseDays = courseTermin.getCourseDays();
        courseHours = courseTermin.getTime();
        studentsUpperBound = courseTermin.getStudentsUpperBound();
        numberOfStudents = studentsUpperBound - courseTermin.getPlacesRemaining();
        endDate = (new Timestamp(courseTermin.getDateTime().getTime() + (long) courseTermin.getWeekLength() * 7 * 86400000).toString()).substring(0, 10);
    }

    public CourseTerminRequestResponse(CourseTermin courseTermin, LessonStatus lessonStatus) {
        startDate = courseTermin.getDate();
        courseDays = courseTermin.getCourseDays();
        courseHours = courseTermin.getTime();
        studentsUpperBound = courseTermin.getStudentsUpperBound();
        numberOfStudents = studentsUpperBound - courseTermin.getPlacesRemaining();
        endDate = (new Timestamp(courseTermin.getDateTime().getTime() + (long) courseTermin.getWeekLength() * 7 * 86400000).toString()).substring(0, 10);
        this.lessonStatus = lessonStatus.toString();
    }
}
