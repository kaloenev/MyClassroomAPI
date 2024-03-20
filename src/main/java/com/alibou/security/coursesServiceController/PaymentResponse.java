package com.alibou.security.coursesServiceController;

import com.alibou.security.user.PaymentStatus;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Integer paymentID;
    private String time;
    private String date;
    private String number;
    private int amount;
    private String paymentStatus;
    private String lesson;
}
