package com.techstud.scheduleuniversity.mapper;

import com.techstud.scheduleuniversity.dto.parser.response.ScheduleObject;
import com.techstud.scheduleuniversity.entity.schedule.ScheduleObjectMapping;
import com.techstud.scheduleuniversity.entity.schedule.ScheduleType;
import com.techstud.scheduleuniversity.entity.schedule.TimeSheet;
import com.techstud.scheduleuniversity.entity.schedule.University;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScheduleObjectMapper {

    private final PlaceMapper placeMapper;
    private final TeacherMapper teacherMapper;
    private final GroupMapper groupMapper;

    public ScheduleObjectMapping mapDtoToEntity(List<ScheduleObject> dto, TimeSheet timeSheet, University university) {
        ScheduleObjectMapping scheduleObjectMapping = new ScheduleObjectMapping();
        scheduleObjectMapping.setTimeSheet(timeSheet);
        scheduleObjectMapping.setScheduleObjects(mapScheduleObjects(dto, university));
        return scheduleObjectMapping;
    }

    private List<com.techstud.scheduleuniversity.entity.schedule.ScheduleObject> mapScheduleObjects(List<ScheduleObject> dto, University university) {
        List<com.techstud.scheduleuniversity.entity.schedule.ScheduleObject> scheduleObjectsEntity = new ArrayList<>();
        dto.forEach(scheduleObject -> {
            com.techstud.scheduleuniversity.entity.schedule.ScheduleObject scheduleObjectEntity = new com.techstud.scheduleuniversity.entity.schedule.ScheduleObject();
            scheduleObjectEntity.setType(ScheduleType.valueOf(scheduleObject.getType().toString()));
            scheduleObjectEntity.setName(scheduleObject.getName());
            scheduleObjectEntity.setPlace(placeMapper.mapDtoToEntity(scheduleObject.getPlace(), university));
            scheduleObjectEntity.setTeacher(teacherMapper.mapDtoToEntity(scheduleObject.getTeacher(), university));
            scheduleObjectEntity.setGroups(groupMapper.mapDtoToEntity(scheduleObject.getGroups(), university));
            scheduleObjectsEntity.add(scheduleObjectEntity);
        });
        return scheduleObjectsEntity;
    }
}
