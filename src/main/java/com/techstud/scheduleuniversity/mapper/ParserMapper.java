package com.techstud.scheduleuniversity.mapper;

import com.techstud.scheduleuniversity.dto.ScheduleType;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleDayParserResponse;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleObjectParserResponse;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import com.techstud.scheduleuniversity.dto.parser.response.TimeSheetParserResponse;
import com.techstud.scheduleuniversity.entity.Group;
import com.techstud.scheduleuniversity.entity.Lesson;
import com.techstud.scheduleuniversity.entity.LessonType;
import com.techstud.scheduleuniversity.entity.Place;
import com.techstud.scheduleuniversity.entity.Teacher;
import com.techstud.scheduleuniversity.entity.TimeSheet;
import com.techstud.scheduleuniversity.entity.Schedule;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ParserMapper implements Mapper<ScheduleParserResponse, Schedule> {

    @Override
    public Schedule map(ScheduleParserResponse source) {
        Schedule schedule = new Schedule();

        // Преобразуем дату снимка (Date -> LocalDate)
        if (source.getSnapshotDate() != null) {
            schedule.setSnapshotDate(
                    source.getSnapshotDate()
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
            );
        }

        List<Lesson> lessons = new ArrayList<>();


        lessons.addAll(mapLessons(source.getEvenWeekSchedule(), true));

        lessons.addAll(mapLessons(source.getOddWeekSchedule(), false));

        schedule.setLessonList(lessons);
        return schedule;
    }

    /**
     * Преобразует мапу расписания для недели (ключ – DayOfWeek, значение – ScheduleDayParserResponse)
     * в список сущностей Lesson.
     *
     * @param weekSchedule мапа расписания для недели
     * @param isEvenWeek   флаг, указывающий, что это чётная неделя (true) или нечётная (false)
     * @return список Lesson
     */
    private List<Lesson> mapLessons(Map<DayOfWeek, ScheduleDayParserResponse> weekSchedule, boolean isEvenWeek) {
        List<Lesson> lessons = new ArrayList<>();
        if (weekSchedule != null) {
            for (Map.Entry<DayOfWeek, ScheduleDayParserResponse> dayEntry : weekSchedule.entrySet()) {
                DayOfWeek dayOfWeek = dayEntry.getKey();
                ScheduleDayParserResponse dayResponse = dayEntry.getValue();

                if (dayResponse.getLessons() != null) {
                    // Для каждого временного интервала (TimeSheet) и списка занятий
                    for (Map.Entry<TimeSheetParserResponse, List<ScheduleObjectParserResponse>> timeEntry : dayResponse.getLessons().entrySet()) {
                        TimeSheetParserResponse timeSheetResponse = timeEntry.getKey();
                        TimeSheet timeSheet = mapTimeSheet(timeSheetResponse);

                        List<ScheduleObjectParserResponse> scheduleObjects = timeEntry.getValue();
                        if (scheduleObjects != null) {
                            for (ScheduleObjectParserResponse scheduleObject : scheduleObjects) {
                                Lesson lesson = new Lesson();
                                lesson.setEvenWeek(isEvenWeek);
                                lesson.setDayOfWeek(dayOfWeek);
                                lesson.setName(scheduleObject.getName());
                                lesson.setType(mapLessonType(scheduleObject.getType()));
                                lesson.setTeacher(mapTeacher(scheduleObject.getTeacher()));
                                lesson.setPlace(mapPlace(scheduleObject.getPlace()));
                                lesson.setGroups(mapGroups(scheduleObject.getGroups()));
                                lesson.setTimeSheet(timeSheet);

                                lessons.add(lesson);
                            }
                        }
                    }
                }
            }
        }
        return lessons;
    }

    /**
     * Маппинг объекта TimeSheetParserResponse в сущность TimeSheet.
     */
    private TimeSheet mapTimeSheet(TimeSheetParserResponse tsResponse) {
        TimeSheet timeSheet = new TimeSheet();
        timeSheet.setFromTime(tsResponse.getFrom());
        timeSheet.setToTime(tsResponse.getTo());
        return timeSheet;
    }

    /**
     * Преобразует ScheduleType (тип из DTO) в LessonType (тип для сущности).
     * Здесь предполагается, что имена констант в обоих перечислениях совпадают.
     */
    private LessonType mapLessonType(ScheduleType scheduleType) {
        return LessonType.valueOf(scheduleType.name());
    }

    /**
     * Преобразует строку с именем преподавателя в сущность Teacher.
     */
    private Teacher mapTeacher(String teacherStr) {
        Teacher teacher = new Teacher();
        teacher.setTeacherName(teacherStr);
        // Дополнительно можно распарсить ФИО, если требуется
        return teacher;
    }

    /**
     * Преобразует строку с названием аудитории в сущность Place.
     */
    private Place mapPlace(String placeStr) {
        Place place = new Place();
        place.setPlaceName(placeStr);
        return place;
    }

    /**
     * Преобразует список строк (названия групп) в список сущностей Group.
     * Предполагается, что сущность Group имеет поле groupName.
     */
    private List<Group> mapGroups(List<String> groupNames) {
        List<Group> groups = new ArrayList<>();
        if (groupNames != null) {
            for (String groupName : groupNames) {
                Group group = new Group();
                group.setGroupCode(groupName);
                groups.add(group);
            }
        }
        return groups;
    }
}
