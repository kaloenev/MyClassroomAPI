package com.alibou.security.coursesServiceController;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentRequestResponse {
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
