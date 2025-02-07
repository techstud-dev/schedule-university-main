package com.techstud.scheduleuniversity.dto;

import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MappingScheduleParserDto {

    private String universityShortName;

    ScheduleParserResponse scheduleParserResponse;
}
