package com.techstud.scheduleuniversity.dto.response.schedule;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"evenWeekSchedule", "oddWeekSchedule"}, callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Json ответа на запрос расписания",
        title = "ScheduleApiResponse")
public class ScheduleApiResponse extends RepresentationModel<ScheduleApiResponse> implements Serializable {

    private Map<String, ScheduleDayApiResponse> evenWeekSchedule;

    private Map<String, ScheduleDayApiResponse> oddWeekSchedule;

    @Schema(description = "Дата получения расписания из внешнего API",
            example = "2021-09-01",
            type = "string")
    private String snapshotDate;

    @Override
    @JsonProperty("_links")
    public Links getLinks() {
        return super.getLinks();
    }

}
