package com.alibou.security.coursesServiceController;

import com.alibou.security.lessons.CourseTermin;
import com.alibou.security.lessons.LessonStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseTerminRequestResponse {
    private int courseTerminId;
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
        courseTerminId = courseTermin.getTerminID();
        startDate = courseTermin.getDate();
        weekLength = courseTermin.getWeekLength();
        courseDays = courseTermin.getCourseDays();
        courseHours = courseTermin.getTime();
        studentsUpperBound = courseTermin.getStudentsUpperBound();
        numberOfStudents = studentsUpperBound - courseTermin.getPlacesRemaining();
        endDate = (new Timestamp(courseTermin.getDateTime().getTime() + (long) courseTermin.getWeekLength() * 7 * 86400000).toString()).substring(0, 10);
    }

    public CourseTerminRequestResponse(CourseTermin courseTermin, LessonStatus lessonStatus, int length) {
        courseTerminId = courseTermin.getTerminID();
        startDate = courseTermin.getDate();
        courseDays = courseTermin.getCourseDays();
        weekLength = courseTermin.getWeekLength();
        Timestamp timestamp = Timestamp.valueOf(Instant.ofEpochMilli(courseTermin.getDateTime().getTime()
                + length * 60000L).atZone(ZoneId.systemDefault()).toLocalDateTime());
        courseHours = courseTermin.getTime()  + " - " + timestamp.toString().substring(11, 16);
        studentsUpperBound = courseTermin.getStudentsUpperBound();
        numberOfStudents = studentsUpperBound - courseTermin.getPlacesRemaining();
        endDate = (new Timestamp(courseTermin.getDateTime().getTime() + (long) courseTermin.getWeekLength() * 7 * 86400000).toString()).substring(0, 10);
        this.lessonStatus = lessonStatus.toString();
    }
}
