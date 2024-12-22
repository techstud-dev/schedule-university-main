package com.techstud.scheduleuniversity.repository.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.techstud.scheduleuniversity.dao.HashableDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDayDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleObjectDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.TimeSheetDocument;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleDayParserResponse;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleObjectParserResponse;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import com.techstud.scheduleuniversity.dto.parser.response.TimeSheetParserResponse;
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
import java.util.*;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Stream;

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

    public ScheduleDayDocument cascadeDaySave(ScheduleDayParserResponse scheduleDayDto) {
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

    public Map<String, List<ScheduleObjectDocument>> cascadeLessonSave(
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

    private <T extends HashableDocument> T findOrSave(T entity, Class<T> entityClass, MongoRepository<T, String> repository) {
        T existingEntity = findExistingByHash(entity, entityClass);
        return existingEntity != null ? existingEntity : repository.save(entity);
    }


    private <T extends HashableDocument> T findExistingByHash(T entity, Class<T> entityClass) {
        Query query = new Query(Criteria.where("hash").is(entity.getHash()));
        return mongoTemplate.findOne(query, entityClass);
    }

    public void computeAndSetHash(HashableDocument entity) throws Exception {
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
