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
import com.techstud.scheduleuniversity.service.ScheduleService;
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
public class ScheduleServiceImpl implements ScheduleService {

    private final KafkaProducer kafkaProducer;
    private final KafkaMessageObserver messageObserver;
    private final GroupRepository groupRepository;
    private final UniversityRepository universityRepository;
    private final StudentRepository studentRepository;
    private final Mapper<Schedule, EntityModel<ScheduleApiResponse>> scheduleMapper;
    private final Mapper<ScheduleParserResponse, Schedule> parserMapper;

    /**
     * Импорт расписания (пытаемся найти существующее, если нет — парсим и сохраняем).
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> importSchedule(ImportDto importDto, String username)
            throws ResourceNotFoundException, ParserException, ParserResponseTimeoutException {
        Student student = studentRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Student with username '" + username + "' not found"));

        Schedule currentSchedule = student.getPersonalSchedule();

        if (currentSchedule != null) {
            log.warn("Found personal schedule by student. Return this: {}", currentSchedule);
           return scheduleMapper.map(currentSchedule);
        }

        University university = universityRepository
                .findByShortName(importDto.getUniversityShortName())
                .orElseThrow(() -> new ResourceNotFoundException("University '" + importDto.getUniversityShortName() + "' not found"));

        Group currentGroup = groupRepository
                .findByUniversityAndGroupCode(university, importDto.getGroupCode())
                .orElseThrow(() -> new ResourceNotFoundException("Group '" + importDto.getGroupCode() + "' by university '"
                        +  importDto.getUniversityShortName() + "' not found"));

        currentSchedule = currentGroup.getGroupSchedule();

        if (currentSchedule != null) {
            log.info("Found schedule in db: {}", currentSchedule);
            return scheduleMapper.map(currentSchedule);
        } else {
            ParsingTask task = ParsingTask
                    .builder()
                    .groupId(currentGroup.getUniversityGroupId())
                    .universityName(university.getShortName())
                    .build();
            Schedule parsedSchedule = parseSchedule(task);
            parsedSchedule.getMetadata().put("universityShortName", university.getShortName());
            parsedSchedule.getMetadata().put("username", student.getUsername());
            log.info("Sending parsingTask to parser: {}", task);
            return scheduleMapper.map(parsedSchedule);
        }
    }

    /**
     * Принудительный импорт расписания (всегда парсим, независимо от наличия).
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> forceImportSchedule(ImportDto importDto, String username)
            throws ParserException, ScheduleNotFoundException {
        return null;
    }

    /**
     * Сохранение расписания, переданного напрямую (без парсинга Kafka).
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> createSchedule(ScheduleParserResponse saveDto, String username) {
        return null;
    }

    /**
     * Удаление всего расписания у конкретного студента.
     */
    @Override
    @Transactional
    public void deleteSchedule(Long scheduleId, String username)
            throws ScheduleNotFoundException, StudentNotFoundException {

    }

    /**
     * Удаление одного конкретного дня расписания.
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> deleteScheduleDay(String dayId, String username, boolean isEvenWeek)
            throws ScheduleNotFoundException, StudentNotFoundException {
        return null;
    }

    /**
     * Удаление конкретного занятия (lesson) из дня расписания.
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> deleteLesson(String dayOfWeek, Long timeWindowId, String username)
            throws ScheduleNotFoundException, StudentNotFoundException {
        return null;
    }

    /**
     * Получение расписания напрямую по его ID.
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> getScheduleById(Long scheduleId) throws ScheduleNotFoundException {
        return null;
    }

    /**
     * Получение конкретного занятия по расписанию студента, дню и тайм-слоту.
     */
    @Override
    @Transactional
    public CollectionModel<EntityModel<ScheduleItem>> getLessonByStudentAndScheduleDayAndTimeWindow(
            String studentName, String dayOfWeek, Long timeWindowId)
            throws ScheduleNotFoundException, StudentNotFoundException {

        return null;
    }

    /**
     * Обновление конкретного занятия (через smartLessonUpdate).
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> updateLesson(String dayOfWeek,
                                                         Long timeWindowId,
                                                         ScheduleItem scheduleItem,
                                                         String username)
            throws ScheduleNotFoundException, StudentNotFoundException {

        return null;
    }

    /**
     * Обновление дня расписания (через smartScheduleDayUpdate).
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> updateScheduleDay(String dayId,
                                                              List<ScheduleItem> scheduleItems,
                                                              String username,
                                                              boolean isEvenWeek)
            throws ScheduleNotFoundException, StudentNotFoundException {

        return null;
    }

    /**
     * Создание нового дня расписания.
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> createScheduleDay(List<ScheduleItem> scheduleItems, String username)
            throws ScheduleNotFoundException, StudentNotFoundException, ResourceExistsException {
        return null;
    }

    /**
     * Получение расписания, привязанного к студенту, по username.
     */
    @Override
    @Transactional(readOnly = true)
    public EntityModel<ScheduleApiResponse> getScheduleByStudent(String username)
            throws ScheduleNotFoundException, StudentNotFoundException {
        return null;
    }

    /**
     * Получение всех занятий за день из расписания студента.
     */
    @Override
    @Transactional(readOnly = true)
    public CollectionModel<EntityModel<ScheduleItem>> getLessonsByStudentAndScheduleDay(String username,
                                                                                        String dayOfWeek,
                                                                                        boolean isEvenWeek)
            throws ScheduleNotFoundException, StudentNotFoundException {
        return null;
    }

    /**
     * Создание нового занятия (Lesson) в конкретный день расписания.
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> createLesson(ScheduleItem scheduleItem, String username)
            throws ScheduleNotFoundException, StudentNotFoundException {
        return null;
    }

    private Schedule parseSchedule(ParsingTask task) throws ParserException, ParserResponseTimeoutException {
        UUID messageId = kafkaProducer.sendToParsingQueue(task);
        messageObserver.registerMessage(messageId);
        ScheduleParserResponse response = messageObserver.waitForParserResponse(messageId);
        return parserMapper.map(response);
    }
}
