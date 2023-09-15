package com.alibou.security.user;

import com.alibou.security.lessons.Lesson;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SuperBuilder
public class Teacher extends User {
    private Timestamp timeOfVerificationRequest;
    private boolean isVerified = false;
    private boolean isEnabled = false;
    private int numberOfReviews;
    private double rating;
    private String description;
    private String specialties;
    @Enumerated(EnumType.STRING)
    private City city;
    @Enumerated(EnumType.STRING)
    private Degree degree;
    private String school;
    private String university;
    private String experience;
    @OneToMany(mappedBy = "teacher")
    @ToString.Exclude
    private List<Payment> payments;

    @OneToMany(mappedBy = "teacher")
    @ToString.Exclude
    private List<MessageContact> messages;

    @OneToMany(mappedBy = "teacher")
    @ToString.Exclude
    private List<Review> reviews;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    @ToString.Exclude
    private Student isLikedByStudent;

    @OneToMany(mappedBy = "teacher")
    @ToString.Exclude
    private List<Lesson> lessons;

    public void saveMessage(MessageContact messageContact) {
        messages.remove(messageContact);
        messages.add(0, messageContact);
//        messageContact.getTeacher().pushMessage();
    }

    public boolean isVerified() {
        if (isEnabled) return true;
        if (!isVerified) return false;
            Random random = new Random();
            if ((System.currentTimeMillis() - timeOfVerificationRequest.getTime()) > random.nextInt(21600000)) isEnabled = true;
        return isEnabled;
    }

    public void verifyAccount() {
        isVerified = true;
        timeOfVerificationRequest = new Timestamp(System.currentTimeMillis());
    }

    public void addLesson(Lesson lesson) {
        lessons.add(lesson);
    }

    public void removeLesson(Lesson lesson) {
        lessons.remove(lesson);
    }

    public void updateRating(int newRating) {
        this.rating = (numberOfReviews * rating + newRating) / numberOfReviews;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Teacher teacher = (Teacher) o;
        return id != null && Objects.equals(id, teacher.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
