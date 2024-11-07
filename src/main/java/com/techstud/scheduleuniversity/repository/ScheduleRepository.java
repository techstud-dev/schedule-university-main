package com.techstud.scheduleuniversity.repository;

import com.techstud.scheduleuniversity.entity.schedule.Schedule;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ScheduleRepository extends R2dbcRepository<Schedule, Long> {

    @Query("SELECT * FROM schedule JOIN schedule_group ON schedule.group_id = schedule_group.id " +
            "WHERE schedule_group.group_name = :groupName")
    Mono<Schedule> findByGroupName(String groupName);
}
