package com.techstud.scheduleuniversity.dto.response.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ScheduleDay implements Serializable {

    private String date;

    private Map<String, List<ScheduleObject>> lessons = new LinkedHashMap<>();

}
