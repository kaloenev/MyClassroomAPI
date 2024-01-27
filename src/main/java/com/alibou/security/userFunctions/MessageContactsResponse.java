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
    private int contactId;
    private String name;
    private String dateTime;
    private List<ChatNotification> messages;
}
