package com.alibou.security.lessons;

import com.alibou.security.user.Student;
import com.alibou.security.user.Teacher;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thema_id")
    @ToString.Exclude
    private Thema thema;

    @OneToMany(mappedBy = "assignment")
    @ToString.Exclude
    private List<Solution> solutions;

    @ManyToMany(fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Student> students;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    @ToString.Exclude
    private Teacher teacher;

    public String getTime() {
        return dueDateTime.toString().substring(11, 16);
    }

    public String getDate() {
        return dueDateTime.toString().substring(0, 10);
    }
}
