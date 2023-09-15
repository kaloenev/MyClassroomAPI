package com.alibou.security.coursesServiceController;

import com.alibou.security.user.Teacher;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherResponse {
    private int id;
    private int numberOfReviews;
    private double rating;
    private String specialties;
    private String firstName;
    private String secondName;
    private String description;
    private String experience;
    private List<ReviewResponse> reviews;
    private String urlToProfile;

    public TeacherResponse(Teacher teacher, String url) {
        this.id = teacher.getId();
        this.numberOfReviews = teacher.getNumberOfReviews();
        this.rating = teacher.getRating();
        this.specialties = teacher.getSpecialties();
        this.firstName = teacher.getFirstname();
        this.secondName = teacher.getLastname();
        this.urlToProfile = url;
    }

    public TeacherResponse(Teacher teacher, List<ReviewResponse> reviewResponses) {
        this.id = teacher.getId();
        this.numberOfReviews = teacher.getNumberOfReviews();
        this.rating = teacher.getRating();
        this.specialties = teacher.getSpecialties();
        this.firstName = teacher.getFirstname();
        this.secondName = teacher.getLastname();
        this.reviews = reviewResponses;
    }
}
