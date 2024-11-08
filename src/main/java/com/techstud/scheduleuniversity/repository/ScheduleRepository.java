package com.techstud.scheduleuniversity.repository;

import com.techstud.scheduleuniversity.entity.schedule.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("SELECT s FROM Schedule s JOIN s.group g WHERE g.groupName = :groupName")
    Schedule findByGroupName(String groupName);
}
