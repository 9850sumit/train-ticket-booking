package com.trainbooking.dto;

import com.trainbooking.entity.CancellationStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CancellationResponse {

    private Long id;

    private Long bookingId;

    private String pnr;

    private String reason;

    private LocalDateTime cancelledAt;

    private BigDecimal originalAmount;

    private BigDecimal refundAmount;

    private CancellationStatus status;

    private String bookingStatus;
}