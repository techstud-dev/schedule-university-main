package com.techstud.scheduleuniversity.repository;

import com.techstud.scheduleuniversity.entity.schedule.ScheduleDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public interface ScheduleDayRepository extends JpaRepository<ScheduleDay, Long> {
    ScheduleDay findByDate(LocalDate date);
}