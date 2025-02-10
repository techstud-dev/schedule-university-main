package com.techstud.scheduleuniversity.controller;

import com.techstud.scheduleuniversity.annotation.RateLimit;
import com.techstud.scheduleuniversity.dto.ApiRequest;
import com.techstud.scheduleuniversity.dto.CreateScheduleDto;
import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleApiResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleItem;
import com.techstud.scheduleuniversity.exception.*;
import com.techstud.scheduleuniversity.service.LessonServiceFacade;
import com.techstud.scheduleuniversity.service.ScheduleServiceFacade;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Schedule", description = "API для работы с расписанием")
public class ScheduleController {

    private final ScheduleServiceFacade scheduleServiceFacade;
    private final LessonServiceFacade lessonServiceFacade;
    private final RequestValidationService requestValidationService;

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
            @Parameter(hidden = true) Principal principal) throws RequestException, ScheduleNotFoundException, ParserException, ParserResponseTimeoutException {
        log.info("Incoming request to import schedule, body: {}, user: {}", importRequest, principal.getName());
        requestValidationService.validateImportRequest(importRequest);
        EntityModel<ScheduleApiResponse> importedSchedule =
                null;
        try {
            importedSchedule = scheduleServiceFacade.importSchedule(importRequest.getData(), principal.getName());
        } catch (ResourceNotFoundException e) {
            throw new RuntimeException(e);
        }
        log.info("Outgoing response to import schedule, body: {}, user: {}", importedSchedule, principal.getName());
        return ResponseEntity.ok(importedSchedule);
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
            @Parameter(hidden = true) Principal principal) throws RequestException, ScheduleNotFoundException, ParserException, ParserResponseTimeoutException {
        log.info("Incoming request to force import schedule, body: {}, user: {}", importRequest, principal.getName());
        requestValidationService.validateImportRequest(importRequest);
        EntityModel<ScheduleApiResponse> importedSchedule =
                scheduleServiceFacade.forceImportSchedule(importRequest.getData(), principal.getName());
        log.info("Outgoing response to force import schedule, body: {}, user: {}", importedSchedule, principal.getName());
        return ResponseEntity.ok(importedSchedule);
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
            @PathVariable Long scheduleId,
            @Parameter(hidden = true) Principal principal) throws ScheduleNotFoundException {
        log.info("Incoming request to get Schedule by user {}", principal.getName());
        EntityModel<ScheduleApiResponse> documentSchedule = scheduleServiceFacade.getScheduleById(scheduleId);
        log.info("Outgoing response to get Schedule by user {}, body: {}", principal.getName(), documentSchedule);
        return ResponseEntity.ok(documentSchedule);
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
            @Parameter(hidden = true) Principal principal) throws ParserException, ParserResponseTimeoutException {
        log.info("Incoming request to get schedule post authorize, user: {}", principal.getName());
        EntityModel<ScheduleApiResponse> documentSchedule = scheduleServiceFacade.getScheduleByStudent(principal.getName());
        log.info("Outgoing response to get schedule post authorize, user: {} ", principal.getName());
        return ResponseEntity.ok(documentSchedule);

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
            @RequestBody ApiRequest<CreateScheduleDto> saveObject,
            @Parameter(hidden = true) Principal principal) throws ResourceExistsException {
        log.info("Incoming request to save schedule, body: {}, user: {}", saveObject, principal.getName());
        EntityModel<ScheduleApiResponse> createdSchedule =
                scheduleServiceFacade.createSchedule(saveObject.getData(), principal.getName());
        log.info("Outgoing response to save schedule, body: {}, user: {}", createdSchedule, principal.getName());
        return ResponseEntity.ok(createdSchedule);
    }

    @Operation(
            summary = "Запрос на удаление расписания",
            description = "Удаляет расписание из БД по ID расписания и пользователю.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
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
    public ResponseEntity<Void> deleteSchedule(
            @Parameter(description = "ID расписания", required = true, example = "6763cdfcf16fce69d8f52945")
            @PathVariable Long scheduleId,
            @Parameter(hidden = true) Principal principal) throws ScheduleNotFoundException {
        log.info("Incoming request to delete schedule, scheduleId: {}, user: {}", scheduleId, principal.getName());
        scheduleServiceFacade.deleteSchedule(scheduleId, principal.getName());
        log.info("Success response to delete schedule, scheduleId: {}, user: {}", scheduleId, principal.getName());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
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
    @GetMapping("/scheduleDay/{dayOfWeek}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CollectionModel<EntityModel<ScheduleItem>>> getScheduleDay(
            @Parameter(name = "ID дня расписания", required = true, example = "6763cdfcf16fce69d8f52945")
            @PathVariable String dayOfWeek,
            @Parameter(name = "Четность недели", required = true, example = "true")
            @RequestParam boolean isEvenWeek,
            @Parameter(hidden = true) Principal principal) throws ScheduleNotFoundException {
        log.info("Incoming request to get schedule day, scheduleDayId: {}, user: {}", dayOfWeek, principal.getName());
        CollectionModel<EntityModel<ScheduleItem>> scheduleDocument = lessonServiceFacade.getLessonsByStudentAndScheduleDay(principal.getName(), dayOfWeek, isEvenWeek);
        log.info("Outgoing response to get schedule day, scheduleDayId: {}, user: {}, body: {}", dayOfWeek, principal.getName(), scheduleDocument);
        return ResponseEntity.ok().body(scheduleDocument);
    }

    @Operation(
            summary = "Запрос на создание дня расписания",
            description = "Создает день расписания у пользователя в расписании.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешное создание дня расписания",
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
                                    examples = @ExampleObject(value = Examples.RESPONSE_UNAUTHORIZED))),
                    @ApiResponse(
                            responseCode = "500",
                            description = "День уже существует в расписании этой недели",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(value = Examples.DAY_IS_EXIST))),}
    )
    @PostMapping("/scheduleDay/")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<EntityModel<ScheduleApiResponse>> createScheduleDay(
            @RequestBody ApiRequest<List<ScheduleItem>> saveObject,
            @Parameter(hidden = true) Principal principal) throws ScheduleNotFoundException, ResourceExistsException {
        log.info("Incoming request to save schedule day by user: {}", principal.getName());
        EntityModel<ScheduleApiResponse> createdSchedule = lessonServiceFacade.createScheduleDay(saveObject.getData(), principal.getName());
        log.info("Outgoing response to save schedule day by user: {}, body: {}", principal.getName(), createdSchedule);
        return ResponseEntity.ok().body(createdSchedule);
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
    @PutMapping("/scheduleDay/{dayOfWeek}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<EntityModel<ScheduleApiResponse>> updateScheduleDay(
            @Parameter(description = "ID дня расписания", required = true, example = "6763cdfcf16fce69d8f52945")
            @PathVariable String dayOfWeek,
            @Parameter(name = "Четность недели", required = true, example = "true")
            @RequestParam boolean isEvenWeek,
            @RequestBody ApiRequest<List<ScheduleItem>> updateObject,
            @Parameter(hidden = true) Principal principal) throws ScheduleNotFoundException, StudentNotFoundException {
        log.info("Incoming request to update schedule day,dayOfWeek: {}, user: {}", dayOfWeek, principal.getName());
        EntityModel<ScheduleApiResponse> updatedSchedule = lessonServiceFacade.updateScheduleDay(dayOfWeek, updateObject.getData(), principal.getName(), isEvenWeek);
        log.info("Outgoing response to update schedule day, dayOfWeek: {}, user: {}, body: {}", dayOfWeek, principal.getName(), updatedSchedule);
        return ResponseEntity.ok(updatedSchedule);
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
    @DeleteMapping("/scheduleDay/{dayOfWeek}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<EntityModel<ScheduleApiResponse>> deleteScheduleDay(
            @Parameter(description = "ID дня расписания", required = true, example = "6763cdfcf16fce69d8f52945")
            @PathVariable String dayOfWeek,
            @Parameter(name = "Четность недели", required = true, example = "true")
            @RequestParam boolean isEvenWeek,
            @Parameter(hidden = true) Principal principal) throws ScheduleNotFoundException {
        log.info("Incoming request to delete schedule day, scheduleDayId: {}, user: {}", dayOfWeek, principal.getName());
        EntityModel<ScheduleApiResponse> updatedSchedule = lessonServiceFacade.deleteScheduleDay(dayOfWeek, principal.getName(), isEvenWeek);
        log.info("Outgoing response to delete schedule day, scheduleDayId: {}, user: {}, updatedSchedule: {}", dayOfWeek, principal.getName(), updatedSchedule);
        return ResponseEntity.ok(updatedSchedule);
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
    @GetMapping("/scheduleDay/lesson/{dayOfWeek}/{timeWindowId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CollectionModel<EntityModel<ScheduleItem>>> getLesson(
            @Parameter(description = "ID дня расписания", required = true, example = "6763cdfcf16fce69d8f52945")
            @PathVariable String dayOfWeek,
            @Parameter(description = "ID времени урока", required = true, example = "6763cdfcf16fce69d8f52945")
            @PathVariable Long timeWindowId,
            @Parameter(hidden = true) Principal principal) throws ScheduleNotFoundException, ResourceNotFoundException {
        log.info("Incoming request to get lesson, dayOfWeek: {}, user: {}", dayOfWeek, principal.getName());
        CollectionModel<EntityModel<ScheduleItem>> scheduleItems = lessonServiceFacade.getLessonByStudentAndScheduleDayAndTimeWindow(principal.getName(), dayOfWeek, timeWindowId);
        log.info("Outgoing response to get lesson scheduleDayId: {}, user: {}, payload: {}", dayOfWeek, principal.getName(), scheduleItems);
        return ResponseEntity.ok(scheduleItems);
    }

    @Operation(
            summary = "Запрос на урока в дне расписания",
            description = "Создает урок на определенный день, определенное время.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешное создание урока",
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
                                    examples = @ExampleObject(value = Examples.RESPONSE_UNAUTHORIZED))),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Урок на этот день и это время уже существует",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(value = Examples.LESSON_IS_EXIST)
                            )
                    )}
    )
    @PostMapping("/scheduleDay/lesson/")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<EntityModel<ScheduleApiResponse>> saveLesson(
            @RequestBody ApiRequest<ScheduleItem> saveObject,
            @Parameter(hidden = true) Principal principal) throws ScheduleNotFoundException, StudentNotFoundException, ResourceExistsException {
        log.info("Incoming request to create lesson by user: {}", principal.getName());
        EntityModel<ScheduleApiResponse> updatedSchedule = lessonServiceFacade.createLesson(saveObject.getData(), principal.getName());
        log.info("Outgoing response to create lesson by user: {}, body: {}", principal.getName(), updatedSchedule);
        return ResponseEntity.ok(updatedSchedule);
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
    @PutMapping("/scheduleDay/lesson/{dayOfWeek}/{timeWindowId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<EntityModel<ScheduleApiResponse>> updateLesson(
            @PathVariable String dayOfWeek,
            @PathVariable Long timeWindowId,
            @RequestBody ApiRequest<ScheduleItem> updateRequest,
            @Parameter(hidden = true) Principal principal)
            throws ScheduleNotFoundException, StudentNotFoundException {
        log.info("Incoming request to update lesson, dayOfWeek: {}, user: {}", dayOfWeek, principal.getName());
        EntityModel<ScheduleApiResponse> updatedSchedule = lessonServiceFacade.updateLesson(dayOfWeek, timeWindowId, updateRequest.getData(), principal.getName());
        log.info("Outgoing response to update lesson, dayOfWeek: {}, user: {}, payload: {}", dayOfWeek, principal.getName(), updatedSchedule);
        return ResponseEntity.ok(updatedSchedule);
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
    @DeleteMapping("/scheduleDay/lesson/{dayOfWeek}/{timeWindowId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<EntityModel<ScheduleApiResponse>> deleteLesson(
            @Parameter(description = "ID дня расписания", required = true, example = "6763cdfcf16fce69d8f52945")
            @PathVariable String dayOfWeek,
            @Parameter(description = "ID времени урока", required = true, example = "6763cdfcf16fce69d8f52945")
            @PathVariable Long timeWindowId,
            @Parameter(hidden = true) Principal principal) throws ScheduleNotFoundException, StudentNotFoundException {
        log.info("Incoming request to delete lesson, dayOfWeek: {}, user: {}", dayOfWeek, principal.getName());
        EntityModel<ScheduleApiResponse> updatedSchedule = lessonServiceFacade.deleteLesson(dayOfWeek, timeWindowId, principal.getName());
        log.info("Outgoing response to delete lesson, dayOfWeek: {}, user: {}, payload: {}", dayOfWeek, principal.getName(), updatedSchedule);
        return ResponseEntity.ok(updatedSchedule);
    }
}
