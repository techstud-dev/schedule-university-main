package com.techstud.scheduleuniversity.mapper;

import com.techstud.scheduleuniversity.dao.document.schedule.TimeSheet;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public class TimeSheetMapper {

    public TimeSheet toDocument(com.techstud.scheduleuniversity.dto.parser.response.TimeSheet timeSheet) {
        TimeSheet timeSheetResult = new TimeSheet();
        timeSheetResult.setFrom(timeSheet.getFrom());
        timeSheetResult.setTo(timeSheet.getTo());
        return timeSheetResult;
    }
}
