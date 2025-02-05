package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.entity.Place;
import com.techstud.scheduleuniversity.repository.PlaceRepository;
import com.techstud.scheduleuniversity.service.PlaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.TransientObjectException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceServiceImpl implements PlaceService {

    private final PlaceRepository placeRepository;

    @Override
    @Transactional
    public Place saveOrUpdate(Place place) {

        if (place.getUniversity().getId() == null) {
            throw new TransientObjectException("University in place: " + place + "out of context or not exists in db");
        }

        placeRepository
                .findPlaceByPlaceNameAndUniversity(place.getPlaceName(), place.getUniversity())
                .ifPresent(foundedPlace -> place.setId(foundedPlace.getId()));

        return placeRepository.save(place);
    }

    @Override
    @Transactional
    public List<Place> saveOrUpdateAll(List<Place> places) {
        Set<Place> resultSet = new HashSet<>();

        places.forEach(place -> resultSet.add(saveOrUpdate(place)));

        return resultSet.stream().toList();
    }
}
