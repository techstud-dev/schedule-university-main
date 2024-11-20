package com.techstud.scheduleuniversity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ApiRequest", description = "Универсальный JSON для запросов к API")
public class ApiRequest<T> implements Serializable {

    @Schema(description = "Уникальный идентификатор запроса",
            example = "123e4567-e89b-12d3-a456-426614174000",
            type = "string")
    private String requestId = UUID.randomUUID().toString();

    @Schema(description = "Тип операции",
            example = "create",
            type = "string")
    private String operation;

    @Schema(description = "Имя ресурса",
            example = "user",
            type = "string")
    private String resource;

    @Schema(description = "Фильтры для выборки данных",
            type = "object",
            example = "{\"id\": 123, \"status\": \"active\"}")
    private Map<String, Object> filters;

    @Schema(description = "Информация о пагинации",
            type = "object")
    private Pagination pagination;

    @Schema(description = "Информация о сортировке",
            type = "object")
    private Sorting sorting;

    @Schema(description = "Поля, которые нужно вернуть",
            type = "array",
            example = "[\"id\", \"name\", \"email\"]")
    private List<String> fields;

    @Schema(description = "Связанные данные (например, связанные таблицы)",
            type = "array",
            example = "[\"orders\", \"profile\"]")
    private List<String> relationships;

    @Schema(description = "Полезная нагрузка для операций создания или обновления",
            type = "object",
            example = "{\"name\": \"John Doe\", \"email\": \"john.doe@example.com\", \"password\": \"securePassword123\"}")
    private T data;

    @Schema(description = "Дополнительные данные, которые могут быть полезны для обработки запроса",
            type = "object",
            example = "{\"client\": \"web\", \"locale\": \"en-US\"}")
    private Map<String, Object> metadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "Pagination", description = "Информация о пагинации")
    public static class Pagination {
        @Schema(description = "Номер страницы (начинается с 1)",
                example = "1",
                type = "integer")
        private int page = 1;

        @Schema(description = "Количество элементов на странице",
                example = "20",
                type = "integer")
        private int pageSize = 20;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "Sorting", description = "Информация о сортировке")
    public static class Sorting {
        @Schema(description = "Поле для сортировки",
                example = "name",
                type = "string")
        private String field;

        @Schema(description = "Порядок сортировки (asc для по возрастанию, desc для по убыванию)",
                example = "asc",
                type = "string")
        private String order = "asc";
    }
}
