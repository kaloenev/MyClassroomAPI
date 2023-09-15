package com.alibou.security.coursesServiceController;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FilterRequest {
    private int pageNumber;
    @NotNull
    private String subject;
    @NotNull
    private String grade;
    private double priceLowerBound;
    private double priceUpperBound;
    private int hoursLowerBound;
    private int hoursUpperBound;
    @NotNull
    private String lowerBound;
    @NotNull
    private String upperBound;
    @NotNull
    private String searchTerm;
    @NotNull
    private String sort;
    private boolean isPrivateLesson;
}
