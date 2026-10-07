package com.trainbooking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "schedule_seats",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_schedule_seat",
            columnNames = {"schedule_id", "seat_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class ScheduleSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleSeatStatus status;

    @Column(name = "held_until")
    private LocalDateTime heldUntil;

    @Version
    @Column(nullable = false)
    private Integer version;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "held_by")
    private User heldBy;
    
    
}