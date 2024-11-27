package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDocument;
import com.techstud.scheduleuniversity.dto.ImportDto;

public interface ScheduleService {

    ScheduleDocument importSchedule(ImportDto importDto, String username);
}
