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
    private int lessonTerminId;
    private int length;
    private String date;
    private List<TimePair> lessonHours;
    private String time;
    private String dayOfTheWeek;
    private String status;

    public LessonTerminResponse(int id, String date, String time, int length) {
        this.lessonTerminId = id;
        this.date = date;
        this.time = time;
        this.length = length;
    }

}
