package com.techstud.scheduleuniversity.repository;

import com.techstud.scheduleuniversity.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    Optional<Lesson> findLessonByEvenWeekAndTimeSheetAndNameAndDayOfWeekAndTypeAndTeacherAndPlace(
            boolean evenWeek,
            TimeSheet timeSheet,
            String name,
            DayOfWeek dayOfWeek,
            LessonType type,
            Teacher teacher,
            Place place
    );
}
