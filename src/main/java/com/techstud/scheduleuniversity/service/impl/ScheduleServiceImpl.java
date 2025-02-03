package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleApiResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleItem;
import com.techstud.scheduleuniversity.exception.ParserException;
import com.techstud.scheduleuniversity.exception.ResourceExistsException;
import com.techstud.scheduleuniversity.exception.ScheduleNotFoundException;
import com.techstud.scheduleuniversity.exception.StudentNotFoundException;
import com.techstud.scheduleuniversity.kafka.KafkaMessageObserver;
import com.techstud.scheduleuniversity.kafka.KafkaProducer;
import com.techstud.scheduleuniversity.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {

    private final KafkaProducer kafkaProducer;
    private final KafkaMessageObserver messageObserver;

    /**
     * Импорт расписания (пытаемся найти существующее, если нет — парсим и сохраняем).
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> importSchedule(ImportDto importDto, String username)
            throws ScheduleNotFoundException, ParserException {
        return null;
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
    public void deleteSchedule(String scheduleId, String username)
            throws ScheduleNotFoundException, StudentNotFoundException {

    }

    /**
     * Удаление одного конкретного дня расписания.
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> deleteScheduleDay(String dayId, String username)
            throws ScheduleNotFoundException, StudentNotFoundException {
        return null;
    }

    /**
     * Удаление конкретного занятия (lesson) из дня расписания.
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> deleteLesson(String scheduleDayId, String timeWindowId, String username)
            throws ScheduleNotFoundException, StudentNotFoundException {
        return null;
    }

    /**
     * Получение расписания напрямую по его ID.
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> getScheduleById(String scheduleId) throws ScheduleNotFoundException {
        return null;
    }

    /**
     * Получение конкретного занятия по расписанию студента, дню и тайм-слоту.
     */
    @Override
    @Transactional
    public CollectionModel<EntityModel<ScheduleItem>> getLessonByStudentAndScheduleDayAndTimeWindow(
            String studentName, String scheduleDayId, String timeWindowId)
            throws ScheduleNotFoundException, StudentNotFoundException {

        return null;
    }

    /**
     * Обновление конкретного занятия (через smartLessonUpdate).
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> updateLesson(String scheduleDayId,
                                                         String timeWindowId,
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
                                                              String username)
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
    public CollectionModel<EntityModel<ScheduleItem>> getLessonsByStudentAndScheduleDay(String username, String scheduleDayId)
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

}
