package com.trainbooking.dto;

import com.trainbooking.entity.PaymentMethod;
import com.trainbooking.entity.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentResponse {

    private Long id;
    private String transactionId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private LocalDateTime paymentDate;
}