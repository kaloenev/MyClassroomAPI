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
    private String filesLocation;
    private String date;
    private String time;
    private String status;
    private String[] comments;
    private int commentAmount;
}
