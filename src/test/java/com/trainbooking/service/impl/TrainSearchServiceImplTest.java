package com.trainbooking.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.trainbooking.dto.TrainSearchResponse;
import com.trainbooking.entity.RouteStop;
import com.trainbooking.entity.Schedule;
import com.trainbooking.entity.ScheduleSeat;
import com.trainbooking.entity.ScheduleSeatStatus;
import com.trainbooking.entity.ScheduleStatus;
import com.trainbooking.entity.Station;
import com.trainbooking.entity.Train;
import com.trainbooking.entity.TrainRoute;
import com.trainbooking.entity.TrainType;
import com.trainbooking.repository.RouteStopRepository;
import com.trainbooking.repository.ScheduleRepository;
import com.trainbooking.repository.ScheduleSeatRepository;
import com.trainbooking.repository.StationRepository;
import com.trainbooking.repository.TrainRouteRepository;

@ExtendWith(MockitoExtension.class)
class TrainSearchServiceImplTest {

    @Mock
    private StationRepository stationRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private TrainRouteRepository trainRouteRepository;

    @Mock
    private RouteStopRepository routeStopRepository;

    @Mock
    private ScheduleSeatRepository scheduleSeatRepository;

    @InjectMocks
    private TrainSearchServiceImpl trainSearchService;

    private Station sourceStation;
    private Station destinationStation;
    private Train train;
    private TrainRoute trainRoute;
    private Schedule schedule;

    @BeforeEach
    void setUp() {

        sourceStation = new Station();
        ReflectionTestUtils.setField(sourceStation, "id", 3L);
        sourceStation.setStationCode("MMCT");
        sourceStation.setStationName("Mumbai Central");
        sourceStation.setCity("Mumbai");
        sourceStation.setState("Maharashtra");
        sourceStation.setActive(true);

        destinationStation = new Station();
        ReflectionTestUtils.setField(destinationStation, "id", 1L);
        destinationStation.setStationCode("PUNE");
        destinationStation.setStationName("Pune Railway Station");
        destinationStation.setCity("Pune");
        destinationStation.setState("Maharashtra");
        destinationStation.setActive(true);

        train = new Train();
        ReflectionTestUtils.setField(train, "id", 1L);
        train.setTrainNumber("12124");
        train.setTrainName("Deccan Queen");
        train.setTrainType(TrainType.EXPRESS);
        train.setActive(true);

        trainRoute = new TrainRoute();
        ReflectionTestUtils.setField(trainRoute, "id", 1L);
        trainRoute.setTrain(train);
        trainRoute.setRouteName("Mumbai Pune Route");
        trainRoute.setActive(true);

        schedule = new Schedule();
        ReflectionTestUtils.setField(schedule, "id", 1L);
        schedule.setTrain(train);
        schedule.setJourneyDate(LocalDate.of(2026, 10, 1));
        schedule.setStatus(ScheduleStatus.SCHEDULED);
    }

    @Test
    void shouldReturnTrainWhenValidRouteExists() {

        RouteStop sourceStop = new RouteStop();
        ReflectionTestUtils.setField(sourceStop, "id", 1L);
        sourceStop.setTrainRoute(trainRoute);
        sourceStop.setStation(sourceStation);
        sourceStop.setStopSequence(1);
        sourceStop.setDepartureTime(LocalTime.of(6, 0));

        RouteStop destinationStop = new RouteStop();
        ReflectionTestUtils.setField(destinationStop, "id", 2L);
        destinationStop.setTrainRoute(trainRoute);
        destinationStop.setStation(destinationStation);
        destinationStop.setStopSequence(2);
        destinationStop.setArrivalTime(LocalTime.of(8, 15));

        ScheduleSeat availableSeat = new ScheduleSeat();

        when(stationRepository.findById(3L))
                .thenReturn(Optional.of(sourceStation));

        when(stationRepository.findById(1L))
                .thenReturn(Optional.of(destinationStation));

        when(scheduleRepository.findByJourneyDateAndStatus(
                LocalDate.of(2026, 10, 1),
                ScheduleStatus.SCHEDULED))
                .thenReturn(List.of(schedule));

        when(trainRouteRepository.findByTrainAndActiveTrue(train))
                .thenReturn(List.of(trainRoute));

        when(routeStopRepository.findByTrainRouteOrderByStopSequence(trainRoute))
                .thenReturn(List.of(sourceStop, destinationStop));

        when(scheduleSeatRepository.findByScheduleIdAndStatus(
                1L,
                ScheduleSeatStatus.AVAILABLE))
                .thenReturn(List.of(availableSeat));

        List<TrainSearchResponse> results =
                trainSearchService.searchTrains(
                        3L,
                        1L,
                        LocalDate.of(2026, 10, 1)
                );

        assertEquals(1, results.size());

        TrainSearchResponse response = results.get(0);

        assertEquals(1L, response.getScheduleId());
        assertEquals(1L, response.getTrainId());
        assertEquals("12124", response.getTrainNumber());
        assertEquals("Deccan Queen", response.getTrainName());
        assertEquals("EXPRESS", response.getTrainType());
        assertEquals("Mumbai Central", response.getFromStation());
        assertEquals("Pune Railway Station", response.getToStation());
        assertEquals(LocalTime.of(6, 0), response.getDepartureTime());
        assertEquals(LocalTime.of(8, 15), response.getArrivalTime());
        assertEquals(1, response.getAvailableSeats());
    }

    @Test
    void shouldReturnEmptyListWhenRouteIsReverseDirection() {

        RouteStop sourceStop = new RouteStop();
        ReflectionTestUtils.setField(sourceStop, "id", 1L);
        sourceStop.setTrainRoute(trainRoute);
        sourceStop.setStation(destinationStation);
        sourceStop.setStopSequence(2);

        RouteStop destinationStop = new RouteStop();
        ReflectionTestUtils.setField(destinationStop, "id", 2L);
        destinationStop.setTrainRoute(trainRoute);
        destinationStop.setStation(sourceStation);
        destinationStop.setStopSequence(1);

        when(stationRepository.findById(1L))
                .thenReturn(Optional.of(destinationStation));

        when(stationRepository.findById(3L))
                .thenReturn(Optional.of(sourceStation));

        when(scheduleRepository.findByJourneyDateAndStatus(
                LocalDate.of(2026, 10, 1),
                ScheduleStatus.SCHEDULED))
                .thenReturn(List.of(schedule));

        when(trainRouteRepository.findByTrainAndActiveTrue(train))
                .thenReturn(List.of(trainRoute));

        when(routeStopRepository.findByTrainRouteOrderByStopSequence(trainRoute))
                .thenReturn(List.of(destinationStop, sourceStop));

        List<TrainSearchResponse> results =
                trainSearchService.searchTrains(
                        1L,
                        3L,
                        LocalDate.of(2026, 10, 1)
                );

        assertEquals(0, results.size());
    }

    @Test
    void shouldRejectSameSourceAndDestination() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> trainSearchService.searchTrains(
                                3L,
                                3L,
                                LocalDate.of(2026, 10, 1)
                        )
                );

        assertEquals(
                "Source and destination stations cannot be the same",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectInactiveSourceStation() {

        sourceStation.setActive(false);

        when(stationRepository.findById(3L))
                .thenReturn(Optional.of(sourceStation));

        when(stationRepository.findById(1L))
                .thenReturn(Optional.of(destinationStation));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> trainSearchService.searchTrains(
                                3L,
                                1L,
                                LocalDate.of(2026, 10, 1)
                        )
                );

        assertEquals(
                "Source station is inactive",
                exception.getMessage()
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoScheduleExists() {

        when(stationRepository.findById(3L))
                .thenReturn(Optional.of(sourceStation));

        when(stationRepository.findById(1L))
                .thenReturn(Optional.of(destinationStation));

        when(scheduleRepository.findByJourneyDateAndStatus(
                LocalDate.of(2026, 10, 1),
                ScheduleStatus.SCHEDULED))
                .thenReturn(List.of());

        List<TrainSearchResponse> results =
                trainSearchService.searchTrains(
                        3L,
                        1L,
                        LocalDate.of(2026, 10, 1)
                );

        assertEquals(0, results.size());
    }
}