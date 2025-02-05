package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.entity.Place;

import java.util.List;

public interface PlaceService {

    Place saveOrUpdate(Place place);

    List<Place> saveOrUpdateAll(List<Place> places);
}
