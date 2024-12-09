package com.techstud.scheduleuniversity.controller;

import com.techstud.scheduleuniversity.annotation.RateLimit;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDocument;
import com.techstud.scheduleuniversity.dto.ApiRequest;
import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleApiResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleDayApiResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleObjectApiResponse;
import com.techstud.scheduleuniversity.exception.ParserException;
import com.techstud.scheduleuniversity.exception.RequestException;
import com.techstud.scheduleuniversity.exception.ScheduleNotFoundException;
import com.techstud.scheduleuniversity.mapper.ScheduleMapper;
import com.techstud.scheduleuniversity.service.ScheduleService;
import com.techstud.scheduleuniversity.swagger.ApiRequestImportDto;
import com.techstud.scheduleuniversity.util.ApiResponseConverter;
import com.techstud.scheduleuniversity.swagger.ApiRequestSaveDto;
import com.techstud.scheduleuniversity.validation.RequestValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
@Tag(name = "Schedule", description = "API для работы с расписанием")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final RequestValidationService requestValidationService;
    private final ScheduleMapper scheduleMapper;
    private final ApiResponseConverter apiResponseConverter;

    @PostMapping("/import")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Запрос на импорт расписания",
            description = "Если в БД существует расписание для указанной группы, то оно вернется. " +
                    "Иначе будет произведен запрос на парсинг расписания и сохранение в БД, после привязки к пользователею, совершившего запрос.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный импорт расписания",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ScheduleApiResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Неавторизован",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(value = """
                                            {\
                                              "systemName": "tchs",
                                             \
                                              "applicationName": "schedule-university-main",
                                             \
                                              "message": "Unauthorized"
                                            }"""))),}
    )
    @RateLimit(capacity = 500, refillTokens = 500, refillPeriod = 1, periodUnit = "MINUTES")
    public EntityModel<ScheduleApiResponse> importSchedule(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для импорта расписания",
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiRequestImportDto.class)))
                                                           @RequestBody ApiRequest<ImportDto> importRequest,
                                                           @Parameter(hidden = true) Principal principal) throws RequestException, ScheduleNotFoundException, ParserException {
        log.info("Incoming request to import schedule, body: {}, user: {}", importRequest, principal.getName());
        requestValidationService.validateImportRequest(importRequest);
        ScheduleDocument documentSchedule =
                scheduleService.importSchedule(importRequest.getData(), principal.getName());
        ScheduleApiResponse schedule = scheduleMapper.toResponse(documentSchedule);
        return apiResponseConverter.convertToEntityModel(schedule, documentSchedule);
    }

    @PostMapping("/forceImport")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Запрос на принудительный импорт расписания",
            description = "Запрос на парсинг расписания и сохранение или перезапись в БД, " +
                    "после привязки к пользователею, совершившего запрос.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный импорт расписания",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ScheduleApiResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Неавторизован",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(value = """
                                            {\
                                              "systemName": "tchs",
                                             \
                                              "applicationName": "schedule-university-main",
                                             \
                                              "message": "Unauthorized"
                                            }"""))),}
    )
    @RateLimit(capacity = 200, refillTokens = 200, refillPeriod = 1, periodUnit = "MINUTES")
    public EntityModel<ScheduleApiResponse> forceImportSchedule(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для импорта расписания",
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiRequestImportDto.class)))
                                                           @RequestBody ApiRequest<ImportDto> importRequest,
                                                           @Parameter(hidden = true) Principal principal) throws RequestException, ScheduleNotFoundException, ParserException {
        log.info("Incoming request to force import schedule, body: {}, user: {}", importRequest, principal.getName());
        requestValidationService.validateImportRequest(importRequest);
        ScheduleDocument documentSchedule =
                scheduleService.forceImportSchedule(importRequest.getData(), principal.getName());
        ScheduleApiResponse schedule = scheduleMapper.toResponse(documentSchedule);
        return apiResponseConverter.convertToEntityModel(schedule, documentSchedule);
    }

    @GetMapping("/{scheduleId}")
    @PreAuthorize("hasRole('USER')")
    public EntityModel<ScheduleApiResponse> getSchedule(@PathVariable String scheduleId,
                                                        @Parameter(hidden = true) Principal principal) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Operation(
            summary = "Запрос на сохранение расписания",
            description = "Принимает JSON с расписанием, сохраняет данные в каскадном формате и привязывает" +
                    " Mongo ID расписания к пользователю.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешное сохранение расписания",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ScheduleApiResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Неавторизован",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(value = """
                                            {\
                                              "systemName": "tchs",
                                             \
                                              "applicationName": "schedule-university-main",
                                             \
                                              "message": "Unauthorized"
                                            }"""))),}
    )
    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public EntityModel<ScheduleApiResponse> createSchedule(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для сохранения расписания",
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiRequestSaveDto.class)))
                                                               @RequestBody ApiRequest<ScheduleParserResponse> saveObject,
                                                           @Parameter(hidden = true) Principal principal) {
        log.info("Incoming request to save schedule, body: {}, user: {}", saveObject, principal.getName());
        ScheduleDocument documentSchedule =
                scheduleService.createSchedule(saveObject.getData(), principal.getName());
        ScheduleApiResponse schedule = scheduleMapper.toResponse(documentSchedule);
        return EntityModel.of(schedule);
    }

    @PutMapping("/{scheduleId}")
    @PreAuthorize("hasRole('USER')")
    public EntityModel<ScheduleApiResponse> updateSchedule(@PathVariable String scheduleId,
                                                           @RequestBody ApiRequest<Object> updateObject,
                                                           @Parameter(hidden = true) Principal principal) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasRole('USER')")
    public EntityModel<Void> deleteSchedule(@PathVariable String scheduleId,
                                            Principal principal) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping("/scheduleDay/{scheduleDayId}")
    @PreAuthorize("hasRole('USER')")
    public EntityModel<ScheduleDayApiResponse> getScheduleDay(@PathVariable String scheduleDayId,
                                                              @Parameter(hidden = true) Principal principal) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PostMapping("/scheduleDay/")
    @PreAuthorize("hasRole('USER')")
    public EntityModel<ScheduleDayApiResponse> createScheduleDay(@RequestBody ApiRequest<Object> saveObject,
                                                                 @Parameter(hidden = true) Principal principal) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PutMapping("/scheduleDay/{scheduleDayId}")
    @PreAuthorize("hasRole('USER')")
    public EntityModel<ScheduleDayApiResponse> updateScheduleDay(@PathVariable String scheduleDayId,
                                                                 @RequestBody ApiRequest<Object> updateObject,
                                                                 @Parameter(hidden = true) Principal principal) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @DeleteMapping("/scheduleDay/{scheduleDayId}")
    @PreAuthorize("hasRole('USER')")
    public EntityModel<Void> deleteScheduleDay(@PathVariable String scheduleDayId,
                                               @Parameter(hidden = true) Principal principal) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping("/scheduleDay/lesson/{scheduleDayId}/{timeWindow}")
    @PreAuthorize("hasRole('USER')")
    public EntityModel<List<ScheduleObjectApiResponse>> getLesson(@PathVariable String scheduleDayId,
                                                                  @PathVariable String timeWindow,
                                                                  @Parameter(hidden = true) Principal principal) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PostMapping("/scheduleDay/lesson/")
    @PreAuthorize("hasRole('USER')")
    public EntityModel<List<ScheduleObjectApiResponse>> saveLesson(@RequestBody ApiRequest<Object> saveObject,
                                                                   @Parameter(hidden = true) Principal principal) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PutMapping("/scheduleDay/lesson/{scheduleDayId}/{timeWindow}")
    @PreAuthorize("hasRole('USER')")
    public EntityModel<List<ScheduleObjectApiResponse>> updateLesson(@PathVariable String scheduleDayId,
                                                                     @PathVariable String timeWindow,
                                                                     @RequestBody ApiRequest<Object> updateObject,
                                                                     @Parameter(hidden = true) Principal principal) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @DeleteMapping("/scheduleDay/lesson/{scheduleDayId}/{timeWindow}")
    @PreAuthorize("hasRole('USER')")
    public EntityModel<Void> deleteLesson(@PathVariable String scheduleDayId,
                                          @PathVariable String timeWindow,
                                          @Parameter(hidden = true) Principal principal) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

}
