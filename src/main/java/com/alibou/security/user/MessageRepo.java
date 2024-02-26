package com.alibou.security.user;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepo extends JpaRepository<Message, Integer> {
    Message findFirstByContact_MessageIDOrderByDateTimeDesc(int messageId);
}
