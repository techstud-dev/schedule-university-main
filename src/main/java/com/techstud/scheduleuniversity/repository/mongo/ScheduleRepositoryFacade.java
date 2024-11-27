package com.techstud.scheduleuniversity.repository.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.techstud.scheduleuniversity.dao.HashableDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.Schedule;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDay;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleObject;
import com.techstud.scheduleuniversity.dao.document.schedule.TimeSheet;
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
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public Schedule cascadeSave(com.techstud.scheduleuniversity.dto.parser.response.Schedule scheduleDto) {
        try {
            Schedule schedule = new Schedule();
            schedule.setSnapshotDate(scheduleDto.getSnapshotDate());
            schedule.setEvenWeekSchedule(cascadeWeekSave(scheduleDto.getEvenWeekSchedule()));
            schedule.setOddWeekSchedule(cascadeWeekSave(scheduleDto.getOddWeekSchedule()));
            computeAndSetHash(schedule);

            return findOrSave(schedule, Schedule.class, scheduleRepository);
        } catch (Exception e) {
            throw new RuntimeException("Error cascade save schedule", e);
        }
    }

    private Map<DayOfWeek, ScheduleDay> cascadeWeekSave(Map<DayOfWeek, com.techstud.scheduleuniversity.dto.parser.response.ScheduleDay> weekSchedule) {
        Map<DayOfWeek, ScheduleDay> result = new LinkedHashMap<>();
        weekSchedule.forEach((dayOfWeek, scheduleDayDto) -> {
            ScheduleDay scheduleDay = cascadeDaySave(scheduleDayDto);
            result.put(dayOfWeek, scheduleDay);
        });
        return result;
    }

    private ScheduleDay cascadeDaySave(com.techstud.scheduleuniversity.dto.parser.response.ScheduleDay scheduleDayDto) {
        try {
            ScheduleDay scheduleDay = new ScheduleDay();
            scheduleDay.setDate(scheduleDayDto.getDate());
            scheduleDay.setLessons(cascadeLessonSave(scheduleDayDto.getLessons()));
            computeAndSetHash(scheduleDay);

            return findOrSave(scheduleDay, ScheduleDay.class, scheduleDayRepository);
        } catch (Exception e) {
            throw new RuntimeException("Error cascade save day", e);
        }
    }

    private Map<String, List<ScheduleObject>> cascadeLessonSave(
            Map<com.techstud.scheduleuniversity.dto.parser.response.TimeSheet, List<com.techstud.scheduleuniversity.dto.parser.response.ScheduleObject>> lessons) {

        Map<String, List<ScheduleObject>> result = new LinkedHashMap<>();
        lessons.forEach((timeSheetDto, scheduleObjectsDto) -> {
            try {
                TimeSheet timeSheet = timeSheetMapper.toDocument(timeSheetDto);
                computeAndSetHash(timeSheet);
                timeSheet = findOrSave(timeSheet, TimeSheet.class, timeSheetRepository);

                List<ScheduleObject> savedObjects = scheduleObjectMapper.toDocument(scheduleObjectsDto).stream()
                        .map(scheduleObject -> {
                            try {
                                computeAndSetHash(scheduleObject);
                                return findOrSave(scheduleObject, ScheduleObject.class, scheduleObjectRepository);
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
