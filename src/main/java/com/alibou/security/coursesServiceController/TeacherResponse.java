package com.alibou.security.coursesServiceController;

import com.alibou.security.user.Teacher;
import lombok.*;

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

    public TeacherResponse(Teacher teacher, List<ReviewResponse> reviewResponses) {
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
    }
}
