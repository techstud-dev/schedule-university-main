package com.techstud.scheduleuniversity.dto.parser.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@EqualsAndHashCode(of = "date")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDay implements Serializable {

    private Date date;

    private Map<TimeSheet, List<ScheduleObject>> lessons = new LinkedHashMap<>();

    public void setDateAsString(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        dateFormat.setLenient(false);
        try {
            this.date = dateFormat.parse(date);
        } catch (ParseException ignored) {
        }
    }

}
