package com.techstud.scheduleuniversity.controller;

import com.techstud.scheduleuniversity.annotation.RateLimit;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDocument;
import com.techstud.scheduleuniversity.dto.ApiRequest;
import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleApiResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleItem;
import com.techstud.scheduleuniversity.exception.ParserException;
import com.techstud.scheduleuniversity.exception.RequestException;
import com.techstud.scheduleuniversity.exception.ScheduleNotFoundException;
import com.techstud.scheduleuniversity.exception.StudentNotFoundException;
import com.techstud.scheduleuniversity.mapper.ScheduleMapper;
import com.techstud.scheduleuniversity.service.ScheduleService;
import com.techstud.scheduleuniversity.swagger.ApiRequestImportDto;
import com.techstud.scheduleuniversity.swagger.ApiRequestSaveDto;
import com.techstud.scheduleuniversity.swagger.Examples;
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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Schedule", description = "API для работы с расписанием")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final RequestValidationService requestValidationService;
    private final ScheduleMapper scheduleMapper;

    @SuppressWarnings("all")
    @Operation(
            summary = "Запрос на импорт расписания",
            description = "Если в БД существует расписание для указанной группы, то оно вернется. " +
                    "Иначе будет произведен запрос на парсинг расписания и сохранение в БД, после привязки к пользователею, совершившего запрос.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный импорт расписания",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ScheduleApiResponse.class),
                                    examples = @ExampleObject(value = Examples.RESPONSE_SCHEDULE))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Неавторизован",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(value = Examples.RESPONSE_UNAUTHORIZED))),}
    )
    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @RateLimit(capacity = 500, refillTokens = 500, refillPeriod = 1, periodUnit = "MINUTES")
    public ResponseEntity<EntityModel<ScheduleApiResponse>> importSchedule(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для импорта расписания",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiRequestImportDto.class),
                    examples = @ExampleObject(value = Examples.REQUEST_IMPORT)))
            @RequestBody ApiRequest<ImportDto> importRequest,
            @Parameter(hidden = true) Principal principal) throws RequestException, ScheduleNotFoundException, ParserException {
        log.info("Incoming request to import schedule, body: {}, user: {}", importRequest, principal.getName());
        requestValidationService.validateImportRequest(importRequest);
        ScheduleDocument documentSchedule =
                scheduleService.importSchedule(importRequest.getData(), principal.getName());
        return ResponseEntity.ok(scheduleMapper.toResponse(documentSchedule));
    }

    @SuppressWarnings("all")
    @Operation(
            summary = "Запрос на принудительный импорт расписания",
            description = "Запрос на парсинг расписания и сохранение или перезапись в БД, " +
                    "после привязки к пользователею, совершившего запрос.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешный импорт расписания",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ScheduleApiResponse.class),
                                    examples = @ExampleObject(value = Examples.RESPONSE_SCHEDULE))),
                    @ApiResponse(responseCode = "401",
                            description = "Неавторизован",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(value = Examples.RESPONSE_UNAUTHORIZED))),}
    )
    @PostMapping("/forceImport")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @RateLimit(capacity = 200, refillTokens = 200, refillPeriod = 1, periodUnit = "MINUTES")
    public ResponseEntity<EntityModel<ScheduleApiResponse>> forceImportSchedule(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для импорта расписания",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiRequestImportDto.class),
                    examples = @ExampleObject(value = Examples.REQUEST_IMPORT)))
            @RequestBody ApiRequest<ImportDto> importRequest,
            @Parameter(hidden = true) Principal principal) throws RequestException, ScheduleNotFoundException, ParserException {
        log.info("Incoming request to force import schedule, body: {}, user: {}", importRequest, principal.getName());
        requestValidationService.validateImportRequest(importRequest);
        ScheduleDocument documentSchedule =
                scheduleService.forceImportSchedule(importRequest.getData(), principal.getName());
        return ResponseEntity.ok(scheduleMapper.toResponse(documentSchedule));
    }

    @Operation(
            summary = "Запрос на получение расписания",
            description = "Получение расписания по ID расписания и пользователю.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешное получение расписания",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ScheduleApiResponse.class),
                                    examples = @ExampleObject(value = Examples.RESPONSE_SCHEDULE))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Неавторизован",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(value = Examples.RESPONSE_UNAUTHORIZED))),}
    )
    @GetMapping("/{scheduleId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<EntityModel<ScheduleApiResponse>> getSchedule(
            @Parameter(description = "ID расписания", required = true, example = "6763cdfcf16fce69d8f52945")
            @PathVariable String scheduleId,
            @Parameter(hidden = true) Principal principal) throws ScheduleNotFoundException {
        ScheduleDocument documentSchedule = scheduleService.getScheduleById(scheduleId);
        return ResponseEntity.ok(scheduleMapper.toResponse(documentSchedule));
    }

    @Operation(
            summary = "Запрос на получение расписания после авторизации",
            description = "Первый запрос, при редиректе пользователя на главную страницу с расписанием после авторизации",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешное получение расписания",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ScheduleApiResponse.class),
                                    examples = @ExampleObject(value = Examples.RESPONSE_SCHEDULE))),
                    @ApiResponse(
                            responseCode = "204",
                            description = "Расписание не найдено",
                            content = @Content(
                                    mediaType = "application/json")),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Неавторизован",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(value = Examples.RESPONSE_UNAUTHORIZED))),}
    )
    @GetMapping("/postAuthorize")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<EntityModel<ScheduleApiResponse>> getSchedulePostAuthorize(
            @Parameter(hidden = true) Principal principal) throws ScheduleNotFoundException, StudentNotFoundException {
        log.info("Incoming request to get schedule post authorize, user: {}", principal.getName());
        ScheduleDocument documentSchedule = scheduleService.getScheduleByStudentName(principal.getName());
        if (documentSchedule == null) {
            return new ResponseEntity<>(null, NO_CONTENT);
        }

        EntityModel<ScheduleApiResponse> response = scheduleMapper.toResponse(documentSchedule);
        return ResponseEntity.ok(response);
    }

    @SuppressWarnings("all")
    @Operation(
            summary = "Запрос на сохранение расписания",
            description = "Принимает JSON с расписанием, сохраняет данные в каскадном формате и привязывает" +
                    " Mongo ID расписания к пользователю.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешное сохранение расписания",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ScheduleApiResponse.class))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Неавторизован",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(value = Examples.RESPONSE_UNAUTHORIZED))),}
    )
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<EntityModel<ScheduleApiResponse>> createSchedule(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
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

    @Operation(
            summary = "Запрос на удаление расписания",
            description = "Удаляет расписание из БД по ID расписания и пользователю.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешное удаление расписания",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Неавторизован",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(value = Examples.RESPONSE_UNAUTHORIZED))),}
    )
    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<EntityModel<Void>> deleteSchedule(
            @Parameter(description = "ID расписания", required = true, example = "6763cdfcf16fce69d8f52945")
            @PathVariable String scheduleId,
            @Parameter(hidden = true) Principal principal) throws ScheduleNotFoundException, StudentNotFoundException {
        log.info("Incoming request to delete schedule, scheduleId: {}, user: {}", scheduleId, principal.getName());
        scheduleService.deleteSchedule(scheduleId, principal.getName());
        return ResponseEntity.ok().body(null);
    }


    @Operation(
            summary = "Запрос на получение расписания на день",
            description = "Получение дня расписания по ID дня и пользователю.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешное получение расписания на день",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CollectionModel.class),
                                    examples = @ExampleObject(value = Examples.RESPONSE_SCHEDULE_DAY))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Неавторизован",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(value = Examples.RESPONSE_UNAUTHORIZED))),}
    )
    @GetMapping("/scheduleDay/{scheduleDayId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CollectionModel<EntityModel<ScheduleItem>>> getScheduleDay(
            @Parameter(name = "ID дня расписания", required = true, example = "6763cdfcf16fce69d8f52945")
            @PathVariable String scheduleDayId,
            @Parameter(hidden = true) Principal principal) throws ScheduleNotFoundException, StudentNotFoundException {
        log.info("Incoming request to get schedule day, scheduleDayId: {}, user: {}", scheduleDayId, principal.getName());
        ScheduleDocument scheduleDocument = scheduleService.getScheduleByStudentName(principal.getName());
        return ResponseEntity.ok().body(scheduleMapper.toResponse(scheduleDocument, scheduleDayId));
    }

    @PostMapping("/scheduleDay/")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public EntityModel<ScheduleItem> createScheduleDay(@RequestBody ApiRequest<Object> saveObject,
                                                                 @Parameter(hidden = true) Principal principal) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Operation(
            summary = "Запрос на обновление дня расписания",
            description = "Обновляет день расписания из БД по ID дня и пользователю.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешное обновление дня расписания",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ScheduleApiResponse.class),
                                    examples = @ExampleObject(value = Examples.RESPONSE_SCHEDULE))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Неавторизован",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(value = Examples.RESPONSE_UNAUTHORIZED))),}
    )
    @PutMapping("/scheduleDay/{scheduleDayId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<EntityModel<ScheduleApiResponse>> updateScheduleDay(
            @Parameter(description = "ID дня расписания", required = true, example = "6763cdfcf16fce69d8f52945")
            @PathVariable String scheduleDayId,
            @RequestBody ApiRequest<List<ScheduleItem>> updateObject,
            @Parameter(hidden = true) Principal principal) throws ScheduleNotFoundException, StudentNotFoundException {
        log.info("Incoming request to update schedule day, scheduleDayId: {}, user: {}", scheduleDayId, principal.getName());
        ScheduleDocument updatedSchedule = scheduleService.updateScheduleDay(scheduleDayId, updateObject.getData(), principal.getName());
        return ResponseEntity.ok(scheduleMapper.toResponse(updatedSchedule));
    }

    @Operation(
            summary = "Запрос на удаление дня расписания",
            description = "Удаляет день расписания из БД по ID дня и пользователю.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешное удаление дня расписания",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ScheduleApiResponse.class),
                                    examples = @ExampleObject(value = Examples.RESPONSE_SCHEDULE))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Неавторизован",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(value = Examples.RESPONSE_UNAUTHORIZED))),}
    )
    @DeleteMapping("/scheduleDay/{scheduleDayId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<EntityModel<ScheduleApiResponse>> deleteScheduleDay(
            @Parameter(description = "ID дня расписания", required = true, example = "6763cdfcf16fce69d8f52945")
            @PathVariable String scheduleDayId,
            @Parameter(hidden = true) Principal principal) throws ScheduleNotFoundException, StudentNotFoundException {
        log.info("Incoming request to delete schedule day, scheduleDayId: {}, user: {}", scheduleDayId, principal.getName());
        ScheduleDocument updatedSchedule = scheduleService.deleteScheduleDay(scheduleDayId, principal.getName());
        return ResponseEntity.ok(scheduleMapper.toResponse(updatedSchedule));
    }

    @Operation(
            summary = "Запрос на получение урока",
            description = "Получение урока по ID дня, времени урока и пользователю.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешное получение урока",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CollectionModel.class),
                                    examples = @ExampleObject(value = Examples.RESPONSE_LESSON))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Неавторизован",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(value = Examples.RESPONSE_UNAUTHORIZED))),}
    )
    @GetMapping("/scheduleDay/lesson/{scheduleDayId}/{timeWindowId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CollectionModel<EntityModel<ScheduleItem>>> getLesson(
            @Parameter(description = "ID дня расписания", required = true, example = "6763cdfcf16fce69d8f52945")
            @PathVariable String scheduleDayId,
            @Parameter(description = "ID времени урока", required = true, example = "6763cdfcf16fce69d8f52945")
            @PathVariable String timeWindowId,
            @Parameter(hidden = true) Principal principal) throws ScheduleNotFoundException, StudentNotFoundException {
        log.info("Incoming request to get lesson, scheduleDayId: {}, user: {}", scheduleDayId, principal.getName());
        ScheduleDocument scheduleDocument = scheduleService.getScheduleByStudentName(principal.getName());
        return ResponseEntity.ok(scheduleMapper.toResponse(scheduleDocument, scheduleDayId, timeWindowId));
    }

    @Operation(
            summary = "Запрос на сохранение новых уроков для дня",
            description = "Сохраняет уроки в БД используя входящие данные и пользователя",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешное сохранение уроков",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ScheduleApiResponse.class),
                                    examples = @ExampleObject(value = Examples.RESPONSE_SCHEDULE))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Не авторизован",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(value = Examples.RESPONSE_UNAUTHORIZED))),}
    )
    @PostMapping("/scheduleDay/lesson/save")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<EntityModel<ScheduleApiResponse>> saveLesson(
            @RequestBody ApiRequest<List<ScheduleItem>> saveLessonsObject,
            @Parameter(hidden = true) Principal principal) throws ScheduleNotFoundException, StudentNotFoundException {
        log.info("Successful request to create schedule day, scheduleDay: {}, user: {}", saveLessonsObject, principal.getName());
        return ResponseEntity.ok(scheduleMapper.toResponse(scheduleService.saveLessons(
                saveLessonsObject.getData(),
                principal.getName()
        )));
    }

    @Operation(
            summary = "Запрос на обновление занятия",
            description = "Обновляет занятие из БД по ID дня, ID временного окна и пользователю.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешное обновление дня расписания",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ScheduleApiResponse.class),
                                    examples = @ExampleObject(value = Examples.RESPONSE_SCHEDULE))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Неавторизован",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(value = Examples.RESPONSE_UNAUTHORIZED))),}
    )
    @PutMapping("/scheduleDay/lesson/{scheduleDayId}/{timeWindow}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<EntityModel<ScheduleApiResponse>> updateLesson(
            @PathVariable String scheduleDayId,
            @PathVariable String timeWindow,
            @RequestBody ApiRequest<ScheduleItem> updateRequest,
            @Parameter(hidden = true) Principal principal)
            throws ScheduleNotFoundException, StudentNotFoundException {
        log.info("Incoming request to update lesson, scheduleDayId: {}, user: {}", scheduleDayId, principal.getName());
        ScheduleDocument updatedSchedule = scheduleService.updateLesson(scheduleDayId, timeWindow, updateRequest.getData(), principal.getName());
        return ResponseEntity.ok(scheduleMapper.toResponse(updatedSchedule));
    }

    @Operation(
            summary = "Запрос на удаление урока",
            description = "Удаляет урок из БД по ID дня, времени урока и пользователю.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешное удаление урока",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ScheduleApiResponse.class),
                                    examples = @ExampleObject(value = Examples.RESPONSE_SCHEDULE))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Неавторизован",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(value = Examples.RESPONSE_UNAUTHORIZED))),}
    )
    @DeleteMapping("/scheduleDay/lesson/{scheduleDayId}/{timeWindowId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<EntityModel<ScheduleApiResponse>> deleteLesson(
            @Parameter(description = "ID дня расписания", required = true, example = "6763cdfcf16fce69d8f52945")
            @PathVariable String scheduleDayId,
            @Parameter(description = "ID времени урока", required = true, example = "6763cdfcf16fce69d8f52945")
            @PathVariable String timeWindowId,
            @Parameter(hidden = true) Principal principal) throws ScheduleNotFoundException, StudentNotFoundException {
        log.info("Incoming request to delete lesson, scheduleDayId: {}, user: {}", scheduleDayId, principal.getName());
        ScheduleDocument updatedSchedule = scheduleService.deleteLesson(scheduleDayId, timeWindowId, principal.getName());
        return ResponseEntity.ok(scheduleMapper.toResponse(updatedSchedule));
    }
}
