package com.alibou.security.schedulingtasks;

import com.alibou.security.coursesServiceController.NotificationResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.sql.Timestamp;
import java.util.List;

public class NotificationSenderTask implements Runnable{
    private final SimpMessagingTemplate messagingTemplate;
    private final String text;
    private final List<String> receiverTokens;

    private final String lesson;

    public NotificationSenderTask(SimpMessagingTemplate messagingTemplate, String text, List<String> receiverToken, String lesson) {
        this.messagingTemplate = messagingTemplate;
        this.text = text;
        this.receiverTokens = receiverToken;
        this.lesson = lesson;
    }

    @Override
    public void run() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        for (String token : receiverTokens) {
            messagingTemplate.convertAndSendToUser(token, "/queue/notifications",
                    NotificationResponse.builder().isChat(false).content(text).date(timestamp.toString().substring(0, 10))
                            .time(timestamp.toString().substring(11, 16)).lesson(lesson).build());
        }
    }
}
