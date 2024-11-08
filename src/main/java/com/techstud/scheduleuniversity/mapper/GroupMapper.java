package com.techstud.scheduleuniversity.mapper;

import com.techstud.scheduleuniversity.entity.schedule.Group;
import com.techstud.scheduleuniversity.entity.schedule.University;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GroupMapper {
    public List<Group> mapDtoToEntity(List<String> dtos, University university) {
        return dtos.stream()
                .map(dto -> {
                    Group group = new Group();
                    group.setUniversity(university);
                    group.setGroupCode(dto);
                    return group;
                })
                .toList();
    }
}
