package com.alibou.security.coursesServiceController;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingMessageResponse {
    private String message;
    private String link;
}
