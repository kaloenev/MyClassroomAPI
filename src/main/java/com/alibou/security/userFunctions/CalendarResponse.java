package com.alibou.security.userFunctions;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarResponse {
    private String title;
    private String start;
    private String startDate;
    private String startTime;
    private String endTime;
    private String dayOfTheWeek;
    private String end;
    private String enrolledStudents;
    private String className;
    private String type;
    private String themaTitle;
}
