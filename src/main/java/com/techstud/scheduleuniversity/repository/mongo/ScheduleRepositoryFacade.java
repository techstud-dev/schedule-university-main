package com.techstud.scheduleuniversity.repository.mongo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.techstud.scheduleuniversity.dao.document.Schedule;
import com.techstud.scheduleuniversity.dao.document.ScheduleDay;
import com.techstud.scheduleuniversity.dao.document.ScheduleObject;
import com.techstud.scheduleuniversity.dao.document.TimeSheet;
import com.techstud.scheduleuniversity.mapper.ScheduleObjectMapper;
import com.techstud.scheduleuniversity.mapper.TimeSheetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    public Schedule cascadeSave(com.techstud.scheduleuniversity.dto.parser.response.Schedule schedule)
            throws NoSuchAlgorithmException, JsonProcessingException {
        Schedule resultAfterSave = new Schedule();

        resultAfterSave.setSnapshotDate(schedule.getSnapshotDate());
        resultAfterSave.setEvenWeekSchedule(cascadeWeekSave(schedule.getEvenWeekSchedule()));
        resultAfterSave.setOddWeekSchedule(cascadeWeekSave(schedule.getOddWeekSchedule()));

        resultAfterSave.setHash(computeHash(resultAfterSave));

        Schedule existingSchedule = isScheduleExistInDb(resultAfterSave);
        if (existingSchedule != null) {
            return existingSchedule;
        }

        return scheduleRepository.save(resultAfterSave);
    }

    public Map<DayOfWeek, ScheduleDay> cascadeWeekSave(
            Map<DayOfWeek, com.techstud.scheduleuniversity.dto.parser.response.ScheduleDay> weekSchedule) {
        Map<DayOfWeek, ScheduleDay> result = new LinkedHashMap<>();

        weekSchedule.forEach((dayOfWeek, scheduleDay) -> {
            try {
                result.put(dayOfWeek, cascadeDaySave(scheduleDay));
            } catch (JsonProcessingException | NoSuchAlgorithmException e) {
                throw new RuntimeException("Error during cascading week save", e);
            }
        });

        return result;
    }

    public ScheduleDay cascadeDaySave(com.techstud.scheduleuniversity.dto.parser.response.ScheduleDay scheduleDay)
            throws JsonProcessingException, NoSuchAlgorithmException {
        ScheduleDay result = new ScheduleDay();

        result.setDate(scheduleDay.getDate());
        result.setLessons(cascadeLessonSave(scheduleDay.getLessons()));

        result.setHash(computeHash(result));

        ScheduleDay existingScheduleDay = isScheduleDayExistInDb(result);
        if (existingScheduleDay != null) {
            return existingScheduleDay;
        }

        return scheduleDayRepository.save(result);
    }

    public Map<String, List<ScheduleObject>> cascadeLessonSave(
            Map<com.techstud.scheduleuniversity.dto.parser.response.TimeSheet,
                    List<com.techstud.scheduleuniversity.dto.parser.response.ScheduleObject>> lessons) {
        Map<String, List<ScheduleObject>> result = new LinkedHashMap<>();

        lessons.forEach((timeSheetDto, scheduleObjectsDto) -> {
            try {
                TimeSheet documentTimeSheet = timeSheetMapper.toDocument(timeSheetDto);
                documentTimeSheet.setHash(computeHash(documentTimeSheet));

                TimeSheet existingTimeSheet = isTimeSheetExistInDb(documentTimeSheet);
                if (existingTimeSheet != null) {
                    documentTimeSheet = existingTimeSheet;
                } else {
                    documentTimeSheet = timeSheetRepository.save(documentTimeSheet);
                }

                List<ScheduleObject> savedObjects = scheduleObjectMapper.toDocument(scheduleObjectsDto).stream()
                        .map(scheduleObject -> {
                            try {
                                scheduleObject.setHash(computeHash(scheduleObject));
                                ScheduleObject existingScheduleObject = isScheduleObjectExistInDb(scheduleObject);
                                return existingScheduleObject != null ? existingScheduleObject : scheduleObjectRepository.save(scheduleObject);
                            } catch (JsonProcessingException | NoSuchAlgorithmException e) {
                                throw new RuntimeException("Error during saving ScheduleObject", e);
                            }
                        })
                        .toList();

                result.put(documentTimeSheet.getId(), savedObjects);
            } catch (JsonProcessingException | NoSuchAlgorithmException e) {
                throw new RuntimeException("Error during cascading lesson save", e);
            }
        });

        return result;
    }

    private ScheduleObject isScheduleObjectExistInDb(ScheduleObject scheduleObject) {
        Query query = new Query();
        query.addCriteria(Criteria.where("hash").is(scheduleObject.getHash()));

        return mongoTemplate.findOne(query, ScheduleObject.class);
    }

    private TimeSheet isTimeSheetExistInDb(TimeSheet timeSheet) {
        Query query = new Query();
        query.addCriteria(Criteria.where("hash").is(timeSheet.getHash()));

        return mongoTemplate.findOne(query, TimeSheet.class);
    }

    private ScheduleDay isScheduleDayExistInDb(ScheduleDay scheduleDay) {
        Query query = new Query();
        query.addCriteria(Criteria.where("hash").is(scheduleDay.getHash()));

        return mongoTemplate.findOne(query, ScheduleDay.class);
    }

    private Schedule isScheduleExistInDb(Schedule schedule) {
        Query query = new Query();
        query.addCriteria(Criteria.where("hash").is(schedule.getHash()));

        return mongoTemplate.findOne(query, Schedule.class);
    }

    public String computeHash(ScheduleObject scheduleObject) throws JsonProcessingException, NoSuchAlgorithmException {
        ObjectMapper objectMapper = createObjectMapper();

        ScheduleObject clone = new ScheduleObject();
        clone.setType(scheduleObject.getType());
        clone.setName(scheduleObject.getName());
        clone.setTeacher(scheduleObject.getTeacher());
        clone.setPlace(scheduleObject.getPlace());
        clone.setGroups(scheduleObject.getGroups());

        String json = objectMapper.writeValueAsString(clone);

        return computeSHA256Hash(json);
    }

    public String computeHash(TimeSheet timeSheet) throws JsonProcessingException, NoSuchAlgorithmException {
        ObjectMapper objectMapper = createObjectMapper();

        TimeSheet clone = new TimeSheet();
        clone.setFrom(timeSheet.getFrom());
        clone.setTo(timeSheet.getTo());

        String json = objectMapper.writeValueAsString(clone);

        return computeSHA256Hash(json);
    }

    public String computeHash(ScheduleDay scheduleDay) throws JsonProcessingException, NoSuchAlgorithmException {
        ObjectMapper objectMapper = createObjectMapper();

        ScheduleDay clone = new ScheduleDay();
        clone.setDate(scheduleDay.getDate());
        clone.setLessons(scheduleDay.getLessons());

        String json = objectMapper.writeValueAsString(clone);

        return computeSHA256Hash(json);
    }

    public String computeHash(Schedule schedule) throws JsonProcessingException, NoSuchAlgorithmException {
        ObjectMapper objectMapper = createObjectMapper();

        Schedule clone = new Schedule();
        clone.setEvenWeekSchedule(schedule.getEvenWeekSchedule());
        clone.setOddWeekSchedule(schedule.getOddWeekSchedule());
        clone.setSnapshotDate(schedule.getSnapshotDate());

        String json = objectMapper.writeValueAsString(clone);

        return computeSHA256Hash(json);
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper()
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper;
    }

    private String computeSHA256Hash(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashBytes);
    }
}
