package com.techstud.scheduleuniversity.repository;

import com.techstud.scheduleuniversity.entity.TimeSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.Optional;

@Repository
public interface TimeSheetRepository extends JpaRepository<TimeSheet, Long> {

    Optional<TimeSheet> findTimeSheetByFromTimeAndToTime(LocalTime fromTime, LocalTime toTime);
}
