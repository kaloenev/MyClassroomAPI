package com.alibou.security.coursesServiceController;

import com.alibou.security.userFunctions.UserProfileResponse;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomPageResponse {
    private String lessonTitle;
    private String lessonDescription;
    private int courseTerminId;
    private String startDate;
    private String endDate;
    private int[] courseDaysNumbers;
    private String courseHours;
    private int enrolledStudents;
    private List<UserProfileResponse> students = new ArrayList<>();
    private List<ThemaResponse> themas = new ArrayList<>();
}
