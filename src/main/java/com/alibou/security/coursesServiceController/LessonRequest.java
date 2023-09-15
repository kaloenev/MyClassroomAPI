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
public class LessonRequest {
    @NotNull
    private int id;
    @NotNull
    private String sort;
    @NotNull
    private int page;

}
