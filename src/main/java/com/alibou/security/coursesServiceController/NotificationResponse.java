package com.alibou.security.coursesServiceController;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private String recipientId;
    private String content;
    private String date;
    private String time;
    private boolean isChat;
    private String lesson;
    private String link;
}
