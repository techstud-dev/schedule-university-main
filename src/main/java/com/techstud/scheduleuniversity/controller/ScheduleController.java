package com.techstud.scheduleuniversity.controller;

import com.techstud.scheduleuniversity.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vi/schedule")
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping("/import")
    public ResponseEntity<Object> importSchedule() {
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
