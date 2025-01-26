package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.dao.document.schedule.*;
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
import com.techstud.scheduleuniversity.repository.mongo.*;
import com.techstud.scheduleuniversity.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    private final TimeSheetRepository timeSheetRepository;

    /**
     * Импорт расписания (пытаемся найти существующее, если нет — парсим и сохраняем).
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
     * Принудительный импорт расписания (всегда парсим, независимо от наличия).
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

        // Сохраняем новое через каскадный метод facade
        ScheduleDocument savedDocument = scheduleRepositoryFacade.cascadeSave(saveDto);

        // Привязываем к студенту
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

        // Проверяем, действительно ли это расписание принадлежит студенту
        if (!Objects.equals(schedule.getId(), scheduleId)) {
            throw new ScheduleNotFoundException("Schedule scheduleId does not match student's schedule scheduleId");
        }

        // Удаляем из Mongo
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

        // Используем smart-метод из facade
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

        // smart-метод для удаления занятий
        ScheduleDocument updated = scheduleRepositoryFacade.smartLessonDelete(schedule, scheduleDayId, timeWindowId);

        return scheduleMapper.toResponse(updated);
    }

    /**
     * Получение расписания напрямую по его ID.
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
     * Обновление конкретного занятия (через smartLessonUpdate).
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> updateLesson(String scheduleDayId,
                                                         String timeWindowId,
                                                         ScheduleItem scheduleItem,
                                                         String username)
            throws ScheduleNotFoundException, StudentNotFoundException {

        ScheduleDocument schedule = getScheduleByStudentOrThrow(username);

        // smart-обновление одного занятия
        ScheduleDocument updated = scheduleRepositoryFacade.smartLessonUpdate(schedule, scheduleDayId, timeWindowId, scheduleItem);

        // Обновляем ссылку у студента (ID не меняется, но для единообразия)
        Student student = studentRepository.findByUsername(username).orElseThrow();
        updateStudentSchedule(student, updated.getId());

        return scheduleMapper.toResponse(updated);
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

        ScheduleDocument schedule = getScheduleByStudentOrThrow(username);

        // smart-обновление дня
        ScheduleDocument updated = scheduleRepositoryFacade.smartScheduleDayUpdate(schedule, dayId, scheduleItems);

        // Обновляем ссылку у студента
        Student student = studentRepository.findByUsername(username).orElseThrow();
        updateStudentSchedule(student, updated.getId());

        return scheduleMapper.toResponse(updated);
    }

    /**
     * Создание нового дня расписания.
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> createScheduleDay(List<ScheduleItem> scheduleItems, String username)
            throws ScheduleNotFoundException, StudentNotFoundException, ResourceExistsException {

        ScheduleDocument userSchedule = getScheduleByStudentOrThrow(username);
        final DayOfWeek requestDayOfWeek = scheduleMapper.getDayOfWeekByRuName(scheduleItems.get(0).getDayOfWeek());
        final boolean isEvenWeek = scheduleItems.get(0).isEven();

        Map<DayOfWeek, ScheduleDayDocument> currentWeek = isEvenWeek
                ? userSchedule.getEvenWeekSchedule()
                : userSchedule.getOddWeekSchedule();

        if (!currentWeek.containsKey(requestDayOfWeek)) {
            // Формируем новый ScheduleDayDocument из DTO
            ScheduleDayDocument newScheduleDay = scheduleMapper.toScheduleDayDocument(scheduleItems);

            // Кладём в карту и сортируем
            currentWeek.put(requestDayOfWeek, newScheduleDay);
            List<DayOfWeek> sortedDayOfWeekList = currentWeek.keySet().stream().sorted().toList();

            Map<DayOfWeek, ScheduleDayDocument> sortedResultCurrentWeekMap = new LinkedHashMap<>();
            sortedDayOfWeekList.forEach(dayOfWeek -> {
                sortedResultCurrentWeekMap.put(dayOfWeek, currentWeek.get(dayOfWeek));
            });

            // Обновляем расписание
            if (isEvenWeek) {
                userSchedule.setEvenWeekSchedule(sortedResultCurrentWeekMap);
            } else {
                userSchedule.setOddWeekSchedule(sortedResultCurrentWeekMap);
            }

            // Каскадное сохранение всего расписания
            userSchedule = scheduleRepositoryFacade.cascadeSave(userSchedule);
        } else {
            throw new ResourceExistsException("Schedule day " + requestDayOfWeek + " already exists in "
                    + (isEvenWeek ? "even" : "odd") + " week for schedule " + userSchedule.getId());
        }
        return scheduleMapper.toResponse(userSchedule);
    }

    /**
     * Получение расписания, привязанного к студенту, по username.
     */
    @Override
    @Transactional(readOnly = true)
    public EntityModel<ScheduleApiResponse> getScheduleByStudent(String username)
            throws ScheduleNotFoundException, StudentNotFoundException {

        ScheduleDocument schedule = getScheduleByStudentOrThrow(username);
        return scheduleMapper.toResponse(schedule);
    }

    /**
     * Получение всех занятий за день из расписания студента.
     */
    @Override
    @Transactional(readOnly = true)
    public CollectionModel<EntityModel<ScheduleItem>> getLessonsByStudentAndScheduleDay(String username, String scheduleDayId)
            throws ScheduleNotFoundException, StudentNotFoundException {

        ScheduleDocument schedule = getScheduleByStudentOrThrow(username);
        return scheduleMapper.toResponse(schedule, scheduleDayId);
    }

    /**
     * Создание нового занятия (Lesson) в конкретный день расписания.
     */
    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> createLesson(ScheduleItem scheduleItem, String username)
            throws ScheduleNotFoundException, StudentNotFoundException {

        // 1. Получаем расписание пользователя (студента)
        ScheduleDocument userSchedule = getScheduleByStudentOrThrow(username);

        // 2. Определяем день недели и чётность
        final DayOfWeek requestDayOfWeek = scheduleMapper.getDayOfWeekByRuName(scheduleItem.getDayOfWeek());
        final boolean isEvenWeek = scheduleItem.isEven();

        // 3. Берём нужную "неделю" (чет/нечет)
        Map<DayOfWeek, ScheduleDayDocument> currentWeek = isEvenWeek
                ? userSchedule.getEvenWeekSchedule()
                : userSchedule.getOddWeekSchedule();

        // 4. Получаем расписание на указанный день
        ScheduleDayDocument currentScheduleDayDocument = currentWeek.get(requestDayOfWeek);
        if (currentScheduleDayDocument == null) {
            throw new ScheduleNotFoundException("Not found schedule day for update lesson");
        }

        // 5. Текущее содержимое уроков
        Map<String, List<ScheduleObjectDocument>> currentLessonMap = currentScheduleDayDocument.getLessons();

        // 6. Находим или создаём TimeSheet для указанного промежутка
        String timeSheetId = findOrCreateTimeSheet(scheduleItem);

        // 7. Проверяем, что в данном тайм-слоте нет занятий (по вашей логике)
        List<ScheduleObjectDocument> existingLessons = currentLessonMap.getOrDefault(timeSheetId, new ArrayList<>());
        if (!existingLessons.isEmpty()) {
            throw new ScheduleNotFoundException("Lesson already exists in this timeslot for day: "
                    + scheduleItem.getDayOfWeek() + ", time: " + scheduleItem.getTime());
        }

        // 8. Добавляем новое занятие, сортируем заново
        addedLessonAndSortMap(scheduleItem, currentLessonMap, timeSheetId);

        // 9. Обновляем lessons в ScheduleDayDocument
        currentScheduleDayDocument.setLessons(currentLessonMap);
        currentWeek.put(requestDayOfWeek, currentScheduleDayDocument);

        // 10. Подменяем карту в расписании
        if (isEvenWeek) {
            userSchedule.setEvenWeekSchedule(currentWeek);
        } else {
            userSchedule.setOddWeekSchedule(currentWeek);
        }

        // 11. Вызываем каскадное сохранение всего ScheduleDocument
        userSchedule = scheduleRepositoryFacade.cascadeSave(userSchedule);

        return scheduleMapper.toResponse(userSchedule);
    }

    // --------------------------------------------------------
    // ВСПОМОГАТЕЛЬНЫЕ ПРИВАТНЫЕ МЕТОДЫ
    // --------------------------------------------------------

    /**
     * Находим или создаём TimeSheet, используя каскадное сохранение.
     */
    private String findOrCreateTimeSheet(ScheduleItem scheduleItem) {
        // Парсим время формата "HH:mm-HH:mm"
        String[] timeRange = scheduleItem.getTime().split("-");
        String stringFrom = timeRange[0].trim();
        String stringTo = timeRange[1].trim();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime from = LocalTime.parse(stringFrom, dateTimeFormatter);
        LocalTime to = LocalTime.parse(stringTo, dateTimeFormatter);

        // Пытаемся найти уже существующий
        TimeSheetDocument timeSheetDocument = timeSheetRepository
                .findByFromAndTo(from, to)
                .orElse(null);

        // Если не найден — создаём новый
        if (timeSheetDocument == null) {
            timeSheetDocument = new TimeSheetDocument();
            timeSheetDocument.setFrom(from);
            timeSheetDocument.setTo(to);
        }

        timeSheetDocument = scheduleRepositoryFacade.cascadeSave(timeSheetDocument);
        return timeSheetDocument.getId();
    }

    /**
     * Добавляем новое занятие (ScheduleObjectDocument) и пересортировываем Map<timeSheetId, List<ScheduleObjectDocument>>.
     */
    private void addedLessonAndSortMap(ScheduleItem scheduleItem,
                                       Map<String, List<ScheduleObjectDocument>> lessonMap,
                                       String currentTimeSheetId) {

        // 1. Преобразуем ScheduleItem -> ScheduleObjectDocument
        ScheduleObjectDocument scheduleObject = scheduleMapper.mapToScheduleObjectDocument(scheduleItem);

        // 2. Сохраняем/находим объект через cascadeSave (чтобы он попал в БД и имел корректный хэш/ID)
        scheduleObject = scheduleRepositoryFacade.cascadeSave(scheduleObject);

        // 3. Кладём в карту
        lessonMap.computeIfAbsent(currentTimeSheetId, k -> new ArrayList<>()).add(scheduleObject);

        // 4. Собираем все TimeSheetDocument-ы, чтобы отсортировать по времени "from"
        List<TimeSheetDocument> allTimeSheetsFromMap = timeSheetRepository.findAllById(lessonMap.keySet());
        allTimeSheetsFromMap.sort(Comparator.comparing(TimeSheetDocument::getFrom));

        // 5. Создаём новую LinkedHashMap с учётом порядка
        Map<String, List<ScheduleObjectDocument>> sortedMap = new LinkedHashMap<>();
        for (TimeSheetDocument tsDoc : allTimeSheetsFromMap) {
            String tsId = tsDoc.getId();
            if (lessonMap.containsKey(tsId)) {
                sortedMap.put(tsId, lessonMap.get(tsId));
            }
        }

        // 6. Заменяем исходный lessonMap на отсортированный
        lessonMap.clear();
        lessonMap.putAll(sortedMap);
    }

    /**
     * Получить существующего студента или создать нового.
     */
    private Student getOrCreateStudent(String username) {
        return studentRepository.findByUsername(username)
                .orElseGet(() -> studentRepository.save(new Student(username)));
    }

    /**
     * Получить группу по ImportDto или кинуть ошибку, если нет.
     */
    private UniversityGroup getUniversityGroup(ImportDto importDto) {
        return universityGroupRepository.findByUniversityShortNameAndGroupCode(
                importDto.getUniversityName(),
                importDto.getGroupCode()
        ).orElseThrow(() -> new IllegalArgumentException(
                "Group " + importDto.getGroupCode() + " not found"));
    }

    /**
     * Найти готовое расписание у группы или студента.
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
     * Отправляем задачу в Kafka для парсинга, ждём ответ, сохраняем, привязываем к студенту.
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
                // Сохраняем результат парсинга через cascadeSave
                ScheduleDocument savedSchedule = scheduleRepositoryFacade.cascadeSave(parserResponse);

                // Удаляем старое расписание, если было
                deleteExistingScheduleIfExists(student.getScheduleMongoId());

                // Привязываем новое к студенту
                updateStudentSchedule(student, savedSchedule.getId());
                return savedSchedule;
            }
        } catch (ParserResponseTimeoutException e) {
            log.error("Error while waiting for parser response", e);
        }
        return null;
    }

    /**
     * Получить расписание, привязанное к студенту.
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
     * Удалить расписание, если оно есть (по его ID).
     */
    private void deleteExistingScheduleIfExists(String scheduleId) {
        if (scheduleId != null) {
            scheduleRepository.deleteById(scheduleId);
        }
    }

    /**
     * Привязать новое расписание к студенту, заодно обновив дату последнего действия.
     */
    private void updateStudentSchedule(Student student, String newScheduleId) {
        student.setScheduleMongoId(newScheduleId);
        student.setLastAction(LocalDate.now());
        studentRepository.save(student);
    }
}
