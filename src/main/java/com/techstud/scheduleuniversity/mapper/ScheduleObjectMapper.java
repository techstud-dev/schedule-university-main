package com.techstud.scheduleuniversity.mapper;

import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleObjectDocument;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleObjectParserResponse;
import com.techstud.scheduleuniversity.dto.ScheduleType;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleObjectApiResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.ArrayList;
import java.util.List;

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

    public ScheduleObjectParserResponse toResponse(ScheduleObjectDocument scheduleObject) {
         ScheduleObjectParserResponse scheduleObjectResult = new ScheduleObjectParserResponse();
         scheduleObjectResult.setGroups(scheduleObject.getGroups());
         scheduleObjectResult.setPlace(scheduleObject.getPlace());
         scheduleObjectResult.setTeacher(scheduleObject.getTeacher());
         scheduleObjectResult.setName(scheduleObject.getName());
         scheduleObjectResult.setType(ScheduleType.valueOf(scheduleObject.getType().name()));
         return scheduleObjectResult;
    }

    public List<ScheduleObjectParserResponse> toResponse(List<ScheduleObjectDocument> scheduleObjects) {
         List<ScheduleObjectParserResponse> list = new ArrayList<>(scheduleObjects.size());
         for (ScheduleObjectDocument scheduleObject : scheduleObjects) {
             list.add(toResponse(scheduleObject));
         }
         return list;
    }

    public ScheduleObjectApiResponse toApiResponse(ScheduleObjectDocument scheduleObject){
         ScheduleObjectApiResponse scheduleObjectResult = new ScheduleObjectApiResponse();
         scheduleObjectResult.setGroups(scheduleObject.getGroups());
         scheduleObjectResult.setPlace(scheduleObject.getPlace());
         scheduleObjectResult.setTeacher(scheduleObject.getTeacher());
         scheduleObjectResult.setName(scheduleObject.getName());
        scheduleObjectResult.setType(scheduleObject.getType().toString());
         return scheduleObjectResult;
    }

    public ScheduleObjectDocument fromApiResponse(ScheduleObjectApiResponse scheduleObjectApiResponse) {
         ScheduleObjectDocument scheduleObjectDocument = new ScheduleObjectDocument();
         scheduleObjectDocument.setGroups(scheduleObjectApiResponse.getGroups());
         scheduleObjectDocument.setPlace(scheduleObjectApiResponse.getPlace());
         scheduleObjectDocument.setTeacher(scheduleObjectApiResponse.getTeacher());
         scheduleObjectDocument.setName(scheduleObjectApiResponse.getName());
         scheduleObjectDocument.setType(ScheduleType.valueOf(scheduleObjectApiResponse.getType()));
         return scheduleObjectDocument;
    }

}
