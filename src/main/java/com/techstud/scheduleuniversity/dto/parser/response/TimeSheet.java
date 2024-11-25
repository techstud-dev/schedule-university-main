package com.techstud.scheduleuniversity.dto.parser.response;

import lombok.*;

import java.io.Serializable;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of = {"from", "to"})
public class TimeSheet implements Serializable {

    private LocalTime from;

    private LocalTime to;

    @Override
    public String toString() {
        return "TimeSheet(from=" + from + ", to=" + to + ")";
    }
}
