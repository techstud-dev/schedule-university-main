package com.techstud.scheduleuniversity.mapper;

import com.techstud.scheduleuniversity.dto.MappingScheduleParserDto;
import com.techstud.scheduleuniversity.dto.ScheduleType;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleDayParserResponse;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleObjectParserResponse;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import com.techstud.scheduleuniversity.dto.parser.response.TimeSheetParserResponse;
import com.techstud.scheduleuniversity.entity.*;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ParserMapper implements Mapper<MappingScheduleParserDto, Schedule> {

    @Override
    public Schedule map(MappingScheduleParserDto source) {
        String universityName = source.getUniversityShortName();
        ScheduleParserResponse scheduleParserResponse = source.getScheduleParserResponse();
        Schedule schedule = new Schedule();

        // Преобразуем дату снимка (Date -> LocalDate)
        if (scheduleParserResponse.getSnapshotDate() != null) {
            schedule.setSnapshotDate(
                    scheduleParserResponse.getSnapshotDate()
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
            );
        }

        List<Lesson> lessons = new ArrayList<>();


        lessons.addAll(mapLessons(scheduleParserResponse.getEvenWeekSchedule(), true, universityName));

        lessons.addAll(mapLessons(scheduleParserResponse.getOddWeekSchedule(), false, universityName));

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
    private List<Lesson> mapLessons(Map<DayOfWeek, ScheduleDayParserResponse> weekSchedule, boolean isEvenWeek, String universityName) {
        List<Lesson> lessons = new ArrayList<>();
        if (weekSchedule != null) {
            for (Map.Entry<DayOfWeek, ScheduleDayParserResponse> dayEntry : weekSchedule.entrySet()) {
                DayOfWeek dayOfWeek = dayEntry.getKey();
                ScheduleDayParserResponse dayResponse = dayEntry.getValue();

                if (dayResponse.getLessons() != null) {

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
                                lesson.setTeacher(mapTeacher(scheduleObject.getTeacher(), universityName));
                                lesson.setPlace(mapPlace(scheduleObject.getPlace(), universityName));
                                lesson.setGroups(mapGroups(scheduleObject.getGroups(), universityName));
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
    private Teacher mapTeacher(String teacherName, String universityShortName) {
        Teacher teacher = new Teacher();
        String[] fio = parseFIO(teacherName);
        teacher.setTeacherName(fio[0] + " " + fio[1] + " " + fio[2]);
        teacher.setLastName(fio[0]);
        teacher.setFirstName(fio[1]);
        teacher.setMiddleName(fio[2]);
        University university = new University();
        university.setShortName(universityShortName);
        teacher.setUniversity(university);
        return teacher;
    }

    /**
     * Преобразует строку с названием аудитории в сущность Place.
     */
    private Place mapPlace(String placeStr, String universityName) {
        University university = new University();
        university.setShortName(universityName);
        Place place = new Place();
        place.setPlaceName(placeStr);
        place.setUniversity(university);
        return place;
    }

    /**
     * Преобразует список строк (названия групп) в список сущностей Group.
     * Предполагается, что сущность Group имеет поле groupName.
     */
    private List<Group> mapGroups(List<String> groupNames, String universityName) {
        List<Group> groups = new ArrayList<>();
        if (groupNames != null) {
            for (String groupName : groupNames) {
                University university = new University();
                university.setShortName(universityName);
                Group group = new Group();
                group.setGroupCode(groupName);
                group.setUniversity(university);
                groups.add(group);
            }
        }
        return groups;
    }

    private String[] parseFIO(String fullName) {

        String[] result = new String[3];

        for (int i = 0; i < 3; i++) {
            result[i] = "";
        }

        if (fullName == null || fullName.trim().isEmpty()) {
            return result;
        }

        String[] parts = fullName.trim().split("\\s+");

        result[0] = parts[0];

        if (parts.length > 1) {
            parseNameOrInitials(parts[1], result, true);
        }

        if (parts.length > 2) {
            parseNameOrInitials(parts[2], result, false);
        }

        return result;
    }

    private void parseNameOrInitials(String token, String[] result, boolean isName) {
        // Убираем все лишние точки в конце, чтобы избежать "С.." и т.п.
        // Но сначала нужно понять, есть ли внутри несколько инициалов
        // Например "С.П." => split('.') => ["С","П",""]
        String[] initials = token.split("\\.");

        // Случай когда в строке несколько частей, разделенных точками (инициалы)
        if (initials.length > 1) {
            // Например "С.П." -> ["С", "П", ""]
            // Фильтруем пустые элементы (например, последний "" после точки)
            List<String> filtered = new ArrayList<>();
            for (String part : initials) {
                if (!part.isEmpty()) {
                    filtered.add(part);
                }
            }

            if (filtered.size() == 1) {
                // Например "С." -> 1 инициала => это Имя (или Отчество)
                if (isName) {
                    result[1] = filtered.get(0);  // Имя
                } else {
                    result[2] = filtered.get(0);  // Отчество
                }
            } else if (filtered.size() == 2) {
                // Например "С.П." -> 2 инициала
                // Если это поле Имени (isName == true), тогда:
                //  - первый инициал идет в Имя
                //  - второй инициал идет в Отчество
                // Если это поле Отчества (isName == false), то обычно так не пишут,
                // но можно записать первый инициал как Отчество, второй игнорировать
                if (isName) {
                    result[1] = filtered.get(0);
                    result[2] = filtered.get(1);
                } else {
                    //Бредовый случай, но на всякий случай предусмотрим это
                    result[2] = filtered.get(0);
                }
            } else {
                // Если инициалов почему-то больше 2,
                // либо некорректный формат, выберем первые 2
                if (isName) {
                    result[1] = filtered.get(0);
                    result[2] = filtered.get(1);
                } else {
                    // Положим хотя бы первый в отчество
                    result[2] = filtered.get(0);
                }
            }
        } else {
            // Если точек нет или split вернул 1 элемент, значит это полное Имя или Отчество
            if (isName) {
                result[1] = token;
            } else {
                result[2] = token;
            }
        }
    }
}
