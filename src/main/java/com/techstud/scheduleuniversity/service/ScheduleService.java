package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.entity.Schedule;
import com.techstud.scheduleuniversity.exception.ScheduleNotFoundException;

import java.util.List;

public interface ScheduleService {

    Schedule saveOrUpdate(Schedule schedule);

    List<Schedule> saveOrUpdateAll(List<Schedule> schedules);

    void deleteById(Long id);

    Schedule findById(Long id) throws ScheduleNotFoundException;
}
