package com.techstud.scheduleuniversity.mapper;

import com.techstud.scheduleuniversity.entity.schedule.TimeSheet;
import com.techstud.scheduleuniversity.entity.schedule.University;
import org.springframework.stereotype.Component;

@Component
public class TimeSheetMapper {

    public TimeSheet mapDtoToEntity(com.techstud.scheduleuniversity.dto.parser.response.TimeSheet dto, University university) {
        TimeSheet timeSheet = new TimeSheet();
        timeSheet.setUniversity(university);
        timeSheet.setTimeFrom(dto.getFrom());
        timeSheet.setTimeTo(dto.getTo());
        return timeSheet;
    }
}
