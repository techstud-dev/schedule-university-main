package com.techstud.scheduleuniversity.dto.parser.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleObject implements Serializable {

    private ScheduleType type;
    private String name;
    private String teacher;
    private String place;
    private List<String> groups = new ArrayList<>();

}
