package com.alibou.security.coursesServiceController;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationResponse {
    private String recipientId;
    private String content;
    private String date;
    private String time;
    private boolean isChat;
    private String lesson;
}
