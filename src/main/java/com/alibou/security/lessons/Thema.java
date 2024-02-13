package com.alibou.security.lessons;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_thema")
public class Thema {
    @Id
    @GeneratedValue
    protected Integer themaID;
    protected String linkToRecording;
    protected String presentation;
    private String title;
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courseTermin_id")
    @ToString.Exclude
    private CourseTermin courseTermin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @OneToOne(mappedBy = "thema", cascade = CascadeType.ALL)
    private LessonTermin lessonTermin;

    @OneToOne(mappedBy = "thema")
    @ToString.Exclude
    protected Assignment assignment;
}
