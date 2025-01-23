package com.techstud.scheduleuniversity.mapper;

import com.techstud.scheduleuniversity.dao.document.schedule.TimeSheetDocument;
import com.techstud.scheduleuniversity.dto.parser.response.TimeSheetParserResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleItem;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.time.LocalTime;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public class TimeSheetMapper {

    public TimeSheetDocument toDocument(TimeSheetParserResponse timeSheet) {
        TimeSheetDocument timeSheetResult = new TimeSheetDocument();
        timeSheetResult.setFrom(timeSheet.getFrom());
        timeSheetResult.setTo(timeSheet.getTo());
        return timeSheetResult;
    }

    public TimeSheetDocument toDocument(ScheduleItem item){
        LocalTime to = LocalTime.parse(item.getTime().split("-")[0]);
        LocalTime from = LocalTime.parse(item.getTime().split("-")[1]);

        TimeSheetDocument timeSheetResult = new TimeSheetDocument();
        timeSheetResult.setTo(to);
        timeSheetResult.setFrom(from);
        return timeSheetResult;
    }
}
