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
    private boolean isVerified = true;
    private boolean isEnabled = true;
    private int numberOfReviews;
    private double rating;
    @Column()
    private String description;
    @Column()
    private String specialties;
    @Enumerated(EnumType.STRING)
    @Column()
    private City city;
    @Enumerated(EnumType.STRING)
    @Column()
    private Degree degree;
    @Column()
    private String school;
    @Column()
    private String university;
    @Column()
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

    @ManyToMany(fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Student> isLikedByStudent;

    @OneToMany(mappedBy = "teacher")
    @ToString.Exclude
    private List<Lesson> lessons;

    public void saveMessage(MessageContact messageContact) {
        messages.remove(messageContact);
        messages.add(0, messageContact);
//        messageContact.getTeacher().pushMessage();
    }

    public void leaveReview(Review review) {
        reviews.add(review);
        double tempRating = rating * numberOfReviews + review.getRating();
        numberOfReviews++;
        rating = tempRating / numberOfReviews;
    }

    public boolean isVerified() {
        if (!isEnabled) return false;
        if (isVerified) return true;
//            Random random = new Random();
//            if ((System.currentTimeMillis() - timeOfVerificationRequest.getTime()) > random.nextInt(21600000)) isVerified = true;
        return isVerified;
    }

    public void removeStudentFromIsLiked(Student student) {
        isLikedByStudent.remove(student);
    }

    public void verifyAccount() {
        isVerified = true;
        timeOfVerificationRequest = new Timestamp(System.currentTimeMillis());
    }

    public void addIsLikedByStudent(Student student) {
        isLikedByStudent.add(student);
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
