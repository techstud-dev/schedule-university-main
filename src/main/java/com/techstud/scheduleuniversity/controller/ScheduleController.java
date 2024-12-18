package com.techstud.scheduleuniversity.controller;

import com.techstud.scheduleuniversity.annotation.RateLimit;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDocument;
import com.techstud.scheduleuniversity.dto.ApiRequest;
import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleDayApiResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleObjectApiResponse;
import com.techstud.scheduleuniversity.dto.response.scheduleV.ScheduleApiResponse;
import com.techstud.scheduleuniversity.dto.response.scheduleV.ScheduleItem;
import com.techstud.scheduleuniversity.exception.ParserException;
import com.techstud.scheduleuniversity.exception.RequestException;
import com.techstud.scheduleuniversity.exception.ScheduleNotFoundException;
import com.techstud.scheduleuniversity.exception.StudentNotFoundException;
import com.techstud.scheduleuniversity.mapper.ScheduleMapper;
import com.techstud.scheduleuniversity.service.ScheduleService;
import com.techstud.scheduleuniversity.swagger.ApiRequestImportDto;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.NO_CONTENT;

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
    public ResponseEntity<EntityModel<ScheduleApiResponse>> importSchedule(@io.swagger.v3.oas.annotations.parameters.RequestBody(
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
        return ResponseEntity.ok(scheduleMapper.toResponse(documentSchedule));
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
    public ResponseEntity<EntityModel<ScheduleApiResponse>> forceImportSchedule(@io.swagger.v3.oas.annotations.parameters.RequestBody(
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
        return ResponseEntity.ok(scheduleMapper.toResponse(documentSchedule));
    }

    @GetMapping("/{scheduleId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EntityModel<ScheduleApiResponse>> getSchedule(@PathVariable String scheduleId,
                                                        @Parameter(hidden = true) Principal principal) throws ScheduleNotFoundException {
        ScheduleDocument documentSchedule = scheduleService.getScheduleById(scheduleId);
        return ResponseEntity.ok(scheduleMapper.toResponse(documentSchedule));
    }

    @GetMapping("/postAuthorize")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EntityModel<ScheduleApiResponse>> getSchedulePostAuthorize(@Parameter(hidden = true) Principal principal) throws ScheduleNotFoundException, StudentNotFoundException {
        log.info("Incoming request to get schedule post authorize, user: {}", principal.getName());
        ScheduleDocument documentSchedule = scheduleService.getScheduleByStudentName(principal.getName());
        if (documentSchedule == null) {
            return new ResponseEntity<>(null, NO_CONTENT);
        }

        EntityModel<ScheduleApiResponse> response = scheduleMapper.toResponse(documentSchedule);
        return ResponseEntity.ok(response);
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
    public ResponseEntity<EntityModel<ScheduleApiResponse>> createSchedule(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для сохранения расписания",
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiRequestSaveDto.class)))
                                                               @RequestBody ApiRequest<ScheduleParserResponse> saveObject,
                                                           @Parameter(hidden = true) Principal principal) {
        log.info("Incoming request to save schedule, body: {}, user: {}", saveObject, principal.getName());
        ScheduleDocument documentSchedule =
                scheduleService.createSchedule(saveObject.getData(), principal.getName());
        return ResponseEntity.ok(scheduleMapper.toResponse(documentSchedule));
    }

    @PutMapping("/{scheduleId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EntityModel<ScheduleApiResponse>> updateSchedule(@PathVariable String scheduleId,
                                                           @RequestBody ApiRequest<Object> updateObject,
                                                           @Parameter(hidden = true) Principal principal) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EntityModel<Void>> deleteSchedule(@PathVariable String scheduleId,
                                            @Parameter(hidden = true) Principal principal) throws ScheduleNotFoundException, StudentNotFoundException {
        log.info("Incoming request to delete schedule, scheduleId: {}, user: {}", scheduleId, principal.getName());
        scheduleService.deleteSchedule(scheduleId, principal.getName());
        return ResponseEntity.ok().body(null);
    }

    @GetMapping("/scheduleDay/{scheduleDayId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EntityModel<ScheduleDayApiResponse>> getScheduleDay(@PathVariable String scheduleDayId,
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
    public ResponseEntity<EntityModel<ScheduleDayApiResponse>> updateScheduleDay(@PathVariable String scheduleDayId,
                                                                 @RequestBody ApiRequest<Object> updateObject,
                                                                 @Parameter(hidden = true) Principal principal) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @DeleteMapping("/scheduleDay/{scheduleDayId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EntityModel<ScheduleApiResponse>> deleteScheduleDay(@PathVariable String scheduleDayId,
                                                              @Parameter(hidden = true) Principal principal) throws ScheduleNotFoundException, StudentNotFoundException {
        log.info("Incoming request to delete schedule day, scheduleDayId: {}, user: {}", scheduleDayId, principal.getName());
        ScheduleDocument updatedSchedule = scheduleService.deleteScheduleDay(scheduleDayId, principal.getName());
        return ResponseEntity.ok(scheduleMapper.toResponse(updatedSchedule));
    }

    @GetMapping("/scheduleDay/lesson/{scheduleDayId}/{timeWindowId}")
    @PreAuthorize("hasRole('USER')")
    public  ResponseEntity<List<EntityModel<ScheduleItem>>> getLesson(@PathVariable String scheduleDayId,
                                                      @PathVariable String timeWindowId,
                                                      @Parameter(hidden = true) Principal principal) throws ScheduleNotFoundException, StudentNotFoundException {
        log.info("Incoming request to get lesson, scheduleDayId: {}, user: {}", scheduleDayId, principal.getName());
        ScheduleDocument scheduleDocument = scheduleService.getScheduleByStudentName(principal.getName());
        return ResponseEntity.ok(scheduleMapper.toResponse(scheduleDocument, scheduleDayId, timeWindowId));
    }

    @PostMapping("/scheduleDay/lesson/")
    @PreAuthorize("hasRole('USER')")
    public EntityModel<List<ScheduleObjectApiResponse>> saveLesson(@RequestBody ApiRequest<Object> saveObject,
                                                                   @Parameter(hidden = true) Principal principal) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PutMapping("/scheduleDay/lesson/{scheduleDayId}/{timeWindow}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EntityModel<List<ScheduleObjectApiResponse>>> updateLesson(@PathVariable String scheduleDayId,
                                                                     @PathVariable String timeWindow,
                                                                     @RequestBody ApiRequest<Object> updateObject,
                                                                     @Parameter(hidden = true) Principal principal) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @DeleteMapping("/scheduleDay/lesson/{scheduleDayId}/{timeWindowId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EntityModel<ScheduleApiResponse>> deleteLesson(@PathVariable String scheduleDayId,
                                          @PathVariable String timeWindowId,
                                          @Parameter(hidden = true) Principal principal) throws ScheduleNotFoundException, StudentNotFoundException {
        log.info("Incoming request to delete lesson, scheduleDayId: {}, user: {}", scheduleDayId, principal.getName());
        ScheduleDocument updatedSchedule = scheduleService.deleteLesson(scheduleDayId, timeWindowId, principal.getName());
        return ResponseEntity.ok(scheduleMapper.toResponse(updatedSchedule));
    }
}
