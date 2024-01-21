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
    private String date;
    private String message;
    private int rating;
    private String studentName;
    private String studentSurname;

    private String lessonTitle;

    public ReviewResponse(Review review) {
        date = review.getDateTime().toString().substring(0, 10);
        message = review.getMessage();
        rating = review.getRating();
        studentName = review.getStudentName();
        studentSurname = review.getStudentSurname();
        lessonTitle = review.getLesson().getTitle();
    }
}
