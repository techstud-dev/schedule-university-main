package com.techstud.scheduleuniversity.dto.response.schedule;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduleDay extends RepresentationModel<ScheduleDay> implements Serializable {

    private String date;

    private Map<String, List<ScheduleObject>> lessons = new LinkedHashMap<>();

}
