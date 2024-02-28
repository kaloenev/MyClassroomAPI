package com.alibou.security.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepo extends JpaRepository<Message, Integer> {
    Message findTopByContact_MessageIDOrderByDateTimeDesc(int messageId);

    List<Message> getAllMessagesByContact_MessageIDOrderByDateTimeAsc(int contactId);
}
