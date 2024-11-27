package com.techstud.scheduleuniversity.swagger;

import com.techstud.scheduleuniversity.dto.ApiRequest;
import com.techstud.scheduleuniversity.dto.ImportDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос с данными для импорта расписания")
public class ApiRequestImportDto extends ApiRequest<ImportDto> {
}
