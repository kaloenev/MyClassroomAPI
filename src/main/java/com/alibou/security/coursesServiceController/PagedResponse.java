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

    public PagedResponse(long total, int perPage, List<LessonResponse> lessonResponses, List<ReviewResponse> reviewResponses) {
        this.total = total;
        this.perPage = perPage;
        this.lessonResponses = lessonResponses;
        this.reviewResponses = reviewResponses;
    }

    private int perPage;
    private List<LessonResponse> lessonResponses;
    private List<ReviewResponse> reviewResponses;

    public PagedResponse(long total, int perPage, List<TeacherResponse> teacherResponses) {
        this.total = total;
        this.perPage = perPage;
        this.teacherResponses = teacherResponses;
    }

    private List<TeacherResponse> teacherResponses;
}
