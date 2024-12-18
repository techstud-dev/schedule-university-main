package com.techstud.scheduleuniversity.dto.response.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ScheduleItem implements Serializable {

    private String id = "-";
    @JsonProperty(value = "isEven")
    private boolean isEven;
    private String dayOfWeek = "-";
    private long date;
    private String time = "-";
    private String type = "Другое";
    private String name = "-";
    private String teacher = "-";
    private String place  = "-";
    private List<String> groups;
}
