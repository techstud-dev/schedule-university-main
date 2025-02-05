package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.entity.Lesson;

import java.util.List;

public interface LessonService {

    Lesson saveOrUpdate(Lesson lesson);

    List<Lesson> saveOrUpdateAll(List<Lesson> lessons);
}
