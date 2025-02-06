package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.entity.Schedule;

import java.util.List;

public interface ScheduleService {

    Schedule saveOrUpdate(Schedule schedule);

    List<Schedule> saveOrUpdateAll(List<Schedule> schedules);
}
