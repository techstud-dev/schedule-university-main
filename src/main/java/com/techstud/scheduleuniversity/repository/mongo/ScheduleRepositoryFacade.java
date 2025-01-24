package com.techstud.scheduleuniversity.repository.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.techstud.scheduleuniversity.dao.HashableDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.*;
import com.techstud.scheduleuniversity.dto.ScheduleType;
import com.techstud.scheduleuniversity.dto.parser.response.*;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleItem;
import com.techstud.scheduleuniversity.mapper.ScheduleObjectMapper;
import com.techstud.scheduleuniversity.mapper.TimeSheetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduleRepositoryFacade {

    private final ScheduleDayRepository scheduleDayRepository;
    private final ScheduleObjectRepository scheduleObjectRepository;
    private final ScheduleRepository scheduleRepository;
    private final TimeSheetRepository timeSheetRepository;
    private final ScheduleObjectMapper scheduleObjectMapper;
    private final TimeSheetMapper timeSheetMapper;
    private final MongoTemplate mongoTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


    /**
     * Каскадное сохранение ScheduleDocument, если он уже сформирован в коде
     * (не через парсер, а, например, после каких-то преобразований).
     */
    public ScheduleDocument cascadeSave(ScheduleDocument scheduleDocument) {
        try {
            // Пробегаем по чётной неделе
            Map<DayOfWeek, ScheduleDayDocument> evenWeekResult = new LinkedHashMap<>();
            if (scheduleDocument.getEvenWeekSchedule() != null) {
                scheduleDocument.getEvenWeekSchedule().forEach((dayOfWeek, dayDoc) -> {
                    ScheduleDayDocument savedDay = cascadeSave(dayDoc);
                    evenWeekResult.put(dayOfWeek, savedDay);
                });
            }

            // Пробегаем по нечётной неделе
            Map<DayOfWeek, ScheduleDayDocument> oddWeekResult = new LinkedHashMap<>();
            if (scheduleDocument.getOddWeekSchedule() != null) {
                scheduleDocument.getOddWeekSchedule().forEach((dayOfWeek, dayDoc) -> {
                    ScheduleDayDocument savedDay = cascadeSave(dayDoc);
                    oddWeekResult.put(dayOfWeek, savedDay);
                });
            }

            // Подменяем в оригинальном документе
            scheduleDocument.setEvenWeekSchedule(evenWeekResult);
            scheduleDocument.setOddWeekSchedule(oddWeekResult);

            // Вычисляем хэш и сохраняем
            computeAndSetHash(scheduleDocument);
            return findOrSave(scheduleDocument, ScheduleDocument.class, scheduleRepository);

        } catch (Exception e) {
            throw new RuntimeException("Error cascade save scheduleDocument", e);
        }
    }

    /**
     * Каскадное сохранение ScheduleDayDocument.
     */
    public ScheduleDayDocument cascadeSave(ScheduleDayDocument scheduleDayDocument) {
        try {
            // Сформируем новый Map<TimeSheetId, List<ScheduleObjectDocument>> с каскадным сохранением
            Map<String, List<ScheduleObjectDocument>> updatedLessons = new LinkedHashMap<>();
            if (scheduleDayDocument.getLessons() != null) {
                for (Map.Entry<String, List<ScheduleObjectDocument>> entry : scheduleDayDocument.getLessons().entrySet()) {
                    String timeSheetId = entry.getKey();
                    List<ScheduleObjectDocument> objects = entry.getValue();

                    // Попробуем найти TimeSheet по ID (если структура именно так связана)
                    TimeSheetDocument timeSheet = timeSheetRepository.findById(timeSheetId).orElse(null);

                    if (timeSheet == null) {
                        throw new RuntimeException("TimeSheet with ID = " + timeSheetId + " not found in DB. " +
                                "Can't cascade save day properly.");
                    }

                    // Сохраняем/находим каждую пару ScheduleObjectDocument
                    List<ScheduleObjectDocument> savedObjects = new ArrayList<>();
                    if (objects != null) {
                        for (ScheduleObjectDocument obj : objects) {
                            ScheduleObjectDocument savedObj = cascadeSave(obj);
                            savedObjects.add(savedObj);
                        }
                    }

                    // Кладём в новый мап уже сохранённые ID
                    updatedLessons.put(timeSheet.getId(), savedObjects);
                }
            }

            scheduleDayDocument.setLessons(updatedLessons);

            computeAndSetHash(scheduleDayDocument);
            return findOrSave(scheduleDayDocument, ScheduleDayDocument.class, scheduleDayRepository);

        } catch (Exception e) {
            throw new RuntimeException("Error cascade save scheduleDayDocument", e);
        }
    }

    /**
     * Каскадное сохранение одного урока (ScheduleObjectDocument).
     * Т.к. у ScheduleObjectDocument нет «дочерних» сущностей, тут всё просто:
     */
    public ScheduleObjectDocument cascadeSave(ScheduleObjectDocument scheduleObjectDocument) {
        try {
            computeAndSetHash(scheduleObjectDocument);
            return findOrSave(scheduleObjectDocument, ScheduleObjectDocument.class, scheduleObjectRepository);
        } catch (Exception e) {
            throw new RuntimeException("Error cascade save scheduleObjectDocument", e);
        }
    }

    /**
     * Каскадное (или «умное») сохранение TimeSheetDocument.
     * Если у TimeSheetDocument нет вложенных сущностей, фактически это просто
     * «посчитать хэш → найти/сохранить».
     */
    public TimeSheetDocument cascadeSave(TimeSheetDocument timeSheetDocument) {
        try {
            computeAndSetHash(timeSheetDocument);
            return findOrSave(timeSheetDocument, TimeSheetDocument.class, timeSheetRepository);
        } catch (Exception e) {
            throw new RuntimeException("Error cascade save timeSheetDocument", e);
        }
    }

    /**
     * Каскадное сохранение расписания, полученного из парсера (ScheduleParserResponse).
     * Использует приватный helper `cascadeWeekSave` для чётной/нечётной недели.
     */
    public ScheduleDocument cascadeSave(ScheduleParserResponse scheduleDto) {
        try {
            ScheduleDocument schedule = new ScheduleDocument();
            schedule.setSnapshotDate(scheduleDto.getSnapshotDate());
            schedule.setEvenWeekSchedule(cascadeWeekSave(scheduleDto.getEvenWeekSchedule()));
            schedule.setOddWeekSchedule(cascadeWeekSave(scheduleDto.getOddWeekSchedule()));

            computeAndSetHash(schedule);
            return findOrSave(schedule, ScheduleDocument.class, scheduleRepository);

        } catch (Exception e) {
            throw new RuntimeException("Error cascade save schedule", e);
        }
    }

    /**
     * «Умное» сохранение TimeSheetDocument:
     * фактически то же самое, что и cascadeSave(timeSheet),
     * но может содержать дополнительную логику, если нужно.
     */
    public TimeSheetDocument smartTimeSheetSave(TimeSheetDocument timeSheetDocument) {
        try {
            computeAndSetHash(timeSheetDocument);
            return findOrSave(timeSheetDocument, TimeSheetDocument.class, timeSheetRepository);
        } catch (Exception e) {
            throw new RuntimeException("Error smart TimeSheet save", e);
        }
    }


    public ScheduleDocument smartScheduleDayDelete(ScheduleDocument scheduleDocument, ScheduleDayDocument scheduleDayDocument) {
        try {
            boolean removedEven = scheduleDocument.getEvenWeekSchedule().values().removeIf(
                    scheduleDay -> scheduleDay.getId().equals(scheduleDayDocument.getId())
            );

            boolean removedOdd = scheduleDocument.getOddWeekSchedule().values().removeIf(
                    scheduleDay -> scheduleDay.getId().equals(scheduleDayDocument.getId())
            );

            if (removedEven || removedOdd) {
                scheduleDayRepository.delete(scheduleDayDocument);
                computeAndSetHash(scheduleDocument);
                return scheduleRepository.save(scheduleDocument);
            }
            return scheduleDocument;

        } catch (Exception e) {
            throw new RuntimeException("Error smart delete schedule day", e);
        }
    }

    public ScheduleDocument smartLessonDelete(ScheduleDocument scheduleDocument, String scheduleDayId, String timeWindowId) {
        scheduleDocument.getOddWeekSchedule().forEach((dayOfWeek, scheduleDay) -> {
            if (scheduleDay.getId().equals(scheduleDayId)) {
                scheduleDay.getLessons().forEach((timeSheetId, scheduleObjects) -> {
                    TimeSheetDocument timeSheet = timeSheetRepository.findById(timeSheetId).orElseThrow();
                    if (timeWindowId.equals(timeSheet.getId())) {
                        scheduleObjectRepository.deleteAll(scheduleObjects);
                        scheduleDay.getLessons().put(timeSheetId, new ArrayList<>());
                    }
                });
            }
        });

        scheduleDocument.getEvenWeekSchedule().forEach((dayOfWeek, scheduleDay) -> {
            if (scheduleDay.getId().equals(scheduleDayId)) {
                scheduleDay.getLessons().forEach((timeSheetId, scheduleObjects) -> {
                    TimeSheetDocument timeSheet = timeSheetRepository.findById(timeSheetId).orElseThrow();
                    if (timeWindowId.equals(timeSheet.getId())) {
                        scheduleObjectRepository.deleteAll(scheduleObjects);
                        scheduleDay.getLessons().put(timeSheetId, new ArrayList<>());
                    }
                });
            }
        });

        try {
            computeAndSetHash(scheduleDocument);
        } catch (Exception e) {
            throw new RuntimeException("Error computing hash", e);
        }
        return scheduleRepository.save(scheduleDocument);
    }

    public ScheduleDocument smartLessonUpdate(ScheduleDocument scheduleDocument,
                                              String scheduleDayId,
                                              String timeWindowId,
                                              ScheduleItem scheduleObjectDocument) {
        ScheduleObjectDocument updatedScheduleObject = new ScheduleObjectDocument();
        updatedScheduleObject.setName(scheduleObjectDocument.getName());
        updatedScheduleObject.setTeacher(scheduleObjectDocument.getTeacher());
        updatedScheduleObject.setGroups(scheduleObjectDocument.getGroups());
        updatedScheduleObject.setType(ScheduleType.ruValueOf(scheduleObjectDocument.getType()));
        updatedScheduleObject.setPlace(scheduleObjectDocument.getPlace());

        scheduleDocument.getOddWeekSchedule().forEach((dayOfWeek, scheduleDay) -> {
            if (scheduleDay.getId().equals(scheduleDayId)) {
                Map<String, List<ScheduleObjectDocument>> lessons = scheduleDay.getLessons();
                    updatedScheduleObject.setId(lessons.get(timeWindowId).get(0).getId());
                    updatedScheduleObject.setHash(findOrSave(
                            updatedScheduleObject,
                            ScheduleObjectDocument.class,
                            scheduleObjectRepository).getHash());
                    lessons.put(timeWindowId, List.of(updatedScheduleObject));
                    scheduleDay.setLessons(lessons);
            }
        });

        scheduleDocument.getEvenWeekSchedule().forEach((dayOfWeek, scheduleDay) -> {
            if (scheduleDay.getId().equals(scheduleDayId)) {
                Map<String, List<ScheduleObjectDocument>> lessons = scheduleDay.getLessons();
                updatedScheduleObject.setId(lessons.get(timeWindowId).get(0).getId());
                updatedScheduleObject.setHash(findOrSave(
                        updatedScheduleObject,
                        ScheduleObjectDocument.class,
                        scheduleObjectRepository).getHash());
                lessons.put(timeWindowId, List.of(updatedScheduleObject));
                scheduleDay.setLessons(lessons);
            }
        });
        return scheduleRepository.save(scheduleDocument);
    }

    public ScheduleDocument smartScheduleDayUpdate(ScheduleDocument scheduleDocument,
                                                   String scheduleDayId,
                                                   List<ScheduleItem> scheduleItems) {
        // Попробуем найти ScheduleDayDocument в четной или нечетной неделях
        ScheduleDayDocument scheduleDay = scheduleDocument.getEvenWeekSchedule().values().stream()
                .filter(day -> day.getId().equals(scheduleDayId))
                .findFirst()
                .orElseGet(() -> scheduleDocument.getOddWeekSchedule().values().stream()
                        .filter(day -> day.getId().equals(scheduleDayId))
                        .findFirst()
                        .orElse(null));

        // Если не найден, создаем новый ScheduleDayDocument
        if (scheduleDay == null) {
            scheduleDay = new ScheduleDayDocument();
            scheduleDay.setId(scheduleDayId);
            scheduleDay.setLessons(new LinkedHashMap<>());
        }

        // Преобразуем ScheduleItems в Map<TimeSheetId, List<ScheduleObjectDocument>>
        Map<String, List<ScheduleObjectDocument>> updatedLessons = new LinkedHashMap<>();
        scheduleItems.forEach(scheduleItem -> {
            try {
                // Найти TimeSheet по времени
                TimeSheetDocument timeSheet = timeSheetRepository.findAll().stream()
                        .filter(ts -> isTimeMatching(scheduleItem.getTime(), ts))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("TimeSheet not found for time: " + scheduleItem.getTime()));

                // Создать или обновить ScheduleObjectDocument
                ScheduleObjectDocument scheduleObjectDocument = new ScheduleObjectDocument();
                scheduleObjectDocument.setName(scheduleItem.getName());
                scheduleObjectDocument.setTeacher(scheduleItem.getTeacher());
                scheduleObjectDocument.setGroups(scheduleItem.getGroups());
                scheduleObjectDocument.setType(ScheduleType.ruValueOf(scheduleItem.getType()));
                scheduleObjectDocument.setPlace(scheduleItem.getPlace());
                computeAndSetHash(scheduleObjectDocument);

                ScheduleObjectDocument savedObject = findOrSave(scheduleObjectDocument, ScheduleObjectDocument.class, scheduleObjectRepository);

                // Добавить объект к уроку
                updatedLessons.computeIfAbsent(timeSheet.getId(), k -> new ArrayList<>()).add(savedObject);

            } catch (Exception e) {
                throw new RuntimeException("Error processing ScheduleItem: " + scheduleItem, e);
            }
        });

        // Удаляем старые промежутки времени, для которых больше нет объектов
        scheduleDay.getLessons().forEach((timeSheetId, existingObjects) -> {
            if (!updatedLessons.containsKey(timeSheetId)) {
                scheduleObjectRepository.deleteAll(existingObjects);
            }
        });

        // Перезаписываем уроки
        scheduleDay.setLessons(updatedLessons);

        // Сохраняем обновленный ScheduleDayDocument
        try {
            computeAndSetHash(scheduleDay);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        scheduleDay = findOrSave(scheduleDay, ScheduleDayDocument.class, scheduleDayRepository);

        // Обновляем ScheduleDocument
        if (scheduleDocument.getEvenWeekSchedule().containsValue(scheduleDay)) {
            scheduleDocument.getEvenWeekSchedule().put(DayOfWeek.of(scheduleDay.getDate().getDay() + 1), scheduleDay);
        } else if (scheduleDocument.getOddWeekSchedule().containsValue(scheduleDay)) {
            scheduleDocument.getOddWeekSchedule().put(DayOfWeek.of(scheduleDay.getDate().getDay() + 1), scheduleDay);
        } else {
            // Если нет привязки к неделям, добавляем в четную неделю как новый день
            scheduleDocument.getEvenWeekSchedule().put(DayOfWeek.of(scheduleDay.getDate().getDay() + 1), scheduleDay);
        }

        // Сохраняем документ расписания
        try {
            computeAndSetHash(scheduleDocument);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return scheduleRepository.save(scheduleDocument);
    }


    /**
     * Приватный метод каскадного сохранения недели, используется в cascadeSave(ScheduleParserResponse).
     */
    private Map<DayOfWeek, ScheduleDayDocument> cascadeWeekSave(Map<DayOfWeek, ScheduleDayParserResponse> weekSchedule) {
        Map<DayOfWeek, ScheduleDayDocument> result = new LinkedHashMap<>();
        if (weekSchedule != null) {
            weekSchedule.forEach((dayOfWeek, scheduleDayDto) -> {
                ScheduleDayDocument scheduleDay = cascadeDaySave(scheduleDayDto);
                boolean allEmpty = scheduleDay.getLessons().isEmpty()
                        || scheduleDay.getLessons().values().stream().allMatch(List::isEmpty);
                if (!allEmpty) {
                    result.put(dayOfWeek, scheduleDay);
                }
            });
        }
        return result;
    }

    private boolean isTimeMatching(String timeRange, TimeSheetDocument timeSheetDocument) {

        LocalTime from = timeSheetDocument.getFrom();
        LocalTime to = timeSheetDocument.getTo();

        String[] timeParts = timeRange.split(" - ");
        if (timeParts.length != 2) {
            throw new IllegalArgumentException("Invalid time range format. Expected format: 'HH:mm - HH:mm'");
        }

        LocalTime rangeFrom = LocalTime.parse(timeParts[0].trim());
        LocalTime rangeTo = LocalTime.parse(timeParts[1].trim());

        return rangeFrom.equals(from) && rangeTo.equals(to);
    }

    /**
     * Приватный метод каскадного сохранения дня, используется внутри cascadeWeekSave(...).
     * Преобразует DTO (ScheduleDayParserResponse) в документ, затем сохраняет.
     */
    private ScheduleDayDocument cascadeDaySave(ScheduleDayParserResponse scheduleDayDto) {
        try {
            ScheduleDayDocument scheduleDay = new ScheduleDayDocument();
            scheduleDay.setDate(scheduleDayDto.getDate());
            scheduleDay.setLessons(cascadeLessonSave(scheduleDayDto.getLessons()));

            computeAndSetHash(scheduleDay);
            return findOrSave(scheduleDay, ScheduleDayDocument.class, scheduleDayRepository);
        } catch (Exception e) {
            throw new RuntimeException("Error cascade save day", e);
        }
    }

    /**
     * Приватный метод каскадного сохранения lessons (Map<TimeSheetParserResponse, List<ScheduleObjectParserResponse>>).
     */
    private Map<String, List<ScheduleObjectDocument>> cascadeLessonSave(
            Map<TimeSheetParserResponse, List<ScheduleObjectParserResponse>> lessons) {

        Map<String, List<ScheduleObjectDocument>> result = new LinkedHashMap<>();
        if (lessons != null) {
            lessons.forEach((timeSheetDto, scheduleObjectsDto) -> {
                try {
                    TimeSheetDocument timeSheet = timeSheetMapper.toDocument(timeSheetDto);
                    computeAndSetHash(timeSheet);
                    timeSheet = findOrSave(timeSheet, TimeSheetDocument.class, timeSheetRepository);

                    List<ScheduleObjectDocument> savedObjects = scheduleObjectMapper.toDocument(scheduleObjectsDto).stream()
                            .map(scheduleObject -> {
                                try {
                                    computeAndSetHash(scheduleObject);
                                    return findOrSave(scheduleObject, ScheduleObjectDocument.class, scheduleObjectRepository);
                                } catch (Exception e) {
                                    throw new RuntimeException("Error saving ScheduleObjectDocument", e);
                                }
                            })
                            .toList();

                    result.put(timeSheet.getId(), savedObjects);
                } catch (Exception e) {
                    throw new RuntimeException("Error saving TimeSheet or schedule objects", e);
                }
            });
        }
        return result;
    }

    // ---------------------------------------------------------
    // 4. БАЗОВЫЕ findOrSave & computeHash
    // ---------------------------------------------------------
    private <T extends HashableDocument> T findOrSave(T entity, Class<T> entityClass, MongoRepository<T, String> repository) {
        T existingEntity = findExistingByHash(entity, entityClass);
        if (existingEntity != null) {
            return existingEntity;
        }
        return repository.save(entity);
    }

    private <T extends HashableDocument> T findExistingByHash(T entity, Class<T> entityClass) {
        Query query = new Query(Criteria.where("hash").is(entity.getHash()));
        return mongoTemplate.findOne(query, entityClass);
    }

    private void computeAndSetHash(HashableDocument entity) throws Exception {
        String json = objectMapper.writeValueAsString(entity);
        String hash = computeSHA256Hash(json);
        entity.setHash(hash);
    }

    private String computeSHA256Hash(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashBytes);
    }
}
