package com.techstud.scheduleuniversity.swagger;

import com.techstud.scheduleuniversity.dto.ApiRequest;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name ="Request with saved data by ScheduleDay")
public class ApiRequestSaveDayDto extends ApiRequest<ScheduleParserResponse> {
}