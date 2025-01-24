package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDayDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleObjectDocument;
import com.techstud.scheduleuniversity.dao.entity.Student;
import com.techstud.scheduleuniversity.dao.entity.UniversityGroup;
import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.parser.request.ParsingTask;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleApiResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleItem;
import com.techstud.scheduleuniversity.exception.*;
import com.techstud.scheduleuniversity.kafka.KafkaMessageObserver;
import com.techstud.scheduleuniversity.kafka.KafkaProducer;
import com.techstud.scheduleuniversity.mapper.ScheduleMapper;
import com.techstud.scheduleuniversity.repository.jpa.StudentRepository;
import com.techstud.scheduleuniversity.repository.jpa.UniversityGroupRepository;
import com.techstud.scheduleuniversity.repository.mongo.ScheduleDayRepository;
import com.techstud.scheduleuniversity.repository.mongo.ScheduleRepository;
import com.techstud.scheduleuniversity.repository.mongo.ScheduleRepositoryFacade;
import com.techstud.scheduleuniversity.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UniversityGroupRepository universityGroupRepository;
    private final ScheduleRepositoryFacade scheduleRepositoryFacade;
    private final KafkaProducer kafkaProducer;
    private final KafkaMessageObserver messageObserver;
    private final StudentRepository studentRepository;
    private final ScheduleDayRepository scheduleDayRepository;
    private final ScheduleMapper scheduleMapper;

    /**
     * Импорт расписания:
     * 1. Пытаемся найти готовое расписание для группы или студента.
     * 2. Если не нашли, вызываем парсер, сохраняем и возвращаем.
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> importSchedule(ImportDto importDto, String username)
            throws ScheduleNotFoundException, ParserException {
        log.info("Importing schedule for university: {}, group: {}", importDto.getUniversityName(), importDto.getGroupCode());

        Student student = getOrCreateStudent(username);
        UniversityGroup group = getUniversityGroup(importDto);

        // 1. Проверяем, есть ли уже расписание у группы или у студента
        ScheduleDocument existingSchedule = findExistingSchedule(group, student);
        if (existingSchedule != null) {
            return scheduleMapper.toResponse(existingSchedule);
        }

        log.warn("Schedule not found for group {}. Attempting to import from parser.", importDto.getGroupCode());

        // 2. Парсим и сохраняем
        ScheduleDocument scheduleDocument = parseAndSaveNewSchedule(group, student);
        if (scheduleDocument == null) {
            throw new ScheduleNotFoundException("Schedule not found for group " + importDto.getGroupCode());
        }
        return scheduleMapper.toResponse(scheduleDocument);
    }

    /**
     * Принудительный импорт расписания (всегда вызываем парсер, независимо от существующего расписания).
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> forceImportSchedule(ImportDto importDto, String username)
            throws ParserException, ScheduleNotFoundException {
        log.info("Forcing schedule import for university: {}, group: {}", importDto.getUniversityName(), importDto.getGroupCode());

        Student student = getOrCreateStudent(username);
        UniversityGroup group = getUniversityGroup(importDto);

        // Всегда парсим и перезаписываем
        ScheduleDocument scheduleDocument = parseAndSaveNewSchedule(group, student);
        if (scheduleDocument == null) {
            throw new ScheduleNotFoundException("Schedule not found for group " + importDto.getGroupCode());
        }
        return scheduleMapper.toResponse(scheduleDocument);
    }

    /**
     * Сохранение расписания, переданного напрямую (без парсинга Kafka).
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> createSchedule(ScheduleParserResponse saveDto, String username) {
        log.info("Saving schedule for user: {}", username);

        Student student = getOrCreateStudent(username);

        // Удаляем предыдущее расписание, если есть
        deleteExistingScheduleIfExists(student.getScheduleMongoId());

        // Сохраняем новое
        ScheduleDocument savedDocument = scheduleRepositoryFacade.cascadeSave(saveDto);
        updateStudentSchedule(student, savedDocument.getId());

        return scheduleMapper.toResponse(savedDocument);
    }

    /**
     * Удаление всего расписания у конкретного студента.
     */
    @Override
    @Transactional
    public void deleteSchedule(String scheduleId, String username)
            throws ScheduleNotFoundException, StudentNotFoundException {
        ScheduleDocument schedule = getScheduleByStudentOrThrow(username);

        // Сверяем, действительно ли это расписание принадлежит студенту
        if (!Objects.equals(schedule.getId(), scheduleId)) {
            throw new ScheduleNotFoundException("Schedule scheduleId does not match student's schedule scheduleId");
        }

        scheduleRepository.deleteById(scheduleId);

        // Очищаем у студента ссылку на расписание
        Student student = studentRepository.findByUsername(username)
                .orElseThrow(() -> new StudentNotFoundException("Student not found for username: " + username));
        student.setScheduleMongoId(null);
        studentRepository.save(student);
    }

    /**
     * Удаление одного конкретного дня расписания.
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> deleteScheduleDay(String dayId, String username)
            throws ScheduleNotFoundException, StudentNotFoundException {
        ScheduleDocument schedule = getScheduleByStudentOrThrow(username);

        ScheduleDayDocument scheduleDay = scheduleDayRepository.findById(dayId)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule day not found for id: " + dayId));

        ScheduleDocument updated = scheduleRepositoryFacade.smartScheduleDayDelete(schedule, scheduleDay);
        return scheduleMapper.toResponse(updated);
    }

    /**
     * Удаление конкретного занятия (lesson) из дня расписания.
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> deleteLesson(String scheduleDayId, String timeWindowId, String username)
            throws ScheduleNotFoundException, StudentNotFoundException {
        ScheduleDocument schedule = getScheduleByStudentOrThrow(username);

        ScheduleDocument updated = scheduleRepositoryFacade.smartLessonDelete(schedule, scheduleDayId, timeWindowId);
        return scheduleMapper.toResponse(updated);
    }

    /**
     * Получение расписания напрямую по его ID (без привязки к студенту).
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> getScheduleById(String scheduleId) throws ScheduleNotFoundException {
        ScheduleDocument schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found for id: " + scheduleId));
        return scheduleMapper.toResponse(schedule);
    }

    /**
     * Получение конкретного занятия по расписанию студента, дню и тайм-слоту.
     */
    @Override
    @Transactional
    public CollectionModel<EntityModel<ScheduleItem>> getLessonByStudentAndScheduleDayAndTimeWindow(
            String studentName, String scheduleDayId, String timeWindowId)
            throws ScheduleNotFoundException, StudentNotFoundException {
        ScheduleDocument schedule = getScheduleByStudentOrThrow(studentName);
        return scheduleMapper.toResponse(schedule, scheduleDayId, timeWindowId);
    }

    /**
     * Обновление конкретного занятия.
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> updateLesson(String scheduleDayId,
                                                         String timeWindowId,
                                                         ScheduleItem scheduleItem,
                                                         String username)
            throws ScheduleNotFoundException, StudentNotFoundException {
        ScheduleDocument schedule = getScheduleByStudentOrThrow(username);

        ScheduleDocument updated = scheduleRepositoryFacade.smartLessonUpdate(schedule, scheduleDayId, timeWindowId, scheduleItem);

        // Обновляем ссылку у студента (хотя ID расписания не меняется, сохранение для единообразия)
        Student student = studentRepository.findByUsername(username).orElseThrow();
        updateStudentSchedule(student, updated.getId());

        return scheduleMapper.toResponse(updated);
    }

    /**
     * Обновление дня расписания (списка занятий за день).
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> updateScheduleDay(String dayId,
                                                              List<ScheduleItem> scheduleItems,
                                                              String username)
            throws ScheduleNotFoundException, StudentNotFoundException {
        ScheduleDocument schedule = getScheduleByStudentOrThrow(username);

        ScheduleDocument updated = scheduleRepositoryFacade.smartScheduleDayUpdate(schedule, dayId, scheduleItems);

        // Аналогично обновляем студента
        Student student = studentRepository.findByUsername(username).orElseThrow();
        updateStudentSchedule(student, updated.getId());

        return scheduleMapper.toResponse(updated);
    }

    /**
     * Создание нового дня расписания
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> createScheduleDay(List<ScheduleItem> scheduleItems, String username)
            throws ScheduleNotFoundException, StudentNotFoundException, ResourceExistsException {
        ScheduleDocument userSchedule = getScheduleByStudentOrThrow(username);
        final DayOfWeek requestDayOfWeek = scheduleMapper.getDayOfWeekByRuName(scheduleItems.get(0).getDayOfWeek());
        final boolean isEvenWeek = scheduleItems.get(0).isEven();

        Map<DayOfWeek, ScheduleDayDocument> currentWeek = isEvenWeek ? userSchedule.getEvenWeekSchedule() : userSchedule.getOddWeekSchedule();

        if (!currentWeek.containsKey(requestDayOfWeek)) {
            ScheduleDayDocument scheduleObjectDocumentList = scheduleMapper.toScheduleDayDocument(scheduleItems);
            currentWeek.put(requestDayOfWeek, scheduleObjectDocumentList);
            List<DayOfWeek> sortedDayOfWeekList = currentWeek.keySet().stream().sorted().toList();
            Map<DayOfWeek, ScheduleDayDocument> sortedResultCurrentWeekMap = new LinkedHashMap<>();

            sortedDayOfWeekList.forEach(dayOfWeek -> {
                sortedResultCurrentWeekMap.put(dayOfWeek, currentWeek.get(dayOfWeek));
            });

            if (isEvenWeek) {
               userSchedule.setEvenWeekSchedule(sortedResultCurrentWeekMap);
            } else {
                userSchedule.setOddWeekSchedule(sortedResultCurrentWeekMap);
            }
            userSchedule =  scheduleRepositoryFacade.cascadeSave(userSchedule);
        } else {
            throw new ResourceExistsException("Schedule day + " +
                    requestDayOfWeek + " is exist in " + (isEvenWeek ? "even" : "odd") + " week in schedule " + userSchedule.getId());
        }
        return scheduleMapper.toResponse(userSchedule);
    }

    /**
     * Получение расписания, привязанного к студенту по username.
     */
    @Override
    @Transactional(readOnly = true)
    public EntityModel<ScheduleApiResponse> getScheduleByStudent(String username)
            throws ScheduleNotFoundException, StudentNotFoundException {
        ScheduleDocument schedule = getScheduleByStudentOrThrow(username);
        return scheduleMapper.toResponse(schedule);
    }

    /**
     * Получение всех занятий за день по расписанию студента.
     */
    @Override
    @Transactional(readOnly = true)
    public CollectionModel<EntityModel<ScheduleItem>> getLessonsByStudentAndScheduleDay(String username, String scheduleDayId)
            throws ScheduleNotFoundException, StudentNotFoundException {
        ScheduleDocument schedule = getScheduleByStudentOrThrow(username);
        return scheduleMapper.toResponse(schedule, scheduleDayId);
    }

    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> createLesson(ScheduleItem scheduleItem, String username) throws ScheduleNotFoundException, StudentNotFoundException {
        ScheduleDocument userSchedule = getScheduleByStudentOrThrow(username);
        final DayOfWeek requestDayOfWeek = scheduleMapper.getDayOfWeekByRuName(scheduleItem.getDayOfWeek());
        final boolean isEvenWeek = scheduleItem.isEven();
        Map<DayOfWeek, ScheduleDayDocument> currentWeek = isEvenWeek ? userSchedule.getEvenWeekSchedule() : userSchedule.getOddWeekSchedule();
        ScheduleDayDocument currentScheduleDayDocument = currentWeek.getOrDefault(requestDayOfWeek, null);

        if (currentScheduleDayDocument != null) {

        }

        return scheduleMapper.toResponse(userSchedule);
    }

    /**
     * Получить существующего студента по username или создать нового.
     */
    private Student getOrCreateStudent(String username) {
        return studentRepository.findByUsername(username)
                .orElseGet(() -> studentRepository.save(new Student(username)));
    }

    /**
     * Получить группу по ImportDto или кинуть ошибку, если такой нет.
     */
    private UniversityGroup getUniversityGroup(ImportDto importDto) {
        return universityGroupRepository
                .findByUniversityShortNameAndGroupCode(importDto.getUniversityName(), importDto.getGroupCode())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Group " + importDto.getGroupCode() + " not found"));
    }

    /**
     * Найти существующее расписание:
     * - сначала у самой группы (group.scheduleMongoId),
     * - если там нет — у студента (student.scheduleMongoId).
     */
    private ScheduleDocument findExistingSchedule(UniversityGroup group, Student student) {
        ScheduleDocument scheduleDocument = null;

        if (group.getScheduleMongoId() != null) {
            scheduleDocument = scheduleRepository.findById(group.getScheduleMongoId()).orElse(null);
        }
        if (scheduleDocument == null && student.getScheduleMongoId() != null) {
            scheduleDocument = scheduleRepository.findById(student.getScheduleMongoId()).orElse(null);
        }
        return scheduleDocument;
    }

    /**
     * Универсальный метод: отправить задачу в Kafka-парсер, дождаться ответа, сохранить в Mongo и привязать студенту.
     */
    private ScheduleDocument parseAndSaveNewSchedule(UniversityGroup group, Student student) throws ParserException {
        ParsingTask parsingTask = ParsingTask.builder()
                .groupId(group.getUniversityGroupId())
                .universityName(group.getUniversity().getShortName())
                .build();

        UUID taskId = kafkaProducer.sendToParsingQueue(parsingTask);
        try {
            ScheduleParserResponse parserResponse = messageObserver.waitForParserResponse(taskId);
            if (parserResponse != null) {
                // Сохраняем ответ от парсера
                ScheduleDocument savedSchedule = scheduleRepositoryFacade.cascadeSave(parserResponse);

                // Удаляем старое расписание, если было
                deleteExistingScheduleIfExists(student.getScheduleMongoId());

                // Привязываем новое расписание к студенту
                updateStudentSchedule(student, savedSchedule.getId());
                return savedSchedule;
            }
        } catch (ParserResponseTimeoutException e) {
            log.error("Error while waiting for parser response", e);
        }
        return null;
    }

    /**
     * Получить расписание, привязанное к студенту, или кинуть исключение.
     */
    private ScheduleDocument getScheduleByStudentOrThrow(String username)
            throws StudentNotFoundException, ScheduleNotFoundException {
        Student student = studentRepository.findByUsername(username)
                .orElseThrow(() -> new StudentNotFoundException("Student not found for username: " + username));

        if (student.getScheduleMongoId() == null) {
            throw new ScheduleNotFoundException("No schedule linked to student " + username);
        }

        return scheduleRepository.findById(student.getScheduleMongoId())
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found for student: " + username));
    }

    /**
     * Удалить существующее расписание по scheduleId, если оно есть.
     */
    private void deleteExistingScheduleIfExists(String scheduleId) {
        if (scheduleId != null) {
            scheduleRepository.deleteById(scheduleId);
        }
    }

    /**
     * Обновить у студента ссылку на расписание и время последнего действия.
     */
    private void updateStudentSchedule(Student student, String newScheduleId) {
        student.setScheduleMongoId(newScheduleId);
        student.setLastAction(LocalDate.now());
        studentRepository.save(student);
    }
}
