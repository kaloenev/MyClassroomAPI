package com.alibou.security.coursesServiceController;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentRequest {
    private int id;
    private String title;
    private String studentName;
    private String teacherName;
    private String description;
    private String[] fileNames;
    private String date;
    private String time;
    private String status;
    private String comment;
    private int commentAmount;
}
