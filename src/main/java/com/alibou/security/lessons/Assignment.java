package com.alibou.security.lessons;

import com.alibou.security.user.Student;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Assignment {
    @Id
    @GeneratedValue
    private Integer assignmentID;
    private String title;
    private String description;
    private Timestamp dueDateTime;
    private String assignmentLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    @ToString.Exclude
    private Termin lesson;

    @OneToMany(mappedBy = "assignment")
    @ToString.Exclude
    private List<Solution> solutions;
}
