package com.techstud.scheduleuniversity.repository;

import com.techstud.scheduleuniversity.entity.Lesson;
import com.techstud.scheduleuniversity.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    Optional<Schedule> findByLessonListIn(List<Lesson> lessonList);

}
