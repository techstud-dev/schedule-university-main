package com.techstud.scheduleuniversity.dao.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "time_sheet")
public class TimeSheet implements Serializable {

    @Id
    private String id;

    private LocalTime from;

    private LocalTime to;

    @Override
    public String toString() {
        return "TimeSheet(from=" + from + ", to=" + to + ")";
    }
}
