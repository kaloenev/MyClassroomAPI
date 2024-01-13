package com.alibou.security.user;

import com.alibou.security.lessons.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SuperBuilder
public class Student extends User{
    @ManyToMany(mappedBy = "enrolledStudents", fetch = FetchType.LAZY)
    @ToString.Exclude
    //TODO Find a way to order column or rewrite code
    private List<CourseTermin> courses;

    @OneToMany(mappedBy = "student")
    @ToString.Exclude
    @OrderColumn
    private List<LessonTermin> privateLessons;

    @ManyToMany(mappedBy = "isLikedByStudent")
    @ToString.Exclude
    private List<Teacher> favouriteTeachers;

    @ManyToMany(mappedBy = "isLikedByStudent")
    @ToString.Exclude
    private List<Lesson> favouriteLessons;

    @OneToMany(mappedBy = "student")
    @ToString.Exclude
    private List<Payment> payments;

    @OneToMany(mappedBy = "student")
    @ToString.Exclude
    private List<MessageContact> messages;

    @ManyToMany(mappedBy = "students")
    @ToString.Exclude
    private List<Assignment> assignments;

    public void addLessonTermin(LessonTermin lesson) {
        int counter = 0;
        for (LessonTermin termin : privateLessons) {
            if (termin.getDateTime().after(lesson.getDateTime())) {
                privateLessons.add(counter, lesson);
            }
            counter++;
        }
    }

    public void addCourseTermin(CourseTermin course) {
        int counter = 0;
        for (CourseTermin termin : courses) {
            if (termin.getDateTime().after(course.getDateTime())) {
                courses.add(counter, course);
            }
            counter++;
        }
    }

    public void saveMessage(MessageContact messageContact) {
        messages.remove(messageContact);
        messages.add(0, messageContact);
    }

    public void addAssignment(Assignment assignment) {
        assignments.add(assignment);
    }

    public void saveLessonToLiked(Lesson lesson) {
        favouriteLessons.add(0, lesson);
    }

    public void removeLessonsFromLiked(Lesson lesson) {
        favouriteLessons.remove(lesson);
    }

    public void saveTeacherToLiked(Teacher teacher) {
        favouriteTeachers.add(0, teacher);
    }

    public void removeTeacherFromLiked(Teacher teacher) {
        favouriteTeachers.remove(teacher);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Student student = (Student) o;
        return id != null && Objects.equals(id, student.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
