package com.techstud.scheduleuniversity.controller;

import com.techstud.scheduleuniversity.dto.ApiRequest;
import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.response.schedule.Schedule;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleDay;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleObject;
import com.techstud.scheduleuniversity.exception.RequestException;
import com.techstud.scheduleuniversity.mapper.ScheduleMapper;
import com.techstud.scheduleuniversity.service.ScheduleService;
import com.techstud.scheduleuniversity.util.ScheduleHateoasAssembler;
import com.techstud.scheduleuniversity.validation.RequestValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final RequestValidationService requestValidationService;
    private final ScheduleMapper scheduleMapper;

    @PostMapping("/import")
    @PreAuthorize("hasRole('USER')")
    public EntityModel<Schedule> importSchedule(@RequestBody ApiRequest<ImportDto> importRequest, Principal principal) throws RequestException {
        log.info("Incoming request to import schedule, body: {}, user: {}", importRequest, principal.getName());
        requestValidationService.validateImportRequest(importRequest);
        com.techstud.scheduleuniversity.dao.document.schedule.Schedule documentSchedule = scheduleService.importSchedule(importRequest.getData(), principal.getName());
        Schedule schedule = scheduleMapper.toResponse(documentSchedule);
     return ScheduleHateoasAssembler.toModel(schedule, documentSchedule);
    }

    @GetMapping("/{scheduleId}")
    public EntityModel<Schedule> getSchedule(@PathVariable String scheduleId) {
     throw new UnsupportedOperationException("Not implemented yet");
    }

    @PutMapping("/{scheduleId}")
    public EntityModel<Schedule> updateSchedule(@PathVariable String scheduleId, @RequestBody ApiRequest<ImportDto> importRequest) {
     throw new UnsupportedOperationException("Not implemented yet");
    }

    @DeleteMapping("/{scheduleId}")
    public EntityModel<Schedule> deleteSchedule(@PathVariable String scheduleId) {
     throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping("/{scheduleId}/day/{weekType}/{day}")
    public EntityModel<ScheduleDay> getScheduleDay(@PathVariable String scheduleId, @PathVariable String weekType, @PathVariable String day) {
     throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping("/{scheduleId}/day/{weekType}/{day}/lesson/{time}")
    public EntityModel<List<ScheduleObject>> getLesson(@PathVariable String scheduleId, @PathVariable String weekType, @PathVariable String day, @PathVariable String time) {
     throw new UnsupportedOperationException("Not implemented yet");
    }


    @GetMapping("/scene")
    public ResponseEntity<Object> getScene() {
     throw new UnsupportedOperationException("Not implemented yet");
    }

    @PostMapping("/scheduleObject/create")
    public ResponseEntity<Object> createScheduleObject() {
     throw new UnsupportedOperationException("Not implemented yet");
    }

    @PostMapping("/scheduleDay/create")
    public ResponseEntity<Object> createScheduleDay() {
     throw new UnsupportedOperationException("Not implemented yet");
    }

    //TODO: Add more api
}
