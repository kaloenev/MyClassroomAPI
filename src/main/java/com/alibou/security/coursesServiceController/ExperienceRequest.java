package com.alibou.security.coursesServiceController;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceRequest {
    private String place;
    private String startYear;
    private String endYear;
    private String description;

    @Override
    public String toString() {
        return "Worked at " + place + "from " + startYear + "to " + endYear + " \n Description: " + description;
    }
}
