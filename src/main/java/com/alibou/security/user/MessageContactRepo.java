package com.alibou.security.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageContactRepo extends JpaRepository<MessageContact, Integer> {
    MessageContact getMessageContactByMessageID(int id);
}
