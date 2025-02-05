package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.entity.Lesson;
import com.techstud.scheduleuniversity.repository.LessonRepository;
import com.techstud.scheduleuniversity.service.LessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.TransientObjectException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;

    //Throw extended runtime exception for rollback transaction
    //use this method after saving nested objects in lesson
    @Override
    @Transactional
    public Lesson saveOrUpdate(Lesson lesson) {

        if (lesson.getTimeSheet().getId() == null) {
            throw new TransientObjectException("Time sheet in lesson: " + lesson + "out of context or not exists in db");
        }

        if (lesson.getPlace().getId() == null) {
            throw new TransientObjectException("Place in lesson: " + lesson + "out of context or not exists in db");
        }

        if (lesson.getTeacher().getId() == null) {
            throw new TransientObjectException("Teacher in lesson: " + lesson + "out of context or not exists in db");
        }

        lessonRepository.findLessonByEvenWeekAndTimeSheetAndNameAndDayOfWeekAndTypeAndTeacherAndPlace(
                lesson.isEvenWeek(),
                lesson.getTimeSheet(),
                lesson.getName(),
                lesson.getDayOfWeek(),
                lesson.getType(),
                lesson.getTeacher(),
                lesson.getPlace()
        ).ifPresent(foundedLesson -> lesson.setId(foundedLesson.getId()));

        return lessonRepository.save(lesson);
    }

    @Override
    @Transactional
    public List<Lesson> saveOrUpdateAll(List<Lesson> lessons) {
        Set<Lesson> resultSet = new HashSet<>();

        lessons.forEach(lesson -> resultSet.add(saveOrUpdate(lesson)));

        return resultSet.stream().toList();
    }
}
