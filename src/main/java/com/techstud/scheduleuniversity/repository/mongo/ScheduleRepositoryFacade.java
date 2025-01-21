package com.techstud.scheduleuniversity.repository.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.techstud.scheduleuniversity.dao.HashableDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDayDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleObjectDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.TimeSheetDocument;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleDayParserResponse;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleObjectParserResponse;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import com.techstud.scheduleuniversity.dto.parser.response.TimeSheetParserResponse;
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
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.techstud.scheduleuniversity.dto.ScheduleType.ruValueOf;

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
        updatedScheduleObject.setType(ruValueOf(scheduleObjectDocument.getType()));
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
                scheduleObjectDocument.setType(ruValueOf(scheduleItem.getType()));
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

    private Map<DayOfWeek, ScheduleDayDocument> cascadeWeekSave(Map<DayOfWeek, ScheduleDayParserResponse> weekSchedule) {
        Map<DayOfWeek, ScheduleDayDocument> result = new LinkedHashMap<>();
        weekSchedule.forEach((dayOfWeek, scheduleDayDto) -> {
            ScheduleDayDocument scheduleDay = cascadeDaySave(scheduleDayDto);

            boolean allEmpty = scheduleDay.getLessons().isEmpty()
                    || scheduleDay.getLessons().values().stream().allMatch(List::isEmpty);

            if (!allEmpty) {
                result.put(dayOfWeek, scheduleDay);
            }
        });
        return result;
    }


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

    private Map<String, List<ScheduleObjectDocument>> cascadeLessonSave(
            Map<TimeSheetParserResponse, List<ScheduleObjectParserResponse>> lessons) {

        Map<String, List<ScheduleObjectDocument>> result = new LinkedHashMap<>();
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
                                throw new RuntimeException("Error of save ScheduleObject", e);
                            }
                        })
                        .toList();

                result.put(timeSheet.getId(), savedObjects);
            } catch (Exception e) {
                throw new RuntimeException("Error of save lessons", e);
            }
        });
        return result;
    }

    private List<ScheduleObjectDocument> mapToDocument(List<ScheduleItem> scheduleItems) {
        return scheduleItems.stream()
                .map(scheduleItem -> {
                    ScheduleObjectDocument scheduleObjectDocument = new ScheduleObjectDocument();
                    scheduleObjectDocument.setName(scheduleItem.getName());
                    scheduleObjectDocument.setTeacher(scheduleItem.getTeacher());
                    scheduleObjectDocument.setGroups(scheduleItem.getGroups());
                    scheduleObjectDocument.setType(ruValueOf(scheduleItem.getType()));
                    scheduleObjectDocument.setPlace(scheduleItem.getPlace());
                    return scheduleObjectDocument;
                })
                .toList();
    }

    private <T extends HashableDocument> T findOrSave(T entity, Class<T> entityClass, MongoRepository<T, String> repository) {
        T existingEntity = findExistingByHash(entity, entityClass);
        return existingEntity != null ? existingEntity : repository.save(entity);
    }

    private <T extends HashableDocument> T findExistingByHash(T entity, Class<T> entityClass) {
        Query query = new Query(Criteria.where("hash").is(entity.getHash()));
        return mongoTemplate.findOne(query, entityClass);
    }

    private void computeAndSetHash(HashableDocument entity) {
        try {
            String json = objectMapper.writeValueAsString(entity);
            String hash = computeSHA256Hash(json);
            entity.setHash(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute hash ", e);
        }
    }

    private String computeSHA256Hash(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashBytes);
    }

    public ScheduleDocument smartScheduleDaySave(ScheduleDocument schedule, List<ScheduleItem> items) {
        var weekParity = items.stream()
                .map(item -> item.isEven())
                .findFirst();
        log.info("Successful definition week parity: {}", weekParity);

        Map<DayOfWeek, ScheduleDayDocument> weekForSave = new LinkedHashMap<>();

        if (weekParity.isPresent()){
            weekForSave = schedule.getOddWeekSchedule();
        } else {
            weekForSave = schedule.getEvenWeekSchedule();
        }
        log.info("Successful return week for saving: {}", weekForSave);

        var scheduleDay = ScheduleDayDocument.builder()
                .date(new Date(items.stream()
                        .map(item -> item.getDate())
                        .findFirst()
                        .get()
                ))
                .lessons(new LinkedHashMap<>())
                .build();
        log.info("Создали день: {}", scheduleDay);

        var savedLessons = convertItemToLessons(items);
        log.info("Successful conversion items to lessons, {}", savedLessons);

        scheduleDay.setLessons(savedLessons);
        log.info("Successful save lessons, {}", scheduleDay);

        computeAndSetHash(scheduleDay);
        log.info("Successful save hash, {}", scheduleDay.getHash());

        scheduleDay = findOrSave(scheduleDay, ScheduleDayDocument.class, scheduleDayRepository);
        log.info("Successful find or save day, {}", scheduleDay);

        weekForSave.put(
                DayOfWeek.of(scheduleDay.getDate().getDay() + 1),
                scheduleDay
        );
        log.info("Successful save for a week, {}", weekForSave.get(DayOfWeek.of(scheduleDay.getDate().getDay() + 1)));

        if (schedule.getEvenWeekSchedule().containsValue(scheduleDay)) {
            schedule.getEvenWeekSchedule().put(DayOfWeek.of(scheduleDay.getDate().getDay() + 1), scheduleDay);
        } else if (schedule.getOddWeekSchedule().containsValue(scheduleDay)) {
            schedule.getOddWeekSchedule().put(DayOfWeek.of(scheduleDay.getDate().getDay() + 1), scheduleDay);
        } else {
            schedule.getEvenWeekSchedule().put(DayOfWeek.of(scheduleDay.getDate().getDay() + 1), scheduleDay);
        }

        computeAndSetHash(schedule);
        log.info("Successful update schedule, {}", schedule);

        return scheduleRepository.save(schedule);
    }

    public Optional<ScheduleDayDocument> smartScheduleDaySearch(
            List<ScheduleItem> itemsResponse, ScheduleDocument schedule
    ) {
        //Общая подготовка к поиску, вычленение флага недели(odd/even)
        var weekParity = itemsResponse.stream()
                .map(ScheduleItem::isEven)
                .findFirst();
        Map<DayOfWeek, ScheduleDayDocument> weekForSearch = new LinkedHashMap<>();

        if (weekParity.isPresent()){
            weekForSearch = schedule.getOddWeekSchedule();
        } else {
            weekForSearch = schedule.getEvenWeekSchedule();
        }

        //Первая итерация поиска, поиск наличия по дню недели
        var dayOfWeek = Instant.ofEpochMilli(itemsResponse.stream()
                .map(ScheduleItem::getDate)
                .findFirst()
                        .get())
                .atZone(ZoneId.systemDefault()).plusDays(1).getDayOfWeek();
        log.info("Day of week from response: {}", dayOfWeek);

        var isDayInWeek = weekForSearch.keySet().stream()
                .filter(key -> key.equals(dayOfWeek))
                .findFirst();
        log.info("Search result day of week in DB, {}", isDayInWeek);

        if (isDayInWeek.isPresent()) {
            return weekForSearch.entrySet().stream()
                    .filter(entry -> entry.getKey().equals(dayOfWeek))
                    .map(Map.Entry::getValue)
                    .findFirst();
        }
        return Optional.empty();

//        //Вторая итерация поиска, поиск по хэшу
//        var hashDayCollect = weekForSearch.values()
//                .stream()
//                .map(day -> Map.entry(day.getHash(), day))
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//        log.info("Successful create hash day collection, {}", hashDayCollect);
//
//        var scheduleDay = ScheduleDayDocument.builder()
//                .date(new Date(itemsResponse.stream()
//                        .map(ScheduleItem::getDate)
//                        .findFirst()
//                        .get()
//                ))
//                .lessons(new LinkedHashMap<>())
//                .build();
//
//        var createdLessons = convertItemToLessons(itemsResponse);
//        log.info("Successful create lessons, {}", createdLessons);
//
//        scheduleDay.setLessons(createdLessons);
//        log.info("Successful save lessons in search, {}", scheduleDay.getLessons());
//
//        computeAndSetHash(scheduleDay);
//        log.info("Successful save hash for search, {}", scheduleDay.getHash());
//
//        return hashDayCollect.entrySet()
//                .stream()
//                .filter(entry -> entry.getKey().equals(scheduleDay.getHash()))
//                .map(Map.Entry::getValue)
//                .findFirst();
    }

    private Map<String, List<ScheduleObjectDocument>> convertItemToLessons(List<ScheduleItem> items) {
        log.info("Items to lessons, {}", items);
        return items.stream()
                .map(item -> {
                    String[] parts = item.getTime().split("-");
                    TimeSheetDocument timeSheet = TimeSheetDocument.builder()
                            .from(LocalTime.parse(parts[0].trim()))
                            .to(LocalTime.parse(parts[1].trim()))
                            .build();
                    computeAndSetHash(timeSheet);
                    timeSheet = findOrSave(timeSheet, TimeSheetDocument.class, timeSheetRepository);

                    var scheduleObjectDocument = ScheduleObjectDocument.builder()
                            .name(item.getName())
                            .teacher(item.getTeacher())
                            .groups(item.getGroups())
                            .type(ruValueOf(item.getType()))
                            .place(item.getPlace()).build();

                    log.info("ScheduleObjectDocument, {}", scheduleObjectDocument);
                    computeAndSetHash(scheduleObjectDocument);
                    findOrSave(scheduleObjectDocument, ScheduleObjectDocument.class, scheduleObjectRepository);

                    return new AbstractMap.SimpleEntry<>(timeSheet.getId(), scheduleObjectDocument);
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> List.of(entry.getValue()),
                        (oldValue, newValue) -> {
                            oldValue.addAll(newValue);
                            return oldValue;
                        },
                        LinkedHashMap::new
                ));
    }
}


