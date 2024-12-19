package com.techstud.scheduleuniversity.dto.response.schedule;

import lombok.Data;
import org.springframework.hateoas.EntityModel;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class ScheduleApiResponse implements Serializable {

    private List<EntityModel<ScheduleItem>> oddWeekSchedules;
    private List<EntityModel<ScheduleItem>> evenWeekSchedules;
    private LocalDate snapshotDate;
}
