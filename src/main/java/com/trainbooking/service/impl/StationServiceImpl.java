package com.trainbooking.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trainbooking.dto.StationRequest;
import com.trainbooking.dto.StationResponse;
import com.trainbooking.entity.Station;
import com.trainbooking.exception.DuplicateResourceException;
import com.trainbooking.exception.ResourceNotFoundException;
import com.trainbooking.repository.StationRepository;
import com.trainbooking.service.StationService;

@Service
@Transactional
public class StationServiceImpl implements StationService {

    private final StationRepository stationRepository;

    public StationServiceImpl(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @Override
    public StationResponse createStation(StationRequest request) {

        if (stationRepository.existsByStationCode(request.getStationCode())) {
            throw new DuplicateResourceException("Station code already exists");
        }

        Station station = new Station();

        station.setStationCode(request.getStationCode());
        station.setStationName(request.getStationName());
        station.setCity(request.getCity());
        station.setState(request.getState());
        station.setActive(true);

        Station savedStation = stationRepository.save(station);

        return mapToResponse(savedStation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StationResponse> getAllStations() {

        return stationRepository.findAll()
                .stream()
                .filter(station -> Boolean.TRUE.equals(station.getActive()))
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StationResponse getStationById(Long id) {

        Station station = stationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Station not found with id: " + id));

        return mapToResponse(station);
    }

    @Override
    public StationResponse updateStation(Long id, StationRequest request) {

        Station station = stationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Station not found with id: " + id));

        if (!station.getStationCode().equals(request.getStationCode())
                && stationRepository.existsByStationCode(request.getStationCode())) {
            throw new DuplicateResourceException("Station code already exists");
        }

        station.setStationCode(request.getStationCode());
        station.setStationName(request.getStationName());
        station.setCity(request.getCity());
        station.setState(request.getState());

        Station updatedStation = stationRepository.save(station);

        return mapToResponse(updatedStation);
    }

    @Override
    public void deactivateStation(Long id) {

        Station station = stationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Station not found with id: " + id));

        station.setActive(false);

        stationRepository.save(station);
    }

    private StationResponse mapToResponse(Station station) {

        StationResponse response = new StationResponse();

        response.setId(station.getId());
        response.setStationCode(station.getStationCode());
        response.setStationName(station.getStationName());
        response.setCity(station.getCity());
        response.setState(station.getState());
        response.setActive(station.getActive());
        response.setCreatedAt(station.getCreatedAt());
        response.setUpdatedAt(station.getUpdatedAt());

        return response;
    }
}