package com.techstud.scheduleuniversity.mapper;

import com.techstud.scheduleuniversity.entity.schedule.Place;
import com.techstud.scheduleuniversity.entity.schedule.University;
import org.springframework.stereotype.Component;

@Component
public class PlaceMapper {

    public Place mapDtoToEntity(String dto, University university) {
        Place place = new Place();
        place.setName(dto);
        place.setUniversity(university);
        return place;
    }
}
