package com.alibou.security.coursesServiceController;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemaResponse {
    protected Integer themaID;
    protected String linkToRecording;
    protected String presentation;
    private int assignmentId;
    protected int studentsNumber;
    protected int studentSolutions;
    private String title;
    private String description;
}
