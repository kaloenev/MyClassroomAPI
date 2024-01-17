package com.alibou.security.coursesServiceController;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ThemaResponse {
    protected Integer themaID;
    protected String linkToRecording;
    protected String linkToClassroom;
    protected String presentation;
    //TODO Add commented out lines
//    protected int studentsNumber;
//    protected int studentSolutions;
    private String title;
    private String description;
}
