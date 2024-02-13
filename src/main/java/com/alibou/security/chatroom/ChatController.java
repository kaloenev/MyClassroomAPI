package com.alibou.security.chatroom;

import com.alibou.security.exceptionHandling.CustomException;
import com.alibou.security.userFunctions.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage) {
        try {
            userService.sendMessage(Integer.parseInt(chatMessage.getSenderId()), chatMessage.getContent(),
                    Integer.parseInt(chatMessage.getRecipientId()));
            String dateTime = LocalDateTime.now().toString();
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getRecipientId(), "/queue/messages",
                    new ChatNotification(
                            chatMessage.getSenderId(),
                            chatMessage.getRecipientId(),
                            chatMessage.getContent(),
                            dateTime.substring(5, 9).replace("-", "."),
                            dateTime.substring(11, 16)
                    )
            );
        } catch (CustomException ignored) {

        }
    }
}