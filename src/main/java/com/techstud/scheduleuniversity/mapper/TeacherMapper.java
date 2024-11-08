package com.techstud.scheduleuniversity.mapper;

import com.techstud.scheduleuniversity.entity.schedule.Teacher;
import com.techstud.scheduleuniversity.entity.schedule.University;
import org.springframework.stereotype.Component;

@Component
public class TeacherMapper {

    public Teacher mapDtoToEntity(String dto, University university) {
        Teacher teacher = new Teacher();
        teacher.setUniversity(university);
        teacher.setName(dto);
        return teacher;
    }
}
