package com.techstud.scheduleuniversity.swagger;

import com.techstud.scheduleuniversity.dto.ApiRequest;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос с данными для сохранения расписания")
public class ApiRequestSaveDto extends ApiRequest<ScheduleParserResponse> {
}
