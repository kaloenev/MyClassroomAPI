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
public class LessonTerminResponse {
    private String date;
    private List<TimePair> times;
    private String dayOfTheWeek;
    private String status;
}
