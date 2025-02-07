package com.techstud.scheduleuniversity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleItem;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@EqualsAndHashCode(of = {"lessons"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateScheduleDto implements Serializable {

    private String universityShortName;

    private List<ScheduleItem> lessons;

    private final LocalDateTime snapshotDate = LocalDateTime.now();

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
