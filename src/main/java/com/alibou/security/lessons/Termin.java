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
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_termin")
public class Termin {
    @Id
    @GeneratedValue
    protected Integer terminID;
    protected String linkToRecording;
    protected String linkToClassroom;
    protected File presentation;
    @Enumerated(EnumType.STRING)
    protected LessonStatus lessonStatus;
    private Timestamp dateTime;
    protected boolean isFull = false;
    protected boolean isEmpty = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    @ToString.Exclude
    protected Lesson lesson;

    @OneToMany(mappedBy = "lesson")
    @ToString.Exclude
    protected List<Assignment> assignments;
}
