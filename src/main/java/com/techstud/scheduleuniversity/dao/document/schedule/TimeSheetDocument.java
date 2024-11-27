package com.techstud.scheduleuniversity.dao.document.schedule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.techstud.scheduleuniversity.dao.HashableDocument;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "time_sheet")
@EqualsAndHashCode(of = {"from", "to"})
public class TimeSheetDocument implements Serializable, HashableDocument {

    @Id
    @JsonIgnore
    private String id;

    private LocalTime from;

    private LocalTime to;

    @Indexed(unique = true)
    private String hash;

    @Override
    public String toString() {
        return "TimeSheet(from=" + from + ", to=" + to + ")";
    }
}
