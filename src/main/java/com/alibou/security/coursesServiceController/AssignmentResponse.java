package com.alibou.security.coursesServiceController;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponse {
    private int id;
    private String title;
    private String studentName;
    private String teacherName;
    private String description;
    private String fileNames;
    private int solutionId;
    private String solutionFileNames;
    private String date;
    private String time;
    private String altTime;
    private String status;
    private String comment;
    private int commentAmount;
    private List<AssignmentResponse> comments;
}
