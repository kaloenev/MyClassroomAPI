package com.alibou.security.coursesServiceController;

import com.alibou.security.user.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Timestamp dateTime;
    private String message;
    private int rating;
    private String studentName;
    private String studentSurname;

    public ReviewResponse(Review review) {
        dateTime = review.getDateTime();
        message = review.getMessage();
        rating = review.getRating();
        studentName = review.getStudentName();
        studentSurname = review.getStudentSurname();
    }
}
