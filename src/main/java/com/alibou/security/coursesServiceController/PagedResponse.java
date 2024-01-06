package com.alibou.security.coursesServiceController;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse {
    private long total;
    private int perPage;
    private List<LessonResponse> lessonResponses;
    private List<ReviewResponse> reviewResponses;
}
