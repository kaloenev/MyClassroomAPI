package com.alibou.security.userFunctions;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarResponse {
    private String title;
    private String start;
    private String end;
}
