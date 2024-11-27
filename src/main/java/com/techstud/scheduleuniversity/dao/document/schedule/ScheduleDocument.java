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
import java.time.DayOfWeek;
import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "schedule")
@EqualsAndHashCode(of = {"evenWeekSchedule", "oddWeekSchedule"})
public class ScheduleDocument implements Serializable, HashableDocument {

    @Id
    @JsonIgnore
    private String id;

    private Map<DayOfWeek, ScheduleDayDocument> evenWeekSchedule;

    private Map<DayOfWeek, ScheduleDayDocument> oddWeekSchedule;

    private Date snapshotDate = new Date();

    @Indexed(unique = true)
    private String hash;
}
