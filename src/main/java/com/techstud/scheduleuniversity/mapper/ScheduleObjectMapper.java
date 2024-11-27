package com.techstud.scheduleuniversity.mapper;

import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleObject;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public class ScheduleObjectMapper {

     public ScheduleObject toDocument(com.techstud.scheduleuniversity.dto.parser.response.ScheduleObject scheduleObject) {
        ScheduleObject scheduleObjectResult = new ScheduleObject();
        scheduleObjectResult.setGroups(scheduleObject.getGroups());
        scheduleObjectResult.setPlace(scheduleObject.getPlace());
        scheduleObjectResult.setTeacher(scheduleObject.getTeacher());
        scheduleObjectResult.setName(scheduleObject.getName());
        scheduleObjectResult.setType(ScheduleType.valueOf(scheduleObject.getType().name()));
        return scheduleObjectResult;
    }

    public List<ScheduleObject> toDocument(List<com.techstud.scheduleuniversity.dto.parser.response.ScheduleObject> scheduleObjects) {
        List<ScheduleObject> list = new ArrayList<>(scheduleObjects.size());
        for (com.techstud.scheduleuniversity.dto.parser.response.ScheduleObject scheduleObject : scheduleObjects) {
            list.add(toDocument(scheduleObject));
        }
        return list;
    }


}
