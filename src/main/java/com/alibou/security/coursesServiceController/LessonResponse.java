package com.alibou.security.coursesServiceController;

import com.alibou.security.exceptionHandling.CustomException;
import com.alibou.security.lessons.CourseTermin;
import com.alibou.security.lessons.Lesson;
import com.alibou.security.lessons.LessonTermin;
import com.alibou.security.lessons.Termin;
import com.alibou.security.miscellanious.Advantages;
import com.alibou.security.user.Review;
import com.alibou.security.user.Teacher;
import lombok.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonResponse {
    private int lessonID;
    private String title;
    private String description;
    private String grade;
    private String subject;
    private double price;
    private int length;
    private int studentsUpperBound;
    private double rating;
    private int numberOfReviews;

    private int numberOfTermins;

    private String status;

    private int weekLength;
    private boolean isPrivateLesson;

    private String firstDate;

    private String time;

    private String urlToImage;

    private List<ThemaSimpleResponse> themas;

    private List<ReviewResponse> reviewResponses;

    private List<CourseTerminRequestResponse> courseTerminResponses;

    private List<String> lessonTerminResponses;

    private String teacherName;
    private String teacherSurname;

    private int teacherId;



    public LessonResponse(Lesson lesson, String dateTime, String time) {
        lessonID = lesson.getLessonID();
        title = lesson.getTitle();
        description = lesson.getDescription();
        grade = lesson.getGrade();
        subject = lesson.getSubject();
        price = lesson.getPrice();
        length = lesson.getLength();
        studentsUpperBound = lesson.getStudentsUpperBound();
        rating = lesson.getRating();
        numberOfReviews = lesson.getNumberOfReviews();
        urlToImage = lesson.getImageLocation();
        firstDate = dateTime;
        this.time = time;
        var teacher = lesson.getTeacher();
        teacherName = teacher.getFirstname();
        teacherSurname = teacher.getLastname();
        teacherId = teacher.getId();
    }

    public LessonResponse(Lesson lesson, List<ReviewResponse> reviews) throws CustomException {
        lessonID = lesson.getLessonID();
        title = lesson.getTitle();
        description = lesson.getDescription();
        grade = lesson.getGrade();
        subject = lesson.getSubject();
        price = lesson.getPrice();
        length = lesson.getLength();
        studentsUpperBound = lesson.getStudentsUpperBound();
        rating = lesson.getRating();
        numberOfReviews = lesson.getNumberOfReviews();
        urlToImage = lesson.getImageLocation();
        courseTerminResponses = new ArrayList<>();
        List<CourseTermin> courseTermins = lesson.getCourseTermins();
        themas = new ArrayList<>();
        for (var thema : courseTermins.get(0).getThemas()) {
            themas.add(new ThemaSimpleResponse(thema.getTitle(), thema.getDescription()));
        }
        for (CourseTermin courseTermin : lesson.getCourseTermins()) {
            CourseTerminRequestResponse courseTerminRequestResponse = new CourseTerminRequestResponse(courseTermin);
            courseTerminResponses.add(courseTerminRequestResponse);
            if (weekLength == 0) this.weekLength = courseTermin.getWeekLength();
        }
        reviewResponses = reviews;
        Teacher teacher = lesson.getTeacher();
        teacherName = teacher.getFirstname();
        teacherSurname = teacher.getLastname();
        teacherId = teacher.getId();
    }

    public LessonResponse(Lesson lesson, List<String> termins, List<ReviewResponse> reviews, ThemaSimpleResponse themaSimpleResponse) {
        lessonID = lesson.getLessonID();
        title = lesson.getTitle();
        description = lesson.getDescription();
        grade = lesson.getGrade();
        subject = lesson.getSubject();
        price = lesson.getPrice();
        length = lesson.getLength();
        studentsUpperBound = lesson.getStudentsUpperBound();
        rating = lesson.getRating();
        numberOfReviews = lesson.getNumberOfReviews();
        urlToImage = lesson.getImageLocation();
        lessonTerminResponses = termins;
        reviewResponses = reviews;
        themas = new ArrayList<>();
        themas.add(themaSimpleResponse);
        var teacher = lesson.getTeacher();
        teacherName = teacher.getFirstname();
        teacherSurname = teacher.getLastname();
        teacherId = teacher.getId();
    }

    public LessonResponse(int lessonID, String title, boolean isPrivateLesson, String teacherName, String teacherSurname, String status, CourseTerminRequestResponse courseTerminRequestResponse, int teacherId) {
        courseTerminResponses = new ArrayList<>();
        courseTerminResponses.add(courseTerminRequestResponse);
        this.lessonID = lessonID;
        this.title = title;
        this.isPrivateLesson = isPrivateLesson;
        this.teacherName = teacherName;
        this.teacherSurname = teacherSurname;
        this.status = status;
        this.teacherId = teacherId;
    }

    public LessonResponse(int lessonID, String title, boolean isPrivateLesson, String teacherName, String teacherSurname, String status, String date, String time, int teacherId) {
        courseTerminResponses = new ArrayList<>();
        this.firstDate = date;
        this.time = time;
        this.lessonID = lessonID;
        this.title = title;
        this.isPrivateLesson = isPrivateLesson;
        this.teacherName = teacherName;
        this.teacherSurname = teacherSurname;
        this.status = status;
        this.teacherId = teacherId;
    }

//
//    public LessonResponse(Lesson lesson, List<String> lessonTermins) {
//        lessonID = lesson.getLessonID();
//        title = lesson.getTitle();
//        description = lesson.getDescription();
//        grade = lesson.getGrade();
//        subject = lesson.getSubject();
//        price = lesson.getPrice();
//        length = lesson.getLength();
//        studentsUpperBound = lesson.getStudentsUpperBound();
//        rating = lesson.getRating();
//        numberOfReviews = lesson.getNumberOfReviews();
//        urlToImage = lesson.getImageLocation();
//        Timestamp dateTime = new Timestamp(Long.MAX_VALUE);
//        for (LessonTermin lessonTermin : lessonTermins) {
//            if (dateTime.before(lessonTermin.getDateTime())) dateTime = lessonTermin.getDateTime();
//        }
//        firstDate = dateTime.toString();
//        teacherName = lesson.getTeacher().getFirstname();
//        teacherSurname = lesson.getTeacher().getLastname();
//    }
}
