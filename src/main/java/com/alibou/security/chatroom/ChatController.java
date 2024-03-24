package com.alibou.security.chatroom;

import com.alibou.security.exceptionHandling.CustomException;
import com.alibou.security.exceptionHandling.CustomWarning;
import com.alibou.security.user.MessageContact;
import com.alibou.security.userFunctions.MessageContactsResponse;
import com.alibou.security.userFunctions.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
            userService.sendMessage(chatMessage.getSenderId(), chatMessage.getContent(),
                    Integer.parseInt(chatMessage.getRecipientId()), chatMessage.isFile());
            MessageContactsResponse messageContactsResponse = userService.getLastContact("Bearer " + chatMessage.getSenderId());
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getRecipientId(), "/queue/messages",
                    messageContactsResponse
            );
        } catch (CustomException e) {
            messagingTemplate.convertAndSendToUser(chatMessage.getRecipientId(), "/queue/messages",
                    "Could not send message");
        }
    }
}