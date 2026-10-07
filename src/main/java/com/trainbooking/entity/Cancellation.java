package com.trainbooking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "cancellations",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_cancellation_booking",
            columnNames = "booking_id"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class Cancellation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "booking_id",
        nullable = false,
        unique = true
    )
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cancelled_by", nullable = false)
    private User cancelledBy;

    @Column(length = 500)
    private String reason;

    @Column(name = "cancelled_at", nullable = false)
    private LocalDateTime cancelledAt;

    @Column(
        name = "original_amount",
        nullable = false,
        precision = 10,
        scale = 2
    )
    private BigDecimal originalAmount;

    @Column(
        name = "refund_amount",
        nullable = false,
        precision = 10,
        scale = 2
    )
    private BigDecimal refundAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CancellationStatus status;

    @PrePersist
    protected void onCreate() {

        if (cancelledAt == null) {
            cancelledAt = LocalDateTime.now();
        }

        if (status == null) {
            status = CancellationStatus.REQUESTED;
        }
    }
}