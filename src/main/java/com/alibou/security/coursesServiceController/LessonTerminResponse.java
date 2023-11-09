package com.alibou.security.coursesServiceController;

import com.alibou.security.lessons.LessonTermin;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonTerminResponse {
    private String date;
    private List<TimePair> times;
    private String time;
    private String dayOfTheWeek;
    private String status;

    public LessonTerminResponse(String date, String time) {
        this.date = date;
        this.time = time;
    }

}
