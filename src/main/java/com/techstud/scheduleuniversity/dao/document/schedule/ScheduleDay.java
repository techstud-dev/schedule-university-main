package com.techstud.scheduleuniversity.dao.document.schedule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.techstud.scheduleuniversity.dao.HashableDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "schedule_day")
@EqualsAndHashCode(of = {"date", "lessons"})
public class ScheduleDay implements Serializable, HashableDocument {

    @Id
    @JsonIgnore
    private String id;

    private Date date;

    private Map<String, List<ScheduleObject>> lessons = new LinkedHashMap<>();

    @Indexed(unique = true)
    private String hash;
}
