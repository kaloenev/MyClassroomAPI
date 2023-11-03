package com.alibou.security.coursesServiceController;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseRequest {
    private String title;
    private String subject;
    private String grade;
    private String description;
    private ThemaSimpleResponse[] themas;
    private String imageLocation;
    private int studentsUpperBound;
    private int length;
    private double price;
    private List<String> privateLessonTermins;
    private List<CourseTerminRequestResponse> courseTerminRequests;
    private int[] nullVariables;


}
