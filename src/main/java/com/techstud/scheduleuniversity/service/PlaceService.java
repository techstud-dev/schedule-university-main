package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.entity.Place;
import com.techstud.scheduleuniversity.entity.University;

import java.util.List;

public interface PlaceService {

    Place saveOrUpdate(Place place);

    List<Place> saveOrUpdateAll(List<Place> places);

    Place findByNameAndUniversity(String name, University university);
}
