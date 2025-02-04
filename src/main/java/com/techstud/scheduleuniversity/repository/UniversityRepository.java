package com.techstud.scheduleuniversity.repository;

import com.techstud.scheduleuniversity.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UniversityRepository extends JpaRepository<Long, University> {

    Optional<University> findByshortName(String shortName);
}
