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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Json дня с расписанием", title = "ScheduleDayApiResponse")
public class ScheduleDayApiResponse extends RepresentationModel<ScheduleDayApiResponse> implements Serializable {

    @Schema(description = "Дата дня",
            example = "2021-09-01",
            type = "string")
    private String date;

    private Map<String, List<ScheduleObjectApiResponse>> lessons = new LinkedHashMap<>();

    @Override
    @JsonProperty("_links")
    public Links getLinks() {
        return super.getLinks();
    }
}
