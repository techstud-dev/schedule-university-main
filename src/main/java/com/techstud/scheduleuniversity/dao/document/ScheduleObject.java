package com.techstud.scheduleuniversity.dao.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "schedule_object")
public class ScheduleObject implements Serializable {

    @Id
    private String id = UUID.randomUUID().toString();

    private ScheduleType type;
    private String name;
    private String teacher;
    private String place;
    private List<String> groups = new ArrayList<>();

}
