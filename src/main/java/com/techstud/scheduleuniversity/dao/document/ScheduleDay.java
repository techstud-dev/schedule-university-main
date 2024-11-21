package com.techstud.scheduleuniversity.dao.document;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.techstud.scheduleuniversity.util.TimeSheetKeyDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "schedule_day")
public class ScheduleDay implements Serializable {

    @Id
    private String id = UUID.randomUUID().toString();

    private Date date;

    @DBRef
    @JsonDeserialize(keyUsing = TimeSheetKeyDeserializer.class)
    private Map<TimeSheet, List<ScheduleObject>> lessons = new LinkedHashMap<>();

}
