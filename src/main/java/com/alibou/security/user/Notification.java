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
@Table(name = "_notification")
public class Notification {
    @Id
    @GeneratedValue
    private Integer notificationID;
    private Timestamp dateTime;
    private String message;
    private boolean isChat;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Notification that = (Notification) o;
        return notificationID != null && Objects.equals(notificationID, that.notificationID);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
