package com.techstud.scheduleuniversity.repository;

import com.techstud.scheduleuniversity.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository extends JpaRepository<Long, Lesson> {

}
