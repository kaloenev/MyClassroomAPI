package com.alibou.security.chatroom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatNotification {
    private String senderId;
    private String recipientId;
    private String content;
    private String date;
    private String time;
    private boolean isFile;
}
