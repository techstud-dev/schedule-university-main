package com.techstud.scheduleuniversity.dto.response.schedule;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class ScheduleObject extends RepresentationModel<ScheduleObject> implements Serializable {

    private String type;
    private String name;
    private String teacher;
    private String place;
    private List<String> groups = new ArrayList<>();

}
