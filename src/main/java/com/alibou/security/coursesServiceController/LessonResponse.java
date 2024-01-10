package com.alibou.security.coursesServiceController;

import com.alibou.security.exceptionHandling.CustomException;
import com.alibou.security.lessons.CourseTermin;
import com.alibou.security.lessons.Lesson;
import com.alibou.security.user.Teacher;
import lombok.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
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

    private double pricePerHour;
    private int length;
    private int studentsUpperBound;
    private double rating;
    private int numberOfReviews;

    private int numberOfTermins;

    private int numberOfStudents;

    private String status;

    private int weekLength;
    private boolean privateLesson;

    private String firstDate;

    private String time;

    private String urlToImage;

    private List<ThemaSimpleResponse> themas;

    private List<ReviewResponse> reviewResponses;

    private List<CourseTerminRequestResponse> courseTerminResponses;

    private List<LessonTerminResponse> lessonTerminResponses;

    private String teacherName;
    private String teacherSurname;

    private int teacherId;

    private TeacherResponse teacherResponse;

    private boolean isDraft;



    public LessonResponse(Lesson lesson, String dateTime, String time, int numberOfStudents) {
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
        this.numberOfStudents = numberOfStudents;
        this.privateLesson = lesson.isPrivateLesson();
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
        if (lesson.getThemas() != null) {
            themas = new ArrayList<>();
            for (var thema : lesson.getThemas()) {
                themas.add(new ThemaSimpleResponse(thema.getTitle(), thema.getDescription()));
            }
        }
        if (lesson.isHasTermins()) {
            courseTerminResponses = new ArrayList<>();
            List<CourseTermin> courseTermins = lesson.getCourseTermins();
            CourseTermin courseTermin1 = null;
            for (CourseTermin courseTermin : courseTermins) {
                CourseTerminRequestResponse courseTerminRequestResponse = new CourseTerminRequestResponse(courseTermin);
                Timestamp timestamp = Timestamp.valueOf(Instant.ofEpochMilli(courseTermin.getDateTime().getTime()
                        + lesson.getLength() * 60000L).atZone(ZoneId.systemDefault()).toLocalDateTime());
                courseTerminRequestResponse.setCourseHours(courseTerminRequestResponse.getCourseHours() + " - " + timestamp.toString().substring(11, 16));
                courseTerminResponses.add(courseTerminRequestResponse);
                if (weekLength == 0) this.weekLength = courseTermin.getWeekLength();
                courseTermin1 = courseTermin;
            }
            String[] days = courseTermin1.getCourseDays().split(",");
            pricePerHour = lesson.getPrice() / (days.length * weekLength);
        }
        reviewResponses = reviews;
        Teacher teacher = lesson.getTeacher();
        teacherResponse = new TeacherResponse(teacher);
        teacherId = teacherResponse.getId();
        this.privateLesson = lesson.isPrivateLesson();
    }

    public LessonResponse(Lesson lesson, List<LessonTerminResponse> termins, List<ReviewResponse> reviews, ThemaSimpleResponse themaSimpleResponse) {
        lessonID = lesson.getLessonID();
        title = lesson.getTitle();
        description = lesson.getDescription();
        grade = lesson.getGrade();
        subject = lesson.getSubject();
        price = lesson.getPrice();
        length = lesson.getLength();
        isDraft = lesson.isDraft();
        studentsUpperBound = lesson.getStudentsUpperBound();
        rating = lesson.getRating();
        numberOfReviews = lesson.getNumberOfReviews();
        urlToImage = lesson.getImageLocation();
        lessonTerminResponses = termins;
        reviewResponses = reviews;
        themas = new ArrayList<>();
        themas.add(themaSimpleResponse);
        var teacher = lesson.getTeacher();
        teacherResponse = new TeacherResponse(teacher);
        teacherId = teacherResponse.getId();
        this.privateLesson = lesson.isPrivateLesson();
    }

    public LessonResponse(int lessonID, String title, boolean privateLesson, String teacherName, String teacherSurname, String status, CourseTerminRequestResponse courseTerminRequestResponse, int teacherId) {
        courseTerminResponses = new ArrayList<>();
        courseTerminResponses.add(courseTerminRequestResponse);
        this.lessonID = lessonID;
        this.title = title;
        this.privateLesson = privateLesson;
        this.teacherName = teacherName;
        this.teacherSurname = teacherSurname;
        this.status = status;
        this.teacherId = teacherId;
    }

    public LessonResponse(int lessonID, String title, boolean privateLesson, String teacherName, String teacherSurname, String status, String date, String time, int teacherId) {
        courseTerminResponses = new ArrayList<>();
        this.firstDate = date;
        this.time = time;
        this.lessonID = lessonID;
        this.title = title;
        this.privateLesson = privateLesson;
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
