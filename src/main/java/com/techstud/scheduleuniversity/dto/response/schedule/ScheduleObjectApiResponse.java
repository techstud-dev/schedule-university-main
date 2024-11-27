package com.techstud.scheduleuniversity.dto.response.schedule;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Json объекта расписания", title = "ScheduleObjectApiResponse")
public class ScheduleObjectApiResponse extends RepresentationModel<ScheduleObjectApiResponse> implements Serializable {

    @Schema(description = "Тип занятия",
            example = "Лекция",
            type = "string")
    private String type;

    @Schema(description = "Название занятия",
            example = "Линейная алгебра",
            type = "string")
    private String name;

    @Schema(description = "Преподаватель",
            example = "Васильева Ольга Альбертовна",
            type = "string")
    private String teacher;

    @Schema(description = "Место проведения",
            example = "417-3",
            type = "string")
    private String place;

    @Schema(description = "Список групп, у которых эта пара стоит в это время",
            example = "[\"1104-150303D\",\"1116-240301D\"]",
            type = "array")
    private List<String> groups = new ArrayList<>();

}
