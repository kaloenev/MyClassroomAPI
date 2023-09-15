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
    private String HOME_PAGE_TITLE;
    private String HOME_PAGE_DESCRIPTION1;
    private String HOME_PAGE_ADVANTAGE1;
    private String HOME_PAGE_ADVANTAGE2;
    private String HOME_PAGE_ADVANTAGE3;
    private String HOME_PAGE_ADVANTAGE4;
    private String HOME_PAGE_ADVANTAGE5;
    private String HOME_PAGE_ADVANTAGE6;

    private String HOME_PAGE_COURSES_LINK;

    private String ASKED_QUESTION_1;
    private String ASKED_QUESTION_ANSWER1;
    private String ASKED_QUESTION_2;
    private String ASKED_QUESTION_ANSWER2;
    private String ASKED_QUESTION_3;
    private String ASKED_QUESTION_ANSWER3;
    private String ASKED_QUESTION_4;
    private String ASKED_QUESTION_ANSWER4;
    private String ASKED_QUESTION_5;
    private String ASKED_QUESTION_ANSWER5;

    private List<LessonResponse> popularLessonsResponse;
    private List<ReviewResponse> reviewsResponse;
}
