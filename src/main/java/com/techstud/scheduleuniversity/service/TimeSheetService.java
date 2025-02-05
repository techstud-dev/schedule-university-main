package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.entity.TimeSheet;

import java.util.List;

public interface TimeSheetService {

    TimeSheet saveOrUpdate(TimeSheet timeSheet);

    List<TimeSheet> saveOrUpdateAll(List<TimeSheet> timeSheets);

}
