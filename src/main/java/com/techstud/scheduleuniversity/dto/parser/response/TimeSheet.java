package com.techstud.scheduleuniversity.dto.parser.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TimeSheet implements Serializable {

    private LocalTime from;

    private LocalTime to;

    @Override
    public String toString() {
        return "TimeSheet(from=" + from + ", to=" + to + ")";
    }
}
