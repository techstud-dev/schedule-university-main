package com.techstud.scheduleuniversity.repository;

import com.techstud.scheduleuniversity.entity.Place;
import com.techstud.scheduleuniversity.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    Optional<Place> findPlaceByPlaceNameAndUniversity(String placeName, University university);

}
