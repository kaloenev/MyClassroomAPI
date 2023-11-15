package com.alibou.security.coursesServiceController;

import com.alibou.security.exceptionHandling.CustomException;
import com.alibou.security.lessons.CourseTermin;
import com.alibou.security.lessons.LessonTermin;
import com.alibou.security.lessons.Termin;
import com.alibou.security.user.Teacher;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherResponse {
    private int id;
    private int numberOfReviews;
    private double rating;
    private String specialties;
    private String firstName;

    private String location;
    private String secondName;
    private String description;
    private String experience;
    private List<ReviewResponse> reviews;

    private List<LessonResponse> lessonResponses;

    public TeacherResponse(Teacher teacher) {
        this.id = teacher.getId();
        this.numberOfReviews = teacher.getNumberOfReviews();
        this.rating = teacher.getRating();
        this.specialties = teacher.getSpecialties();
        this.firstName = teacher.getFirstname();
        this.secondName = teacher.getLastname();
        this.location = teacher.getCity().toString() + ", Bulgaria";
        this.description = teacher.getDescription();
        this.experience = teacher.getExperience();
    }

    public TeacherResponse(Teacher teacher, List<ReviewResponse> reviewResponses) throws CustomException {
        this.id = teacher.getId();
        this.numberOfReviews = teacher.getNumberOfReviews();
        this.rating = teacher.getRating();
        this.specialties = teacher.getSpecialties();
        this.description = teacher.getDescription();
        this.firstName = teacher.getFirstname();
        this.secondName = teacher.getLastname();
        this.reviews = reviewResponses;
        this.location = teacher.getCity().toString() + ", Bulgaria";
        this.experience = teacher.getExperience();
        this.lessonResponses = new ArrayList<>();
        for (var lesson : teacher.getLessons()) {
            if (lesson.isPrivateLesson()) {
                List<LessonTermin> termins = lesson.getLessonTermins();
                lessonResponses.add(new LessonResponse(lesson, termins.get(0).getDate(), termins.get(0).getTime(), 0));
            }
            else {
                List<CourseTermin> termins = lesson.getCourseTermins();
                lessonResponses.add(new LessonResponse(lesson, termins.get(0).getDate(), termins.get(0).getTime(),
                        termins.get(0).getStudentsUpperBound() - termins.get(0).getPlacesRemaining()));
            }
        }
    }
}
