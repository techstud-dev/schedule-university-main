package com.techstud.scheduleuniversity.controller;

import com.techstud.scheduleuniversity.dto.ApiRequest;
import com.techstud.scheduleuniversity.dto.UpdateGroupDataTask;
import com.techstud.scheduleuniversity.service.AdminService;
import com.techstud.scheduleuniversity.swagger.UpdateGroupTaskDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("api/v1/admin")
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("all")
@Tag(name = "Admin", description = "API для работы администратором с чувствительными данными")
public class AdminController {

    private final AdminService adminService;

    @Operation(
            summary = "Запрос на обновление данных группы",
            description = "Обновит все данные о группах университета по запросу.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный импорт расписания",
                            content = @Content(mediaType = "application/text")),
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
                                            }""")))}
    )
    @PostMapping("/groups/update")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateGroups(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для импорта расписания",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UpdateGroupTaskDto.class)))
            @RequestBody ApiRequest<UpdateGroupDataTask> updateTask,
            @Parameter(hidden = true) Principal principal) {
        log.info("Incoming request to update groups data: {}", updateTask);
        adminService.updateGroupsData(updateTask);

        return new ResponseEntity(null, HttpStatus.OK);
    }
}
