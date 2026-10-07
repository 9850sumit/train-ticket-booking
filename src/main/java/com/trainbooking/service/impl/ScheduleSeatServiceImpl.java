package com.trainbooking.service.impl;

import com.trainbooking.dto.ScheduleSeatResponse;
import com.trainbooking.dto.SeatHoldResponse;
import com.trainbooking.entity.Coach;
import com.trainbooking.entity.Schedule;
import com.trainbooking.entity.ScheduleSeat;
import com.trainbooking.entity.ScheduleSeatStatus;
import com.trainbooking.entity.Seat;
import com.trainbooking.entity.Train;
import com.trainbooking.entity.User;
import com.trainbooking.exception.ResourceNotFoundException;
import com.trainbooking.repository.CoachRepository;
import com.trainbooking.repository.ScheduleRepository;
import com.trainbooking.repository.ScheduleSeatRepository;
import com.trainbooking.repository.SeatRepository;
import com.trainbooking.repository.UserRepository;
import com.trainbooking.service.ScheduleSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleSeatServiceImpl implements ScheduleSeatService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleSeatRepository scheduleSeatRepository;
    private final CoachRepository coachRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public List<ScheduleSeatResponse> initializeScheduleSeats(
            Long scheduleId,
            String adminEmail
    ) {

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Schedule not found"));

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Admin user not found"));

        Train train = schedule.getTrain();

        if (train.getCreatedBy() == null ||
                !train.getCreatedBy().getId().equals(admin.getId())) {

            throw new IllegalArgumentException(
                    "You can initialize seats only for schedules of your own trains"
            );
        }

        List<Coach> coaches =
                coachRepository.findByTrainIdAndActiveTrue(train.getId());

        List<ScheduleSeatResponse> responses = new ArrayList<>();

        for (Coach coach : coaches) {

            List<Seat> seats =
                    seatRepository.findByCoachIdAndActiveTrue(coach.getId());

            for (Seat seat : seats) {

                if (!scheduleSeatRepository
                        .existsByScheduleIdAndSeatId(
                                scheduleId,
                                seat.getId()
                        )) {

                    ScheduleSeat scheduleSeat = new ScheduleSeat();

                    scheduleSeat.setSchedule(schedule);
                    scheduleSeat.setSeat(seat);
                    scheduleSeat.setStatus(
                            ScheduleSeatStatus.AVAILABLE
                    );
                    scheduleSeat.setHeldUntil(null);
                    scheduleSeat.setHeldBy(null);
                    scheduleSeat.setVersion(0);

                    scheduleSeatRepository.save(scheduleSeat);
                }
            }
        }

        List<ScheduleSeat> scheduleSeats =
                scheduleSeatRepository
                        .findByScheduleOrderBySeatSeatNumber(schedule);

        for (ScheduleSeat scheduleSeat : scheduleSeats) {

            normalizeExpiredHold(scheduleSeat);

            responses.add(mapToResponse(scheduleSeat));
        }

        return responses;
    }

    @Override
    @Transactional
    public List<ScheduleSeatResponse> getScheduleSeats(
            Long scheduleId
    ) {

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Schedule not found"));

        List<ScheduleSeat> scheduleSeats =
                scheduleSeatRepository
                        .findByScheduleOrderBySeatSeatNumber(schedule);

        List<ScheduleSeatResponse> responses = new ArrayList<>();

        for (ScheduleSeat scheduleSeat : scheduleSeats) {

            normalizeExpiredHold(scheduleSeat);

            responses.add(mapToResponse(scheduleSeat));
        }

        return responses;
    }

    @Override
    @Transactional
    public SeatHoldResponse holdSeat(
            Long scheduleSeatId,
            String userEmail
    ) {

        ScheduleSeat scheduleSeat =
                scheduleSeatRepository.findByIdForUpdate(scheduleSeatId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Schedule seat not found"
                                ));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        LocalDateTime now = LocalDateTime.now();

        if (scheduleSeat.getStatus() == ScheduleSeatStatus.BOOKED) {

            throw new IllegalStateException(
                    "Seat is already booked"
            );
        }

        if (scheduleSeat.getStatus() == ScheduleSeatStatus.BLOCKED) {

            throw new IllegalStateException(
                    "Seat is blocked"
            );
        }

        if (scheduleSeat.getStatus() == ScheduleSeatStatus.HELD) {

            if (scheduleSeat.getHeldUntil() != null &&
                    scheduleSeat.getHeldUntil().isAfter(now)) {

                if (scheduleSeat.getHeldBy() != null &&
                        scheduleSeat.getHeldBy().getId().equals(user.getId())) {

                    throw new IllegalStateException(
                            "You already hold this seat"
                    );
                }

                throw new IllegalStateException(
                        "Seat is currently held by another user"
                );
            }

            scheduleSeat.setStatus(
                    ScheduleSeatStatus.AVAILABLE
            );

            scheduleSeat.setHeldBy(null);
            scheduleSeat.setHeldUntil(null);
        }

        LocalDateTime heldUntil =
                now.plusMinutes(10);

        scheduleSeat.setStatus(
                ScheduleSeatStatus.HELD
        );

        scheduleSeat.setHeldBy(user);
        scheduleSeat.setHeldUntil(heldUntil);

        ScheduleSeat saved =
                scheduleSeatRepository.save(scheduleSeat);

        return mapToHoldResponse(saved);
    }

    private void normalizeExpiredHold(
            ScheduleSeat scheduleSeat
    ) {

        if (scheduleSeat.getStatus() != ScheduleSeatStatus.HELD) {
            return;
        }

        LocalDateTime heldUntil =
                scheduleSeat.getHeldUntil();

        if (heldUntil == null) {
            return;
        }

        if (!heldUntil.isAfter(LocalDateTime.now())) {

            scheduleSeat.setStatus(
                    ScheduleSeatStatus.AVAILABLE
            );

            scheduleSeat.setHeldBy(null);
            scheduleSeat.setHeldUntil(null);

            scheduleSeatRepository.save(scheduleSeat);
        }
    }

    private ScheduleSeatResponse mapToResponse(
            ScheduleSeat scheduleSeat
    ) {

        Seat seat = scheduleSeat.getSeat();
        Coach coach = seat.getCoach();

        ScheduleSeatResponse response =
                new ScheduleSeatResponse();

        response.setId(
                scheduleSeat.getId()
        );

        response.setScheduleId(
                scheduleSeat.getSchedule().getId()
        );

        response.setSeatId(
                seat.getId()
        );

        response.setCoachNumber(
                coach.getCoachNumber()
        );

        response.setSeatNumber(
                seat.getSeatNumber()
        );

        response.setSeatType(
                seat.getSeatType().name()
        );

        response.setStatus(
                scheduleSeat.getStatus().name()
        );

        response.setHeldUntil(
                scheduleSeat.getHeldUntil()
        );

        response.setHeldBy(
                scheduleSeat.getHeldBy() != null
                        ? scheduleSeat.getHeldBy().getId()
                        : null
        );

        response.setVersion(
                scheduleSeat.getVersion()
        );

        return response;
    }

    private SeatHoldResponse mapToHoldResponse(
            ScheduleSeat scheduleSeat
    ) {

        Seat seat = scheduleSeat.getSeat();
        Coach coach = seat.getCoach();

        SeatHoldResponse response =
                new SeatHoldResponse();

        response.setScheduleSeatId(
                scheduleSeat.getId()
        );

        response.setScheduleId(
                scheduleSeat.getSchedule().getId()
        );

        response.setSeatId(
                seat.getId()
        );

        response.setCoachNumber(
                coach.getCoachNumber()
        );

        response.setSeatNumber(
                seat.getSeatNumber()
        );

        response.setSeatType(
                seat.getSeatType().name()
        );

        response.setStatus(
                scheduleSeat.getStatus().name()
        );

        response.setHeldBy(
                scheduleSeat.getHeldBy() != null
                        ? scheduleSeat.getHeldBy().getId()
                        : null
        );

        response.setHeldUntil(
                scheduleSeat.getHeldUntil()
        );

        return response;
    }
}