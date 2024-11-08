package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.entity.schedule.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class TransactionExecutor {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public Mono<Schedule> smartScheduleSave(Schedule schedule) {
        return Mono.just(findOrCreateSchedule(schedule));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Schedule findOrCreateSchedule(Schedule schedule) {
        if (schedule == null) {
            return null;
        }

        schedule.setGroup(findOrCreateGroup(schedule.getGroup()));

        Map<DayOfWeek, ScheduleDay> savedEvenWeekSchedule = findOrCreateScheduleDays(schedule.getEvenWeekSchedule());
        schedule.setEvenWeekSchedule(savedEvenWeekSchedule);

        Map<DayOfWeek, ScheduleDay> savedOddWeekSchedule = findOrCreateScheduleDays(schedule.getOddWeekSchedule());
        schedule.setOddWeekSchedule(savedOddWeekSchedule);

        Schedule existingSchedule = em.createQuery(
                        "SELECT s FROM Schedule s WHERE s.group = :group AND s.snapshotDate = :snapshotDate", Schedule.class)
                .setParameter("group", schedule.getGroup())
                .setParameter("snapshotDate", schedule.getSnapshotDate())
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (existingSchedule != null) {
            // Обновляем существующий Schedule
            existingSchedule.setEvenWeekSchedule(schedule.getEvenWeekSchedule());
            existingSchedule.setOddWeekSchedule(schedule.getOddWeekSchedule());
            existingSchedule.setModified(LocalDate.now());

            // Обновляем другие поля при необходимости
            // ...

            em.merge(existingSchedule);
            return existingSchedule;
        } else {
            // Сохраняем новый Schedule
            schedule.setCreated(LocalDate.now());
            schedule.setModified(LocalDate.now());
            em.persist(schedule);
            return schedule;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Map<DayOfWeek, ScheduleDay> findOrCreateScheduleDays(Map<DayOfWeek, ScheduleDay> scheduleDays) {
        if (scheduleDays == null) {
            return null;
        }
        Map<DayOfWeek, ScheduleDay> savedScheduleDays = new LinkedHashMap<>();

        for (Map.Entry<DayOfWeek, ScheduleDay> entry : scheduleDays.entrySet()) {
            DayOfWeek dayOfWeek = entry.getKey();
            ScheduleDay scheduleDay = entry.getValue();

            ScheduleDay savedScheduleDay = findOrCreateScheduleDay(scheduleDay);
            savedScheduleDays.put(dayOfWeek, savedScheduleDay);
        }

        return savedScheduleDays;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ScheduleDay findOrCreateScheduleDay(ScheduleDay scheduleDay) {
        if (scheduleDay == null) {
            return null;
        }

        Map<TimeSheet, ScheduleObjectMapping> savedLessons = new LinkedHashMap<>();

        for (Map.Entry<TimeSheet, ScheduleObjectMapping> entry : scheduleDay.getLessons().entrySet()) {
            TimeSheet timeSheet = saveOrUpdateTimeSheet(entry.getKey());
            ScheduleObjectMapping scheduleObjectMapping = findOrCreateScheduleObjectMapping(entry.getValue());

            savedLessons.put(timeSheet, scheduleObjectMapping);
        }

        scheduleDay.setLessons(savedLessons);

        List<ScheduleDay> existingScheduleDays = em.createQuery(
                        "SELECT sd FROM ScheduleDay sd WHERE sd.date = :date", ScheduleDay.class)
                .setParameter("date", scheduleDay.getDate())
                .getResultList();

        for (ScheduleDay existingScheduleDay : existingScheduleDays) {
            if (areLessonsEqual(existingScheduleDay.getLessons(), scheduleDay.getLessons())) {
                return existingScheduleDay;
            }
        }

        em.persist(scheduleDay);
        return scheduleDay;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean areLessonsEqual(Map<TimeSheet, ScheduleObjectMapping> lessons1, Map<TimeSheet, ScheduleObjectMapping> lessons2) {
        if (lessons1.size() != lessons2.size()) {
            return false;
        }

        for (Map.Entry<TimeSheet, ScheduleObjectMapping> entry : lessons1.entrySet()) {
            TimeSheet key = entry.getKey();
            ScheduleObjectMapping value = entry.getValue();

            ScheduleObjectMapping otherValue = lessons2.get(key);
            if (otherValue == null) {
                return false;
            }

            if (!value.equals(otherValue)) {
                return false;
            }
        }

        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ScheduleObjectMapping findOrCreateScheduleObjectMapping(ScheduleObjectMapping mapping) {
        if (mapping == null) {
            return null;
        }

        mapping.setTimeSheet(saveOrUpdateTimeSheet(mapping.getTimeSheet()));

        List<ScheduleObject> savedScheduleObjects = mapping.getScheduleObjects().stream()
                .map(this::findOrCreateScheduleObject)
                .collect(Collectors.toList());
        mapping.setScheduleObjects(savedScheduleObjects);

        List<ScheduleObjectMapping> existingMappings = em.createQuery(
                        "SELECT som FROM ScheduleObjectMapping som WHERE som.timeSheet = :timeSheet", ScheduleObjectMapping.class)
                .setParameter("timeSheet", mapping.getTimeSheet())
                .getResultList();

        for (ScheduleObjectMapping existingMapping : existingMappings) {
            if (areScheduleObjectsEqual(existingMapping.getScheduleObjects(), mapping.getScheduleObjects())) {
                return existingMapping;
            }
        }

        em.persist(mapping);
        return mapping;
    }


    public boolean areScheduleObjectsEqual(List<ScheduleObject> list1, List<ScheduleObject> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        return new HashSet<>(list1).containsAll(list2) && new HashSet<>(list2).containsAll(list1);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ScheduleObject findOrCreateScheduleObject(ScheduleObject scheduleObject) {
        if (scheduleObject == null) {
            return null;
        }

        scheduleObject.setTeacher(findOrCreateTeacher(scheduleObject.getTeacher()));
        scheduleObject.setPlace(findOrCreatePlace(scheduleObject.getPlace()));

        List<Group> savedGroups = scheduleObject.getGroups().stream()
                .map(this::findOrCreateGroup)
                .collect(Collectors.toList());
        scheduleObject.setGroups(savedGroups);

        List<ScheduleObject> existingObjects = em.createQuery(
                        "SELECT so FROM ScheduleObject so WHERE so.name = :name AND so.type = :type AND so.teacher = :teacher AND so.place = :place", ScheduleObject.class)
                .setParameter("name", scheduleObject.getName())
                .setParameter("type", scheduleObject.getType())
                .setParameter("teacher", scheduleObject.getTeacher())
                .setParameter("place", scheduleObject.getPlace())
                .getResultList();

        // Сравниваем группы для найденных объектов
        for (ScheduleObject existingObject : existingObjects) {
            if (areGroupsEqual(existingObject.getGroups(), scheduleObject.getGroups())) {
                return existingObject;
            }
        }

        em.persist(scheduleObject);
        return scheduleObject;
    }

    public boolean areGroupsEqual(List<Group> groups1, List<Group> groups2) {
        if (groups1 == null && groups2 == null) {
            return true;
        }
        if (groups1 == null || groups2 == null) {
            return false;
        }

        List<Group> filteredGroups1 = groups1.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Group> filteredGroups2 = groups2.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (filteredGroups1.size() != filteredGroups2.size()) {
            return false;
        }

        if (filteredGroups1.stream().anyMatch(g -> g.getId() == null) ||
                filteredGroups2.stream().anyMatch(g -> g.getId() == null)) {
            return false;
        }

        Set<Long> groupIds1 = filteredGroups1.stream()
                .map(Group::getId)
                .collect(Collectors.toSet());
        Set<Long> groupIds2 = filteredGroups2.stream()
                .map(Group::getId)
                .collect(Collectors.toSet());
        return groupIds1.equals(groupIds2);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Place findOrCreatePlace(Place place) {
        if (place == null) {
            return null;
        }

        place.setUniversity(findOrCreateUniversity(place.getUniversity()));

        return em.createQuery(
                        "SELECT p FROM Place p WHERE p.name = :name AND p.university = :university", Place.class)
                .setParameter("name", place.getName())
                .setParameter("university", place.getUniversity())
                .getResultStream()
                .findFirst()
                .orElseGet(() -> {
                    em.persist(place);
                    return place;
                });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Teacher findOrCreateTeacher(Teacher teacher) {
        if (teacher == null) {
            return null;
        }
        teacher.setUniversity(findOrCreateUniversity(teacher.getUniversity()));

        return em.createQuery("SELECT t FROM Teacher t WHERE t.name = :name AND t.university = :university", Teacher.class)
                .setParameter("name", teacher.getName())
                .setParameter("university", teacher.getUniversity())
                .getResultStream()
                .findFirst()
                .orElseGet(() -> {
                    em.persist(teacher);
                    return teacher;
                });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TimeSheet saveOrUpdateTimeSheet(TimeSheet timeSheet) {
        TimeSheet existingTimeSheet = em.createQuery(
                        "SELECT ts FROM TimeSheet ts WHERE ts.university = :university AND ts.timeFrom = :timeFrom AND ts.timeTo = :timeTo", TimeSheet.class)
                .setParameter("university", timeSheet.getUniversity())
                .setParameter("timeFrom", timeSheet.getTimeFrom())
                .setParameter("timeTo", timeSheet.getTimeTo())
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (existingTimeSheet != null) {
            existingTimeSheet.setTimeFrom(timeSheet.getTimeFrom());
            existingTimeSheet.setTimeTo(timeSheet.getTimeTo());

            existingTimeSheet.setUniversity(findOrCreateUniversity(timeSheet.getUniversity()));

            return em.merge(existingTimeSheet);
        } else {
            timeSheet.setUniversity(findOrCreateUniversity(timeSheet.getUniversity()));
            em.persist(timeSheet);
            return timeSheet;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public University findOrCreateUniversity(University university) {
        if (university == null) {
            return null;
        }
        return em.createQuery("SELECT u FROM University u WHERE u.name = :name", University.class)
                .setParameter("name", university.getName())
                .getResultStream()
                .findFirst()
                .orElseGet(() -> {
                    em.persist(university);
                    return university;
                });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Group findOrCreateGroup(Group group) {
        if (group == null) {
            return null;
        }
        group.setUniversity(findOrCreateUniversity(group.getUniversity()));
        return em.createQuery("SELECT g FROM Group g WHERE g.groupCode = :groupCode AND g.university = :university", Group.class)
                .setParameter("groupCode", group.getGroupCode())
                .setParameter("university", group.getUniversity())
                .getResultStream()
                .findFirst()
                .orElseGet(() -> {
                    if (group.getGroupName() != null) {
                        em.persist(group);
                        return group;
                    }
                    return null;
                });
    }
}