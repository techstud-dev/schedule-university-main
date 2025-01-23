package com.techstud.scheduleuniversity.dao.document.schedule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.techstud.scheduleuniversity.dao.HashableDocument;
import com.techstud.scheduleuniversity.dto.ScheduleType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "schedule_object")
@EqualsAndHashCode(of = {"type", "name", "teacher", "place", "groups"})
@Builder
public class ScheduleObjectDocument implements Serializable, HashableDocument {

    @Id
    @JsonIgnore
    private String id;

    private ScheduleType type;
    private String name;
    private String teacher;
    private String place;
    private List<String> groups = new ArrayList<>();

    @Indexed(unique = true)
    private String hash;

}
