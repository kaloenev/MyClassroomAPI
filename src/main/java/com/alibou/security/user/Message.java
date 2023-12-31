package com.alibou.security.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Message {
    @Id
    @GeneratedValue
    private Integer messageID;
    private Timestamp dateTime;
    private String content;
    private boolean isStudentTheSender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    @ToString.Exclude
    private MessageContact contact;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Message message = (Message) o;
        return messageID != null && Objects.equals(messageID, message.messageID);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
