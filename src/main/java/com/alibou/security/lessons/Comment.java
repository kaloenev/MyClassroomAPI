package com.alibou.security.lessons;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Comment {
    @Id
    @GeneratedValue
    private Integer commentID;
    private String actualComment;
    private Timestamp dateTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solution_id")
    @ToString.Exclude
    private Solution solution;

    public String getTime() {
        return dateTime.toString().substring(11, 16);
    }

    public String getDate() {
        return dateTime.toString().substring(0, 10);
    }
}
