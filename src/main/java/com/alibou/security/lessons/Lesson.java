package com.alibou.security.lessons;

import com.alibou.security.exceptionHandling.CustomException;
import com.alibou.security.miscellanious.Advantages;
import com.alibou.security.user.Review;
import com.alibou.security.user.Student;
import com.alibou.security.user.Teacher;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_lesson")
public class Lesson {
    @Id
    @GeneratedValue
    private Integer lessonID;
    @Column(columnDefinition = "nvarchar")
    private String title;
    @Column(columnDefinition = "nvarchar")
    private String description;
    private String imageLocation;
    @Column(columnDefinition = "nvarchar")
    private String grade;
    @Column(columnDefinition = "nvarchar")
    private String subject;
    private String advantages;
    private boolean isPrivateLesson;
    private boolean isDraft;
    private double price;
    private int length;
    private int studentsUpperBound;
    @Column(columnDefinition = "nvarchar")
    private String themas;
    private double rating;
    private int numberOfReviews = 0;
    private int popularity = 0;

    @OneToMany(mappedBy = "lesson")
    @ToString.Exclude
    @OrderColumn
    private List<Termin> termins = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    @ToString.Exclude
    private Teacher teacher;

    @OneToMany(mappedBy = "lesson")
    @ToString.Exclude
    private List<Review> reviews;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    @ToString.Exclude
    private Student isLikedByStudent;

    public void addTermin(Termin termin) {
        if (this.termins == null) {
            termins = new ArrayList<>();
        }
        termins.add(termin);
    }

    public void removeTermin(Termin termin) {
        termins.remove(termin);
    }

    public void removeAllTermins() {
        termins.clear();
    }

    public void increasePopularity() {
        popularity++;
    }

    public List<CourseTermin> getCourseTermins() throws CustomException {
        if (isPrivateLesson) throw new CustomException(HttpStatus.BAD_REQUEST, "This is a private lesson");
        List<CourseTermin> courseTermins = new ArrayList<>();
        for (Termin termin : termins) {
            courseTermins.add((CourseTermin) termin);
        }
        return courseTermins;
    }

    public List<LessonTermin> getLessonTermins() throws CustomException {
        if (!isPrivateLesson) throw new CustomException(HttpStatus.BAD_REQUEST, "This is a private lesson");
        List<LessonTermin> lessonTermins = new ArrayList<>();
        for (Termin termin : termins) {
            lessonTermins.add((LessonTermin) termin);
        }
        return lessonTermins;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Lesson lesson = (Lesson) o;
        return lessonID != null && Objects.equals(lessonID, lesson.lessonID);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
