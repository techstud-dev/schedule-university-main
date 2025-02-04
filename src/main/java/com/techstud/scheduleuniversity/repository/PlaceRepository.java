package com.techstud.scheduleuniversity.repository;

import com.techstud.scheduleuniversity.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

}
