package com.techstud.scheduleuniversity.repository;

import com.techstud.scheduleuniversity.entity.schedule.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UniversityRepository extends JpaRepository<University, Long> {

    University findByName(String name);

}
