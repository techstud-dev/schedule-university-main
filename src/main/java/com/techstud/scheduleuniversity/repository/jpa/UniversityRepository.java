package com.techstud.scheduleuniversity.repository.jpa;

import com.techstud.scheduleuniversity.dao.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UniversityRepository extends JpaRepository<University, Long> {

    Optional<University> findByShortName(String shortName);
}
