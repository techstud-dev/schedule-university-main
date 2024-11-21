package com.techstud.scheduleuniversity.dao.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "schedule")
public class Schedule implements Serializable {

    @Id
    private String id = UUID.randomUUID().toString();

    @DBRef
    private Map<DayOfWeek, ScheduleDay> evenWeekSchedule;

    @DBRef
    private Map<DayOfWeek, ScheduleDay> oddWeekSchedule;

    private Date snapshotDate = new Date();

}
