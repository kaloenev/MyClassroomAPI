package com.alibou.security.coursesServiceController;

import com.alibou.security.exceptionHandling.CustomException;
import com.alibou.security.lessons.CourseTermin;
import com.alibou.security.lessons.Lesson;
import com.alibou.security.lessons.LessonTermin;
import com.alibou.security.lessons.Termin;
import com.alibou.security.miscellanious.Advantages;
import com.alibou.security.user.Review;
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
    private boolean isPrivateLesson;

    private String firstDate;

    private String urlToImage;

    private String[] themas;

    private String urlToTeacherProfile;

    private String urlSendTeacherMessage;

    private List<ReviewResponse> reviewResponses;

    private List<CourseTerminRequestResponse> courseTerminResponses;

    private List<String> lessonTerminResponses;

    private String teacherName;
    private String teacherSurname;



    public LessonResponse(Lesson lesson, String dateTime) {
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
        teacherName = lesson.getTeacher().getFirstname();
        teacherSurname = lesson.getTeacher().getLastname();
    }

    public LessonResponse(Lesson lesson, List<Review> reviews, String url1, String url2) throws CustomException {
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
        themas = lesson.getThemas().split(",");
        courseTerminResponses = new ArrayList<>();
        for (CourseTermin courseTermin : lesson.getCourseTermins()) {
            CourseTerminRequestResponse courseTerminRequestResponse = new CourseTerminRequestResponse(courseTermin);
            courseTerminResponses.add(courseTerminRequestResponse);
        }
        reviewResponses = new ArrayList<>();
        for (Review review : reviews) {
            ReviewResponse reviewResponse = new ReviewResponse(review);
            reviewResponses.add(reviewResponse);
        }
        urlToTeacherProfile = url1;
        urlSendTeacherMessage = url2;
        teacherName = lesson.getTeacher().getFirstname();
        teacherSurname = lesson.getTeacher().getLastname();
    }

    public LessonResponse(Lesson lesson, List<String> termins, List<Review> reviews, String url1, String url2) {
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
        reviewResponses = new ArrayList<>();
        for (Review review : reviews) {
            ReviewResponse reviewResponse = new ReviewResponse(review);
            reviewResponses.add(reviewResponse);
        }
        urlToTeacherProfile = url1;
        urlSendTeacherMessage = url2;
        teacherName = lesson.getTeacher().getFirstname();
        teacherSurname = lesson.getTeacher().getLastname();
    }

    public LessonResponse(int lessonID, String title, boolean isPrivateLesson, String teacherName, String teacherSurname, String status, CourseTerminRequestResponse courseTerminRequestResponse, String url) {
        courseTerminResponses = new ArrayList<>();
        courseTerminResponses.add(courseTerminRequestResponse);
        this.lessonID = lessonID;
        this.title = title;
        this.isPrivateLesson = isPrivateLesson;
        this.teacherName = teacherName;
        this.teacherSurname = teacherSurname;
        this.status = status;
        this.urlToTeacherProfile = url;
    }

    public LessonResponse(int lessonID, String title, boolean isPrivateLesson, String teacherName, String teacherSurname, String status, Timestamp dateTime) {
        courseTerminResponses = new ArrayList<>();
        firstDate = dateTime.toString();
        this.lessonID = lessonID;
        this.title = title;
        this.isPrivateLesson = isPrivateLesson;
        this.teacherName = teacherName;
        this.teacherSurname = teacherSurname;
        this.status = status;
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
