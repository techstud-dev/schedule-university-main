package com.techstud.scheduleuniversity.controller;

import com.techstud.scheduleuniversity.dto.ApiRequest;
import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.response.schedule.Schedule;
import com.techstud.scheduleuniversity.exception.RequestException;
import com.techstud.scheduleuniversity.service.ScheduleService;
import com.techstud.scheduleuniversity.validation.RequestValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final RequestValidationService requestValidationService;

    @PostMapping("/import")
    public EntityModel<Schedule> importSchedule(@RequestBody ApiRequest<ImportDto> importRequest) throws RequestException {
        log.info("Incoming request to import schedule, body: {}", importRequest);
        requestValidationService.validateImportRequest(importRequest);
        Schedule schedule = scheduleService.importSchedule(importRequest.getData());
     return EntityModel.of(schedule);
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
