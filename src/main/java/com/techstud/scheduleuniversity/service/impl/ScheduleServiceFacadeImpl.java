package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.parser.request.ParsingTask;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleApiResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleItem;
import com.techstud.scheduleuniversity.entity.Group;
import com.techstud.scheduleuniversity.entity.Schedule;
import com.techstud.scheduleuniversity.entity.Student;
import com.techstud.scheduleuniversity.entity.University;
import com.techstud.scheduleuniversity.exception.*;
import com.techstud.scheduleuniversity.kafka.KafkaMessageObserver;
import com.techstud.scheduleuniversity.kafka.KafkaProducer;
import com.techstud.scheduleuniversity.mapper.Mapper;
import com.techstud.scheduleuniversity.repository.GroupRepository;
import com.techstud.scheduleuniversity.repository.StudentRepository;
import com.techstud.scheduleuniversity.repository.UniversityRepository;
import com.techstud.scheduleuniversity.service.GroupService;
import com.techstud.scheduleuniversity.service.ParserService;
import com.techstud.scheduleuniversity.service.ScheduleServiceFacade;
import com.techstud.scheduleuniversity.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleServiceFacadeImpl implements ScheduleServiceFacade {

    private final StudentService studentService;
    private final Mapper<Schedule, EntityModel<ScheduleApiResponse>> scheduleMapper;
    private final ParserService parserService;
    private final Mapper<ScheduleParserResponse, Schedule> parserResponseMapper;
    private final GroupService groupService;

    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> importSchedule(ImportDto importDto, String username)
            throws ParserException, ParserResponseTimeoutException, RequestException {
        Student student = studentService.findByUsername(username);

        if (!student.getGroup().getGroupCode().equalsIgnoreCase(importDto.getGroupCode())) {
            throw new RequestException("Group in import dto +'" + importDto.getGroupCode() + "' and student group '"
                    + student.getGroup().getGroupCode() +"' not match!");
        }

        Schedule returnedSchedule = student.getPersonalSchedule();

        if (returnedSchedule != null) {
            log.info("For student '{}' found personal schedule with id = {}", username, returnedSchedule.getId());
            return scheduleMapper.map(returnedSchedule);
        }

        returnedSchedule = student.getGroup().getGroupSchedule();

        if (returnedSchedule != null) {
            log.info("For student '{}' found group schedule with id = {}", username, returnedSchedule.getId());
            return scheduleMapper.map(returnedSchedule);
        }

        ScheduleParserResponse parserResponse = parserService.parseSchedule(importDto, student);

        returnedSchedule = parserResponseMapper.map(parserResponse);

        Group studentGroup = student.getGroup();
        studentGroup.setGroupSchedule(returnedSchedule);
        studentGroup = groupService.saveOrUpdate(studentGroup);
        student.setGroup(studentGroup);
        studentService.saveOrUpdate(student);
        if (returnedSchedule != null) {
           log.info("For student '{}' parsed schedule from university '{}', group '{}', scheduleId: {}",
                   username, importDto.getUniversityShortName(), importDto.getGroupCode(), returnedSchedule.getId());
           return scheduleMapper.map(returnedSchedule);
        }
        throw new ParserException("Null returned schedule!");
    }

    @Override
    public EntityModel<ScheduleApiResponse> forceImportSchedule(ImportDto importDto, String username)
            throws ParserException, ScheduleNotFoundException {
        return null;
    }

    @Override
    public EntityModel<ScheduleApiResponse> createSchedule(ScheduleParserResponse saveDto, String username) {
        return null;
    }

    @Override
    public void deleteSchedule(Long id, String username) throws ScheduleNotFoundException, StudentNotFoundException {

    }

    @Override
    public EntityModel<ScheduleApiResponse> getScheduleById(Long scheduleId) throws ScheduleNotFoundException {
        return null;
    }

    @Override
    public EntityModel<ScheduleApiResponse> getScheduleByStudent(String username) {
        return null;
    }

}