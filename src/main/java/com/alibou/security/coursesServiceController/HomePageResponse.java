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
public class HomePageResponse {
    private List<LessonResponse> popularLessonsResponse;
    private List<LessonResponse> popularCourseResponse;
    private List<ReviewResponse> reviewsResponse;
}
