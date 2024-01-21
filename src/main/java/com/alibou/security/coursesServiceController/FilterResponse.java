package com.alibou.security.coursesServiceController;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FilterResponse {

    private List<String> subjects;
    private List<String> grades;

    private double[] prices;

    public FilterResponse(List<String> subjects, List<String> grades, double[] prices) {
        this.subjects = subjects;
        this.grades = grades;
        this.prices = prices;
        for (int i = 0; i < prices.length; i++) {
            prices[i] = Math.round(prices[i] * 100.0) / 100.0;
        }
    }
}
