package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.dao.document.schedule.Schedule;
import com.techstud.scheduleuniversity.dto.ImportDto;

public interface ScheduleService {

    Schedule importSchedule(ImportDto importDto, String username);
}
