package com.alibou.security.userFunctions;

import com.alibou.security.chatroom.ChatNotification;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageContactsResponse {
    private int receiverId;
    private String picture;
    private String name;
    private String dateTime;
    private String senderId;
    private String recipientId;
    private String content;
    private String date;
    private String time;
    private boolean isFile;
    private boolean isRead;
}
