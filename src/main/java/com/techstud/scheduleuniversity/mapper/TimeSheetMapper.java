package com.techstud.scheduleuniversity.mapper;

import com.techstud.scheduleuniversity.dao.document.schedule.TimeSheetDocument;
import com.techstud.scheduleuniversity.dto.parser.response.TimeSheetParserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public class TimeSheetMapper {

    public TimeSheetDocument toDocument(TimeSheetParserResponse timeSheet) {
        TimeSheetDocument timeSheetResult = new TimeSheetDocument();
        timeSheetResult.setFrom(timeSheet.getFrom());
        timeSheetResult.setTo(timeSheet.getTo());
        return timeSheetResult;
    }
}
