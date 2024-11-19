package com.techstud.scheduleuniversity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Json запроса на импорт расписания", title = "ImportDto")
public class ImportDto implements Serializable {

    @Schema(description = "Аббревиатура университета", example = "SSAU", type = "string")
    private String universityName;

    @Schema(description = "Номер группы", example = "1237-150403D", type = "string")
    private String groupCode;

}
