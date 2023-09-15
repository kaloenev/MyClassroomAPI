package com.alibou.security.user;

import com.alibou.security.lessons.Lesson;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity

public class Review {
    @Id
    @GeneratedValue
    private Integer reviewID;
    private Timestamp dateTime;
    private String message;
    private int rating;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    @ToString.Exclude
    private Teacher teacher;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    @ToString.Exclude
    private Lesson lesson;
    private String studentName;
    private String studentSurname;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Review review = (Review) o;
        return reviewID != null && Objects.equals(reviewID, review.reviewID);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
