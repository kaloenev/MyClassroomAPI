package com.alibou.security.lessons;

import com.alibou.security.user.Student;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;

import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SuperBuilder
public class LessonTermin extends Termin {
    private int lessonHours;
    @OneToOne
    @JoinColumn(name = "thema_themaID")
    private Thema thema;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    @ToString.Exclude
    private Student student;

    public void enrollStudent(Student student) {
        this.student = student;
        isFull = true;
        isEmpty = false;
        lesson.increasePopularity();
    }
}
