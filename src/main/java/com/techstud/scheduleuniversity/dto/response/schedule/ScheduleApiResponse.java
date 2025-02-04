package com.techstud.scheduleuniversity.dto.response.schedule;

import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ScheduleApiResponse extends RepresentationModel<ScheduleApiResponse> implements Serializable {

    private CollectionModel<EntityModel<ScheduleItem>> oddWeekSchedules;
    private CollectionModel<EntityModel<ScheduleItem>> evenWeekSchedules;
    private LocalDate snapshotDate;

}
