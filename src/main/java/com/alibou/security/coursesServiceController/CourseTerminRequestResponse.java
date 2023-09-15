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
    private String courseHours;
    private int studentsUpperBound;
    private int studentsLowerBound;

    private String lessonStatus;

    public CourseTerminRequestResponse(CourseTermin courseTermin) {
        startDate = courseTermin.getDateTime().toString();
        weekLength = courseTermin.getWeekLength();
        courseDays = courseTermin.getCourseDays();
        courseHours = courseTermin.getCourseHours();
        studentsUpperBound = courseTermin.getStudentsUpperBound();
        studentsLowerBound = 1;
        endDate = new Timestamp(courseTermin.getDateTime().getTime() + (long) courseTermin.getWeekLength() * 7 * 86400000).toString();
    }

    public CourseTerminRequestResponse(CourseTermin courseTermin, LessonStatus lessonStatus) {
        startDate = courseTermin.getDateTime().toString();
        courseDays = courseTermin.getCourseDays();
        courseHours = courseTermin.getCourseHours();
        studentsUpperBound = courseTermin.getStudentsUpperBound();
        studentsLowerBound = studentsUpperBound - courseTermin.getPlacesRemaining();
        endDate = new Timestamp(courseTermin.getDateTime().getTime() + (long) courseTermin.getWeekLength() * 7 * 86400000).toString();
        this.lessonStatus = lessonStatus.toString();
    }
}
