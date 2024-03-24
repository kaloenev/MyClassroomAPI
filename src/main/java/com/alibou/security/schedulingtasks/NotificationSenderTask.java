package com.alibou.security.schedulingtasks;

import com.alibou.security.coursesServiceController.NotificationResponse;
import com.alibou.security.user.Notification;
import com.alibou.security.user.NotificationRepo;
import com.alibou.security.user.User;
import com.alibou.security.user.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.sql.Timestamp;
import java.util.List;

public class NotificationSenderTask implements Runnable{
    private final SimpMessagingTemplate messagingTemplate;
    private final String text;
    private final List<String> receiverTokens;

    private final String lesson;
    private final String link;
    private final NotificationRepo notificationRepo;

    private final UserRepository userRepository;

    public NotificationSenderTask(SimpMessagingTemplate messagingTemplate, String text, List<String> receiverToken,
                                  String lesson, String link, NotificationRepo notificationRepo, UserRepository userRepo) {
        this.messagingTemplate = messagingTemplate;
        this.text = text;
        this.receiverTokens = receiverToken;
        this.lesson = lesson;
        this.link = link;
        this.notificationRepo = notificationRepo;
        this.userRepository = userRepo;
    }

    @Override
    public void run() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        for (String token : receiverTokens) {
            User user = userRepository.findUserByTokens_token(token.substring(7));
            Notification notification = Notification.builder().isChat(false).message(text).dateTime(timestamp)
                    .lesson(lesson).link(link).user(user).build();
            notificationRepo.save(notification);
            messagingTemplate.convertAndSendToUser(token, "/queue/notifications",
                    NotificationResponse.builder().isChat(false).content(text).date(timestamp.toString().substring(0, 10))
                            .time(timestamp.toString().substring(11, 16)).lesson(lesson).link(link).build());
        }
    }
}
