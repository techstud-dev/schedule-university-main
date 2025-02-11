package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.entity.TimeSheet;

import java.time.LocalTime;
import java.util.List;

public interface TimeSheetService {

    TimeSheet saveOrUpdate(TimeSheet timeSheet);

    List<TimeSheet> saveOrUpdateAll(List<TimeSheet> timeSheets);

    TimeSheet findByStandardPattern(String pattern);

    TimeSheet findByTimeFromAndTimeTo(LocalTime from, LocalTime to);

}
