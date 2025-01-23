package com.techstud.scheduleuniversity.mapper;

import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleObjectDocument;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleObjectParserResponse;
import com.techstud.scheduleuniversity.dto.ScheduleType;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleItem;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.ArrayList;
import java.util.List;

import static com.techstud.scheduleuniversity.dto.ScheduleType.ruValueOf;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public class ScheduleObjectMapper {

     public ScheduleObjectDocument toDocument(ScheduleObjectParserResponse scheduleObject) {
        ScheduleObjectDocument scheduleObjectResult = new ScheduleObjectDocument();
        scheduleObjectResult.setGroups(scheduleObject.getGroups());
        scheduleObjectResult.setPlace(scheduleObject.getPlace());
        scheduleObjectResult.setTeacher(scheduleObject.getTeacher());
        scheduleObjectResult.setName(scheduleObject.getName());
        scheduleObjectResult.setType(ScheduleType.valueOf(scheduleObject.getType().name()));
        return scheduleObjectResult;
    }

    public List<ScheduleObjectDocument> toDocument(List<ScheduleObjectParserResponse> scheduleObjects) {
        List<ScheduleObjectDocument> list = new ArrayList<>(scheduleObjects.size());
        for (ScheduleObjectParserResponse scheduleObject : scheduleObjects) {
            list.add(toDocument(scheduleObject));
        }
        return list;
    }

    public ScheduleObjectDocument toDocument(ScheduleItem item){
         ScheduleObjectDocument scheduleObjectResult = new ScheduleObjectDocument();
         scheduleObjectResult.setGroups(item.getGroups());
         scheduleObjectResult.setPlace(item.getPlace());
         scheduleObjectResult.setTeacher(item.getTeacher());
         scheduleObjectResult.setName(item.getName());
         scheduleObjectResult.setType(ruValueOf(item.getType()));
         return scheduleObjectResult;
    }


}
