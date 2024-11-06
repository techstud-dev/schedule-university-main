package com.techstud.scheduleuniversity.repository;

import com.techstud.scheduleuniversity.dto.parser.response.Schedule;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends R2dbcRepository<Schedule, Long> {
}
