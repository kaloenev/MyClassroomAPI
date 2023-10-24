package com.alibou.security.lessons;

import com.alibou.security.exceptionHandling.CustomException;
import com.alibou.security.user.Student;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SuperBuilder
public class CourseTermin extends Termin{
    private int weekLength;
    @Column(columnDefinition = "nvarchar")
    private String courseDays;
    private String courseHours;
    private int courseHoursNumber;
    private int studentsUpperBound;
    private int placesRemaining = studentsUpperBound;

    @ManyToMany(fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Student> enrolledStudents;

    public void enrollStudent(Student student) throws CustomException {
        if (isFull) throw new CustomException(HttpStatus.CONFLICT, "Error the course is already full");
        enrolledStudents.add(student);
        isEmpty = false;
        placesRemaining--;
        if (placesRemaining <= 0) {
            isFull = true;
        }
        lesson.increasePopularity();
    }
}
