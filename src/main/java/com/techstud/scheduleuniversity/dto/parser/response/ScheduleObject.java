package com.techstud.scheduleuniversity.dto.parser.response;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class ScheduleObject implements Serializable {

    private ScheduleType type;
    private String name;
    private String teacher;
    private String place;
    private List<String> groups = new ArrayList<>();

}
