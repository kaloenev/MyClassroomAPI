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
    private String title;
    private String description;
}
