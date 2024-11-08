package com.techstud.scheduleuniversity.controller;

import com.techstud.scheduleuniversity.dto.parser.request.ParsingTask;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleObject;
import com.techstud.scheduleuniversity.entity.schedule.Schedule;
import com.techstud.scheduleuniversity.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/schedule")
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/{groupName}")
    public Mono<Schedule> getSchedule(@PathVariable String groupName) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PostMapping("/import")
    public Mono<Schedule> importSchedule(@RequestBody ParsingTask task) {
        return scheduleService.importSchedule(task);
    }

    @PostMapping("/scheduleDay/scheduleObject/add")
    public ServerResponse addScheduleObject(@RequestBody ScheduleObject scheduleObject) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PutMapping("/scheduleObject/{id}")
    public ServerResponse updateScheduleObject(@PathVariable Long id, @RequestBody ScheduleObject scheduleObject) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @DeleteMapping("/scheduleWeek/{isOdd}")
    public ServerResponse deleteScheduleWeek(@PathVariable Boolean isOdd, @RequestBody Schedule schedule) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @DeleteMapping("/{id}")
    public ServerResponse deleteSchedule(@PathVariable Long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @DeleteMapping("/scheduleDay/scheduleObject/{id}")
    public ServerResponse deleteScheduleObject(@PathVariable Long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
