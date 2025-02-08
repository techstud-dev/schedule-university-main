package com.techstud.scheduleuniversity.mapper;

import com.techstud.scheduleuniversity.dto.MappingScheduleItemDto;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleItem;
import com.techstud.scheduleuniversity.entity.*;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ScheduleItemMapper implements Mapper<MappingScheduleItemDto, Lesson> {
    @Override
    public Lesson map(MappingScheduleItemDto source) {
        String universityShortName = source.getUniversityShortName();
        ScheduleItem scheduleItem = source.getScheduleItem();
        Lesson lesson = new Lesson();
        lesson.setName(scheduleItem.getName());
        lesson.setType(LessonType.ruValueOf(scheduleItem.getType()));
        lesson.setTeacher(mapTeacher(scheduleItem.getTeacher(), universityShortName));
        lesson.setGroups(mapGroups(scheduleItem.getGroups(), universityShortName));
        lesson.setEvenWeek(scheduleItem.isEven());
        lesson.setPlace(mapPlace(scheduleItem.getPlace(), universityShortName));
        lesson.setTimeSheet(mapTimeSheet(scheduleItem.getTime()));

        return lesson;
    }

    private TimeSheet mapTimeSheet(String timeSheet) {

        // Проверяем, что строка не пуста и содержит разделитель "-"
        if (timeSheet == null || !timeSheet.contains("-")) {
            throw new IllegalArgumentException("Incorrect time sheet format: " + timeSheet);
        }

        // Разделяем по дефису
        String[] parts = timeSheet.split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Incorrect time sheet format: " + timeSheet);
        }

        // Убираем возможные пробелы в начале/конце
        String fromTimeStr = parts[0].trim();
        String toTimeStr = parts[1].trim();

        // Преобразуем строки во время LocalTime
        LocalTime from = LocalTime.parse(fromTimeStr);
        LocalTime to = LocalTime.parse(toTimeStr);

        // Создаём и возвращаем заполненный объект TimeSheet
        TimeSheet result = new TimeSheet();
        result.setFromTime(from);
        result.setToTime(to);
        return result;
    }

    private List<Group> mapGroups(List<String> groupCodes, String universityShortName) {
        University university = new University();
        university.setShortName(universityShortName);
        List<Group> groups = new ArrayList<>();
        groupCodes.forEach(groupCode -> {
            Group group = new Group();
            group.setGroupCode(groupCode);
            group.setUniversity(university);
            groups.add(group);
        });
        return groups;
    }

    private Place mapPlace(String placeName, String universityShortName) {
        University university = new University();
        university.setShortName(universityShortName);
        Place place = new Place();
        place.setPlaceName(placeName);
        place.setUniversity(university);
        return place;
    }

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

    /**
     * parseNameOrInitials - метод, который разбирает строку и записывает либо "Имя" (index = 1),
     * либо "Отчество" (index = 2), либо инициалы в эти поля.
     *
     * @param token  строка, которую нужно разобрать (например "С.", "С.П." или "Сергей")
     * @param result массив для сохранения результата [Фамилия, Имя, Отчество]
     * @param isName если true, пишем в result[1] (Имя) и если там обнаружим два инициала сразу,
     *               то второй попадет в Отчество;
     *               если false, пишем в result[2] (Отчество).
     */
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
