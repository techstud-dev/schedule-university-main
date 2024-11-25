package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.response.schedule.Schedule;

public interface ScheduleService {

    Schedule importSchedule(ImportDto importDto);
}
